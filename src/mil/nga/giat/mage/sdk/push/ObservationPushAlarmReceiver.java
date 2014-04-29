package mil.nga.giat.mage.sdk.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ObservationPushAlarmReceiver extends BroadcastReceiver {
	
	public static final int REQUEST_CODE = 91001;
	private static final String LOG_NAME = ObservationPushAlarmReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_NAME, "Alarm fired to push dirty observations");

		Intent observationIntent = new Intent(context, ObservationPushIntentService.class);
		context.startService(observationIntent);
	}
}
