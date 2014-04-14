package mil.nga.giat.mage.sdk.http.post;

import java.io.File;
import java.net.URI;
import java.net.URL;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.DaoStore;
import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.datastore.location.LocationHelper;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.gson.deserializer.AttachmentDeserializer;
import mil.nga.giat.mage.sdk.gson.deserializer.ObservationDeserializer;
import mil.nga.giat.mage.sdk.gson.serializer.LocationSerializer;
import mil.nga.giat.mage.sdk.gson.serializer.ObservationSerializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.MediaUtility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
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
	
	/**
	 * POST an Observation to the server.
	 * @param observation The Observation to post.
	 * @param context
	 */
	public static Observation postObservation(Observation observation, Context context) {
		
		ObservationHelper observationHelper = ObservationHelper.getInstance(context);		
		Observation savedObservation = observation;
		
		try {
			String fieldObservationLayerId = MageServerGetRequests.getFieldObservationLayerId(context);
			
			URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));
			URI endpointUri = new URL(serverURL +  "/FeatureServer/" + fieldObservationLayerId + "/features").toURI();
			
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
				savedObservation = ObservationDeserializer.getGsonBuilder().fromJson(savedObservationJson, Observation.class);
				savedObservation.setId(observation.getId());
				savedObservation.setAttachments(observation.getAttachments());
				savedObservation.setDirty(Boolean.FALSE);
	        	
	        	//TODO: clean this up.  Need an update in the Helper?
	        	observationHelper.delete(observation.getId());
	        	observation.setDirty(Boolean.FALSE);
	        	observationHelper.create(savedObservation);	        	
	        	
	        }
	        else {
	        	Log.e(LOG_NAME, "Bad request made to MAGE server.");
	        }		        		        		        
							
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return savedObservation;		
	}	
	
	/**
	 * POST an attachment to the server.
	 * @param attachment The attachment to post.
	 * @param context
	 */
	public static void postAttachment(Attachment attachment, Context context) {
		Observation o = attachment.getObservation();
		DefaultHttpClient httpClient = HttpClientManager.getInstance(context).getHttpClient();	
		try {
			URI endpointUri = new URL(o.getUrl() + "/attachments").toURI();	
			HttpPost request = new HttpPost(endpointUri);
			String mimeType = MediaUtility.getMimeType(attachment.getLocalPath());

			FileBody fileBody = new FileBody(new File(attachment.getLocalPath()));
			FormBodyPart fbp = new FormBodyPart("attachment", fileBody);
			fbp.addField("Content-Type", mimeType);

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart(fbp);

			request.setEntity(reqEntity);

			HttpResponse response = httpClient.execute(request);

			HttpEntity resEntity = response.getEntity();

			if (resEntity != null) {
				String json = EntityUtils.toString(resEntity);
				Attachment a = AttachmentDeserializer.getGsonBuilder().fromJson(json, Attachment.class);
				attachment.setContentType(a.getContentType());
				attachment.setName(a.getName());
				attachment.setRemoteId(a.getRemoteId());
				attachment.setRemotePath(a.getRemotePath());
				attachment.setSize(a.getSize());
				attachment.setUrl(a.getUrl());
				
				// TODO go save this attachment again
				DaoStore.getInstance(context).getAttachmentDao().update(attachment);
			}

		} 
		catch (Exception e) {
			Log.e("Attachment", "Error posting attachment " + attachment.getLocalPath(), e);
		}
	}
	
	public static Location postLocation(Location location, Context context) {
		
		Location savedLocation = location;
		
		try {			
			URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));
			URI endpointUri = new URL(serverURL + "/api/locations").toURI();
			
			DefaultHttpClient httpClient = HttpClientManager.getInstance(context).getHttpClient();
			HttpPost request = new HttpPost(endpointUri);
			
			Gson gson = LocationSerializer.getGsonBuilder(context);
			String json = gson.toJson(location);
			StringEntity payload = new StringEntity(json);
	        
			request.addHeader("Content-Type", "application/json; charset=utf-8");
	        request.setEntity(payload);
	        
	        HttpResponse response = httpClient.execute(request);
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	        	//we've sync'ed.  Don't need the location anymore.
				LocationHelper.getInstance(context).delete(location.getId());	        	 
	        }
	        else {
	        	String locationError = EntityUtils.toString(response.getEntity());
	        	Log.e(LOG_NAME, "Bad request made to MAGE server.");
	        	Log.e(LOG_NAME, locationError);
	        }		        		        		        
							
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return savedLocation;
	}
	
}
