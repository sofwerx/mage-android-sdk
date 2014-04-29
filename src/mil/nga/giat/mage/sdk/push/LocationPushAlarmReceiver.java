package mil.nga.giat.mage.sdk.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationPushAlarmReceiver extends BroadcastReceiver {
	
	public static final int REQUEST_CODE = 92001;
	private static final String LOG_NAME = ObservationPushAlarmReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_NAME, "Alarm fired to push locations");

		Intent locationIntent = new Intent(context, LocationPushIntentService.class);
		context.startService(locationIntent);
	}
}
