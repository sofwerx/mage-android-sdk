package mil.nga.giat.mage.sdk.fetch;

import java.util.Collection;
import java.util.Date;

import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.datastore.location.LocationHelper;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class LocationServerFetchIntentService extends IntentService {
	
	private static final String LOG_NAME = LocationServerFetchIntentService.class.getName();
	
	public LocationServerFetchIntentService() {
		super("LocationServerFetchIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		LocationHelper locationHelper = LocationHelper.getInstance(getApplicationContext());
		UserHelper userHelper = UserHelper.getInstance(getApplicationContext());
		UserServerFetch userFetch = new UserServerFetch(getApplicationContext());
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		if (!ConnectivityUtility.isOnline(getApplicationContext())) {
			Log.d(LOG_NAME, "Not connected.  Will try later.");
			return;
		}
		
		Boolean isDataFetchEnabled = sharedPreferences.getBoolean("dataFetchEnabled", true);
		if (!isDataFetchEnabled) {
			Log.d(LOG_NAME, "Data fetch disabled.  Will try later.");
			return;
		}
		
		Log.d(LOG_NAME, "The device is currently connected. Attempting to fetch Locations...");
		try {
			Collection<Location> locations = MageServerGetRequests
					.getLocations(getApplicationContext());
			for (Location location : locations) {

				// make sure that the user exists and is persisted in the
				// local data-store
				User user = null;
				String userId = location.getPropertiesMap().get("user");
				if (userId != null) {
					user = userHelper.read(userId);
					// TODO : test the timer to make sure users are
					// updated as needed!
					final long sixHoursInMillseconds = 6 * 60 * 60 * 1000;
					if (user == null || (new Date()).after(new Date(user.getFetchedDate().getTime() + sixHoursInMillseconds))) {
						// get any users that were not recognized or expired
						userFetch.fetch(new String[] { userId });
						user = userHelper.read(userId);
						location.setUser(user);
					}
				}

				Location existingLocation = locationHelper.read(location
						.getRemoteId());
				// if there is no existing location, create one
				if (existingLocation == null) {
					// jik a user wasn't read in above
					if (user == null) {
						user = userHelper.read(userId);
					}
					// finally, delete user's old location and create a new
					// one.
					if (user != null && user.getRemoteId() != null) {
						location.setUser(user);
						locationHelper.deleteUserLocations(String.valueOf(user.getPk_id()));
						locationHelper.create(location);
						Log.d(LOG_NAME, "created location with remote_id " + location.getRemoteId());
					} else {
						Log.w(LOG_NAME, "Warning, a location was trying to be saved w/ no user.");
					}
				}
			}
		} 
		catch (Exception e) {
			Log.e(LOG_NAME, "There was a failure while performing an Location Fetch opperation.", e);
		}
	}

}
