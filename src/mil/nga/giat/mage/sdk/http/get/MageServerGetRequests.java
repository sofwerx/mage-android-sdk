package mil.nga.giat.mage.sdk.http.get;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.gson.deserializer.ObservationDeserializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.DateUtility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

/**
 * A class that contains common GET requests to the MAGE server.
 * 
 * @author travis
 * 
 */
public class MageServerGetRequests {

	private static final String LOG_NAME = MageServerGetRequests.class.getName();

	/**
	 * Makes a GET request to the MAGE server for the Field Observation Layer
	 * Id.
	 * 
	 * @param context
	 * @return
	 */
	public static int getFieldObservationLayerId(Context context) {

		int fieldObservationLayerId = 0;
		String fieldObservationLayerName = "Field Observations";
		DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
		HttpEntity entity = null;
		try {
			URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));

			HttpGet get = new HttpGet(new URL(serverURL, "api/layers").toURI());
			HttpResponse response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				JSONArray json = new JSONArray(EntityUtils.toString(entity));
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
			Log.e(LOG_NAME, "There was a failure while getting the " + fieldObservationLayerName + "layer.", e);
		} finally {
			try {
				if (entity != null) {
					entity.consumeContent();
				}
			} catch (Exception e) {
				Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
			}
		}
		return fieldObservationLayerId;
	}

	/**
	 * Returns the observations from the server. Uses a date as in filter in the
	 * request.
	 * 
	 * @param context
	 * @return
	 */
	public static Collection<Observation> getObservations(Context context) {
		Collection<Observation> observations = new ArrayList<Observation>();
		int fieldObservationLayerId = MageServerGetRequests.getFieldObservationLayerId(context);
		HttpEntity entity = null;
		try {
			URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));

			ObservationHelper observationHelper = ObservationHelper.getInstance(context);

			// TODO : should we add one millisecond to this?
			Date lastModifiedDate = observationHelper.getLatestRemoteLastModified();

			final Gson observationDeserializer = ObservationDeserializer.getGsonBuilder();

			URL observationURL = new URL(serverURL, "/FeatureServer/" + fieldObservationLayerId + "/features");
			Uri.Builder uriBuilder = Uri.parse(observationURL.toURI().toString()).buildUpon();
			uriBuilder.appendQueryParameter("startDate", DateUtility.getISO8601().format(lastModifiedDate));

			DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
			Log.d(LOG_NAME, uriBuilder.build().toString());
			HttpGet get = new HttpGet(new URI(uriBuilder.build().toString()));
			HttpResponse response = httpclient.execute(get);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				JSONObject json = new JSONObject(EntityUtils.toString(entity));

				if (json != null && json.has("features")) {
					JSONArray features = json.getJSONArray("features");
					for (int i = 0; i < features.length(); i++) {
						JSONObject feature = (JSONObject) features.get(i);
						if (feature != null) {
							observations.add(observationDeserializer.fromJson(feature.toString(), Observation.class));
						}
					}
				}
			}
		} catch (Exception e) {
			// this block should never flow exceptions up! Log for now.
			Log.e(LOG_NAME, "There was a failure while performing an Observation Fetch opperation.", e);
		} finally {
			try {
				if (entity != null) {
					entity.consumeContent();
				}
			} catch (Exception e) {
				Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
			}
		}
		return observations;
	}

}
