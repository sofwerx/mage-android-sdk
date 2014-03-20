package com.nga.giat.mage.sdk.serverfetch;

import mil.nga.giat.mage.sdk.R;
import android.content.Context;
import android.util.Log;

public class UserServerFetchAsyncTask extends ServerFetchAsyncTask {

	
	private static final String LOG_NAME = UserServerFetchAsyncTask.class.getName();
	
	public UserServerFetchAsyncTask(Context context) {
		super(context);
	}

	@Override
	protected Object doInBackground(Object... params) {
		
		while(Status.RUNNING.equals(getStatus())) {
			
			Log.d(LOG_NAME, "User fetch iteration " + runCount++);
			
			Long frequency = 
					getFrequency(R.string.userFetchFrequencyKey, 
							     R.string.userFetchFrequencyDefaultValue);
			
			//////////////////////////////////
			//TODO: DO THE WORK HERE!
			//////////////////////////////////
			
			try {				
				Log.d(LOG_NAME, "User fetch sleeping for " + frequency + "ms.");
				Thread.sleep(frequency);
			} 
			catch (InterruptedException ie) {
				Log.w("Interupted.  Unable to sleep " + frequency, ie);
				runCount = 0L;
				//TODO: evaluate if canceling the AsyncTask is the right thing to do.
				cancel(Boolean.TRUE);
			}
			
		}
				
		return runCount;
		
	}

}
