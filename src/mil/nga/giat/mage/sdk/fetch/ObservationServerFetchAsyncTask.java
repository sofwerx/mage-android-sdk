package mil.nga.giat.mage.sdk.fetch;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.common.State;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A simple procedure thats pull observations from the server.
 * 
 * @author wiedemannse
 * 
 */
public class ObservationServerFetchAsyncTask extends ServerFetchAsyncTask implements OnSharedPreferenceChangeListener {

	private static final String LOG_NAME = ObservationServerFetchAsyncTask.class.getName();

	public ObservationServerFetchAsyncTask(Context context) {
		super(context);
		PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(this);
	}

	protected AtomicBoolean fetchSemaphore = new AtomicBoolean(false);

	protected final synchronized long getobservationFetchFrequency() {
		return PreferenceHelper.getInstance(mContext).getValue(R.string.observationFetchFrequencyKey, Long.class, R.string.observationFetchFrequencyDefaultValue);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Object... params) {

		Boolean status = Boolean.TRUE;

		ObservationHelper observationHelper = ObservationHelper.getInstance(mContext);
		UserHelper userHelper = UserHelper.getInstance(mContext);

		while (Status.RUNNING.equals(getStatus()) && !isCancelled()) {

			if (isConnected) {

				Log.d(LOG_NAME, "The device is currently connected. Attempting to fetch...");
				try {
					Collection<Observation> observations = MageServerGetRequests.getObservations(mContext);
					for (Observation observation : observations) {
						// stop doing stuff if the task is told to shutdown
						if(isCancelled()) {
							break;
						}
						// TODO: the server is going to move the user id to
						// a different section of the json
						String userId = observation.getPropertiesMap().get("userId");
						if (userId != null) {
							User user = userHelper.read(userId);
							// TODO : test the timer to make sure users are
							// updated as needed!
							final long sixHoursInMillseconds = 6 * 60 * 60 * 1000;
							if (user == null || (new Date()).after(new Date(user.getFetchedDate().getTime() + sixHoursInMillseconds))) {
								// get any users that were not recognized or expired
								new UserServerFetch(mContext).fetch(new String[] { userId });
							}
						}

						Observation oldObservation = observationHelper.read(observation.getRemoteId());
						if (observation.getState().equals(State.ARCHIVE) && oldObservation != null) {
							observationHelper.delete(oldObservation.getId());
							Log.d(LOG_NAME, "delete observation with remote_id " + observation.getRemoteId());
						} else if (!observation.getState().equals(State.ARCHIVE) && oldObservation == null) {
							observation = observationHelper.create(observation);
							Log.d(LOG_NAME, "created observation with remote_id " + observation.getRemoteId());
						} else if (!observation.getState().equals(State.ARCHIVE) && oldObservation != null) {
							observation = observationHelper.update(observation, oldObservation);
							Log.d(LOG_NAME, "updated observation with remote_id " + observation.getRemoteId());
						}
					}
				} catch (Exception e) {
					Log.e(LOG_NAME, "There was a failure while performing an Observation Fetch opperation.", e);
				}

			} else {
				Log.d(LOG_NAME, "The device is currently disconnected. Nothing to fetch.");
			}

			long frequency = getobservationFetchFrequency();
			long lastFetchTime = new Date().getTime();
			long currentTime = new Date().getTime();
			try {
				while (lastFetchTime + (frequency = getobservationFetchFrequency()) > (currentTime = new Date().getTime())) {
					synchronized (fetchSemaphore) {
						Log.d(LOG_NAME, "Observation fetch sleeping for " + (lastFetchTime + frequency - currentTime) + "ms.");
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
				Log.w("Interupted.  Unable to sleep " + frequency, ie);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
				status = Boolean.FALSE;
			} finally {
				isConnected = ConnectivityUtility.isOnline(mContext);
			}
		}
		return status;
	}

	/**
	 * Will alert the fetching thread that changes have been made
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equalsIgnoreCase(mContext.getString(R.string.observationFetchFrequencyKey))) {
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
