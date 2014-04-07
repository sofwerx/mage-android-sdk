package mil.nga.giat.mage.sdk.push;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.AttachmentHelper;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.http.post.MageServerPostRequests;
import android.content.Context;
import android.util.Log;

public class ObservationServerPushAsyncTask extends ServerPushAsyncTask {

	private static final String LOG_NAME = ObservationServerPushAsyncTask.class
			.getName();

	protected AtomicBoolean pushSemaphore = new AtomicBoolean(false);

	public ObservationServerPushAsyncTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Object... params) {

		Boolean status = Boolean.TRUE;

		while (Status.RUNNING.equals(getStatus()) && !isCancelled()) {

			if (IS_CONNECTED) {

				ObservationHelper observationHelper = 
						ObservationHelper.getInstance(mContext);
				List<Observation> observations = observationHelper.getDirty();
				for (Observation observation : observations) {
					Observation savedObservation = 
							MageServerPostRequests.postObservation(observation, mContext);
					
					//sync the observation's attachments.  NOTE: this can be 
					//moved into a separate task if needed.
					Collection<Attachment> attachments = savedObservation.getAttachments();
					for(Attachment attachment : attachments) {

						//stage the attachment
						AttachmentHelper.stageForUpload(attachment, mContext);
						
						//persist the attachment
						MageServerPostRequests.postAttachment(attachment, mContext);
					}					
					
				}

			} 
			else {
				Log.d(LOG_NAME, "The device is currently disconnected. Nothing to push.");
			}
			long frequency = 60000L;
			long lastFetchTime = new Date().getTime();
			long currentTime = new Date().getTime();

			try {
				while (lastFetchTime + (frequency = 60000L) > (currentTime = new Date()
						.getTime())) {
					synchronized (pushSemaphore) {
						Log.d(LOG_NAME, "Observation push sleeping for "
								+ (lastFetchTime + frequency - currentTime)
								+ "ms.");
						pushSemaphore.wait(lastFetchTime + frequency
								- currentTime);
						if (pushSemaphore.get() == true) {
							break;
						}
					}
				}
				synchronized (pushSemaphore) {
					pushSemaphore.set(false);
				}
			} 
			catch (InterruptedException ie) {
				Log.w("Interupted.  Unable to sleep " + frequency, ie);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
				status = Boolean.FALSE;
			} 
			finally {
				IS_CONNECTED = ConnectivityUtility.isOnline(mContext);
			}

		}
		return status;

	}

}
