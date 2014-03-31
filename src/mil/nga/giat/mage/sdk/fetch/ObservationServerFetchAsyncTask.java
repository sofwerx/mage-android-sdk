package mil.nga.giat.mage.sdk.fetch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.exceptions.ObservationException;
import mil.nga.giat.mage.sdk.gson.deserializer.ObservationDeserializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.DateUtility;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
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

	protected AtomicBoolean preferenceSemaphore = new AtomicBoolean(false);

	protected final synchronized long getobservationFetchFrequency() {
		return PreferenceHelper.getInstance(mContext).getValue(R.string.observationFetchFrequencyKey, Long.class, R.string.observationFetchFrequencyDefaultValue);
	}

	@Override
	protected Void doInBackground(Void... params) {

		ObservationHelper observationHelper = ObservationHelper.getInstance(mContext);

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
		} catch (MalformedURLException mue) {
			// TODO Auto-generated catch block
			mue.printStackTrace();
		} catch (URISyntaxException use) {
			// TODO Auto-generated catch block
			use.printStackTrace();
		} catch (ClientProtocolException cpe) {
			// TODO Auto-generated catch block
			cpe.printStackTrace();
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		} catch (ParseException pe) {
			// TODO Auto-generated catch block
			pe.printStackTrace();
		} catch (JSONException je) {
			// TODO Auto-generated catch block
			je.printStackTrace();
		}

		final Gson observationDeserializer = ObservationDeserializer.getGsonBuilder();

		while (Status.RUNNING.equals(getStatus())) {
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
					JSONArray features = json.getJSONArray("features");
					if (features != null) {
						for (int i = 0; i < features.length(); i++) {
							JSONObject feature = (JSONObject) features.get(i);
							if (feature != null) {
								Observation observation = observationDeserializer.fromJson(feature.toString(), Observation.class);

								if (observation != null) {
									if (!observationHelper.observationExists(observation.getRemoteId())) {
										observation = observationHelper.createObservation(observation);
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
			} catch (MalformedURLException mue) {
				// TODO Auto-generated catch block
				mue.printStackTrace();
			} catch (URISyntaxException use) {
				// TODO Auto-generated catch block
				use.printStackTrace();
			} catch (ClientProtocolException cpe) {
				// TODO Auto-generated catch block
				cpe.printStackTrace();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			} catch (ParseException pe) {
				// TODO Auto-generated catch block
				pe.printStackTrace();
			} catch (JSONException je) {
				// TODO Auto-generated catch block
				je.printStackTrace();
			} catch (ObservationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long frequency = getobservationFetchFrequency();
			long lastFetchTime = new Date().getTime();
			long currentTime = new Date().getTime();
			try {
				while (lastFetchTime + (frequency = getobservationFetchFrequency()) > (currentTime = new Date().getTime())) {
					synchronized (preferenceSemaphore) {
						Log.d(LOG_NAME, "Observation fetch sleeping for " + (lastFetchTime + frequency - currentTime) + "ms.");
						preferenceSemaphore.wait(lastFetchTime + frequency - currentTime);
					}
				}
			} catch (InterruptedException ie) {
				Log.w("Interupted.  Unable to sleep " + frequency, ie);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
			}
		}
		return null;
	}

	/**
	 * Will alert the fetching thread that changes have been made
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equalsIgnoreCase(mContext.getString(R.string.observationFetchFrequencyKey))) {
			synchronized (preferenceSemaphore) {
				preferenceSemaphore.notifyAll();
			}
		}
	}
}
