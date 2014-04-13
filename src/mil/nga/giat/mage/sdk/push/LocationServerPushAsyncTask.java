package mil.nga.giat.mage.sdk.push;

import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.datastore.location.LocationHelper;
import mil.nga.giat.mage.sdk.http.post.MageServerPostRequests;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import android.content.Context;
import android.util.Log;

public class LocationServerPushAsyncTask extends ServerPushAsyncTask {

private static final String LOG_NAME = LocationServerPushAsyncTask.class.getName();
	
	// in milliseconds
	private long pushFrequency;
	
	public LocationServerPushAsyncTask(Context context) {
		super(context);
		pushFrequency = getLocationPushFrequency();
	}

	protected final long getLocationPushFrequency() {
		return PreferenceHelper.getInstance(mContext).getValue(R.string.locationPushFrequencyKey, Long.class, R.string.locationPushFrequencyDefaultValue);
	}
	
	@Override
	protected Boolean doInBackground(Object... params) {
		Boolean status = Boolean.TRUE;
		while (Status.RUNNING.equals(getStatus()) && !isCancelled()) {

			if (isConnected) {
				pushFrequency = getLocationPushFrequency();
				LocationHelper locationHelper = LocationHelper.getInstance(mContext);
				List<Location> locations = locationHelper.getDirty();
				for (Location location : locations) {
					
					// TODO : Is this the right thing to do?
					if(isCancelled()) {
						break;
					}
					//TODO: Implement this method...
					//Location savedLocation = MageServerPostRequests.postLocation(location, mContext);
				}
			} 
			else {
				Log.d(LOG_NAME, "The device is currently disconnected. Can't push locations.");
				pushFrequency = Math.min(pushFrequency*2, 10*60*1000);
			}
			long lastFetchTime = new Date().getTime();
			long currentTime = new Date().getTime();

			try {
				while (lastFetchTime + pushFrequency > (currentTime = new Date().getTime())) {
					Log.d(LOG_NAME, "Location push sleeping for " + (lastFetchTime + pushFrequency - currentTime) + "ms.");
					Thread.sleep(lastFetchTime + pushFrequency - currentTime);
				}
			} catch (InterruptedException ie) {
				Log.w("Interupted.  Unable to sleep " + pushFrequency, ie);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
				status = Boolean.FALSE;
			} finally {
				isConnected = ConnectivityUtility.isOnline(mContext);
			}
		}
		return status;

	}

}
