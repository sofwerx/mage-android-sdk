package mil.nga.giat.mage.sdk.fetch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.exceptions.ObservationException;
import mil.nga.giat.mage.sdk.gson.deserializer.ObservationDeserializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import android.content.Context;
import android.util.Log;

/**
 * FIXME: Prototype for a procedure that might pull observations from the
 * server.
 * 
 * @author wiedemannse
 * 
 */
public class ObservationServerFetchAsyncTask extends ServerFetchAsyncTask {

	private static final String LOG_NAME = ObservationServerFetchAsyncTask.class.getName();
	
	private static final Gson observationDeserializer = ObservationDeserializer.getGson();
	
	public ObservationServerFetchAsyncTask(Context context) {
		super(context);
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

		while (Status.RUNNING.equals(getStatus())) {
			Long frequency = PreferenceHelper.getInstance(mContext).getValue(R.string.observationFetchFrequencyKey, Long.class, R.string.observationFetchFrequencyDefaultValue);

			
			
			
			try {
				URL serverURL = new URL(PreferenceHelper.getInstance(mContext).getValue(R.string.serverURLKey));

				HttpGet get = new HttpGet(new URL(serverURL, "/FeatureServer/" + fieldObservationLayerId + "/features").toURI());
				HttpResponse response = httpclient.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
					// FIXME : use jackson??? to transform this JSON into
					// observations!
					
					JSONArray features = json.getJSONArray("features");
					for(int i = 0; i < features.length(); i++) {
						JSONObject feature = (JSONObject)features.get(i);
						Observation observation = observationDeserializer.fromJson(feature.toString(), Observation.class);						
						
						//if the Observation does NOT currently exist in the local datastore
						if(!observationHelper.observationExists(observation.getRemote_id())) {
							observation = ObservationHelper.getInstance(mContext).createObservation(observation);
							Log.d(LOG_NAME, "created observation with remote_id " + observation.getRemote_id());
						}
						//the Observation DOES exist...
						else {
							Log.d(LOG_NAME, "observation with remote_id " + observation.getRemote_id() + " already exists!");
							//TODO: perform an update?
						}
																							
					}
					
					
					
					
					Log.d(LOG_NAME, json.toString());
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
			
			try {
				Log.d(LOG_NAME, "Observation fetch sleeping for " + frequency + "ms.");
				Thread.sleep(frequency);
			} catch (InterruptedException ie) {
				Log.w("Interupted.  Unable to sleep " + frequency, ie);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
			}
		}
		return null;
	}

}
