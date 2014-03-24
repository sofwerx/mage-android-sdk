package mil.nga.giat.mage.sdk.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.exceptions.LoginException;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.ConnectivityUtility;

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
			return new AccountStatus(Boolean.FALSE, errorIndices, errorMessages);
		}

		String macAddress = ConnectivityUtility.getMacAddress(mApplicationContext);
		if (macAddress == null) {
			List<Integer> errorIndices = new ArrayList<Integer>();
			errorIndices.add(2);
			List<String> errorMessages = new ArrayList<String>();
			errorMessages.add("No mac address found on device");
			return new AccountStatus(Boolean.FALSE, errorIndices, errorMessages);
		}

		// is server a valid URL? (already checked username and password)
		try {
			URL sURL = new URL(serverURL);
			PreferenceHelper.getInstance(mApplicationContext).initializeRemote(sURL);
		} catch (MalformedURLException e) {
			List<Integer> errorIndices = new ArrayList<Integer>();
			errorIndices.add(2);
			List<String> errorMessages = new ArrayList<String>();
			errorMessages.add("Bad URL");
			return new AccountStatus(Boolean.FALSE, errorIndices, errorMessages);
		}

		try {
			DefaultHttpClient httpclient = HttpClientManager.getInstance(mApplicationContext).getHttpClient();
			HttpPost post = new HttpPost(new URL(new URL(serverURL), "api/login").toURI());

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("password", password));
			nameValuePairs.add(new BasicNameValuePair("uid", macAddress));
			nameValuePairs.add(new BasicNameValuePair("username", username));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(post);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));

				// put the token information in the shared preferences
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
				Editor editor = sharedPreferences.edit();
				editor.putString(mApplicationContext.getString(R.string.tokenKey), json.getString("token").trim()).commit();
				// FIXME : add the actually tokenExpirationDate once the server passes it back
				editor.putString(mApplicationContext.getString(R.string.tokenExpirationDateKey), new Date(new Date().getTime() + 8 * 60 * 60 * 1000).toString()).commit();
				return new AccountStatus(Boolean.TRUE, new ArrayList<Integer>(), new ArrayList<String>(), json);
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

		return new AccountStatus(Boolean.FALSE);
	}
}
