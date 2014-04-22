package mil.nga.giat.mage.sdk.service;

import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ObservationAlarmReceiver extends BroadcastReceiver {
	
	public static final int REQUEST_CODE = 91937;
	private static final String LOG_NAME = ObservationAlarmReceiver.class.getName();
	
	private long pushFrequency = -1;
	
	protected final long getObservationPushFrequency(Context context) {
		return PreferenceHelper.getInstance(context).getValue(R.string.observationPushFrequencyKey, Long.class, R.string.observationPushFrequencyDefaultValue);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_NAME, "Alarm fired to push dirty observations");
		
		if (pushFrequency == -1) {
			pushFrequency = getObservationPushFrequency(context);
		}
		
		if (ConnectivityUtility.isOnline(context)) {
			pushFrequency = getObservationPushFrequency(context);
		} else {
			Log.d(LOG_NAME, "The device is currently disconnected. Can't push observations.");
			pushFrequency = Math.min(pushFrequency * 2, 10 * 60 * 1000);
			scheduleNewAlarm(context);
			return;
		}
		
		ObservationHelper observationHelper = ObservationHelper.getInstance(context);
		List<Observation> observations = observationHelper.getDirty();
		
		for (Observation observation : observations) {
			Log.i(LOG_NAME, "Scheduling observation: " + observation.getId());
			Intent observationIntent = new Intent(context, ObservationIntentService.class);
			observationIntent.putExtra(ObservationIntentService.OBSERVATION_ID, observation.getId());
			context.startService(observationIntent);
		}
		
		scheduleNewAlarm(context);
	}
	
	private void scheduleNewAlarm(Context context) {
		Log.i(LOG_NAME, "Scheduling new observation alarm for " + (pushFrequency/1000) + " seconds.");
		Intent intent = new Intent(context, ObservationAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ObservationAlarmReceiver.REQUEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis() + pushFrequency;
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, firstMillis, pendingIntent);
	}
}
