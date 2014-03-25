package mil.nga.giat.mage.sdk.fetch;

import android.content.Context;
import android.os.AsyncTask;

public abstract class ServerFetchAsyncTask extends AsyncTask<Void, Void, Void> {

	protected final Context mContext;

	/**
	 * Construct task with a Context.
	 * 
	 * @param context
	 */
	public ServerFetchAsyncTask(Context context) {
		super();
		mContext = context;
	}
}
