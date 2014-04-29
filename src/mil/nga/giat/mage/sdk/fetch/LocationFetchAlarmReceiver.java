package mil.nga.giat.mage.sdk.fetch;

import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationFetchAlarmReceiver extends BroadcastReceiver {

	public static final int REQUEST_CODE = 92000;
	private static final String LOG_NAME = LocationFetchAlarmReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_NAME, "Alarm fired to fetch locations");

		if (!ConnectivityUtility.isOnline(context)) {
			Log.d(LOG_NAME, "The device is currently disconnected. Can't fetch locations.");
			return;
		}

		Intent observationIntent = new Intent(context, LocationFetchIntentService.class);
		context.startService(observationIntent);
	}
}
