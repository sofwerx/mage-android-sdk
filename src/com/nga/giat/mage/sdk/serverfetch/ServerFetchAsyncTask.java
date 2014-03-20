package com.nga.giat.mage.sdk.serverfetch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public abstract class ServerFetchAsyncTask extends AsyncTask<Object, Object, Object> {


	final SharedPreferences sharedPreferences;

	final Context mContext;
	
	/**
	 * Incremented every time the Task attempts to do work.  
	 * Warning, the runCount will buffer overrun in 2.9 quintillion years.
	 */
	Long runCount = 0L;
	
	/**
	 * Construct task with a Context.
	 * @param context
	 */
	public ServerFetchAsyncTask(Context context) {
		super();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		mContext = context;
	}	
	
	/**
	 * Utility for getting the system's frequency.
	 * @param key The key to use.  Look in R.string.
	 * @param defaultValue The default value to use.  Look in R.string.
	 * @return
	 */
	public Long getFrequency(Integer key, Integer defaultValue) {
		
		Long frequency = Long.parseLong(sharedPreferences.getString(
				mContext.getString(key), mContext.getString(defaultValue)));
		
		return frequency;
	}	

}
