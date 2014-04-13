package mil.nga.giat.mage.sdk.gson.serializer;

import java.lang.reflect.Type;

import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.datastore.location.LocationProperty;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
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

		
	@Override
	public JsonElement serialize(Location location, Type locationType,
			JsonSerializationContext context) {

		//this is what we're returning
		JsonObject user = new JsonObject();
		
		//set user id
		String userId = location.getUser().getRemoteId();		
		if(userId != null) {
			user.add("user",new JsonPrimitive(userId));
		}
		
		//create required components
		JsonArray jsonLocations = new JsonArray();
		JsonObject jsonLocation = new JsonObject();
		JsonObject jsonProperties = new JsonObject();
				
		//set json location values...
		conditionalAdd("type", location.getType(), jsonLocation);
		conditionalAdd("_id", location.getRemoteId(), jsonLocation);
		jsonLocation.add("geometry", new JsonParser().parse(GeometrySerializer.getGsonBuilder().toJson(location.getLocationGeometry().getGeometry())));
		jsonLocation.add("properties", jsonProperties);
		
		//...including json properties
		for(LocationProperty property : location.getProperties()) {
			//TODO: are there explicit properties that are NOT strings?  If so,
			//      treat them as such.
			conditionalAdd(property.getKey(), property.getValue(), jsonProperties);
		}
		
		//assemble final user object
		jsonLocations.add(jsonLocation);
		user.add("locations", jsonLocations);		
		
		return user;
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
		gsonBuilder.registerTypeAdapter(Location.class, new LocationSerializer());
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
