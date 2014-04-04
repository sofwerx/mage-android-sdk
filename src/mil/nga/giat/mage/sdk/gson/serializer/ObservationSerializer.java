package mil.nga.giat.mage.sdk.gson.serializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.common.GeometryType;
import mil.nga.giat.mage.sdk.datastore.common.PointGeometry;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationProperty;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class ObservationSerializer implements JsonSerializer<Observation> {

	private Set<String> dateProperties = new HashSet<String>();
	
	public ObservationSerializer(Context context) {
		super();
		//initialize a Set of known properties that are to be treated as Dates.
		String[] dateFields = context.getResources().getStringArray(R.array.date_fields_array);
		if(dateFields != null && dateFields.length > 0) {
			dateProperties = new HashSet<String>(Arrays.asList(dateFields));
		}
	}

	/**
	 * Convenience method for returning a Gson object with a registered GSon
	 * TypeAdaptor i.e. custom serializer.
	 * 
	 * @return A Gson object that can be used to convert {@link Observation} object
	 * into a JSON string.
	 */
	public static Gson getGsonBuilder(Context context) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Observation.class, new ObservationSerializer(context));
		return gsonBuilder.create();
	}	

	@Override
	public JsonElement serialize(Observation pObs, Type pType,
			JsonSerializationContext pContext) {

		
		JsonObject feature = new JsonObject();
		feature.add("type", new JsonPrimitive("Feature"));
		conditionalAdd("id", pObs.getRemoteId(), feature);		
		
		//serialize the observation's geometry.  POINT only for now.
		if(GeometryType.POINT.equals(pObs.getObservationGeometry().getGeometry().getType())) {			
			PointGeometry pointGeometry = (PointGeometry)pObs.getObservationGeometry().getGeometry();
			JsonArray coordinates = new JsonArray();
			coordinates.add(new JsonPrimitive(pointGeometry.getLongitude()));
			coordinates.add(new JsonPrimitive(pointGeometry.getLatitude()));
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
			
			String key = property.getKey();
			String value = property.getValue();
			
			if(dateProperties.contains(key)) {				
				//TODO: This will eventually be changed to a Date...
				//TODO: Perhaps we should wrap in an Exception?
				Long convertedValue = Long.valueOf(value);
				properties.add(key, new JsonPrimitive(convertedValue));
			}
			else {
				conditionalAdd(key, value, properties);
			}
			
		}
		feature.add("properties", properties);
		
		//serialize the observation's attachments.
		/*
		JsonArray attachments = new JsonArray();
		for(Attachment attachment : pObs.getAttachments()) {
			JsonObject jsonAttachment = new JsonObject();		
			conditionalAdd("id",attachment.getRemoteId(),jsonAttachment);
			conditionalAdd("contentType",attachment.getContentType(),jsonAttachment);
			conditionalAdd("size",attachment.getSize(),jsonAttachment);
			conditionalAdd("name",attachment.getName(),jsonAttachment);
			conditionalAdd("relativePath",attachment.getRemotePath(),jsonAttachment);
			conditionalAdd("url",attachment.getUrl(),jsonAttachment);			
			attachments.add(jsonAttachment);
		}
		feature.add("attachments", attachments);
		*/
		
		//serialize the observation's state
		JsonObject jsonState = new JsonObject();
		jsonState.add("name", new JsonPrimitive(pObs.getState().toString()));
		feature.add("state", jsonState);
				
		return feature;
	}	
	
	
	private JsonObject conditionalAdd(String property, Object toAdd, final JsonObject pJsonObject) {
		if(toAdd != null) {
			pJsonObject.add(property, new JsonPrimitive(toAdd.toString()));
		}
		return pJsonObject;
	}
	
	
}
