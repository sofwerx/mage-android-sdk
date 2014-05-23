package mil.nga.giat.mage.sdk.datastore.observation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.google.common.io.Files;

import mil.nga.giat.mage.sdk.datastore.DaoStore;
import mil.nga.giat.mage.sdk.utils.MediaUtility;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class AttachmentHelper {
	
	private static final String LOG_NAME = AttachmentHelper.class.getName();
	/**
	 * This method will attempt to correctly rotate and format an image in
	 * preparation for display or uploading.
	 * 
	 * @param attachment
	 * @param context
	 */
	public static void stageForUpload(Attachment attachment, Context context) throws Exception {
		//boolean compressed = false;
		File stageDir = MediaUtility.getMediaStageDirectory();
		File inFile = new File(attachment.getLocalPath());
		File stagedFile = new File(stageDir, new File(attachment.getLocalPath()).getName());
		/*
		// TODO : only rotate image media. ignore videos...
		if (MediaUtility.isImage(stagedFile.getAbsolutePath())) {
			
			Log.d(LOG_NAME, "Staging file: " + stagedFile.getAbsolutePath());
			Log.d(LOG_NAME, "Local path is: " + attachment.getLocalPath());
			if (stagedFile.getAbsolutePath().equalsIgnoreCase(attachment.getLocalPath())) {
				Log.d(LOG_NAME, "Nothing to do, already moved, returning");
				return;
			}
			
			OutputStream out = new FileOutputStream(stagedFile);
		    // XXX problem with this is that other exif data is lost
		    // we either need to grab all the fields and re-write them or this should happen on the server (probably better)
		    // Only reason we are doing this is because then we guarantee images are oriented properly on all devices
			Bitmap bitmap = BitmapFactory.decodeFile(attachment.getLocalPath());
			Log.d(LOG_NAME, "Decoded file into a bitmap");
			//Bitmap rotated = ImageResizer.orientImage(attachment.getLocalPath(), ));
			compressed = bitmap.compress(CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			bitmap.recycle();
		}
		
		Log.d(LOG_NAME, "Compressed and moved the file? " + compressed);
		
		if (compressed) {
			attachment.setLocalPath(stagedFile.getAbsolutePath());
			DaoStore.getInstance(context).getAttachmentDao().update(attachment);
		}*/
		Files.copy(inFile, stagedFile);
		attachment.setLocalPath(stagedFile.getAbsolutePath());
		DaoStore.getInstance(context).getAttachmentDao().update(attachment);
	}
	
}
