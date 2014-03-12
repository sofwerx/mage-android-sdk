package mil.nga.giat.mage.sdk.location;

import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;

/**
 * Query the device for the device's location. If userReportingFrequency is set
 * to never, the Service will listen for changes to userReportingFrequency.
 */
public class LocationService extends Service implements LocationListener {

	private final Context mContext;

	// TODO: The two settings below should be configured in a low medium high fashion from the user
	// Minimum meters between updates
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

	// Minimum milliseconds between updates
	private static final long MIN_TIME_BW_UPDATES = 1 * 1000;

	protected final LocationManager locationManager;
	
	protected boolean pollingRunning = false;
	
	public synchronized boolean isPolling() {
		return pollingRunning;
	}

	protected Thread locationPollingThread = null;

	// the last time a location was pulled form the phone.
	private long lastLocationPullTime = 0;
	
	public synchronized long getLastLocationPullTime() {
		return lastLocationPullTime;
	}

	public synchronized void setLastLocationPullTime(long lastLocationPullTime) {
		this.lastLocationPullTime = lastLocationPullTime;
	}
	
	protected boolean locationUpdatesEnabled = false;

	public synchronized boolean getLocationUpdatesEnabled() {
		return locationUpdatesEnabled;
	}
	
	// How often should the service check the settings to see if there's been a change.  Will go away if event driven!
	private static final long checkSettingsFrequencyMilliseconds = 12000;
	
	private final Handler mHandler = new Handler();

	/**
	 * FIXME: Should this take a storage utility to save locations?
	 * 
	 * @param context
	 */
	public LocationService(Context context) {
		this.mContext = context;
		this.locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		//requestLocationUpdates();
		//getLocation();
	}
	
	private void requestLocationUpdates() {
		if (locationManager != null) {
			final List<String> providers = locationManager.getAllProviders();
			if (providers != null) {
				
				if (providers.contains(LocationManager.GPS_PROVIDER)) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					locationUpdatesEnabled = true;
				}
			}
		}
	}

	private void removeLocationUpdates() {
		locationManager.removeUpdates(this);
		locationUpdatesEnabled = false;
	}

	/**
	 * Return a location or <code>null</code> is no location is available.
	 * 
	 * @return A {@link Location}.
	 */
	public Location getLocation() {
		Location location = null;

		// if GPS Enabled get Location using GPS Services
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		} else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		setLastLocationPullTime(System.currentTimeMillis());
		return location;
	}


	/**
	 * Method to show settings alert dialog On pressing Settings button will
	 * launch Settings Options
	 */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle("GPS settings");
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		setLastLocationPullTime(System.currentTimeMillis());
		saveLocation(location, "ACTIVE");
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

	/**
	 * Call this to start the location service
	 */
	public void start() {
		if(!isPolling()) {
			pollingRunning = Boolean.TRUE;
			locationPollingThread = createLocationPollingThread();
			locationPollingThread.start();
		}
	}
	
	/**
	 * Call this to stop the location service
	 */
	public void stop() {
		pollingRunning = Boolean.FALSE;
		if (locationManager != null) {
			removeLocationUpdates();
		}
	}
	
	protected Long getUserReportingFrequency() {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return Long.parseLong(sharedPreferences.getString("userReportingFrequency", "15000"));
	}
	
	private void saveLocation(Location location, String state) {
		long reportedTime = System.currentTimeMillis();
		System.out.println(state + " " + location.getLatitude() + ", " + location.getLongitude() + ", " + location.getTime() + ", " + location.getProvider() + ", " + reportedTime);
	}
	
	private Thread createLocationPollingThread() {
		return new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				
				while (isPolling()) {
					try {
						// FIXME: userReportingFrequency might change. make event driven?
						long userReportingFrequency = getUserReportingFrequency();
						// if we should pull, then do it.
						if(userReportingFrequency > 0) {
							
							// make sure the service is configured to report locations
							if(!getLocationUpdatesEnabled()) {
								mHandler.post(new Runnable() {
									public void run() {
										requestLocationUpdates();
									}
								});
							}
							
							final Location location = getLocation();
							
							if (location != null) {
								mHandler.post(new Runnable() {
									public void run() {
										saveLocation(location, "STALE");
									}
								});
							}
							long currentTime = new Date().getTime();
							long lLPullTime = getLastLocationPullTime();
							// we only need to pull if a location has not been saved in the last 'pollingInterval' seconds.
							// the location could have been saved from a motion event, or from the last time the parent loop ran
							// use local variables in order to maintain data integrity across instructions. 
							while (((lLPullTime = getLastLocationPullTime()) + (userReportingFrequency = getUserReportingFrequency()) > (currentTime = new Date().getTime())) && isPolling()) {
								// check every 12 seconds at most to check the settings
								Thread.sleep(Math.min(lLPullTime + userReportingFrequency - currentTime, checkSettingsFrequencyMilliseconds));
							}
						} else {
							// disable location updates
							mHandler.post(new Runnable() {
								public void run() {
									removeLocationUpdates();
								}
							});
							// sleep for 12 seconds
							Thread.sleep(checkSettingsFrequencyMilliseconds);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
						pollingRunning = Boolean.FALSE;
					}
				}
			}
		});

	}

}