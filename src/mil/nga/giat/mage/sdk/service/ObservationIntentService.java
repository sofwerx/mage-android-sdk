package mil.nga.giat.mage.sdk.service;

import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.exceptions.ObservationException;
import mil.nga.giat.mage.sdk.http.post.MageServerPostRequests;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ObservationIntentService extends IntentService {
	
	private static final String LOG_NAME = ObservationIntentService.class.getName();
	
	public static final String OBSERVATION_PUSHED = "mil.nga.giat.mage.sdk.service.OBSERVATION_PUSHED";
	
	public static final String OBSERVATION_ID = "observationId";
	
	public ObservationIntentService() {
		super("ObservationIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (!ConnectivityUtility.isOnline(getApplicationContext())) {
			Log.d(LOG_NAME, "Not connected.  Will try later.");
			return;
		}
		Long observationId = intent.getLongExtra(OBSERVATION_ID, -1);
		Log.i(LOG_NAME, "Handling observation: " + observationId);
		try {
			Observation observation = ObservationHelper.getInstance(getApplicationContext()).read(observationId);
			if (!observation.isDirty()) {
				Log.i(LOG_NAME, "Already pushed observation " + observationId + ", skipping.");
				return;
			}
			Log.d(LOG_NAME, "Pushing observation with id: " + observation.getId());
			
			observation = MageServerPostRequests.postObservation(observation, getApplicationContext());
			if (observation == null) {
				Log.w(LOG_NAME, "Failed to push observation");
			} else {
				Log.d(LOG_NAME, "Pushed observation with remote_id: " + observation.getRemoteId());
			}
		} catch (ObservationException oe) {
			Log.e(LOG_NAME, "Error obtaining observation: " + observationId, oe);
		}

	}

}
