package mil.nga.giat.mage.sdk.gson.deserializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import mil.nga.giat.mage.sdk.datastore.common.Geometry;
import mil.nga.giat.mage.sdk.datastore.common.GeometryType;
import mil.nga.giat.mage.sdk.datastore.common.PointGeometry;
import mil.nga.giat.mage.sdk.datastore.common.State;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationGeometry;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationProperty;
import mil.nga.giat.mage.sdk.utils.DateUtility;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * JSON to {@link Observation}
 * 
 * @author wiedemannse
 * 
 */
public class ObservationDeserializer implements JsonDeserializer<Observation> {

	private static final String LOG_NAME = ObservationDeserializer.class.getName();

	/**
	 * Convenience method for returning a Gson object with a registered GSon
	 * TypeAdaptor i.e. custom deserializer.
	 * 
	 * @return A Gson object that can be used to convert Json into an
	 *         {@link Observation} object.
	 */
	public static Gson getGsonBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Observation.class, new ObservationDeserializer());
		return gsonBuilder.create();
	}

	@Override
	public Observation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		JsonObject feature = json.getAsJsonObject();

		Observation observation = new Observation();
		observation.setRemoteId(feature.get("id").getAsString());

		//if the remote id is defined, then assume not dirty for now.
		if(observation.getRemoteId() != null && !"".equals(observation.getRemoteId())) {
			observation.setDirty(Boolean.FALSE);
		}		
		
		try {
			Date d = DateUtility.getISO8601().parse(feature.get("timestamp").getAsString());
			observation.setLastModified(d);
		} catch (ParseException e) {
			Log.e(LOG_NAME, "Problem paring date.");
		}
		
		// TODO: deal with EVENTCLEAR!?!? will this become a state?

		// deserialize state
		JsonObject stateFeature = feature.getAsJsonObject("state");
		if (stateFeature != null) {
			String stateString = stateFeature.get("name").getAsString().toUpperCase(Locale.US);
			try {
				State state = State.valueOf(stateString);
				observation.setState(state);
			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();
			}
		}
		
		observation.setUrl(feature.get("url").getAsString());

		// deserialize geometry
		JsonObject geometryFeature = feature.get("geometry").getAsJsonObject();
		if (geometryFeature != null) {
			GeometryType geometryType = GeometryType.POINT;
			String geometryTypeString = geometryFeature.get("type").getAsString().toUpperCase(Locale.US);
			try {
				geometryType = GeometryType.valueOf(geometryTypeString);
			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();
			}

			Geometry geometry = null;
			switch (geometryType) {
			case POINT:
				String coordinates = geometryFeature.get("coordinates").getAsJsonArray().toString();
				geometry = new PointGeometry(coordinates);
				break;
			default:
				Log.e(LOG_NAME, "Not a valid geomerty");
				return null;
			}
			observation.setObservationGeometry(new ObservationGeometry(geometry));
		}

		// deserialize properties
		JsonObject jsonProperties = feature.get("properties").getAsJsonObject();
		if (jsonProperties != null) {
			Collection<ObservationProperty> properties = new ArrayList<ObservationProperty>();
			Set<Map.Entry<String, JsonElement>> keys = jsonProperties.entrySet();
			for (Map.Entry<String, JsonElement> key : keys) {
				ObservationProperty property = new ObservationProperty();
				property.setKey(key.getKey());
				property.setValue(key.getValue().getAsString());
				property.setObservation(observation);
				properties.add(property);
			}
			observation.setProperties(properties);
		}

		// deserialize attachments
		if (feature.has("attachments")) {
			JsonArray jsonAttachments = feature.get("attachments").getAsJsonArray();
			if (jsonAttachments != null) {
	
				Collection<Attachment> attachments = new ArrayList<Attachment>();
				for (int i = 0; i < jsonAttachments.size(); i++) {
					JsonObject jsonAttachment = jsonAttachments.get(i).getAsJsonObject();
					Attachment attachment = new Attachment();
					attachment.setContentType(jsonAttachment.get("contentType").getAsString());
					attachment.setRemotePath(jsonAttachment.get("relativePath").getAsString());
					attachment.setName(jsonAttachment.get("name").getAsString());
					attachment.setSize(jsonAttachment.get("size").getAsLong());
					attachment.setRemoteId(jsonAttachment.get("id").getAsString());
					if (jsonAttachment.has("url")) {
						attachment.setUrl(jsonAttachment.get("url").getAsString());
					}
					attachment.setObservation(observation);
					attachments.add(attachment);
				}
				observation.setAttachments(attachments);
			}
		}

		return observation;
	}
}
