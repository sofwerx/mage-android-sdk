package mil.nga.giat.mage.sdk.service;

import java.util.List;

import mil.nga.giat.mage.sdk.connectivity.ConnectivityUtility;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AttachmentAlarmReceiver extends BroadcastReceiver {
	public static final int REQUEST_CODE = 91837;
	private static final String LOG_NAME = AttachmentAlarmReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_NAME, "Alarm fired to push new attachments");
		if (!ConnectivityUtility.isOnline(context)) {
			Log.d(LOG_NAME, "Not connected.  Will try later.");
			return;
		}
		List<Attachment> attachments = ObservationHelper.getInstance(context).getDirtyAttachments();
		for (Attachment attachment : attachments) {

			if (attachment.getObservation().getRemoteId() != null) {
				Log.i(LOG_NAME, "Scheduling attachment: " + attachment.getId());
				Intent attachmentIntent = new Intent(context, AttachmentIntentService.class);
				attachmentIntent.putExtra(AttachmentIntentService.ATTACHMENT_ID, attachment.getId());
				context.startService(attachmentIntent);
			}
		}
	}

}
