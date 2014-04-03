package mil.nga.giat.mage.sdk.push;

import java.util.List;

import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.http.post.MageServerPostRequests;
import android.content.Context;

public class ObservationServerPushAsyncTask extends ServerPushAsyncTask {

	private static final String LOG_NAME = ObservationServerPushAsyncTask.class.getName();
	
	public ObservationServerPushAsyncTask(Context context) {
		super(context);		
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		
		ObservationHelper observationHelper = ObservationHelper.getInstance(mContext);
		List<Observation> observations = observationHelper.getDirty();				
		for(Observation observation : observations) {
			MageServerPostRequests.postObservation(observation, mContext);
		}
				
		return Boolean.TRUE;
		
	}	
	
}
