package mil.nga.giat.mage.sdk.login;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.exceptions.UserException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A Task intended to be used for local authentication only. Testing or off-line
 * modes perhaps.
 * 
 * @author travis
 * 
 */
public class LocalAuthLoginTask extends AbstractAccountTask {

	private static final String LOG_NAME = LocalAuthLoginTask.class.getName();

	private UserHelper userHelper;

	public LocalAuthLoginTask(AccountDelegate delegate, Context applicationContext) {
		super(delegate, applicationContext);
		userHelper = UserHelper.getInstance(applicationContext);
	}

	/**
	 * Called from execute
	 * 
	 * @param params
	 *            Should contain username and password; in that order.
	 */
	@Override
	protected AccountStatus doInBackground(String... params) {

		// retrieve the user name.
		String username = params[0];
		String password = params[1];

		try {
			// use a hash of the password as the token
			String md5Password = MessageDigest.getInstance("MD5").digest(password.getBytes("UTF-8")).toString();
			// put the token information in the shared preferences
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
			Editor editor = sharedPreferences.edit();
			editor.putString(mApplicationContext.getString(R.string.tokenKey), md5Password).commit();
			// FIXME : 8 hours for now?
			editor.putString(mApplicationContext.getString(R.string.tokenExpirationDateKey), new Date(new Date().getTime() + 8 * 60 * 60 * 1000).toString()).commit();
		} catch (NoSuchAlgorithmException nsae) {
			nsae.printStackTrace();
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}

		// initialize local active user
		try {
			// get active users
			List<User> activeUsers = userHelper.readActiveUsers();
			if (activeUsers == null || activeUsers.size() != 1) {

				// delete active user(s)
				userHelper.deleteCurrentUsers();

				// create new active user.
				User currentUser = new User("unknown", "unknown", "unknown", username, null);
				currentUser.setCurrentUser(Boolean.TRUE);
				currentUser = userHelper.createUser(currentUser);
			} else {
				Log.d(LOG_NAME, "A Current Active User exists." + activeUsers.get(0));
			}

		} catch (UserException e) {
			// for now, treat as a warning. Not a great state to be in.
			Log.w(LOG_NAME, "Unable to initialize a local Active User.");
		}

		return new AccountStatus(Boolean.TRUE);
	}
}
