package mil.nga.giat.mage.sdk.datastore.location;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.giat.mage.sdk.datastore.DBHelper;
import mil.nga.giat.mage.sdk.datastore.common.GeometryType;
import mil.nga.giat.mage.sdk.exceptions.LocationException;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

public class LocationHelper {

	private static final String LOG_NAME = LocationHelper.class.getName();

	// required DBHelper and DAOs for handing CRUD operations for Observations
	private DBHelper helper;
	private Dao<Location, Long> locationDao;
	private Dao<LocationGeometry, Long> locationGeometryDao;
	private Dao<GeometryType, Long> geometryTypeDao;
	private Dao<LocationProperty, Long> locationPropertyDao;

	
	/**
	 * This Map can be used to ensure that a valid GEOMETRY TYPE is used when performing CRUD 
	 * operations on Observations.
	 */
	public static final Map<String, GeometryType> GEOMETRY_TYPE_LOOKUP_MAP = new HashMap<String, GeometryType>();
	
	/**
	 * Singleton.
	 */
	private static LocationHelper mLocationHelper;

	/**
	 * Use of a Singleton here ensures that an excessive amount of DAOs are not created.
	 * @param context Application Context
	 * @return A fully constructed and operational LocationHelper.
	 */
	public static LocationHelper getInstance(Context context) {
		if(mLocationHelper == null) {
			mLocationHelper = new LocationHelper(context);
		}
		return mLocationHelper;
	}
	
	/**
	 * Only one-per JVM.  Singleton.
	 * @param context
	 */
	private LocationHelper(Context context) {
		
		helper = DBHelper.getInstance(context);
		
		try {
			locationDao = helper.getLocationDao();
			locationGeometryDao = helper.getLocationGeometryDao();
			geometryTypeDao = helper.getGeometryTypeDao();
			locationPropertyDao = helper.getLocationPropertyDao();
			
			//initialize geometry type values;
			List<GeometryType> geometryTypes = geometryTypeDao.queryForAll();
			for(GeometryType geometryType : geometryTypes) {
				GEOMETRY_TYPE_LOOKUP_MAP.put(geometryType.getType(), geometryType);
			}
			
		}
		catch(SQLException sqle) {
			Log.e(LOG_NAME, "Unable to communicate "
					+ "with Location database.", sqle);

			//Fatal Error!
			throw new IllegalStateException("Unable to communicate "
					+ "with Location database.", sqle);				
		}
		
	}
	
	/**
	 * This utility method abstracts the complexities of persisting a new Location.
	 * All the caller needs to to is construct a Location object and call the
	 * appropriate setters.
	 * @param pLocation A constructed Location.
	 * @return A fully constructed Location complete with database primary keys.
	 * @throws LocationException If the Observation being created violates any database
	 *                      constraints.
	 */
	public Location createLocation(Location pLocation) throws LocationException {
				
		Location createdLocation;
				
		//Validate GeometryType.  ORM will not allow a null Geometry.
		GeometryType geometryType = pLocation.getLocationGeometry().getGeometryType();
		if(GEOMETRY_TYPE_LOOKUP_MAP.containsKey(geometryType.getType())) {
			//This sets the GeometryType to the value already persisted in the lookup table.
			//We don't want duplicates.
			pLocation.getLocationGeometry().setGeometryType(GEOMETRY_TYPE_LOOKUP_MAP.get(geometryType.getType()));
		}
		else {
			Log.w(LOG_NAME, "The geometry type: '" + geometryType.getType()
					+ "' is invalid.  Valid geometry types are "
					+ GEOMETRY_TYPE_LOOKUP_MAP.keySet());
		}				
		
		try {			
			//create Location geometry.
			locationGeometryDao.create(pLocation.getLocationGeometry());			
			
			createdLocation = locationDao.createIfNotExists(pLocation);
			
			//create Location properties.
			Collection<LocationProperty> locationProperties = pLocation.getProperties();
			if (locationProperties != null) {
				for (LocationProperty locationProperty : locationProperties) {
					locationProperty.setLocation(createdLocation);
					locationPropertyDao.create(locationProperty);
				}
			}
			
			
		} 
		catch (SQLException sqle) {
			Log.e(LOG_NAME,"There was a problem creating the location: " + pLocation + ".",sqle);
			throw new LocationException("There was a problem creating the observation: " + pLocation + ".",sqle);
		}
		
		return createdLocation;
		
	}
	
	
	
}
