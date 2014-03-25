package mil.nga.giat.mage.sdk.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

	private static final String LOG_NAME = NetworkChangeReceiver.class.getName();

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifi.isAvailable()) {
			Log.d(LOG_NAME, "WIFI IS ON");
		} else {
			Log.d(LOG_NAME, "WIFI IS OFF");
		}

		if (mobile.isAvailable()) {
			Log.d(LOG_NAME, "MOBILE DATA IS ON");
		} else {
			Log.d(LOG_NAME, "MOBILE DATA IS OFF");
		}
	}
}