package mil.nga.giat.mage.sdk.push;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.common.Geometry;
import mil.nga.giat.mage.sdk.datastore.common.PointGeometry;
import mil.nga.giat.mage.sdk.datastore.common.State;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationGeometry;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationProperty;
import mil.nga.giat.mage.sdk.gson.serializer.ObservationSerializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.DateUtility;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

import com.google.gson.Gson;

public class ObservationServerPushAsyncTask extends ServerPushAsyncTask {

	public ObservationServerPushAsyncTask(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Boolean doInBackground(Object... params) {

		
		Observation obs = createFakeObservation();
		
		ObservationHelper observationHelper = ObservationHelper.getInstance(mContext);
		List<Observation> observations = observationHelper.getDirty();
		
		
		try {
			DefaultHttpClient httpClient = HttpClientManager.getInstance(mContext).getHttpClient();
			URL serverURL = new URL(PreferenceHelper.getInstance(mContext).getValue(R.string.serverURLKey));			
			HttpPost request = new HttpPost(new URL(serverURL +  "/FeatureServer/3/features").toURI());
		
			Gson gson = ObservationSerializer.getGsonBuilder();
			String json = gson.toJson(obs);
			
			
			StringEntity payload = new StringEntity(json);
	        request.addHeader("Content-Type", "application/json; charset=utf-8");
	        request.setEntity(payload);
	        HttpResponse response = httpClient.execute(request);
		
	        System.out.println(response);
		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
		
		return null;
	}

	//TODO: Remove this
	private Observation createFakeObservation() {				
		
		try {
			Observation obs = new Observation();

			//Collection<Attachment> attachments = new ArrayList<Attachment>();
			// attachment = new Attachment("contentType", 12345L,
			//		"attachment1", "./localPath", "./remotePath");
			//attachment.setObservation(obs);
			//attachment.setUrl("http://www.cnn.com");
			//attachments.add(attachment);

			Collection<ObservationProperty> properties = new ArrayList<ObservationProperty>();
			ObservationProperty property1 = new ObservationProperty();
			property1.setKey("EVENTDATE");
			property1.setValue(DateUtility.getISO8601().format(new Date()));
			property1.setObservation(obs);
			properties.add(property1);

			//obs.setAttachments(attachments);
			obs.setProperties(properties);

			obs.setDirty(Boolean.TRUE);
			obs.setLastModified(new Date());

			Geometry geometry = new PointGeometry(1.0, 0.0);
			obs.setObservationGeometry(new ObservationGeometry(geometry));

			obs.setRemoteId("r12345" + Math.random()*100000L);
			obs.setState(State.ACTIVE);

			ObservationHelper observationHelper = ObservationHelper
					.getInstance(mContext);
			return observationHelper.create(obs);

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	
}
