package mil.nga.giat.mage.sdk.gson.deserializer;

import java.lang.reflect.Type;

import mil.nga.giat.mage.sdk.datastore.layer.Layer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * JSON to {@link Layer}
 * 
 * @author wiedemannse
 * 
 */
public class LayerDeserializer implements JsonDeserializer<Layer> {

	private static final String LOG_NAME = LayerDeserializer.class.getName();
	
	/**
	 * Convenience method for returning a Gson object with a registered GSon
	 * TypeAdaptor i.e. custom deserializer.
	 * 
	 * @return A Gson object that can be used to convert Json into a {@link Layer}.
	 */
	public static Gson getGsonBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Layer.class, new LayerDeserializer());
		return gsonBuilder.create();
	}

	@Override
	public Layer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		JsonObject feature = json.getAsJsonObject();
		Long remoteId = feature.get("id").getAsLong();
		String type = feature.get("type").getAsString();
		String name = feature.get("name").getAsString();

		Layer layer = new Layer(remoteId, type, name);
		return layer;
	}
}
