package mil.nga.giat.mage.sdk;

import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.connectivity.NetworkChangeReceiver;
import mil.nga.giat.mage.sdk.event.connectivity.IConnectivityEventListener;
import mil.nga.giat.mage.sdk.event.user.IUserEventListener;
import android.content.Context;
import android.os.AsyncTask;

public abstract class ServerAsyncTask extends AsyncTask<Object, Object, Boolean> implements IConnectivityEventListener, IUserEventListener {

	protected final Context mContext;
	protected Boolean isConnected = Boolean.TRUE;
	
	/**
	 * Construct task with a Context.
	 * 
	 * @param context
	 */
	public ServerAsyncTask(Context context) {
		super();

		//initialize this task's Context
		mContext = context;
		
		//set up initial connection state
		isConnected = ConnectivityUtility.isOnline(context);
		//enable connectivity event handling
		NetworkChangeReceiver.getInstance().addListener(this);
	}

	@Override
	public void onError(Throwable error) {
				
	}

	@Override
	public void onAllDisconnected() {
		isConnected = Boolean.FALSE;
	}

	@Override
	public void onAnyConnected() {
		isConnected = Boolean.TRUE;
	}

	@Override
	public void onWifiConnected() {
		//if more granular connectivity management is ever needed.  i.e. for attachments?  		
	}

	@Override
	public void onWifiDisconnected() {
		//if more granular connectivity management is ever needed.  i.e. for attachments?		
	}

	@Override
	public void onMobileDataConnected() {
		//if more granular connectivity management is ever needed.  i.e. for attachments?		
	}

	@Override
	public void onMobileDataDisconnected() {
		//if more granular connectivity management is ever needed.  i.e. for attachments?	
	}

	@Override
	public void onTokenExpired() {
		cancel(true);
	}
	
	public void destroy() {
		cancel(true);
	}
}
