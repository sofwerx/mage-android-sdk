package mil.nga.giat.mage.sdk.fetch;

import java.util.Arrays;

import android.content.Context;
import android.util.Log;

/**
 * Currently not used directly!  Will fetch users from the server.
 * 
 * @author wiedemannse
 *
 */
public class UserServerFetchAsyncTask extends ServerFetchAsyncTask {

	private static final String LOG_NAME = UserServerFetchAsyncTask.class.getName();
	
	public UserServerFetchAsyncTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Object... params) {

		Boolean status = Boolean.TRUE;
		if(params != null) {
			try {
				new UserServerFetch(mContext).fetch(Arrays.copyOf(params, params.length, String[].class));		
			} catch (Exception e) {
				Log.e(LOG_NAME, "There was a failure when fetching users.", e);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
				status = Boolean.FALSE;
			}
		}
		return status;
	}	
}
