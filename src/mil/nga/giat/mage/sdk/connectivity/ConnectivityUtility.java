package mil.nga.giat.mage.sdk.connectivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * Utility that dealing with connection like information. Connectivity, mac
 * address, etc.
 * 
 * @author wiedemannse
 * 
 */
public class ConnectivityUtility {

	/**
	 * Used to check for connectivity
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public static boolean isResolvable(String hostname) {
		try {
			InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			return false;
		}
		return true;
	}

	public static boolean canConnect(InetAddress address, int port) {
		Socket socket = new Socket();
		SocketAddress socketAddress = new InetSocketAddress(address, port);
		try {
			// Only try for 2 seconds before giving up
			socket.connect(socketAddress, 2000);
		} catch (IOException e) {
			// Something went wrong during the connection
			return false;
		} finally {
			// Always close the socket after we're done
			if (socket.isConnected()) {
				try {
					socket.close();
				} catch (IOException e) {
					// Nothing we can do here
					e.printStackTrace();
				}
			}
		}

		return true;
	}

	/**
	 * Get the Wi-Fi mac address, used to login
	 */
	public static String getMacAddress(Context context) {
		return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
	}
}