package mil.nga.giat.mage.sdk.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.exceptions.LoginException;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.DateUtility;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Performs login to specified server with username and password. TODO: Should
 * this also handle device registration?  TODO: throw {@link LoginException}
 * 
 * @author wiedemannse
 * 
 */
public class FormAuthLoginTask extends AbstractAccountTask {

	public FormAuthLoginTask(AccountDelegate delegate, Context context) {
		super(delegate, context);
	}

	/**
	 * Called from execute
	 * 
	 * @param params
	 *            Should contain username, password, and serverURL; in that
	 *            order.
	 * @return On success, {@link AccountStatus#getAccountInformation()}
	 *         contains the user's token
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

		// Make sure you have connectivity
		if (!ConnectivityUtility.isOnline(mApplicationContext)) {
			List<Integer> errorIndices = new ArrayList<Integer>();
			errorIndices.add(2);
			List<String> errorMessages = new ArrayList<String>();
			errorMessages.add("No connection");
			return new AccountStatus(AccountStatus.Status.FAILED_LOGIN, errorIndices, errorMessages);
		}

		String macAddress = ConnectivityUtility.getMacAddress(mApplicationContext);
		if (macAddress == null) {
			List<Integer> errorIndices = new ArrayList<Integer>();
			errorIndices.add(2);
			List<String> errorMessages = new ArrayList<String>();
			errorMessages.add("No mac address found on device");
			return new AccountStatus(AccountStatus.Status.FAILED_LOGIN, errorIndices, errorMessages);
		}

		// is server a valid URL? (already checked username and password)
		try {
			URL sURL = new URL(serverURL);
			
			// Make sure host exists
			if (!ConnectivityUtility.isResolvable(sURL.getHost())) {
				List<Integer> errorIndices = new ArrayList<Integer>();
				errorIndices.add(2);
				List<String> errorMessages = new ArrayList<String>();
				errorMessages.add("Bad hostname");
				return new AccountStatus(AccountStatus.Status.FAILED_LOGIN, errorIndices, errorMessages);
			}
			
			try {
				PreferenceHelper.getInstance(mApplicationContext).initializeRemote(sURL);
			} catch (Exception e) {
				List<Integer> errorIndices = new ArrayList<Integer>();
				errorIndices.add(2);
				List<String> errorMessages = new ArrayList<String>();
				errorMessages.add("Problem connecting to server");
				return new AccountStatus(AccountStatus.Status.FAILED_LOGIN, errorIndices, errorMessages);
			}
		} catch (MalformedURLException e) {
			List<Integer> errorIndices = new ArrayList<Integer>();
			errorIndices.add(2);
			List<String> errorMessages = new ArrayList<String>();
			errorMessages.add("Bad URL");
			return new AccountStatus(AccountStatus.Status.FAILED_LOGIN, errorIndices, errorMessages);
		}

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
		
		try {
			DefaultHttpClient httpClient = HttpClientManager.getInstance(mApplicationContext).getHttpClient();
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("password", password));
			nameValuePairs.add(new BasicNameValuePair("uid", macAddress));
			nameValuePairs.add(new BasicNameValuePair("username", username));
			UrlEncodedFormEntity authParams = new UrlEncodedFormEntity(nameValuePairs);
			
			// If we think we need to register, go do it
			if(!sharedPreferences.getBoolean(mApplicationContext.getString(R.string.deviceRegisteredKey), false)) {
				AccountStatus.Status regStatus = registerDevice(serverURL, authParams);
				
				if (regStatus == AccountStatus.Status.SUCCESSFUL_REGISTRATION) {
					return new AccountStatus(AccountStatus.Status.SUCCESSFUL_REGISTRATION, new ArrayList<Integer>(), new ArrayList<String>());
				} else if (regStatus == AccountStatus.Status.FAILED_LOGIN) {
					return new AccountStatus(AccountStatus.Status.FAILED_LOGIN);
				}
			}
			HttpPost post = new HttpPost(new URL(new URL(serverURL), "api/login").toURI());
			post.setEntity(authParams);
			HttpResponse response = httpClient.execute(post);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));

				// put the token information in the shared preferences
				
				Editor editor = sharedPreferences.edit();
				editor.putString(mApplicationContext.getString(R.string.tokenKey), json.getString("token").trim()).commit();				
				try {
					editor.putString(mApplicationContext.getString(R.string.tokenExpirationDateKey), DateUtility.getISO8601().format(DateUtility.getISO8601().parse(json.getString("expirationDate").trim()))).commit();
				} catch (java.text.ParseException e) {
				}
				return new AccountStatus(AccountStatus.Status.SUCCESSFUL_LOGIN, new ArrayList<Integer>(), new ArrayList<String>(), json);
			} else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				
				response.getEntity();
				// Could be that the device is not registered.
				if(sharedPreferences.getBoolean(mApplicationContext.getString(R.string.deviceRegisteredKey), false)) {
					// If we think the device was registered but failed to login, try to register it again
					Editor editor = PreferenceManager.getDefaultSharedPreferences(mApplicationContext).edit();
					editor.putBoolean(mApplicationContext.getString(R.string.deviceRegisteredKey), false);
					editor.commit();
					return login(params);
				}
			}
		} catch (MalformedURLException mue) {
			// already checked for this!
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

		return new AccountStatus(AccountStatus.Status.FAILED_LOGIN);
	}
	
	private AccountStatus.Status registerDevice(String serverURL, UrlEncodedFormEntity authParams) {
		try {
			DefaultHttpClient httpClient = HttpClientManager.getInstance(mApplicationContext).getHttpClient();
			HttpPost register = new HttpPost(new URL(new URL(serverURL), "api/devices").toURI());
			register.setEntity(authParams);
			HttpResponse registrationResponse = httpClient.execute(register);
			if (registrationResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject jsonObject = new JSONObject(EntityUtils.toString(registrationResponse.getEntity()));
				String token = jsonObject.getString("registered");
				if (token.equalsIgnoreCase("true")) {
					// This device has already been registered and approved, login
					Editor editor = PreferenceManager.getDefaultSharedPreferences(mApplicationContext).edit();
					editor.putBoolean(mApplicationContext.getString(R.string.deviceRegisteredKey), true);
					editor.commit();
					return AccountStatus.Status.ALREADY_REGISTERED;
				} else {
					// device registration has been submitted
					return AccountStatus.Status.SUCCESSFUL_REGISTRATION; //new AccountStatus(AccountStatus.Status.SUCCESSFUL_REGISTRATION, new ArrayList<Integer>(), new ArrayList<String>(), jsonObject);
				}
			}
		} catch (MalformedURLException mue) {
			// already checked for this!
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
		
		return AccountStatus.Status.FAILED_LOGIN; //new AccountStatus(AccountStatus.Status.FAILED_LOGIN);
	}
}
