package mil.nga.giat.mage.sdk.http.get;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.layer.Layer;
import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeature;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.gson.deserializer.LayerDeserializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.jackson.deserializer.LocationDeserializer;
import mil.nga.giat.mage.sdk.jackson.deserializer.ObservationDeserializer;
import mil.nga.giat.mage.sdk.jackson.deserializer.StaticFeatureDeserializer;
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

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

/**
 * A class that contains common GET requests to the MAGE server.
 * 
 * @author travis
 * 
 */
public class MageServerGetRequests {

    private static final String LOG_NAME = MageServerGetRequests.class.getName();
    private static ObservationDeserializer observationDeserializer = new ObservationDeserializer();
    private static StaticFeatureDeserializer featureDeserializer = new StaticFeatureDeserializer();
    private static LocationDeserializer locationDeserializer = new LocationDeserializer();

    private static List<Layer> layers = new ArrayList<Layer>();
    
    /**
     * Gets layers from the server.
     * 
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static List<Layer> getFeatureLayers(Context context) {
    	if (!layers.isEmpty()) {
    		return layers;
    	}
        final Gson layerDeserializer = LayerDeserializer.getGsonBuilder();
        DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
        HttpEntity entity = null;
        try {
            Uri uri = Uri.parse(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey)).buildUpon()
                .appendPath("api")
                .appendPath("layers")
                .appendQueryParameter("type", "Feature")
                .build();

            HttpGet get = new HttpGet(uri.toString());
            HttpResponse response = httpclient.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();
                JSONArray featureArray = new JSONArray(EntityUtils.toString(entity));
                for (int i = 0; i < featureArray.length(); i++) {
                    JSONObject feature = featureArray.getJSONObject(i);
                    if (feature != null) {
                        layers.add(layerDeserializer.fromJson(feature.toString(), Layer.class));
                    }
                }
			} else {
				entity = response.getEntity();
				String error = EntityUtils.toString(entity);
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
			}
        } catch (Exception e) {
            // this block should never flow exceptions up! Log for now.
            Log.e(LOG_NAME, "Failure parsing layer information.", e);
        } finally {
            try {
                if (entity != null) {
                    entity.consumeContent();
                }
            } catch (Exception e) {
                Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
            }
        }
        return layers;
    }

    public static List<Layer> getStaticLayers(Context context) {
        final Gson layerDeserializer = LayerDeserializer.getGsonBuilder();
        List<Layer> layers = new ArrayList<Layer>();
        DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
        HttpEntity entity = null;
        try {
            
            Uri uri = Uri.parse(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey)).buildUpon()
                .appendPath("api")
                .appendPath("layers")
                .appendQueryParameter("type", "External")
                .build();

            HttpGet get = new HttpGet(uri.toString());
            HttpResponse response = httpclient.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();
                JSONArray featureArray = new JSONArray(EntityUtils.toString(entity));
                for (int i = 0; i < featureArray.length(); i++) {
                    JSONObject feature = featureArray.getJSONObject(i);
                    if (feature != null) {
                        layers.add(layerDeserializer.fromJson(feature.toString(), Layer.class));
                    }
                }
			} else {
				entity = response.getEntity();
				String error = EntityUtils.toString(entity);
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
			}
        } catch (Exception e) {
            // this block should never flow exceptions up! Log for now.
            Log.e(LOG_NAME, "Failure parsing layer information.", e);
        } finally {
            try {
                if (entity != null) {
                    entity.consumeContent();
                }
            } catch (Exception e) {
                Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
            }
        }
        return layers;
    }

    /**
     * Makes a GET request to the MAGE server for the Field Observation Layer
     * Id.
     * 
     * @param context
     * @return
     */
    public static String getFieldObservationLayerId(Context context) {
        String fieldObservationLayerId = null;
        List<Layer> layers = MageServerGetRequests.getFeatureLayers(context);
        for (Layer layer : layers) {
            fieldObservationLayerId = layer.getRemoteId();
        }
        
        return fieldObservationLayerId;
    }

    public static Collection<StaticFeature> getStaticFeatures(Context context, Layer layer) {
        long start = 0;

        Collection<StaticFeature> staticFeatures = new ArrayList<StaticFeature>();
        HttpEntity entity = null;
        try {
            URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));

            URL staticFeatureURL = new URL(serverURL, "/FeatureServer/" + layer.getRemoteId() + "/features");
            DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
            Log.d(LOG_NAME, staticFeatureURL.toString());
            HttpGet get = new HttpGet(staticFeatureURL.toURI());
            HttpResponse response = httpclient.execute(get);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();
                start = System.currentTimeMillis();
                staticFeatures = featureDeserializer.parseStaticFeatures(entity.getContent(), layer);
            } else {
                String error = EntityUtils.toString(response.getEntity());
                Log.e(LOG_NAME, "Bad request.");
                Log.e(LOG_NAME, error);
            }
        } catch (Exception e) {
            // this block should never flow exceptions up! Log for now.
            Log.e(LOG_NAME, "There was a failure while retriving static features.", e);
        } finally {
            try {
                if (entity != null) {
                    entity.consumeContent();
                }
            } catch (Exception e) {
                Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
            }
        }
        
        long stop = System.currentTimeMillis();
        
        if(staticFeatures.size() > 0) {
        	Log.d(LOG_NAME, "Took " + (stop - start) + " millis to deserialize " + staticFeatures.size() + " static features.");
        }
        return staticFeatures;
    }

    /**
     * Returns the observations from the server. Uses a date as in filter in the
     * request.
     * 
     * @param context
     * @return
     */
    public static List<Observation> getObservations(Context context) {  
        long start = 0;
        
        List<Observation> observations = new ArrayList<Observation>();
        String fieldObservationLayerId = MageServerGetRequests.getFieldObservationLayerId(context);
        HttpEntity entity = null;
        try {
            URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));

            ObservationHelper observationHelper = ObservationHelper.getInstance(context);

            Date lastModifiedDate = observationHelper.getLatestCleanLastModified();

            URL observationURL = new URL(serverURL, "/FeatureServer/" + fieldObservationLayerId + "/features");
            Uri.Builder uriBuilder = Uri.parse(observationURL.toURI().toString()).buildUpon();
            uriBuilder.appendQueryParameter("startDate", DateUtility.getISO8601().format(lastModifiedDate));

            DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
            Log.d(LOG_NAME, "Fetching all observations after: " + DateUtility.getISO8601().format(lastModifiedDate));
            Log.d(LOG_NAME, uriBuilder.build().toString());
            HttpGet get = new HttpGet(new URI(uriBuilder.build().toString()));
            HttpResponse response = httpclient.execute(get);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				start = System.currentTimeMillis();
				observations = observationDeserializer.parseObservations(entity.getContent());
			} else {
				entity = response.getEntity();
				String error = EntityUtils.toString(entity);
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
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
        long stop = System.currentTimeMillis();
        
        if(observations.size() > 0) {
        	Log.d(LOG_NAME, "Took " + (stop - start) + " millis to deserialize " + observations.size() + " observations.");
        }
        
        return observations;
    }

	public static Collection<Location> getLocations(Context context) {
		Collection<Location> locations = new ArrayList<Location>();
		HttpEntity entity = null;
		try {
			URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));
			URL locationURL = new URL(serverURL, "/api/locations");

			DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
			HttpGet get = new HttpGet(locationURL.toURI());
			HttpResponse response = httpclient.execute(get);

			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				entity = response.getEntity();
				locations = locationDeserializer.parseLocations(entity.getContent());
			} else {
				entity = response.getEntity();
				String error = EntityUtils.toString(entity);
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
			}
		} catch (Exception e) {
			Log.e(LOG_NAME, "There was a failure while performing an Location Fetch opperation.", e);
		} finally {
			try {
				if (entity != null) {
					entity.consumeContent();
				}
			} catch (Exception e) {
				Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
			}
		}

		return locations;
	}
}