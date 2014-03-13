package mil.nga.giat.mage.sdk.login;

import android.content.Context;

public class LocalAuthLoginTask extends AbstractAccountTask {

	public LocalAuthLoginTask(AccountDelegate delegate, Context applicationContext) {
		super(delegate, applicationContext);
	}

	/**
	 * Called from execute
	 * 
	 * @param params
	 *            Should contain username and password; in that order.
	 */
	@Override
	protected AccountStatus doInBackground(String... params) {

//		// get inputs
//		String username = params[0];
//		String password = params[1];
//
//		// TODO: add actual local authorization implementation
//		if (!username.isEmpty() && password.startsWith("12345")) {
//			return new AccountStatus(Boolean.TRUE);
//		}

		return new AccountStatus(Boolean.TRUE);
	}
}
