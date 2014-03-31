package mil.nga.giat.mage.sdk.fetch;

import android.content.Context;

public class LocationServerFetchAsyncTask extends ServerFetchAsyncTask {

	private static final String LOG_NAME = LocationServerFetchAsyncTask.class.getName();

	public LocationServerFetchAsyncTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		return Boolean.TRUE;
	}

}
