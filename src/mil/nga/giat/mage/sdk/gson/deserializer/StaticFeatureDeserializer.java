package mil.nga.giat.mage.sdk.gson.deserializer;

import java.lang.reflect.Type;

import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeature;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeatureGeometry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vividsolutions.jts.geom.Geometry;

/**
 * JSON to {@link StaticFeature}
 * 
 * @author wiedemannse
 * 
 */
public class StaticFeatureDeserializer implements JsonDeserializer<StaticFeature> {

	private static final String LOG_NAME = StaticFeatureDeserializer.class.getName();

	/**
	 * Convenience method for returning a Gson object with a registered GSon
	 * TypeAdaptor i.e. custom deserializer.
	 * 
	 * @return A Gson object that can be used to convert Json into an
	 *         {@link StaticFeatureDeserializer} object.
	 */
	public static Gson getGsonBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(StaticFeature.class, new StaticFeatureDeserializer());
		return gsonBuilder.create();
	}

	@Override
	public StaticFeature deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		JsonObject feature = json.getAsJsonObject();

		StaticFeature staticFeature = new StaticFeature();
		staticFeature.setRemoteId(feature.get("id").getAsString());

		// deserialize geometry
		JsonObject geometryFeature = feature.get("geometry").getAsJsonObject();
		if (geometryFeature != null) {
			staticFeature.setStaticFeatureGeometry(new StaticFeatureGeometry(GeometryDeserializer.getGsonBuilder().fromJson(geometryFeature, Geometry.class)));
		}

		return staticFeature;
	}
}
