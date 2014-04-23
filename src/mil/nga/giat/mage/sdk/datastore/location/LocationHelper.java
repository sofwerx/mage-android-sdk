package mil.nga.giat.mage.sdk.datastore.location;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import mil.nga.giat.mage.sdk.datastore.DaoHelper;
import mil.nga.giat.mage.sdk.event.IEventDispatcher;
import mil.nga.giat.mage.sdk.event.ILocationEventListener;
import mil.nga.giat.mage.sdk.exceptions.LocationException;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * A utility class for accessing {@link Location} data from the physical data
 * model. The details of ORM DAOs and Lazy Loading should not be exposed past
 * this class.
 * 
 * @author wiedemannse
 * 
 */
public class LocationHelper extends DaoHelper<Location> implements IEventDispatcher<ILocationEventListener> {

	private static final String LOG_NAME = LocationHelper.class.getName();

	private final Dao<Location, Long> locationDao;
	private final Dao<LocationGeometry, Long> locationGeometryDao;
	private final Dao<LocationProperty, Long> locationPropertyDao;

	private Collection<ILocationEventListener> listeners = new CopyOnWriteArrayList<ILocationEventListener>();

	/**
	 * Singleton.
	 */
	private static LocationHelper mLocationHelper;

	/**
	 * Use of a Singleton here ensures that an excessive amount of DAOs are not
	 * created.
	 * 
	 * @param context
	 *            Application Context
	 * @return A fully constructed and operational LocationHelper.
	 */
	public static LocationHelper getInstance(Context context) {
		if (mLocationHelper == null) {
			mLocationHelper = new LocationHelper(context);
		}
		return mLocationHelper;
	}

	/**
	 * Only one-per JVM. Singleton.
	 * 
	 * @param context
	 */
	private LocationHelper(Context context) {
		super(context);

		try {
			locationDao = daoStore.getLocationDao();
			locationGeometryDao = daoStore.getLocationGeometryDao();
			locationPropertyDao = daoStore.getLocationPropertyDao();
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to communicate with Location database.", sqle);

			throw new IllegalStateException("Unable to communicate with Location database.", sqle);
		}

	}

	public List<Location> readAllNonCurrent() throws LocationException {
		List<Location> locations = new ArrayList<Location>();
		try {
			locations = locationDao.queryBuilder().where().eq("current_user", Boolean.FALSE).query(); 
		} 
		catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to read Locations", sqle);
			throw new LocationException("Unable to read Locations.", sqle);
		}
		return locations;
	}

	@Override
	public Location create(Location pLocation) throws LocationException {

		Location createdLocation;

		try {
			// create Location geometry.
			locationGeometryDao.create(pLocation.getLocationGeometry());

			createdLocation = locationDao.createIfNotExists(pLocation);

			// create Location properties.
			Collection<LocationProperty> locationProperties = pLocation.getProperties();
			if (locationProperties != null) {
				for (LocationProperty locationProperty : locationProperties) {
					locationProperty.setLocation(createdLocation);
					locationPropertyDao.create(locationProperty);
				}
			}

			// fire the event
			if(!pLocation.isCurrentUser()) {
				ConcurrentSkipListSet<Location> locations = new ConcurrentSkipListSet<Location>();
				locations.add(createdLocation);
				for (ILocationEventListener listener : listeners) {
					listener.onLocationCreated(locations);
				}
			}

		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "There was a problem creating the location: " + pLocation + ".", sqle);
			throw new LocationException("There was a problem creating the location: " + pLocation + ".", sqle);
		}

		return createdLocation;
	}

	@Override
	public Location read(String pRemoteId) throws LocationException {
		Location location = null;
		try {
			List<Location> results = locationDao.queryBuilder().where().eq("remote_id", pRemoteId).query();
			if (results != null && results.size() > 0) {
				location = results.get(0);
			}
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
			throw new LocationException("Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
		}

		return location;
	}

	/**
	 * Gets a List of Locations from the datastore that are dirty (i.e. should be
	 * synced with the server).
	 * @return
	 */
	public List<Location> getCurrentUserLocations(Long maxReturn) {
		
		QueryBuilder<Location, Long> queryBuilder = locationDao.queryBuilder();
		List<Location> locations = new ArrayList<Location>();

		try {
			queryBuilder.where().eq("current_user", true);
		
			//this is used for psudo-batching...optional.
			if(maxReturn > 0) {
				queryBuilder.limit(maxReturn);
				queryBuilder.orderBy("last_modified", Boolean.FALSE);
			}
			
			locations = locationDao.query(queryBuilder.prepare());
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_NAME, "Could not get dirty Locations.");
		}		
		return locations;
	}
	
	
	/**
	 * This will delete the user's location(s) that have remote_ids. Locations
	 * that do NOT have remote_ids have not been sync'ed w/ the server.
	 * 
	 * @param userLocalId
	 *            The user's local id
	 * @throws LocationException
	 */
	public Integer deleteUserLocations(String userLocalId) throws LocationException {

		int numberLocationsDeleted = 0;

		try {
			QueryBuilder<Location, Long> qb = locationDao.queryBuilder();
			qb.where().eq("user_id", userLocalId).and().isNotNull("remote_id");
			
			//deleting one at a time ensures that all child records are cleaned up and
			//events are fired at the correct granularity.
			//TODO: is this performant enough?
			List<Location> locations = qb.query();
			for(Location location : locations) {
				delete(location.getId());
				numberLocationsDeleted++;
			}
			
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to delete user's locations", sqle);
			throw new LocationException("Unable to delete user's locations", sqle);
		}
		Log.d(LOG_NAME, "Successfully deleted " + numberLocationsDeleted + " for user " + userLocalId);
		return numberLocationsDeleted;
	}

	/**
	 * Deletes a Location. This will also delete a Location's child
	 * Properties and Geometry data.
	 * 
	 * @param pPrimaryKey
	 * @throws OrmException
	 */
	public void delete(Long pPrimaryKey) throws LocationException {
		try {
			// read the full Location in
			Location location = locationDao.queryForId(pPrimaryKey);

			// delete Location properties.
			Collection<LocationProperty> properties = location.getProperties();
			if (properties != null) {
				for (LocationProperty property : properties) {
					locationPropertyDao.deleteById(property.getPk_id());
				}
			}

			// delete Geometry (but not corresponding GeometryType).
			locationGeometryDao.deleteById(location.getLocationGeometry().getPk_id());

			// finally, delete the Location.
			locationDao.deleteById(pPrimaryKey);
			
			for (ILocationEventListener listener : listeners) {
				listener.onLocationDeleted(location);
			}
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to delete Location: " + pPrimaryKey, sqle);
			throw new LocationException("Unable to delete Location: " + pPrimaryKey, sqle);
		}
	}
	
	
	@Override
	public boolean addListener(final ILocationEventListener listener) {
		return listeners.add(listener);
	}

	@Override
	public boolean removeListener(ILocationEventListener listener) {
		return listeners.remove(listener);
	}
}
