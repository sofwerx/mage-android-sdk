package mil.nga.giat.mage.sdk.http.client;

import java.io.IOException;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

public class HttpClientManager {

	private HttpClientManager() {
	}

	private static HttpClientManager httpClientManager;
	private static Context mContext;

	public static HttpClientManager getInstance(final Context context) {
		if (context == null) {
			return null;
		}
		if (httpClientManager == null) {
			httpClientManager = new HttpClientManager();
		}
		mContext = context;
		return httpClientManager;
	}

	DefaultHttpClient httpClient = null;

	public DefaultHttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
			// add the token to every request!
			httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
				@Override
				public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {

					String token = PreferenceHelper.getInstance(mContext).getValue(R.string.tokenKey);
					if (token != null && !token.trim().isEmpty()) {
						request.addHeader("Bearer", token);
					}
				}
			});
		}
		return httpClient;
	}
}
