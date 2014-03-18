package mil.nga.giat.mage.sdk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class MediaUtils {
	
	private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }
	
	public static String getFilename(Uri uri, Context c) 
	{
	    String fileName = null;
	    String scheme = uri.getScheme();
	    if (scheme.equals("file")) {
	        fileName = uri.getPath();
	    }
	    else if (scheme.equals("content")) {
	    	Cursor cursor = null;
	    	  try { 
	    	    String[] proj = { MediaStore.Images.Media.DATA };
	    	    cursor = c.getContentResolver().query(uri,  proj, null, null, null);
	    	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    	    cursor.moveToFirst();
	    	    return cursor.getString(column_index);
	    	  } finally {
	    	    if (cursor != null) {
	    	      cursor.close();
	    	    }
	    	  }
	    }
	    return fileName;
	}
	
	public static Bitmap getThumbnailFromContent(Uri uri, int thumbsize, Context c) throws FileNotFoundException, IOException {
		InputStream is = c.getContentResolver().openInputStream(uri);
		return MediaUtils.getThumbnail(is, thumbsize, getFilename(uri, c));
	}
	
	public static Bitmap getThumbnail(File file, int thumbsize) throws FileNotFoundException, IOException {
		FileInputStream input = new FileInputStream(file);
		return MediaUtils.getThumbnail(input, thumbsize, file.getAbsolutePath());
    }
	
	// TODO: this will only allow thumbnails based on the max of width or height.  We should allow choosing either height or width.
	// Be aware that this method also rotates the image so height/width potentially could change and I think we should probably
	// not rotate until it is resized to save memory
	public static Bitmap getThumbnail(InputStream input, int thumbsize, String absoluteFilePath) throws FileNotFoundException, IOException {
		BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        //input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > thumbsize) ? (originalSize / thumbsize) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        input = new FileInputStream(absoluteFilePath);
        
        Bitmap bitmap = MediaUtils.orientBitmap(BitmapFactory.decodeStream(input, null, bitmapOptions), absoluteFilePath);
        input.close();
        return bitmap;
	}
	
	public static Bitmap orientBitmap(Bitmap bitmap, String absoluteFilePath) throws IOException {
		// Rotate the picture based on the exif data
        ExifInterface exif = new ExifInterface(absoluteFilePath);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        Matrix matrix = new Matrix();
        if (orientation == 6) {
            matrix.postRotate(90);
        }
        else if (orientation == 3) {
            matrix.postRotate(180);
        }
        else if (orientation == 8) {
            matrix.postRotate(270);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
	}
	
	public static Bitmap orientImage(File original) {
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(original), null, null);
			return orientBitmap(bitmap, original.getAbsolutePath());
		} catch (Exception e) {
			Log.e("MediaUtils", "Error loading bitmap from " + original.getAbsolutePath());
		}
		return null;
	
	}

}
