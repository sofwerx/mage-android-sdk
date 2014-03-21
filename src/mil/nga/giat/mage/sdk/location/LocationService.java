package mil.nga.giat.mage.sdk.location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.common.GeometryType;
import mil.nga.giat.mage.sdk.datastore.location.LocationGeometry;
import mil.nga.giat.mage.sdk.datastore.location.LocationHelper;
import mil.nga.giat.mage.sdk.datastore.location.LocationProperty;
import mil.nga.giat.mage.sdk.event.IEventDispatcher;
import mil.nga.giat.mage.sdk.exceptions.LocationException;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.GeometryUtil;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

/**
 * Query the device for the device's location. If userReportingFrequency is set
 * to never, the Service will listen for changes to userReportingFrequency.
 * 
 * TODO: implement {@link IEventDispatcher} for location updates?
 */
public class LocationService extends Service implements LocationListener, OnSharedPreferenceChangeListener {

	private static final String LOG_NAME = LocationService.class.getName();

	private final Context mContext;

	// Minimum milliseconds between updates
	private static final long MIN_TIME_BW_UPDATES = 0 * 1000;

	protected final LocationManager locationManager;
	
	protected boolean pollingRunning = false;
	
	protected Collection<LocationListener> locationListeners = new ArrayList<LocationListener>();
	
	protected synchronized boolean isPolling() {
		return pollingRunning;
	}
	
	// False means don't re-read gps settings.  True means re-read gps settings.  Gets triggered from preference change
	protected AtomicBoolean preferenceSemaphore = new AtomicBoolean(false);

	// the last time a location was pulled form the phone.
	protected long lastLocationPullTime = 0;
	
	protected synchronized long getLastLocationPullTime() {
		return lastLocationPullTime;
	}

	protected synchronized void setLastLocationPullTime(long lastLocationPullTime) {
		this.lastLocationPullTime = lastLocationPullTime;
	}
	
	/**
	 * GPS Sensitivity Setting
	 * 
	 * @return
	 */
	private final synchronized long getMinimumDistanceChangeForUpdates() {
		return PreferenceHelper.getInstance(mContext).getValue(R.string.gpsSensitivityKey, Long.class, R.string.gpsSensitivityDefaultValue);
	}
	
	/**
	 * User Reporting Frequency Setting
	 * 
	 * @return
	 */
	protected final synchronized long getUserReportingFrequency() {
		return PreferenceHelper.getInstance(mContext).getValue(R.string.userReportingFrequencyKey, Long.class, R.string.userReportingFrequencyDefaultValue);
	}
	
	protected boolean locationUpdatesEnabled = false;

	public synchronized boolean getLocationUpdatesEnabled() {
		return locationUpdatesEnabled;
	}
	
	private final Handler mHandler = new Handler();

	/**
	 * FIXME: Should this take a storage utility to save locations?
	 * 
	 * @param context
	 */
	public LocationService(Context context) {
		this.mContext = context;
		this.locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(this);
		preferenceSemaphore.set(false);
	}
	
	public void registerOnLocationListener(LocationListener listener) {
	    locationListeners.add(listener);
	}
	
	public void unregisterOnLocationListener(LocationListener listener) {
	    locationListeners.remove(listener);
	}
	
	private void requestLocationUpdates() {
		if (locationManager != null) {
			final List<String> providers = locationManager.getAllProviders();
			if (providers != null) {
				
				if (providers.contains(LocationManager.GPS_PROVIDER)) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, getMinimumDistanceChangeForUpdates(), this);
					locationUpdatesEnabled = true;
				}
				
				if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, getMinimumDistanceChangeForUpdates(), this);
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
		}
		if(location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
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
		if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
			setLastLocationPullTime(System.currentTimeMillis());
			saveLocation(location, "ACTIVE");
		}
		
		for (LocationListener listener : locationListeners) {
		    listener.onLocationChanged(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
       for (LocationListener listener : locationListeners) {
            listener.onProviderDisabled(provider);
        }
	}

	@Override
	public void onProviderEnabled(String provider) {
       for (LocationListener listener : locationListeners) {
            listener.onProviderEnabled(provider);
        }
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
       for (LocationListener listener : locationListeners) {
            listener.onStatusChanged(provider, status, extras);
        }
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
			createLocationPollingThread().start();
		}
	}
	
	/**
	 * Call this to stop the location service
	 */
	public void stop() {
		pollingRunning = Boolean.FALSE;
		if (locationManager != null) {
			synchronized (preferenceSemaphore) {
				preferenceSemaphore.notifyAll();
			}
			removeLocationUpdates();
		}
	}
	
	// TODO: Should this be in an AsyncTask?
	private void saveLocation(Location location, String state) {
		// TODO: check that location timestamp is not 0!

		// INTEGRATION WITH LOCATION DATASTORE
		LocationHelper locationHelper = LocationHelper.getInstance(mContext);

		// build properties
		Collection<LocationProperty> locationProperties = new ArrayList<LocationProperty>();
		LocationProperty reportedTime = new LocationProperty("REPORTED_TIME", String.valueOf(System.currentTimeMillis()));
		locationProperties.add(reportedTime);

		// build geometry
		String coordinages = GeometryUtil.generate(location.getLatitude(), location.getLongitude());
		LocationGeometry locationGeometry = new LocationGeometry(coordinages, new GeometryType("point"));

		// build location
		mil.nga.giat.mage.sdk.datastore.location.Location loc = new mil.nga.giat.mage.sdk.datastore.location.Location("Feature", locationProperties, locationGeometry);

		loc.setLocationGeometry(locationGeometry);
		loc.setProperties(locationProperties);

		// save the location
		try {
			locationHelper.createLocation(loc);
		} catch (LocationException le) {
			// TODO: is this good enough?
			Log.w(LOG_NAME, "Unable to record current location locally!", le);
		}

		Log.d(LOG_NAME, "A Current Active User exists." + loc);
	}
	
	/**
	 * Polls for locations at time specified by the settings.
	 * 
	 * @return
	 */
	private Thread createLocationPollingThread() {
		return new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				
				while (isPolling()) {
					try {
						long userReportingFrequency = getUserReportingFrequency();
						// if we should pull, then do it.
						if(userReportingFrequency > 0) {
							
							// make sure the service is configured to report locations
							if(!getLocationUpdatesEnabled()) {
								mHandler.post(new Runnable() {
									public void run() {
										removeLocationUpdates();
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
								synchronized (preferenceSemaphore) {
									preferenceSemaphore.wait(lLPullTime + userReportingFrequency - currentTime);
									// this means we need to re-read the gps sensitivity
									if(preferenceSemaphore.get() == true) {
										break;
									}
								}
							}
							synchronized (preferenceSemaphore) {
								preferenceSemaphore.set(false);
							}
						} else {
							// disable location updates
							mHandler.post(new Runnable() {
								public void run() {
									removeLocationUpdates();
								}
							});
							
							synchronized (preferenceSemaphore) {
								preferenceSemaphore.wait();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
						pollingRunning = Boolean.FALSE;
					}
				}
			}
		});

	}

	/**
	 * Will alert the polling thread that changes have been made
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equalsIgnoreCase(mContext.getString(R.string.gpsSensitivityKey))) {
			synchronized (preferenceSemaphore) {
				// this will cause the polling-thread to reset the gps sensitivity
				locationUpdatesEnabled = false;
				preferenceSemaphore.set(true);
				preferenceSemaphore.notifyAll();
			}
		} else if (key.equalsIgnoreCase(mContext.getString(R.string.userReportingFrequencyKey))) {
			synchronized (preferenceSemaphore) {
				preferenceSemaphore.notifyAll();
			}
		}
	}

}