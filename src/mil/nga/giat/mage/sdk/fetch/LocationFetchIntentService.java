package mil.nga.giat.mage.sdk.fetch;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import mil.nga.giat.mage.sdk.ConnectivityAwareIntentService;
import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.datastore.location.LocationHelper;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class LocationFetchIntentService extends ConnectivityAwareIntentService implements OnSharedPreferenceChangeListener {

	private static final String LOG_NAME = LocationFetchIntentService.class.getName();

	public LocationFetchIntentService() {
		super(LOG_NAME);
	}

	protected AtomicBoolean fetchSemaphore = new AtomicBoolean(false);

	protected final synchronized long getLocationFetchFrequency() {
		return PreferenceHelper.getInstance(getApplicationContext()).getValue(R.string.userFetchFrequencyKey, Long.class, R.string.userFetchFrequencyDefaultValue);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		super.onHandleIntent(intent);
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);

		LocationHelper locationHelper = LocationHelper.getInstance(getApplicationContext());
		UserHelper userHelper = UserHelper.getInstance(getApplicationContext());
		UserServerFetch userFetch = new UserServerFetch(getApplicationContext());
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		while (true) {
			Boolean isDataFetchEnabled = sharedPreferences.getBoolean("dataFetchEnabled", Boolean.TRUE);
			if (isConnected && isDataFetchEnabled) {

				Log.d(LOG_NAME, "The device is currently connected. Attempting to fetch Locations...");
				try {
					Collection<Location> locations = MageServerGetRequests.getLocations(getApplicationContext());
					for (Location location : locations) {

						// make sure that the user exists and is persisted in the local data-store
						User user = null;
						String userId = location.getPropertiesMap().get("user");
						if (userId != null) {
							user = userHelper.read(userId);
							// TODO : test the timer to make sure users are updated as needed!
							final long sixHoursInMillseconds = 6 * 60 * 60 * 1000;
							if (user == null || (new Date()).after(new Date(user.getFetchedDate().getTime() + sixHoursInMillseconds))) {
								// get any users that were not recognized or expired
								userFetch.fetch(new String[] { userId });
								user = userHelper.read(userId);
								location.setUser(user);
							}
						}

						Location existingLocation = locationHelper.read(location.getRemoteId());
						// if there is no existing location, create one
						if (existingLocation == null) {
							// a user wasn't read in above
							if (user == null) {
								user = userHelper.read(userId);
							}
							// delete old location and create new one
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
				} catch (Exception e) {
					Log.e(LOG_NAME, "There was a failure while performing an Location Fetch opperation.", e);
				}
			} else {
				Log.d(LOG_NAME, "The device is currently disconnected. No Locations to fetch.");
			}

			long frequency = getLocationFetchFrequency();
			long lastFetchTime = new Date().getTime();
			long currentTime = new Date().getTime();
			try {
				while (lastFetchTime + (frequency = getLocationFetchFrequency()) > (currentTime = new Date().getTime())) {
					synchronized (fetchSemaphore) {
						Log.d(LOG_NAME, "Location fetch sleeping for " + (lastFetchTime + frequency - currentTime) + "ms.");
						fetchSemaphore.wait(lastFetchTime + frequency - currentTime);
						if (fetchSemaphore.get() == true) {
							break;
						}
					}
				}
				synchronized (fetchSemaphore) {
					fetchSemaphore.set(false);
				}
			} catch (InterruptedException ie) {
				Log.e(LOG_NAME, "Interupted.  Unable to sleep " + frequency, ie);
			} finally {
				isConnected = ConnectivityUtility.isOnline(getApplicationContext());
			}
		}
	}
	
	/**
	 * Will alert the fetching thread that changes have been made
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equalsIgnoreCase(getApplicationContext().getString(R.string.userFetchFrequencyKey))) {
			synchronized (fetchSemaphore) {
				fetchSemaphore.notifyAll();
			}
		}
	}

	@Override
	public void onAnyConnected() {
		super.onAnyConnected();
		synchronized (fetchSemaphore) {
			fetchSemaphore.set(true);
			fetchSemaphore.notifyAll();
		}
	}

}
