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
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;

import org.apache.http.HttpEntity;
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
import android.util.Log;

/**
 * Loads the default configuration from the local property files, and also loads
 * the server configuration.
 * 
 * TODO: add setValue methods that provide similar functionallity as getValue
 * 
 * @author wiedemannse
 * 
 */
public class PreferenceHelper {

	private static final String LOG_NAME = PreferenceHelper.class.getName();
	
	private PreferenceHelper() {
	}

	private static PreferenceHelper preferenceHelper;
	private static Context mContext;
	private boolean initialized = false;

	public static PreferenceHelper getInstance(final Context context) {
		if (context == null) {
			return null;
		}
		if (preferenceHelper == null) {
			preferenceHelper = new PreferenceHelper();
			mContext = context;
		}
		return preferenceHelper;
	}

	/**
	 * Should probably be called only once to initialize the settings and
	 * properties.
	 * 
	 */
	public synchronized void initialize(int... xmlFiles) {
		if (!initialized) {
			// load preferences from mdk xml files first
			initializeLocal(new int[] { R.xml.mdkprivatepreferences, R.xml.mdkpublicpreferences, R.xml.locationpreferences, R.xml.fetchpreferences });

			// add programmatic preferences
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			Editor editor = sharedPreferences.edit();
			try {
				editor.putString(mContext.getString(R.string.buildVersionKey), mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName).commit();
			} catch (NameNotFoundException nnfe) {
				nnfe.printStackTrace();
			}

			// load other xml files
			initializeLocal(xmlFiles);
			initialized = true;
		}
	}

	private synchronized void initializeLocal(int... xmlFiles) {
		for (int id : xmlFiles) {
			PreferenceManager.setDefaultValues(mContext, id, true);
		}
	}

	public synchronized void readRemote(URL serverURL) throws InterruptedException, ExecutionException, TimeoutException {
		new RemotePreferenceColonization().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverURL).get(30, TimeUnit.SECONDS);
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
						String keyString = sharedPreferenceName + Character.toUpperCase(key.charAt(0)) + ((key.length() > 1) ? key.substring(1) : "");
						Log.i(LOG_NAME, keyString + " is " + sharedPreferences.getString(keyString, "empty") + ".  Setting it to " + value.toString() + ".");
						editor.putString(keyString, value.toString()).commit();
					}
				} catch (JSONException je) {
					je.printStackTrace();
				}
			}
		}

		private void initialize(URL serverURL) {
			HttpEntity entity = null;
			try {
				DefaultHttpClient httpclient = HttpClientManager.getInstance(mContext).getHttpClient();
				HttpGet get = new HttpGet(new URL(serverURL, "api").toURI());
				HttpResponse response = httpclient.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					entity = response.getEntity();
					JSONObject json = new JSONObject(EntityUtils.toString(entity));
					// preface all global
					populateValues("g", json);
				} else {
					entity = response.getEntity();
					String error = EntityUtils.toString(entity);
					Log.e(LOG_NAME, "Bad request.");
					Log.e(LOG_NAME, error);
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
			} finally {
				try {
					if (entity != null) {
						entity.consumeContent();
					}
				} catch (Exception e) {
				}
			}
		}
	}

	public final String getValue(int key) {
		return getValue(key, String.class, null);
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
			if (valueType.equals(String.class)) {
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
