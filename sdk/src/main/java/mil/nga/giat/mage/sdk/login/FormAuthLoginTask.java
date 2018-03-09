package mil.nga.giat.mage.sdk.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.DaoStore;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.http.resource.DeviceResource;
import mil.nga.giat.mage.sdk.http.resource.UserResource;
import mil.nga.giat.mage.sdk.jackson.deserializer.UserDeserializer;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.DeviceUuidFactory;
import mil.nga.giat.mage.sdk.utils.ISO8601DateFormatFactory;
import mil.nga.giat.mage.sdk.utils.PasswordUtility;

/**
 * Performs login to specified server with username and password.
 *
 * @author wiedemanns
 */
public class FormAuthLoginTask extends AbstractAccountTask {

	private static final String LOG_NAME = FormAuthLoginTask.class.getName();
	private DateFormat iso8601Format = ISO8601DateFormatFactory.ISO8601();
	private UserDeserializer userDeserializer;

	private volatile AccountStatus callbackStatus = null;

	public FormAuthLoginTask(AccountDelegate delegate, Context context) {
		super(delegate, context);

		userDeserializer = new UserDeserializer(context);
	}

	/**
	 * Called from execute
	 *
	 * @param params Should contain username, password, and serverURL; in that
	 *               order.
	 * @return On success, {@link AccountStatus#getAccountInformation()}
	 * contains the user's token
	 */
	@Override
	protected AccountStatus doInBackground(String... params) {
		return login(params);
	}

	private AccountStatus login(String... params) {
		// get inputs
		String username = params[0];
		String password = params[1];
		String serverURL = params[2];
		Boolean needToRegisterDevice = Boolean.valueOf(params[3]);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);

		// Make sure you have connectivity
		if (!ConnectivityUtility.isOnline(mApplicationContext)) {
			// try disconnected login
			try {
				String oldUsername = sharedPreferences.getString(mApplicationContext.getString(R.string.usernameKey), mApplicationContext.getString(R.string.usernameDefaultValue));
				String serverURLPref = sharedPreferences.getString(mApplicationContext.getString(R.string.serverURLKey), mApplicationContext.getString(R.string.serverURLDefaultValue));
				String oldPasswordHash = sharedPreferences.getString(mApplicationContext.getString(R.string.passwordHashKey), null);
				if (oldUsername != null && oldPasswordHash != null && !oldPasswordHash.trim().isEmpty()) {
					if (oldUsername.equals(username) && serverURL.equals(serverURLPref) && PasswordUtility.equal(password, oldPasswordHash)) {
						// put the token expiration information in the shared preferences
						long tokenExpirationLength = Math.max(sharedPreferences.getLong(mApplicationContext.getString(R.string.tokenExpirationLengthKey), 0), 0);
						Date tokenExpiration = new Date(System.currentTimeMillis() + tokenExpirationLength);
						sharedPreferences.edit().putString(mApplicationContext.getString(R.string.tokenExpirationDateKey), iso8601Format.format(tokenExpiration)).commit();

						return new AccountStatus(AccountStatus.Status.DISCONNECTED_LOGIN);
					} else {
						return new AccountStatus(AccountStatus.Status.FAILED_LOGIN);
					}
				}
			} catch (Exception e) {
				Log.e(LOG_NAME, "Could not hash password", e);
			}

			List<Integer> errorIndices = new ArrayList<>();
			errorIndices.add(2);
			List<String> errorMessages = new ArrayList<>();
			errorMessages.add("No connection.");
			return new AccountStatus(AccountStatus.Status.FAILED_LOGIN, errorIndices, errorMessages);
		}

		String uuid = new DeviceUuidFactory(mApplicationContext).getDeviceUuid().toString();
		if (uuid == null) {
			List<Integer> errorIndices = new ArrayList<>();
			errorIndices.add(2);
			List<String> errorMessages = new ArrayList<>();
			errorMessages.add("Problem generating device uuid");
			return new AccountStatus(AccountStatus.Status.FAILED_LOGIN, errorIndices, errorMessages);
		}

		try {
			// Does the device need to be registered?
			if (needToRegisterDevice) {
				AccountStatus.Status regStatus = registerDevice(username, uuid, password);

				if (regStatus.equals(AccountStatus.Status.SUCCESSFUL_REGISTRATION)) {
					return new AccountStatus(regStatus);
				} else if (regStatus == AccountStatus.Status.FAILED_LOGIN) {
					return new AccountStatus(regStatus);
				}
			}

			UserResource userResource = new UserResource(mApplicationContext);
			JsonObject loginJson = userResource.login(username, uuid, password);

			if (loginJson != null) {
				// check server api version to ensure compatibility before continuing
				JsonObject serverVersion = loginJson.get("api").getAsJsonObject().get("version").getAsJsonObject();
				if (!PreferenceHelper.getInstance(mApplicationContext).validateServerVersion(serverVersion.get("major").getAsInt(), serverVersion.get("minor").getAsInt())) {
					Log.e(LOG_NAME, "Server version not compatible");
					return new AccountStatus(AccountStatus.Status.INVALID_SERVER);
				}

				// put the token information in the shared preferences
				Editor editor = sharedPreferences.edit();

				editor.putString(mApplicationContext.getString(R.string.tokenKey), loginJson.get("token").getAsString().trim());
				Log.d(LOG_NAME, "Storing token: " + String.valueOf(sharedPreferences.getString(mApplicationContext.getString(R.string.tokenKey), null)));
				try {
					Date tokenExpiration = iso8601Format.parse(loginJson.get("expirationDate").getAsString().trim());
					long tokenExpirationLength = tokenExpiration.getTime() - (new Date()).getTime();
					editor.putString(mApplicationContext.getString(R.string.tokenExpirationDateKey), iso8601Format.format(tokenExpiration));
					editor.putLong(mApplicationContext.getString(R.string.tokenExpirationLengthKey), tokenExpirationLength);
				} catch (java.text.ParseException e) {
					Log.e(LOG_NAME, "Problem parsing token expiration date.", e);
				}

				// initialize the current user
				JsonObject userJson = loginJson.getAsJsonObject("user");

				// if username is different, then clear the db
				String oldUsername = sharedPreferences.getString(mApplicationContext.getString(R.string.usernameKey), mApplicationContext.getString(R.string.usernameDefaultValue));
				String newUsername = userJson.get("username").getAsString();
				if (oldUsername == null || !oldUsername.equals(newUsername)) {
					DaoStore.getInstance(mApplicationContext).resetDatabase();
				}

				User user = userDeserializer.parseUser(userJson.toString());
				if (user != null) {
					user.setFetchedDate(new Date());
					user = userHelper.createOrUpdate(user);

					userHelper.setCurrentUser(user);

					editor.putString(mApplicationContext.getString(R.string.displayNameKey), user.getDisplayName());
				} else {
					Log.e(LOG_NAME, "Unable to Deserializer user.");
					List<Integer> errorIndices = new ArrayList<>();
					errorIndices.add(2);
					List<String> errorMessages = new ArrayList<>();
					errorMessages.add("Problem retrieving your user.");
					return new AccountStatus(AccountStatus.Status.FAILED_LOGIN, errorIndices, errorMessages);
				}

				editor.commit();

				return new AccountStatus(AccountStatus.Status.SUCCESSFUL_LOGIN, new ArrayList<Integer>(), new ArrayList<String>(), loginJson);
			} else {
				// Could be that the device is not registered.
				if (!needToRegisterDevice) {
					// Try to register it
					params[3] = Boolean.TRUE.toString();
					return login(params);
				}
			}
		} catch (Exception e) {
			Log.e(LOG_NAME, "Problem logging in.", e);
		}


		return new AccountStatus(AccountStatus.Status.FAILED_LOGIN);
	}

	private AccountStatus.Status registerDevice(String username, String uid, String password) {
		try {
			DeviceResource deviceResource = new DeviceResource(mApplicationContext);
			JsonObject deviceJson = deviceResource.createDevice(username, uid, password);
			if (deviceJson != null) {
				if (deviceJson.get("registered").getAsBoolean()) {
					return AccountStatus.Status.ALREADY_REGISTERED;
				} else {
					// device registration has been submitted
					return AccountStatus.Status.SUCCESSFUL_REGISTRATION;
				}
			}
		} catch (Exception e) {
			Log.e(LOG_NAME, "Problem registering device.", e);
		}

		return AccountStatus.Status.FAILED_LOGIN;
	}
}
