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
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
 * 
 * @author travis
 * 
 */
public class MageServerPostRequests {

	private static final String LOG_NAME = MageServerPostRequests.class.getName();

	/**
	 * POST an {@link Observation} to the server.
	 * 
	 * @param observation
	 *            The Observation to post.
	 * @param context
	 */
	public static Observation postObservation(Observation observation, Context context) {

		ObservationHelper observationHelper = ObservationHelper.getInstance(context);
		Observation savedObservation = null;

		HttpEntity entity = null;
		HttpEntityEnclosingRequestBase request = null;
		try {
			String fieldObservationLayerId = MageServerGetRequests.getFieldObservationLayerId(context);
			DefaultHttpClient httpClient = HttpClientManager.getInstance(context).getHttpClient();

			URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));
			URI endpointUri = null;

			if (observation.getRemoteId() == null || observation.getRemoteId().trim().isEmpty()) {
				endpointUri = new URL(serverURL + "/FeatureServer/" + fieldObservationLayerId + "/features").toURI();
				request = new HttpPost(endpointUri);
			} else {
				endpointUri = new URL(serverURL + "/FeatureServer/" + fieldObservationLayerId + "/features/" + observation.getRemoteId()).toURI();
				request = new HttpPut(endpointUri);
			}
			request.addHeader("Content-Type", "application/json; charset=utf-8");
			Gson gson = ObservationSerializer.getGsonBuilder(context);
			request.setEntity(new StringEntity(gson.toJson(observation)));

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				Observation returnedObservation = ObservationDeserializer.getGsonBuilder().fromJson(EntityUtils.toString(entity), Observation.class);
				// not sure if this should be added back.
				//returnedObservation.setAttachments(observation.getAttachments());
				returnedObservation.setDirty(Boolean.FALSE);
				savedObservation = observationHelper.update(returnedObservation, observation);
			} else {
				Log.e(LOG_NAME, "Bad request made to MAGE server.");
			}

		} catch (Exception e) {
			Log.e(LOG_NAME, "Failure pushing observation.", e);
		} finally {
			try {
				if (entity != null) {
					entity.consumeContent();
				}
			} catch (Exception e) {
			}
		}
		return savedObservation;
	}

	/**
	 * POST an {@link Attachment} to the server.
	 * 
	 * @param attachment
	 *            The attachment to post.
	 * @param context
	 */
	public static Attachment postAttachment(Attachment attachment, Context context) {
		DefaultHttpClient httpClient = HttpClientManager.getInstance(context).getHttpClient();
		HttpEntity entity = null;
		try {	
			URL endpoint = new URL(attachment.getObservation().getUrl() + "/attachments");
			
			HttpPost request = new HttpPost(endpoint.toURI());
			String mimeType = MediaUtility.getMimeType(attachment.getLocalPath());

			FileBody fileBody = new FileBody(new File(attachment.getLocalPath()));
			FormBodyPart fbp = new FormBodyPart("attachment", fileBody);
			fbp.addField("Content-Type", mimeType);

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart(fbp);

			request.setEntity(reqEntity);

			HttpResponse response = httpClient.execute(request);
			entity = response.getEntity();
			if (entity != null) {
				Attachment a = AttachmentDeserializer.getGsonBuilder().fromJson(EntityUtils.toString(entity), Attachment.class);
				attachment.setContentType(a.getContentType());
				attachment.setName(a.getName());
				attachment.setRemoteId(a.getRemoteId());
				attachment.setRemotePath(a.getRemotePath());
				attachment.setSize(a.getSize());
				attachment.setUrl(a.getUrl());
				attachment.setDirty(false);

				// TODO go save this attachment again
				DaoStore.getInstance(context).getAttachmentDao().update(attachment);
			}

		} catch (Exception e) {
			Log.e(LOG_NAME, "Failure pushing attachment: " + attachment.getLocalPath(), e);
		} finally {
			try {
				if (entity != null) {
					entity.consumeContent();
				}
			} catch (Exception e) {
			}
		}
		return attachment;
	}

	public static Location postLocation(Location location, Context context) {

		Location savedLocation = location;
		try {
			URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));
			URI endpointUri = new URL(serverURL + "/api/locations").toURI();

			DefaultHttpClient httpClient = HttpClientManager.getInstance(context).getHttpClient();
			HttpPost request = new HttpPost(endpointUri);
			request.addHeader("Content-Type", "application/json; charset=utf-8");
			Gson gson = LocationSerializer.getGsonBuilder(context);
			request.setEntity(new StringEntity(gson.toJson(location)));

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// we've sync'ed. Don't need the location anymore.
				LocationHelper.getInstance(context).delete(location.getId());
			} else {
				String locationError = EntityUtils.toString(response.getEntity());
				Log.e(LOG_NAME, "Bad request made to MAGE server.");
				Log.e(LOG_NAME, locationError);
			}

		} catch (Exception e) {
			Log.e(LOG_NAME, "Failure posting location.", e);
		}
		return savedLocation;
	}

}
