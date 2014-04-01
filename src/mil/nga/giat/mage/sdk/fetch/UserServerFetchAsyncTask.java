package mil.nga.giat.mage.sdk.fetch;

import java.net.URL;
import java.util.Date;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.exceptions.UserException;
import mil.nga.giat.mage.sdk.gson.deserializer.UserDeserializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

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

		// TODO : account for deserialization when no userids are given!
		if(params != null) {
			
			HttpEntity entity = null;
			try {
				URL serverURL = new URL(PreferenceHelper.getInstance(mContext).getValue(R.string.serverURLKey));
				
				final Gson userDeserializer = UserDeserializer.getGsonBuilder();
				DefaultHttpClient httpclient = HttpClientManager.getInstance(mContext).getHttpClient();
				UserHelper userHelper = UserHelper.getInstance(mContext);
	
				// loop over all the ids
				for(int i = 0; i < params.length; i++) {
					String userPath = "api/users";
					String userId = params[i].toString();
					userPath += "/" + userId;
					boolean isCurrentUser = false;
					// is this a request for the current user?
					if (userId.equalsIgnoreCase("myself")) {
						isCurrentUser = true;
					} else {
						try {
							for(User u : userHelper.readCurrentUsers()) {
								String rid = u.getRemoteId();
								if(rid != null && rid.equalsIgnoreCase(userId)) {
									isCurrentUser = true;
									break;
								}
							}
						} catch (UserException e) {
							Log.e(LOG_NAME, "Could not get current users.");
						}	
					}					
					
					URL userURL = new URL(serverURL, userPath);
					
					Log.d(LOG_NAME, userURL.toString());
					HttpGet get = new HttpGet(userURL.toURI());
					HttpResponse response = httpclient.execute(get);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						entity = response.getEntity();
						JSONObject userJson = new JSONObject(EntityUtils.toString(entity));
						if (userJson != null) {
							User user = userDeserializer.fromJson(userJson.toString(), User.class);
							if (user != null) {
								if(userHelper.read(user.getRemoteId()) == null) {
									user.setCurrentUser(isCurrentUser);
									user.setFetchedDate(new Date());
									user = userHelper.create(user);
									Log.d(LOG_NAME, "created user with remote_id " + user.getRemoteId());
								} else {
									// TODO: perform update?
								}
							}
						}
					}
		
				}				
			} catch (Exception e) {
				Log.e(LOG_NAME, "There was a failure when fetching users.", e);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
				status = Boolean.FALSE;
			} finally {
				try {
					if (entity != null) {
						entity.consumeContent();
					}
				} catch (Exception e) {
				}
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
