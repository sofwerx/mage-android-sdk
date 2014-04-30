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

import com.google.gson.Gson;

import android.content.Context;
import android.util.Log;

public class UserServerFetch extends AbstractServerFetch {

	public UserServerFetch(Context context) {
		super(context);
	}

	private static final String LOG_NAME = UserServerFetch.class.getName();
	
	// TODO : should the context be a member variable?
	public void fetch(String... userids) throws Exception {

		// TODO : account for deserialization when no userids are given!
		URL serverURL = new URL(PreferenceHelper.getInstance(mContext).getValue(R.string.serverURLKey));

		HttpEntity entity = null;
		try {
			final Gson userDeserializer = UserDeserializer.getGsonBuilder(mContext);
			DefaultHttpClient httpclient = HttpClientManager.getInstance(mContext).getHttpClient();
			UserHelper userHelper = UserHelper.getInstance(mContext);

			// loop over all the ids
			for (String userId : userids) {
				String userPath = "api/users";
				userPath += "/" + userId;
				boolean isCurrentUser = false;
				// is this a request for the current user?
				if (userId.equalsIgnoreCase("myself")) {
					isCurrentUser = true;
				} else {
					try {
						User u = userHelper.readCurrentUser();
						if(u != null) {
							String rid = u.getRemoteId();
							if (rid != null && rid.equalsIgnoreCase(userId)) {
								isCurrentUser = true;
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
							User oldUser = userHelper.read(user.getRemoteId());
							if (oldUser == null) {
								user.setCurrentUser(isCurrentUser);
								user.setFetchedDate(new Date());
								user = userHelper.create(user);
								Log.d(LOG_NAME, "created user with remote_id " + user.getRemoteId());
							} else {
								// perform update?
								user.setId(oldUser.getId());
								user.setCurrentUser(isCurrentUser);
								user.setFetchedDate(new Date());
								userHelper.update(user);
								Log.d(LOG_NAME, "updated user with remote_id " + user.getRemoteId());
							}
						}
					}
				} else {
					entity = response.getEntity();
					String error = EntityUtils.toString(entity);
					Log.e(LOG_NAME, "Bad request.");
					Log.e(LOG_NAME, error);
				}
			}
		} finally {
			try {
				if (entity != null) {
					entity.consumeContent();
				}
			} catch (Exception e) {
			}
		}
	}

}
