package mil.nga.giat.mage.sdk.datastore.location;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mil.nga.giat.mage.sdk.datastore.DBHelper;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.event.IEventDispatcher;
import mil.nga.giat.mage.sdk.event.IEventListener;
import mil.nga.giat.mage.sdk.event.location.ILocationEventListener;
import mil.nga.giat.mage.sdk.exceptions.LocationException;
import mil.nga.giat.mage.sdk.exceptions.ObservationException;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

/**
 * A utility class for accessing {@link Location} data from the physical data
 * model. The details of ORM DAOs and Lazy Loading should not be exposed past
 * this class.
 * 
 * @author wiedemannse
 * 
 */
public class LocationHelper implements IEventDispatcher<Location> {

	private static final String LOG_NAME = LocationHelper.class.getName();

	// required DBHelper and DAOs for handing CRUD operations for Observations
	private final DBHelper helper;
	private final Dao<Location, Long> locationDao;
	private final Dao<LocationGeometry, Long> locationGeometryDao;
	private final Dao<LocationProperty, Long> locationPropertyDao;

	private List<ILocationEventListener> listeners = new ArrayList<ILocationEventListener>();
	
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

		helper = DBHelper.getInstance(context);

		try {
			locationDao = helper.getLocationDao();
			locationGeometryDao = helper.getLocationGeometryDao();
			locationPropertyDao = helper.getLocationPropertyDao();
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to communicate " + "with Location database.", sqle);

			// Fatal Error!
			throw new IllegalStateException("Unable to communicate " + "with Location database.", sqle);
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
	
	/**
	 * This utility method abstracts the complexities of persisting a new
	 * Location. All the caller needs to to is construct a Location object and
	 * call the appropriate setters.
	 * 
	 * @param pLocation
	 *            A constructed Location.
	 * @return A fully constructed Location complete with database primary keys.
	 * @throws LocationException
	 *             If the Observation being created violates any database
	 *             constraints.
	 */
	public Location createLocation(Location pLocation) throws LocationException {

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
			for (ILocationEventListener listener : listeners) {
				listener.onLocationCreated(createdLocation);
			}

		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "There was a problem creating the location: " + pLocation + ".", sqle);
			throw new LocationException("There was a problem creating the observation: " + pLocation + ".", sqle);
		}

		return createdLocation;
	}

	@Override
	public List<Location> addListener(IEventListener<Location> listener) throws LocationException {
		listeners.add((ILocationEventListener) listener);
		return readAll();
	}

	@Override
	public boolean removeListener(IEventListener<Location> listener) {
		return listeners.remove((ILocationEventListener)listener);
	}
}
