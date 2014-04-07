package mil.nga.giat.mage.sdk.datastore.observation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import mil.nga.giat.mage.sdk.datastore.DaoStore;
import mil.nga.giat.mage.sdk.utils.MediaUtility;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.bumptech.glide.resize.load.ImageResizer;

public class AttachmentHelper {
	
	/**
	 * This method will attempt to correctly rotate and format an image in
	 * preparation for display or uploading.
	 *    
	 * @param attachment
	 * @param context
	 */
	public static void stageForUpload(Attachment attachment, Context context) {
		try {
			File stageDir = MediaUtility.getMediaStageDirectory();
			File stagedFile = new File(stageDir, new File(attachment.getLocalPath()).getName());
		    OutputStream out = new FileOutputStream(stagedFile);
		    
		    // XXX problem with this is that other exif data is lost
		    // we either need to grab all the fields and re-write them or this should happen on the server (probably better)
		    // Only reason we are doing this is because then we guarantee images are oriented properly on all devices
			Bitmap rotated = ImageResizer.orientImage(attachment.getLocalPath(), BitmapFactory.decodeFile(attachment.getLocalPath()));
			rotated.compress(CompressFormat.JPEG, 100, out);
			
		    out.close();
		    attachment.setLocalPath(stagedFile.getAbsolutePath());
		    DaoStore.getInstance(context).getAttachmentDao().update(attachment);
		    rotated.recycle();
		} 
		catch (Exception e) {
			Log.e("Attachment", "Unable to stage for upload", e);
		}
	}
	
}
