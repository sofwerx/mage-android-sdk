package mil.nga.giat.mage.sdk.http.get;

import java.net.URL;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

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

}
