package mil.nga.giat.mage.sdk.http.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.event.IEventDispatcher;
import mil.nga.giat.mage.sdk.event.IEventListener;
import mil.nga.giat.mage.sdk.event.IUserEventListener;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.util.Log;

/**
 * Always use the {@link HttpClientManager#getHttpClient()} for making ALL
 * requests to the server. This class adds request and response interceptors to
 * pass things like a token and handle errors like 403 and 401.
 * 
 * @author wiedemannse
 * 
 */
public class HttpClientManager implements IEventDispatcher<Void> {

	private static final String LOG_NAME = HttpClientManager.class.getName();

	private static Collection<IUserEventListener> listeners = new ArrayList<IUserEventListener>();

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

	private DefaultHttpClient httpClient = null;

	public DefaultHttpClient getHttpClient() {
		if (httpClient == null) {
			BasicHttpParams params = new BasicHttpParams();
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			// do not register http! only https
			// schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
			schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
			// needs to be thread safe!
			ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
			httpClient = new DefaultHttpClient(cm, params);
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 25000);
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			// add the token to every request!
			httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
				@Override
				public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {

					String token = PreferenceHelper.getInstance(mContext).getValue(R.string.tokenKey);
					if (token != null && !token.trim().isEmpty()) {
						request.addHeader("Authorization", "Bearer " + token);
					}
				}
			});
			httpClient.addResponseInterceptor(new HttpResponseInterceptor() {

				@Override
				public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_FORBIDDEN || statusCode == HttpStatus.SC_UNAUTHORIZED) {
						for (IUserEventListener listener : listeners) {
							listener.onTokenExpired();
						}
						Log.w(LOG_NAME, "TOKEN EXPIRED");
						return;
					} else if(statusCode == HttpStatus.SC_NOT_FOUND) {
						Log.w(LOG_NAME, "404 Not Found.");
					}
				}
			});
		}
		return httpClient;
	}

	@Override
	public boolean addListener(IEventListener<Void> listener) {
		return listeners.add((IUserEventListener) listener);
	}

	@Override
	public boolean removeListener(IEventListener<Void> listener) {
		return listeners.remove((IUserEventListener) listener);
	}
}
