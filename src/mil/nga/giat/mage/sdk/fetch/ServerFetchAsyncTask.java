package mil.nga.giat.mage.sdk.fetch;

import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.connectivity.NetworkChangeReceiver;
import mil.nga.giat.mage.sdk.event.connectivity.IConnectivityEventListener;
import android.content.Context;
import android.os.AsyncTask;

public abstract class ServerFetchAsyncTask extends AsyncTask<Void, Void, Boolean> implements IConnectivityEventListener {

	protected final Context mContext;
	protected final NetworkChangeReceiver mNetworkChangeReceiver = new NetworkChangeReceiver();;

	//assume connected for now...
	public Boolean IS_CONNECTED = Boolean.TRUE;
	
	/**
	 * Construct task with a Context.
	 * 
	 * @param context
	 */
	public ServerFetchAsyncTask(Context context) {
		super();

		//initialize this task's Context
		mContext = context;
		
		//enable connectivity event handling
		NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
		networkChangeReceiver.addListener(this);
		
		//set up initial connection state
		IS_CONNECTED = ConnectivityUtility.isOnline(context);
		
	}

	@Override
	public void onComplete(Void item) {		
		
	}

	@Override
	public void onError(Throwable error) {
				
	}

	@Override
	public void onAllDisconnected() {
		IS_CONNECTED = Boolean.FALSE;
	}

	@Override
	public void onAnyConnected() {
		IS_CONNECTED = Boolean.TRUE;
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
	
}
