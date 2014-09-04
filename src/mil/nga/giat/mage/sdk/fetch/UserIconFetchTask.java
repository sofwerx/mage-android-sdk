package mil.nga.giat.mage.sdk.fetch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.datastore.user.UserHelper;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.MediaUtility;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;

public class UserIconFetchTask extends AsyncTask<User, Void, Void> {
	private static final String LOG_NAME = UserIconFetchTask.class.getName();
	
    Context context;

    public UserIconFetchTask(Context context) {
        this.context = context;
    }

    protected Void doInBackground(User... users) {
    	String token = PreferenceHelper.getInstance(context).getValue(R.string.tokenKey);
    	for (User user : users) {
    		Log.d(LOG_NAME, "Fetching icon at url: " + user.getIconUrl());
	        String urldisplay = user.getIconUrl() + "?access_token=" + token;
	        
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            Bitmap bitmap = BitmapFactory.decodeStream(in);
	            boolean isLandscape = bitmap.getWidth() > bitmap.getHeight();
	
	            int newWidth, newHeight;
	            if (isLandscape)
	            {
	                newWidth = 96;
	                newHeight = Math.round(((float) newWidth / bitmap.getWidth()) * bitmap.getHeight());
	            } else
	            {
	                newHeight = 96;
	                newWidth = Math.round(((float) newHeight / bitmap.getHeight()) * bitmap.getWidth());
	            }
	
	            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
	
	            if (resizedBitmap != bitmap)
	            	bitmap.recycle();
	        	
	            Bitmap roundedProfile = Bitmap.createBitmap(resizedBitmap.getWidth(), resizedBitmap
	                    .getHeight(), Config.ARGB_8888);
	            
	            Canvas roundedCanvas = new Canvas(roundedProfile);
	            final int color = 0xff424242;
	            final Paint paint = new Paint();
	            final Rect rect = new Rect(0, 0, roundedProfile.getWidth(), roundedProfile.getHeight());
	            final RectF rectF = new RectF(rect);
	            final float roundPx = 7.0f;
	            
	            paint.setAntiAlias(true);
	            roundedCanvas.drawARGB(0, 0, 0, 0);
	            paint.setColor(color);
	            roundedCanvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	
	            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	            roundedCanvas.drawBitmap(resizedBitmap, rect, rect, paint);
	            
	        	Bitmap icon = roundedProfile.copy(Bitmap.Config.ARGB_8888, true);
	    		roundedCanvas.drawBitmap(icon, 0, 0, null);
	    		
	    		FileOutputStream out = null;
	    		try {
	    			String localPath = MediaUtility.getUserIconDirectory() + "/" + user.getId();
	    		    out = new FileOutputStream(localPath);
	    		    icon.compress(Bitmap.CompressFormat.PNG, 90, out);
	    		    user.setLocalIconPath(localPath);
	    		    UserHelper.getInstance(context).update(user);
	    		} catch (Exception e) {
	    		    e.printStackTrace();
	    		} finally {
	    		    try {
	    		        if (out != null) {
	    		            out.close();
	    		        }
	    		    } catch (IOException e) {
	    		        e.printStackTrace();
	    		    }
	    		}
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
    	}
    	return null;
    }

}
