package mil.nga.giat.mage.sdk.gson.deserializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import mil.nga.giat.mage.sdk.datastore.common.GeometryType;
import mil.nga.giat.mage.sdk.datastore.common.PointGeometry;
import mil.nga.giat.mage.sdk.datastore.common.State;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationGeometry;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationProperty;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 
 * @author travis
 *
 */
public class ObservationDeserializer implements JsonDeserializer<Observation> {

	public Observation deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {

		JsonObject feature = (JsonObject) json;

		Observation observation = new Observation();

		// deserialize remote_id
		observation.setRemote_id(feature.get("id").getAsString());

		// deserialize state
		JsonObject states = (JsonObject) ((JsonArray) feature.get("states"))
				.get(0);
		if (State.ACTIVE.toString().equalsIgnoreCase(
				states.get("name").getAsString())) {
			observation.setState(State.ACTIVE);
		} else if (State.ARCHIVE.toString().equalsIgnoreCase(
				states.get("name").getAsString())) {
			observation.setState(State.ARCHIVE);
		} else if (State.COMPLETE.toString().equalsIgnoreCase(
				states.get("name").getAsString())) {
			observation.setState(State.COMPLETE);
		}

		// deserialize geometry
		JsonObject geometry = (JsonObject) feature.get("geometry");
		String geometryType = geometry.get("type").getAsString();
		// is a POINT?
		if (GeometryType.POINT.toString().equalsIgnoreCase(geometryType)) {
			String coordinates = ((JsonArray) geometry.get("coordinates"))
					.toString();
			observation.setObservationGeometry(new ObservationGeometry(
					new PointGeometry(coordinates)));
		}

		// deserialize properties
		JsonObject jsonProperties = (JsonObject) feature.get("properties");
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

		// deserialize attachments
		JsonArray jsonAttachments = (JsonArray) feature.get("attachments");
		Collection<Attachment> attachments = new ArrayList<Attachment>();

		for (int i = 0; i < jsonAttachments.size(); i++) {
			JsonObject jsonAttachment = (JsonObject) jsonAttachments.get(i);
			// Attachment attachment = new Attachment();
			// attachment.setContent_type(jsonAttachment.get(memberName));
			// TODO: finish this up
		}
		return observation;
	}
}
