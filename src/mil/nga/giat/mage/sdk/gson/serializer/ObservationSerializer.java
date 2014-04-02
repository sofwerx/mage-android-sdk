package mil.nga.giat.mage.sdk.gson.serializer;

import java.lang.reflect.Type;

import mil.nga.giat.mage.sdk.datastore.common.GeometryType;
import mil.nga.giat.mage.sdk.datastore.common.PointGeometry;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationProperty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class ObservationSerializer implements JsonSerializer<Observation> {

	/**
	 * Convenience method for returning a Gson object with a registered GSon
	 * TypeAdaptor i.e. custom serializer.
	 * 
	 * @return A Gson object that can be used to convert {@link Observation} object
	 * into a JSON string.
	 */
	public static Gson getGsonBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Observation.class, new ObservationSerializer());
		return gsonBuilder.create();
	}	

	@Override
	public JsonElement serialize(Observation pObs, Type pType,
			JsonSerializationContext pContext) {

		
		JsonObject feature = new JsonObject();
		feature.add("type", new JsonPrimitive("Feature"));
		conditionalAdd("id", feature, pObs.getRemoteId());		
		
		//serialize the observation's geometry.  POINT only for now.
		if(GeometryType.POINT.equals(pObs.getObservationGeometry().getGeometry().getType())) {			
			PointGeometry pointGeometry = (PointGeometry)pObs.getObservationGeometry().getGeometry();
			JsonArray coordinates = new JsonArray();
			coordinates.add(new JsonPrimitive(pointGeometry.getLatitude()));
			coordinates.add(new JsonPrimitive(pointGeometry.getLongitude()));
			JsonObject geometry = new JsonObject();
			geometry.add("coordinates", coordinates);
			geometry.add("type", new JsonPrimitive("Point"));
			feature.add("geometry", geometry);
		}
		else {
			throw new UnsupportedOperationException("Unable to Serialize "
					+ pObs.getObservationGeometry().getGeometry().getType()
					+ " geometries.");
		}
		
		//serialize the observation's properties.
		JsonObject properties = new JsonObject();
		for(ObservationProperty property : pObs.getProperties()) {			
			conditionalAdd(property.getKey(), properties, property.getValue());
		}
		feature.add("properties", properties);
		
		//serialize the observation's attachments.
		JsonArray attachments = new JsonArray();
		for(Attachment attachment : pObs.getAttachments()) {
			JsonObject jsonAttachment = new JsonObject();		
			conditionalAdd("id",jsonAttachment,attachment.getRemoteId());
			conditionalAdd("contentType",jsonAttachment,attachment.getContentType());
			conditionalAdd("size",jsonAttachment,attachment.getSize());
			conditionalAdd("name",jsonAttachment,attachment.getName());
			conditionalAdd("relativePath",jsonAttachment,attachment.getRemotePath());
			conditionalAdd("url",jsonAttachment,attachment.getUrl());			
			attachments.add(jsonAttachment);
		}
		feature.add("attachments", attachments);
		
		//serialize the observation's state
		JsonObject jsonState = new JsonObject();
		jsonState.add("name", new JsonPrimitive(pObs.getState().toString()));
		feature.add("state", jsonState);
				
		return feature;
	}	
	
	private JsonObject conditionalAdd(String property, final JsonObject pJsonObject, Object toAdd) {
		if(toAdd != null) {
			pJsonObject.add(property, new JsonPrimitive(toAdd.toString()));
		}
		return pJsonObject;
	}
	
	
}
