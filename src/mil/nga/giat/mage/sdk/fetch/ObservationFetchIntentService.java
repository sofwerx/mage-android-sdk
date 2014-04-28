package mil.nga.giat.mage.sdk.fetch;

import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.common.State;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationProperty;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;


public class ObservationFetchIntentService extends IntentService {

	private static final String LOG_NAME = ObservationFetchIntentService.class.getName();
	
	public static final String OBSERVATIONS_FETCHED = "mil.nga.giat.mage.sdk.fetch.OBSERVATIONS_FETCHED";
	
	public ObservationFetchIntentService() {
		super("ObservationFetchIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) { 
		
		ObservationHelper observationHelper = ObservationHelper.getInstance(getApplicationContext());
		UserHelper userHelper = UserHelper.getInstance(getApplicationContext());
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
		
		Log.d(LOG_NAME, "The device is currently connected. Attempting to fetch Observations...");
		try {
			List<Observation> observations = MageServerGetRequests.getObservations(getApplicationContext());
			Log.d(LOG_NAME, "Found observations " + observations.size());
			for (Observation observation : observations) {
				String userId = observation.getUserId();
				if (userId != null) {
					User user = userHelper.read(userId);
					// TODO : test the timer to make sure users are updated as needed!
					final long sixHoursInMillseconds = 6 * 60 * 60 * 1000;
					if (user == null || (new Date()).after(new Date(user.getFetchedDate().getTime() + sixHoursInMillseconds))) {
						// get any users that were not recognized or expired
						new UserServerFetch(getApplicationContext()).fetch(new String[] { userId });
					}
				}

				Observation oldObservation = observationHelper.read(observation.getRemoteId());
				if (observation.getState().equals(State.ARCHIVE) && oldObservation != null) {
					observationHelper.delete(oldObservation.getId());
					Log.d(LOG_NAME, "deleted observation with remote_id " + observation.getRemoteId());
				} else if (!observation.getState().equals(State.ARCHIVE) && oldObservation == null) {
					observation = observationHelper.create(observation);
					// FIXME : a simple proto-type for vibrations
					ObservationProperty observationProperty = observation.getPropertiesMap().get("EVENTLEVEL");
					if(observationProperty != null && observationProperty.getValue().equalsIgnoreCase("high")) {
						Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
						vibrator.vibrate(50);
					}
					Log.d(LOG_NAME, "created observation with remote_id " + observation.getRemoteId());
				} else if (!observation.getState().equals(State.ARCHIVE) && oldObservation != null) {
				    observation.setId(oldObservation.getId());
					observation = observationHelper.update(observation);
					Log.d(LOG_NAME, "updated observation with remote_id " + observation.getRemoteId());
				}
			}
		} catch (Exception e) {
			Log.e(LOG_NAME, "There was a failure while performing an Observation Fetch opperation.", e);
		}
	}
}
