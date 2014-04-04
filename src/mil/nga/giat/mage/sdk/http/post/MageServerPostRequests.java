package mil.nga.giat.mage.sdk.http.post;

import java.net.URI;
import java.net.URL;
import java.util.Iterator;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.gson.deserializer.AttachmentDeserializer;
import mil.nga.giat.mage.sdk.gson.deserializer.ObservationDeserializer;
import mil.nga.giat.mage.sdk.gson.serializer.ObservationSerializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

/**
 * A class that contains common POST requests to the MAGE server.
 * @author travis
 *
 */
public class MageServerPostRequests {
	
	private static final String LOG_NAME = MageServerPostRequests.class.getName();
	
	public static Boolean postObservation(Observation observation, Context context) {
		
		Boolean success = Boolean.FALSE;
		ObservationHelper observationHelper = ObservationHelper.getInstance(context);
		
		try {
			Integer layerNumber = MageServerGetRequests.getFieldObservationLayerId(context);
			
			URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));
			URI endpointUri = new URL(serverURL +  "/FeatureServer/" + layerNumber + "/features").toURI();
			
			DefaultHttpClient httpClient = HttpClientManager.getInstance(context).getHttpClient();
			HttpPost request = new HttpPost(endpointUri);
			
			Gson gson = ObservationSerializer.getGsonBuilder(context);
			String json = gson.toJson(observation);
			StringEntity payload = new StringEntity(json);
	        
			request.addHeader("Content-Type", "application/json; charset=utf-8");
	        request.setEntity(payload);
	        
	        HttpResponse response = httpClient.execute(request);
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	        	
	        	String savedObservationJson = EntityUtils.toString(response.getEntity());
				Observation savedObservation = ObservationDeserializer.getGsonBuilder().fromJson(savedObservationJson, Observation.class);
				savedObservation.setId(observation.getId());
				savedObservation.setAttachments(observation.getAttachments());
				savedObservation.setDirty(Boolean.FALSE);
	        	
	        	//TODO: clean this up.  Need an update in the Helper?
	        	observationHelper.delete(observation.getId());
	        	observation.setDirty(Boolean.FALSE);
	        	observationHelper.create(savedObservation);
	        	success = Boolean.TRUE;
	        	
	        	// TODO: move this to a task
	        	Iterator<Attachment> i = savedObservation.getAttachments().iterator();
	        	while (i.hasNext()) {
	        		Attachment a = i.next();
	        		a.saveToServer(context);
	        	}
	        }
	        else {
	        	Log.e(LOG_NAME, "Bad request made to MAGE server.");
	        }		        		        		        
							
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return success;
	}
	
}
