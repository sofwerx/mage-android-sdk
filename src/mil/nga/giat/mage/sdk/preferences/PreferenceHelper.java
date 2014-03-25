package mil.nga.giat.mage.sdk.preferences;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.login.FormAuthLoginTask;
import mil.nga.giat.mage.sdk.login.LocalAuthLoginTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

/**
 * Loads the default configuration from the local property files, and also loads
 * the server configuration.
 * 
 * @author wiedemannse
 * 
 */
public class PreferenceHelper {

	private PreferenceHelper() {
	}

	private static PreferenceHelper preferenceHelper;
	private static Context mContext;

	public static PreferenceHelper getInstance(final Context context) {
		if (context == null) {
			return null;
		}
		if (preferenceHelper == null) {
			preferenceHelper = new PreferenceHelper();
		}
		mContext = context;
		return preferenceHelper;
	}

	/**
	 * Should probably be called only once to initialize the settings and
	 * properties.
	 * 
	 */
	public synchronized void initializeAll(int... xmlFiles) {
		// load preferences from mdk xml files first
		initializeLocal(new int[] { R.xml.mdkprivatepreferences, R.xml.mdkpublicpreferences, R.xml.locationpreferences });

		// add programmatic preferences
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = sharedPreferences.edit();
		try {
			editor.putString("appVersion", mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName).commit();
		} catch (NameNotFoundException nnfe) {
			nnfe.printStackTrace();
		}

		// load other xml files
		initializeLocal(xmlFiles);

		// see if we need to load preferences from a server
		String serverURLString = sharedPreferences.getString("serverURL", "");
		String className = sharedPreferences.getString("loginTask", FormAuthLoginTask.class.getCanonicalName());
		try {
			Class<?> c = Class.forName(className);
			// if this is not a local login
			if (!c.equals(LocalAuthLoginTask.class.getCanonicalName()) && !serverURLString.trim().isEmpty()) {
				try {
					// load preferences from server
					initializeRemote(new URL(serverURLString));
				} catch (MalformedURLException mue) {
					mue.printStackTrace();
				}
			}
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}

	public synchronized void initializeLocal(int... xmlFiles) {
		for (int id : xmlFiles) {
			PreferenceManager.setDefaultValues(mContext, id, true);
		}
	}

	public synchronized void initializeRemote(URL serverURL) {
		try {
			new RemotePreferenceColonization().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverURL).get(30, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (ExecutionException ee) {
			ee.printStackTrace();
		} catch (TimeoutException te) {
			te.printStackTrace();
		}
	}

	private class RemotePreferenceColonization extends AsyncTask<URL, Void, Void> {

		@Override
		protected Void doInBackground(URL... arg0) {
			initialize(arg0[0]);
			return null;
		}

		/**
		 * Flattens the json from the server and puts key, value pairs in the
		 * DefaultSharedPreferences
		 * 
		 * @param sharedPreferenceName
		 * @param json
		 */
		private void populateValues(String sharedPreferenceName, JSONObject json) {
			@SuppressWarnings("unchecked")
			Iterator<String> iter = json.keys();
			while (iter.hasNext()) {
				String key = iter.next();
				try {
					Object value = json.get(key);
					if (value instanceof JSONObject) {
						populateValues(sharedPreferenceName + Character.toUpperCase(key.charAt(0)) + ((key.length() > 1) ? key.substring(1) : ""), (JSONObject) value);
					} else {
						SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
						Editor editor = sharedPreferences.edit();
						editor.putString(sharedPreferenceName + Character.toUpperCase(key.charAt(0)) + ((key.length() > 1) ? key.substring(1) : ""), value.toString()).commit();
					}
				} catch (JSONException je) {
					je.printStackTrace();
				}
			}
		}

		private void initialize(URL serverURL) {
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpGet get = new HttpGet(new URL(serverURL, "api").toURI());
				HttpResponse response = httpclient.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
					// preface all global
					populateValues("g", json);
				}
			} catch (MalformedURLException mue) {
				mue.printStackTrace();
			} catch (URISyntaxException use) {
				// TODO Auto-generated catch block
				use.printStackTrace();
			} catch (UnsupportedEncodingException uee) {
				// TODO Auto-generated catch block
				uee.printStackTrace();
			} catch (ClientProtocolException cpe) {
				// TODO Auto-generated catch block
				cpe.printStackTrace();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			} catch (ParseException pe) {
				// TODO Auto-generated catch block
				pe.printStackTrace();
			} catch (JSONException je) {
				// TODO Auto-generated catch block
				je.printStackTrace();
			}
		}
	}

	/**
	 * Use this method to get values of correct type form shared preferences.
	 * Does not work with collections yet!
	 * 
	 * @param <T>
	 * @param key
	 * @param valueType
	 * @param defaultValue
	 * @return
	 */
	public final <T extends Object> T getValue(int key, Class<T> valueType, int defaultValue) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		String defaultValueString = sharedPreferences.getString(mContext.getString(defaultValue), mContext.getString(defaultValue));
		Object defaultReturnValue = null;
		if (valueType.equals(String.class)) {
			defaultReturnValue = defaultValueString;
		} else {
			try {
				Method valueOfMethod = valueType.getMethod("valueOf", String.class);
				defaultReturnValue = valueOfMethod.invoke(valueType, defaultValueString);
			} catch (NoSuchMethodException nsme) {
				nsme.printStackTrace();
			} catch (IllegalAccessException iae) {
				iae.printStackTrace();
			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();
			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
			}
		}

		return getValue(key, valueType, (T) defaultReturnValue);
	}

	public final <T extends Object> T getValue(int key, Class<T> valueType, T defaultValue) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		String stringValue = sharedPreferences.getString(mContext.getString(key), null);
		if (stringValue != null) {
			if(valueType.equals(String.class)) {
				return (T) stringValue;
			} else {
				try {
					Method valueOfMethod = valueType.getMethod("valueOf", String.class);
					Object returnValue = valueOfMethod.invoke(valueType, stringValue);
					return (T) returnValue;
				} catch (NoSuchMethodException nsme) {
					nsme.printStackTrace();
				} catch (IllegalAccessException iae) {
					iae.printStackTrace();
				} catch (IllegalArgumentException iae) {
					iae.printStackTrace();
				} catch (InvocationTargetException ite) {
					ite.printStackTrace();
				}
			}
		}

		return defaultValue;
	}
}
