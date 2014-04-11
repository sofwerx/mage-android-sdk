package mil.nga.giat.mage.sdk.gson.deserializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.datastore.location.LocationGeometry;
import mil.nga.giat.mage.sdk.datastore.location.LocationProperty;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.utils.DateUtility;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vividsolutions.jts.geom.Geometry;

public class LocationDeserializer implements JsonDeserializer<Location> {

	private static final String LOG_NAME = LocationDeserializer.class.getName();
	
	/**
	 * Convenience method for returning a Gson object with a registered GSon
	 * TypeAdaptor i.e. custom deserializer.
	 * 
	 * @return A Gson object that can be used to convert Json into a
	 *         {@link Location} object.
	 */
	public static Gson getGsonBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Location.class, new LocationDeserializer());
		return gsonBuilder.create();
	}
	
	@Override
	public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		JsonObject feature = json.getAsJsonObject();
		
		Location location = new Location();
		
		location.setRemoteId(feature.get("_id").getAsString());
		location.setDirty(Boolean.FALSE);
		location.setType("Feature");
				
		// deserialize geometry
		JsonObject geometryFeature = feature.get("geometry").getAsJsonObject();
		if (geometryFeature != null) {
			location.setLocationGeometry(new LocationGeometry(GeometryDeserializer.getGsonBuilder().fromJson(geometryFeature, Geometry.class)));
		}
				
		// deserialize properties
		JsonObject jsonProperties = feature.get("properties").getAsJsonObject();
		if (jsonProperties != null) {
			Collection<LocationProperty> properties = new ArrayList<LocationProperty>();
			Set<Map.Entry<String, JsonElement>> keys = jsonProperties.entrySet();
			for (Map.Entry<String, JsonElement> key : keys) {
				LocationProperty property = new LocationProperty();
				property.setKey(key.getKey());
				property.setValue(key.getValue().getAsString());
				property.setLocation(location);
				properties.add(property);
			}
			location.setProperties(properties);					
		}

		Map<String,String> locationProperties = location.getPropertiesMap();

		// deserialize user
		if(locationProperties.containsKey("user")) {
			User user = new User();
			user.setRemoteId(locationProperties.get("user"));		
			location.setUser(user);	
		}
		
		// deserialize timestamp
		if(locationProperties.containsKey("timestamp")) {						
			try{
				Date d = DateUtility.getISO8601().parse(locationProperties.get("timestamp"));
				location.setLastModified(d.getTime());
			}
			catch(ParseException pe) {
				Log.w("Unable to parse date: "
						+ locationProperties.get("timestamp")
						+ " for location: " + location.getRemoteId(), pe);
			}
		}		
		return location;
	}

}
