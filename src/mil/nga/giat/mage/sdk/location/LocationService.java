package mil.nga.giat.mage.sdk.location;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

/**
 * Query the device for a location
 */
public class LocationService extends Service implements LocationListener {

	private final Context mContext;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// Minimum meters between updates
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;

	// Minimum milliseconds between updates
	private static final long MIN_TIME_BW_UPDATES = 15 * 1000;

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public LocationService(Context context) {
		this.mContext = context;
		getLocation();
	}

	/**
	 * Return a location or <code>null</code> is no location is avaliable.
	 * 
	 * @return A {@link Location}.
	 */
	public Location getLocation() {
		Location location = null;
		locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

		// getting GPS status
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// if GPS Enabled get Location using GPS Services
		if (isGPSEnabled) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		return location;
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	/**
	 * Method to check if GPS is enabled
	 * 
	 * @return Is GSP enabled?
	 */
	public boolean isGPSEnabled() {
		return this.isGPSEnabled;
	}

	/**
	 * Method to show settings alert dialog On pressing Settings button will
	 * launch Settings Options
	 */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle("GPS settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}