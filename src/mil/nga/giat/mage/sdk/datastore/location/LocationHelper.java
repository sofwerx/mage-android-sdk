package mil.nga.giat.mage.sdk.datastore.location;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;

import mil.nga.giat.mage.sdk.datastore.DaoHelper;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.event.IEventDispatcher;
import mil.nga.giat.mage.sdk.event.IEventListener;
import mil.nga.giat.mage.sdk.event.ILocationEventListener;
import mil.nga.giat.mage.sdk.exceptions.LocationException;
import mil.nga.giat.mage.sdk.exceptions.UserException;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

/**
 * A utility class for accessing {@link Location} data from the physical data
 * model. The details of ORM DAOs and Lazy Loading should not be exposed past
 * this class.
 * 
 * @author wiedemannse
 * 
 */
public class LocationHelper extends DaoHelper<Location> implements IEventDispatcher<Location> {

	private static final String LOG_NAME = LocationHelper.class.getName();

	private final Dao<Location, Long> locationDao;
	private final Dao<LocationGeometry, Long> locationGeometryDao;
	private final Dao<LocationProperty, Long> locationPropertyDao;

	private Collection<ILocationEventListener> listeners = new ArrayList<ILocationEventListener>();
	
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

	public List<Location> readAll() throws LocationException {
		List<Location> locations = new ArrayList<Location>();
		try {
			locations = locationDao.queryForAll();
		} catch (SQLException sqle) {
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
			ConcurrentSkipListSet<Location> locations = new ConcurrentSkipListSet<Location>();
			locations.add(createdLocation);
			for (ILocationEventListener listener : listeners) {
				listener.onLocationCreated(locations);
			}

		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "There was a problem creating the location: " + pLocation + ".", sqle);
			throw new LocationException("There was a problem creating the observation: " + pLocation + ".", sqle);
		}

		return createdLocation;
	}
	
	@Override
	public Location read(String pRemoteId) throws LocationException {
		Location location = null;
		try {
			List<Location> results = locationDao.queryBuilder().where().eq("remote_id", pRemoteId).query();
			if(results != null && results.size() > 0) {
				location = results.get(0);
			}
		}
		catch(SQLException sqle) {
			Log.e(LOG_NAME, "Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
			throw new LocationException("Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
		}
		
		return location;
	}
	
	/**
	 * This will delete the user's location(s) that have remote_ids.  Locations that do NOT have 
	 * remote_ids have not been sync'ed w/ the server.
	 * 
	 * @param userLocalId The user's local id
	 * @throws LocationException 
	 */
	public Integer deleteUserLocations(String userLocalId) throws LocationException {
		
		int numberLocationsDeleted = 0; 
		
		try {
			DeleteBuilder<Location, Long> db = locationDao.deleteBuilder();
			db.where().eq("user_id", userLocalId).and().isNotNull("remote_id");
			numberLocationsDeleted = locationDao.delete(db.prepare());
		} 
		catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to delete user's locations", sqle);
			throw new LocationException("Unable to delete user's locations", sqle);
		}	
		Log.d(LOG_NAME, "Successfully deleted " + numberLocationsDeleted + " for user " + userLocalId);
		return numberLocationsDeleted;
	}
	
	@Override
	public boolean addListener(final IEventListener<Location> listener) throws LocationException {
		boolean status = listeners.add((ILocationEventListener) listener);
		
		new Callable<Object>() {
			@Override
			public Object call() throws LocationException {
				((ILocationEventListener)listener).onLocationCreated(readAll());
				return null;
			}
		}.call();
		return status;
	}

	@Override
	public boolean removeListener(IEventListener<Location> listener) {
		return listeners.remove((ILocationEventListener)listener);
	}
}
