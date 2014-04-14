package mil.nga.giat.mage.sdk.gson.serializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.datastore.location.LocationProperty;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.utils.DateUtility;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Used to convert a Location object into a json String.
 * @author travis
 *
 */
public class LocationSerializer implements JsonSerializer<Location>{

	private Set<String> doubleProperties = new HashSet<String>();
		
	public LocationSerializer(Context context) {
		super();
		String[] doubleFields = context.getResources().getStringArray(R.array.double_fields_array);
		if(doubleFields != null && doubleFields.length > 0) {
			doubleProperties = new HashSet<String>(Arrays.asList(doubleFields));
		}	
	}

	@Override
	public JsonElement serialize(Location location, Type locationType,
			JsonSerializationContext context) {

		//this is what we're returning
		JsonObject returnLocation = new JsonObject();
		
		//create required components
		JsonArray jsonLocations = new JsonArray();
		JsonObject jsonLocation = new JsonObject();
		JsonObject jsonProperties = new JsonObject();
				
		//set json location values...
		conditionalAdd("type", location.getType(), jsonLocation);
		conditionalAdd("_id", location.getRemoteId(), jsonLocation);
				
		jsonLocation.add("geometry", new JsonParser().parse(GeometrySerializer.getGsonBuilder().toJson(location.getLocationGeometry().getGeometry())));
		jsonLocation.add("properties", jsonProperties);
		
		//for now, we need to put the timestamp at the root level...	
		returnLocation.add("timestamp", new JsonPrimitive(DateUtility.getISO8601().format(location.getLastModified())));
		
		//...including json properties
		for(LocationProperty property : location.getProperties()) {
			String key = property.getKey();
			String value = property.getValue();

			if (doubleProperties.contains(key)) {
				// TODO: This will eventually be changed to a Date...
				// TODO: Perhaps we should wrap in an Exception?
				Double convertedValue = Double.valueOf(value);
				jsonProperties.add(key, new JsonPrimitive(convertedValue));
			} else {
				conditionalAdd(key, value, jsonProperties);
			}
		}
		
		//assemble final user object
		jsonLocations.add(jsonLocation);
		returnLocation.add("location", jsonLocation);		
		
		return returnLocation;
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
		gsonBuilder.registerTypeAdapter(Location.class, new LocationSerializer(context));
		return gsonBuilder.create();
	}
	
	/**
	 * Utility used to ensure we don't add junk to the json string.  For now,
	 * we skip null property values.
	 * @param property Property to add.
	 * @param toAdd Property value to add.
	 * @param pJsonObject Object to conditionally add to.
	 * @return A reference to json object.
	 */
	private JsonObject conditionalAdd(String property, Object toAdd, final JsonObject pJsonObject) {
		if (toAdd != null) {
			pJsonObject.add(property, new JsonPrimitive(toAdd.toString()));
		}
		return pJsonObject;
	}
	
}
