package mil.nga.giat.mage.sdk.fetch;

import java.util.Arrays;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class UserServerFetchAsyncTask extends ServerFetchAsyncTask {

	private static final String LOG_NAME = UserServerFetchAsyncTask.class.getName();

	/**
	 * Controls the program flow initialization lifecycle
	 */
	private boolean isInitialization = false;
	
	public UserServerFetchAsyncTask(Context context) {
		super(context);
	}
	
	public UserServerFetchAsyncTask(Context context, boolean isInitialization) {
		super(context);
		this.isInitialization = isInitialization;
	}

	@Override
	protected Boolean doInBackground(Object... params) {

		Boolean status = Boolean.TRUE;
		if(params != null) {
			try {
				new UserServerFetch().fetch(mContext, Arrays.copyOf(params, params.length, String[].class));		
			} catch (Exception e) {
				Log.e(LOG_NAME, "There was a failure when fetching users.", e);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
				status = Boolean.FALSE;
			}
		}
		return status;
	}
	
	LocationServerFetchAsyncTask locationTask = null;
	ObservationServerFetchAsyncTask observationTask = null;
	
	@Override
	protected void onPostExecute(Boolean status) {
		super.onPostExecute(status);
		
		if(!status) {
			Log.e(LOG_NAME, "Error getting user!");
		} else if(isInitialization) {
			// start the next fetching tasks!
			locationTask = new LocationServerFetchAsyncTask(mContext);
			locationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			
			observationTask = new ObservationServerFetchAsyncTask(mContext);
			observationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}
	
	public void destroy() {
		cancel(true);
		if(locationTask != null) {
			locationTask.destroy();
		}
		
		if(observationTask != null) {
			observationTask.destroy();
		}
	}
	
}
