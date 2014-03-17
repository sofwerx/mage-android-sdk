package mil.nga.giat.mage.sdk.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

public class MediaUtils {
	
	private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }
	
	public static Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException{
        InputStream input = getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > 50) ? (originalSize / 50) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }
	
	public static Bitmap orientImage(Bitmap bitmap) {
	
	// Rotate the picture based on the exif data
		try {
	        ExifInterface exif = new ExifInterface(f.getAbsolutePath());
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
	    catch (Exception e) {
	    }
	}

}
