package mil.nga.giat.mage.sdk.fetch;

import android.content.Context;

public class LocationServerFetchAsyncTask extends ServerFetchAsyncTask {

	private static final String LOG_NAME = LocationServerFetchAsyncTask.class.getName();

	public LocationServerFetchAsyncTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Object... params) {

		return Boolean.TRUE;
	}
	
	public void destroy() {
		cancel(true);
	}
}
