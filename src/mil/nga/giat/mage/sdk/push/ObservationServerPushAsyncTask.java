package mil.nga.giat.mage.sdk.push;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.AttachmentHelper;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.http.post.MageServerPostRequests;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import android.content.Context;
import android.util.Log;

public class ObservationServerPushAsyncTask extends ServerPushAsyncTask {

	private static final String LOG_NAME = ObservationServerPushAsyncTask.class.getName();
	
	// in milliseconds
	private long pushFrequency;
	
	public ObservationServerPushAsyncTask(Context context) {
		super(context);
		pushFrequency = getObservationPushFrequency();
	}
	
	protected final long getObservationPushFrequency() {
		return PreferenceHelper.getInstance(mContext).getValue(R.string.observationPushFrequencyKey, Long.class, R.string.observationPushFrequencyDefaultValue);
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		
		Boolean status = Boolean.TRUE;
		while (Status.RUNNING.equals(getStatus()) && !isCancelled()) {

			if (isConnected) {
				pushFrequency = getObservationPushFrequency();
				ObservationHelper observationHelper = ObservationHelper.getInstance(mContext);
				List<Observation> observations = observationHelper.getDirty();
				for (Observation observation : observations) {
					Observation savedObservation = MageServerPostRequests.postObservation(observation, mContext);

					// sync the observation's attachments. NOTE: this can be
					// moved into a separate task if needed.
					Collection<Attachment> attachments = savedObservation.getAttachments();
					for (Attachment attachment : attachments) {
						// stage the attachment
						AttachmentHelper.stageForUpload(attachment, mContext);

						// persist the attachment
						MageServerPostRequests.postAttachment(attachment, mContext);
					}
				}
			} 
			else {
				Log.d(LOG_NAME, "The device is currently disconnected. Nothing to push.");
				pushFrequency = Math.min(pushFrequency*2, 10*60*1000);
			}
			long lastFetchTime = new Date().getTime();
			long currentTime = new Date().getTime();

			try {
				while (lastFetchTime + pushFrequency > (currentTime = new Date().getTime())) {
					Log.d(LOG_NAME, "Observation push sleeping for " + (lastFetchTime + pushFrequency - currentTime) + "ms.");
					Thread.sleep(lastFetchTime + pushFrequency - currentTime);
				}
			} catch (InterruptedException ie) {
				Log.w("Interupted.  Unable to sleep " + pushFrequency, ie);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
				status = Boolean.FALSE;
			} finally {
				isConnected = ConnectivityUtility.isOnline(mContext);
			}
		}
		return status;
	}
}
