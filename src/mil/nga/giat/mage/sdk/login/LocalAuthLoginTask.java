package mil.nga.giat.mage.sdk.login;

import java.util.List;

import mil.nga.giat.mage.sdk.datastore.location.User;
import mil.nga.giat.mage.sdk.datastore.location.UserHelper;
import mil.nga.giat.mage.sdk.exceptions.UserException;
import android.content.Context;
import android.util.Log;

/**
 * A Task intended to be used for local authentication only.  
 * Testing or off-line modes perhaps.
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

	
		//retrieve the user name.
		String username = params[0];
		
		//initialize local active user
		try {

			//get active users
			List<User> activeUsers = userHelper.readActiveUsers();			
			if(activeUsers == null | activeUsers.size() != 1) {
				
				//delete active user(s)
				userHelper.deleteCurrentUsers();
				 				
				//create new active user.				
				User currentUser = new User("unknown", "unknown", "unknown",username, null, null, Boolean.TRUE);				
				currentUser = userHelper.createUser(currentUser);				
			}
			else {				
				Log.d(LOG_NAME,"A Current Active User exists." + activeUsers.get(0));
			}
	
		} 
		catch (UserException e) {
			//for now, treat as a warning.  Not a great state to be in.
			Log.w(LOG_NAME, "Unable to initialize a local Active User.");			
		}

		return new AccountStatus(Boolean.TRUE);
	}
}
