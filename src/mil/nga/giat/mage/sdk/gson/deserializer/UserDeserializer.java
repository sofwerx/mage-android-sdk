package mil.nga.giat.mage.sdk.gson.deserializer;

import java.lang.reflect.Type;

import mil.nga.giat.mage.sdk.datastore.user.Role;
import mil.nga.giat.mage.sdk.datastore.user.RoleHelper;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.exceptions.RoleException;
import android.content.Context;
import android.util.Log;

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

	private Context mContext;
	
	public UserDeserializer(Context context) {
		this.mContext = context;
	}
	
	/**
	 * Convenience method for returning a Gson object with a registered GSon
	 * TypeAdaptor i.e. custom deserializer.
	 * 
	 * @return A Gson object that can be used to convert Json into a {@link User}.
	 */
	public static Gson getGsonBuilder(Context context) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(User.class, new UserDeserializer(context));
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

		Role role = null;
		JsonObject roleJSON = feature.get("role").getAsJsonObject();
		if(roleJSON != null) {
			String roleId = roleJSON.get("_id").getAsString();
			try {
				role = RoleHelper.getInstance(mContext).read(roleId);
			} catch (RoleException e) {
				Log.e(LOG_NAME, "Could not find matching role for user.");
			}
		}

		User user = new User(remoteId, email, firstname, lastname, username, role);
		return user;
	}
}
