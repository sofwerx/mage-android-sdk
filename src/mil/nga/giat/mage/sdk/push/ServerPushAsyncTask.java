package mil.nga.giat.mage.sdk.push;

import android.content.Context;
import android.os.AsyncTask;

public abstract class ServerPushAsyncTask extends AsyncTask<Object, Object, Boolean> {

	protected final Context mContext;
	
	public ServerPushAsyncTask(Context context) {
		super();
		mContext = context;
	}

	
	
	
}
