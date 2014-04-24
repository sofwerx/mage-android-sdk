package mil.nga.giat.mage.sdk.http.post;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
				entity = response.getEntity();
				String error = EntityUtils.toString(entity);
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
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
	// The following code will sometimes fail to post attachments
	public static Attachment postAttachment(Attachment attachment, Context context) {
		DefaultHttpClient httpClient = HttpClientManager.getInstance(context).getHttpClient();
		HttpEntity entity = null;
		try {
			Log.d(LOG_NAME, "Pushing attachment " + attachment.getId() + " to " + attachment.getObservation().getUrl() + "/attachments");
			URL endpoint = new URL(attachment.getObservation().getUrl() + "/attachments");
			
			HttpPost request = new HttpPost(endpoint.toURI());
			String mimeType = MediaUtility.getMimeType(attachment.getLocalPath());
			
			Log.d(LOG_NAME, "Mime type is: " + mimeType);

			FileBody fileBody = new FileBody(new File(attachment.getLocalPath()));
			FormBodyPart fbp = new FormBodyPart("attachment", fileBody);
			fbp.addField("Content-Type", mimeType);

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart(fbp);

			request.setEntity(reqEntity);
			
			Log.d(LOG_NAME, "Sending request " + request);
			HttpResponse response = httpClient.execute(request);
			entity = response.getEntity();
			Log.d(LOG_NAME, "Got the entity back " + entity);
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
	
//	public static Attachment postAttachment(Attachment attachment, Context context) {
//		try { 
//			Log.d(LOG_NAME, "Pushing attachment " + attachment.getId() + " to " + attachment.getObservation().getUrl() + "/attachments");
//			URL url = new URL(attachment.getObservation().getUrl() + "/attachments");
//            // open a URL connection to the Servlet
//          FileInputStream fileInputStream = new FileInputStream(new File(attachment.getLocalPath()));
//          String fileName = new File(attachment.getLocalPath()).getName();
//          HttpURLConnection conn = null;
//          DataOutputStream dos = null;
//          int bytesRead, bytesAvailable, bufferSize;
//          byte[] buffer;
//          int maxBufferSize = 1 * 1024 * 1024; 
//          
//          String lineEnd = "\r\n";
//          String twoHyphens = "--";
//          String boundary = "*****";
//          
//          String mimeType = MediaUtility.getMimeType(attachment.getLocalPath());
//           
//          // Open a HTTP  connection to  the URL
//          conn = (HttpURLConnection) url.openConnection(); 
//          conn.setDoInput(true); // Allow Inputs
//          conn.setDoOutput(true); // Allow Outputs
//          conn.setUseCaches(false); // Don't use a Cached Copy
//          conn.setRequestMethod("POST");
//          conn.setRequestProperty("Connection", "Keep-Alive");
//          conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//          conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//          conn.setRequestProperty("uploaded_file", fileName); 
//          String token = PreferenceHelper.getInstance(context).getValue(R.string.tokenKey);
//			if (token != null && !token.trim().isEmpty()) {
//				conn.setRequestProperty("Authorization", "Bearer " + token);
//			}
//           
//          dos = new DataOutputStream(conn.getOutputStream());
// 
//          dos.writeBytes(twoHyphens + boundary + lineEnd); 
//          dos.writeBytes("Content-Disposition: form-data;name=\"attachment\";filename=\""
//                                    + fileName + "\"" + lineEnd + "Content-Type: "+mimeType+ lineEnd);
//           
//          dos.writeBytes(lineEnd);
// 
//          // create a buffer of  maximum size
//          bytesAvailable = fileInputStream.available(); 
// 
//          bufferSize = Math.min(bytesAvailable, maxBufferSize);
//          buffer = new byte[bufferSize];
// 
//          // read file and write it into form...
//          bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
//             
//          while (bytesRead > 0) {
//               
//            dos.write(buffer, 0, bufferSize);
//            bytesAvailable = fileInputStream.available();
//            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
//             
//           }
// 
//          // send multipart form data necesssary after file data...
//          dos.writeBytes(lineEnd);
//          dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
// 
//          // Responses from the server (code and message)
//          int serverResponseCode = conn.getResponseCode();
//          String serverResponseMessage = conn.getResponseMessage();
//            
//          Log.i("uploadFile", "HTTP Response is : "
//                  + serverResponseMessage + ": " + serverResponseCode);
//           
//          if(serverResponseCode == 200){
//               
//              Log.d(LOG_NAME, "Uploaded correctly");
//              
//              BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
//              
//              Attachment a = AttachmentDeserializer.getGsonBuilder().fromJson(reader, Attachment.class);
//				attachment.setContentType(a.getContentType());
//				attachment.setName(a.getName());
//				attachment.setRemoteId(a.getRemoteId());
//				attachment.setRemotePath(a.getRemotePath());
//				attachment.setSize(a.getSize());
//				attachment.setUrl(a.getUrl());
//				attachment.setDirty(false);
//
//				// TODO go save this attachment again
//				DaoStore.getInstance(context).getAttachmentDao().update(attachment);
//          }    
//           
//          //close the streams //
//          fileInputStream.close();
//          dos.flush();
//          dos.close();
//            
//     } catch (MalformedURLException ex) {
//          
//          
//         Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  
//     } catch (Exception e) {
//          
//       
//         Log.e("Upload file to server Exception", "Exception : "
//                                          + e.getMessage(), e);  
//     }
//		return attachment;
//	}

	public static Location postLocation(Location location, Context context) {

		Location savedLocation = location;
		HttpEntity entity = null;
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
				entity = response.getEntity();
				// we've sync'ed. Don't need the location anymore.
				LocationHelper.getInstance(context).delete(location.getId());
			} else {
				entity = response.getEntity();
				String error = EntityUtils.toString(entity);
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
			}
		} catch (Exception e) {
			Log.e(LOG_NAME, "Failure posting location.", e);
		} finally {
			try {
	            if (entity != null) {
	                entity.consumeContent();
	            }
	        } catch (Exception e) {
	            Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
	        }
		}
		return savedLocation;
	}

}
