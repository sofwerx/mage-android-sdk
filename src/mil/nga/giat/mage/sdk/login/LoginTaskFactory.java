package mil.nga.giat.mage.sdk.login;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Deals with login tasks and their configuration.
 * 
 * @author wiedemannse
 *
 */
public class LoginTaskFactory {

	private LoginTaskFactory() {
	}

	private static LoginTaskFactory loginTaskFactory;
	private static Context mContext;

	public static LoginTaskFactory getInstance(final Context context) {
		if (context == null) {
			return null;
		}
		if (loginTaskFactory == null) {
			loginTaskFactory = new LoginTaskFactory();
		}
		mContext = context;
		return loginTaskFactory;
	}

	/**
	 * Retrieves the correct login module from the configuration.
	 * 
	 * @param delegate
	 * @param context
	 * @return
	 */
	public AbstractAccountTask getLoginTask(AccountDelegate delegate, Context context) {
		String className = PreferenceHelper.getInstance(mContext).getValue(R.string.loginTaskKey, String.class, R.string.loginTaskDefaultValue);
		
		try {
			Class<?> c = Class.forName(className);
			Constructor[] constructors = c.getConstructors();
			for (Constructor constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 2 && params[0].isAssignableFrom(AccountDelegate.class) && params[1].isAssignableFrom(Context.class)) {
					return (AbstractAccountTask) constructor.newInstance(delegate, context);
				}
			}
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		} catch (InstantiationException ie) {
			ie.printStackTrace();
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		} catch (InvocationTargetException ite) {
			ite.printStackTrace();
		}
		return new FormAuthLoginTask(delegate, context);
	}
}
