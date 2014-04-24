package mil.nga.giat.mage.sdk.push;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.event.IObservationEventListener;
import mil.nga.giat.mage.sdk.http.post.MageServerPostRequests;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import android.content.Context;
import android.util.Log;

public class ObservationServerPushAsyncTask extends ServerPushAsyncTask implements IObservationEventListener {

	private static final String LOG_NAME = ObservationServerPushAsyncTask.class.getName();

	// in milliseconds
	private long pushFrequency;

	protected AtomicBoolean pushSemaphore = new AtomicBoolean(false);
	
	public ObservationServerPushAsyncTask(Context context) {
		super(context);
		pushFrequency = getObservationPushFrequency();
        ObservationHelper.getInstance(context).addListener(this);
	}

	protected final long getObservationPushFrequency() {
		return PreferenceHelper.getInstance(context).getValue(R.string.observationPushFrequencyKey, Long.class, R.string.observationPushFrequencyDefaultValue);
	}

	@Override
	protected Boolean doInBackground(Object... params) {

		Boolean status = Boolean.TRUE;
		while (Status.RUNNING.equals(getStatus()) && !isCancelled()) {

			if (isConnected) {
				pushFrequency = getObservationPushFrequency();

				// push dirty observations
				ObservationHelper observationHelper = ObservationHelper.getInstance(context);
				List<Observation> observations = observationHelper.getDirty();
				for (Observation observation : observations) {

					// TODO : Is this the right thing to do?
					if (isCancelled()) {
						break;
					}
					Log.d(LOG_NAME, "Pushing observation with id: " + observation.getId());
					observation = MageServerPostRequests.postObservation(observation, context);
					Log.d(LOG_NAME, "Pushed observation with remote_id: " + observation.getRemoteId());
				}

//				// push dirty attachments
//				List<Attachment> attachments = observationHelper.getDirtyAttachments();
//				for (Attachment attachment : attachments) {
//
//					// TODO : Is this the right thing to do?
//					if (isCancelled()) {
//						break;
//					}
//					if (attachment.getObservation().getRemoteId() != null) {
//						Log.i(LOG_NAME, "Scheduling attachment: " + attachment.getId());
//						Intent attachmentIntent = new Intent(context, AttachmentIntentService.class);
//						attachmentIntent.putExtra(AttachmentIntentService.ATTACHMENT_ID, attachment.getId());
//						context.startService(attachmentIntent);
//					}
//				}
			} else {
				Log.d(LOG_NAME, "The device is currently disconnected. Can't push observations.");
				pushFrequency = Math.min(pushFrequency * 2, 30 * 60 * 1000);
			}
			long lastFetchTime = new Date().getTime();
			long currentTime = new Date().getTime();

			try {
				while (lastFetchTime + pushFrequency > (currentTime = new Date().getTime())) {
					synchronized (pushSemaphore) {
						Log.d(LOG_NAME, "Observation push sleeping for " + (lastFetchTime + pushFrequency - currentTime) + "ms.");
						pushSemaphore.wait(lastFetchTime + pushFrequency - currentTime);
						if (pushSemaphore.get() == true) {
							break;
						}
					}
				}
				synchronized (pushSemaphore) {
					pushSemaphore.set(false);
				}
			} catch (InterruptedException ie) {
				Log.w("Interupted.  Unable to sleep " + pushFrequency, ie);
				// TODO: should cancel the AsyncTask?
				cancel(Boolean.TRUE);
				status = Boolean.FALSE;
			} finally {
				isConnected = ConnectivityUtility.isOnline(context);
			}
		}
		return status;
	}

	@Override
	public void onObservationCreated(Collection<Observation> observations) {
		for (Observation observation : observations) {
			if(observation.isDirty()) {
				synchronized (pushSemaphore) {
					pushSemaphore.set(true);
					pushSemaphore.notifyAll();
				}
				break;
			}
		}
	}

	@Override
	public void onObservationUpdated(Observation observation) {
		if(observation.isDirty()) {
			synchronized (pushSemaphore) {
				pushSemaphore.set(true);
				pushSemaphore.notifyAll();
			}
		}
	}

	@Override
	public void onObservationDeleted(Observation observation) {
		// TODO Auto-generated method stub
		
	}
}
