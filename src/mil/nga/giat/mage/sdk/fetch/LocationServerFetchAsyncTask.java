package mil.nga.giat.mage.sdk.fetch;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.datastore.location.LocationHelper;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import android.content.Context;
import android.util.Log;

public class LocationServerFetchAsyncTask extends ServerFetchAsyncTask {

	private static final String LOG_NAME = LocationServerFetchAsyncTask.class
			.getName();

	public LocationServerFetchAsyncTask(Context context) {
		super(context);
	}

	protected AtomicBoolean fetchSemaphore = new AtomicBoolean(false);

	protected final synchronized long getLocationFetchFrequency() {
		return PreferenceHelper.getInstance(mContext).getValue(
				R.string.locationFetchFrequencyKey, Long.class,
				R.string.locationFetchFrequencyDefaultValue);
	}

	@Override
	protected Boolean doInBackground(Object... params) {

		Boolean status = Boolean.TRUE;

		LocationHelper locationHelper = LocationHelper.getInstance(mContext);
		UserHelper userHelper = UserHelper.getInstance(mContext);
		UserServerFetch userFetch = new UserServerFetch(mContext);

		while (Status.RUNNING.equals(getStatus()) && !isCancelled()) {
		
			if (isConnected) {
				try {
					Collection<Location> locations = MageServerGetRequests
							.getLocations(mContext);
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
			else {
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
			} 
			catch (InterruptedException ie) {
				Log.w("Interupted.  Unable to sleep " + frequency, ie);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
				status = Boolean.FALSE;
			} 
			finally {
				isConnected = ConnectivityUtility.isOnline(mContext);
			}
		}
		return status;
	}
}
