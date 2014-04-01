package mil.nga.giat.mage.sdk.gson.deserializer;

import java.lang.reflect.Type;

import mil.nga.giat.mage.sdk.datastore.user.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * JSON to {@link User}
 * 
 * @author wiedemannse
 * 
 */
public class UserDeserializer implements JsonDeserializer<User> {

	private static final String LOG_NAME = UserDeserializer.class.getName();

	/**
	 * Convenience method for returning a Gson object with a registered GSon
	 * TypeAdaptor i.e. custom deserializer.
	 * 
	 * @return A Gson object that can be used to convert Json into a {@link User}.
	 */
	public static Gson getGsonBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(User.class, new UserDeserializer());
		return gsonBuilder.create();
	}

	@Override
	public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		JsonObject feature = json.getAsJsonObject();

		String remoteId = feature.get("_id").getAsString();
		String email = feature.get("email").getAsString();
		String firstname = feature.get("firstname").getAsString();
		String lastname = feature.get("lastname").getAsString();
		String username = feature.get("username").getAsString();

		// FIXME : get role

		User user = new User(remoteId, email, firstname, lastname, username, null);
		return user;
	}
}
