package mil.nga.giat.mage.sdk.datastore.location;

import mil.nga.giat.mage.sdk.datastore.DBHelper;
import mil.nga.giat.mage.sdk.exceptions.LocationException;
import android.content.Context;

public class LocationHelper {

	private static final String LOG_NAME = "mage.observation.log";

	
	// required DBHelper and DAOs for handing CRUD operations for Observations
	private DBHelper helper;
	
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
				
		return null;
		
	}
	
	
	
}
