package mil.nga.giat.mage.sdk.fetch;

import mil.nga.giat.mage.sdk.ServerAsyncTask;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class ServerFetchAsyncTask extends ServerAsyncTask {

	protected final SharedPreferences sharedPreferences;

	public ServerFetchAsyncTask(Context context) {
		super(context);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
}