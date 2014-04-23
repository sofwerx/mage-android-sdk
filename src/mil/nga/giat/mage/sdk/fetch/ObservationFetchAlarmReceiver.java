package mil.nga.giat.mage.sdk.fetch;

import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ObservationFetchAlarmReceiver extends BroadcastReceiver {
	
	public static final int REQUEST_CODE = 91737;
	private static final String LOG_NAME = ObservationFetchAlarmReceiver.class.getName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_NAME, "Alarm fired to fetch observations");
		
		if (!ConnectivityUtility.isOnline(context)) {
			Log.d(LOG_NAME, "The device is currently disconnected. Can't fetch observations.");
			return;
		}
		
		
		Intent observationIntent = new Intent(context, ObservationFetchIntentService.class);
		context.startService(observationIntent);
		
	}
}
