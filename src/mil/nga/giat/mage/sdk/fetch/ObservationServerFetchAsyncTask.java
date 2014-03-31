package mil.nga.giat.mage.sdk.fetch;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.gson.deserializer.ObservationDeserializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.DateUtility;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

/**
 * A simple procedure thats pull observations from the server.
 * 
 * @author wiedemannse
 * 
 */
public class ObservationServerFetchAsyncTask extends ServerFetchAsyncTask implements OnSharedPreferenceChangeListener {

	private static final String LOG_NAME = ObservationServerFetchAsyncTask.class.getName();

	public ObservationServerFetchAsyncTask(Context context) {
		super(context);
		PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(this);
	}

	protected AtomicBoolean fetchSemaphore = new AtomicBoolean(false);

	protected final synchronized long getobservationFetchFrequency() {
		return PreferenceHelper.getInstance(mContext).getValue(R.string.observationFetchFrequencyKey, Long.class, R.string.observationFetchFrequencyDefaultValue);
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		Boolean status = Boolean.TRUE;

		ObservationHelper observationHelper = ObservationHelper.getInstance(mContext);
		UserHelper userHelper = UserHelper.getInstance(mContext);

		int fieldObservationLayerId = 0;
		String fieldObservationLayerName = "Field Observations";
		// get the correct feature server id
		DefaultHttpClient httpclient = HttpClientManager.getInstance(mContext).getHttpClient();
		try {
			URL serverURL = new URL(PreferenceHelper.getInstance(mContext).getValue(R.string.serverURLKey));

			HttpGet get = new HttpGet(new URL(serverURL, "api/layers").toURI());
			HttpResponse response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONArray json = new JSONArray(EntityUtils.toString(response.getEntity()));
				for (int i = 0; i < json.length(); i++) {
					JSONObject j = json.getJSONObject(i);
					if (j.getString("name").equals(fieldObservationLayerName)) {
						fieldObservationLayerId = j.getInt("id");
						break;
					}
				}
			}
		} catch (Exception e) {
			// this block should never flow exceptions up! Log for now.
			e.printStackTrace();
			Log.e(LOG_NAME, "There was a failure while performing an Observation Fetch opperation.", e);
		}

		final Gson observationDeserializer = ObservationDeserializer.getGsonBuilder();

		while (Status.RUNNING.equals(getStatus())) {

			if (IS_CONNECTED) {

				Log.d(LOG_NAME, "The device is currently connected. Attempting to fetch...");

				try {
					URL serverURL = new URL(PreferenceHelper.getInstance(mContext).getValue(R.string.serverURLKey));

					Date lastModifiedDate = observationHelper.getLatestRemoteLastModified();

					URL observationURL = new URL(serverURL, "/FeatureServer/" + fieldObservationLayerId + "/features");
					Uri.Builder uriBuilder = Uri.parse(observationURL.toURI().toString()).buildUpon();
					uriBuilder.appendQueryParameter("startDate", DateUtility.getISO8601().format(lastModifiedDate));

					Log.d(LOG_NAME, uriBuilder.build().toString());
					HttpGet get = new HttpGet(new URI(uriBuilder.build().toString()));
					HttpResponse response = httpclient.execute(get);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));

						if (json != null && json.has("features")) {
							JSONArray features = json.getJSONArray("features");
							for (int i = 0; i < features.length(); i++) {
								JSONObject feature = (JSONObject) features.get(i);
								if (feature != null) {
									Observation observation = observationDeserializer.fromJson(feature.toString(), Observation.class);

									if (observation != null) {
										if (!observationHelper.observationExists(observation.getRemoteId())) {
											observation = observationHelper.createObservation(observation);

											// TODO: if the observation has a userid that we've not seen before, then request the user info!
											// TODO: the server is going to move the user id to a different section of the json
											String userId = observation.getPropertiesMap().get("userId");
											if (userId != null) {
												User user = userHelper.read(userId);
												final long tenHoursInMillseconds = 10 * 60 * 60 * 1000;
												if (user == null || (new Date(user.getFetchedDate().getTime() + tenHoursInMillseconds)).after(new Date())) {
													// TODO: fetch new user
												}
											}
											Log.d(LOG_NAME, "created observation with remote_id " + observation.getRemoteId());
										} else {
											// TODO: perform an update?
											Log.d(LOG_NAME, "observation with remote_id " + observation.getRemoteId() + " already exists!");
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					// this block should never flow exceptions up! Log for now.
					e.printStackTrace();
					Log.e(LOG_NAME, "There was a failure while performing an Observation Fetch opperation.", e);
				}
			} else {
				Log.d(LOG_NAME, "The device is currently disconnected. Nothing to fetch.");
			}

			long frequency = getobservationFetchFrequency();
			long lastFetchTime = new Date().getTime();
			long currentTime = new Date().getTime();
			try {
				while (lastFetchTime + (frequency = getobservationFetchFrequency()) > (currentTime = new Date().getTime())) {
					synchronized (fetchSemaphore) {
						Log.d(LOG_NAME, "Observation fetch sleeping for " + (lastFetchTime + frequency - currentTime) + "ms.");
						fetchSemaphore.wait(lastFetchTime + frequency - currentTime);
						if(fetchSemaphore.get() == true) {
							break;
						}
					}
				}
				synchronized (fetchSemaphore) {
					fetchSemaphore.set(false);
				}
			} catch (InterruptedException ie) {
				Log.w("Interupted.  Unable to sleep " + frequency, ie);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
				status = Boolean.FALSE;
			} finally {
				IS_CONNECTED = ConnectivityUtility.isOnline(mContext);
			}
		}
		return status;
	}

	/**
	 * Will alert the fetching thread that changes have been made
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equalsIgnoreCase(mContext.getString(R.string.observationFetchFrequencyKey))) {
			synchronized (fetchSemaphore) {
				fetchSemaphore.notifyAll();
			}
		}
	}
	
	@Override
	public void onAnyConnected() {
		super.onAnyConnected();
		synchronized (fetchSemaphore) {
			fetchSemaphore.set(true);
			fetchSemaphore.notifyAll();
		}
	}
}
