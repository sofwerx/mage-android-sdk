package mil.nga.giat.mage.sdk.glide;

import java.net.MalformedURLException;
import java.net.URL;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.loader.model.GenericLoaderFactory;
import com.bumptech.glide.loader.model.ModelLoader;
import com.bumptech.glide.loader.model.ModelLoaderFactory;
import com.bumptech.glide.loader.stream.StreamLoader;
import com.bumptech.glide.volley.VolleyUrlLoader;

public class MageUrlLoader extends VolleyUrlLoader {
	
	public static class Factory implements ModelLoaderFactory<URL> {
        private RequestQueue requestQueue;

        public Factory() { }

        public Factory(RequestQueue requestQueue) {
            this.requestQueue = requestQueue;
        }

        protected RequestQueue getRequestQueue(Context context) {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(context);
            }
            return requestQueue;
        }

        @Override
        public ModelLoader<URL> build(Context context, GenericLoaderFactory factories) {
            return new MageUrlLoader(getRequestQueue(context), context);
        }

        @Override
        public Class<? extends ModelLoader<URL>> loaderClass() {
            return MageUrlLoader.class;
        }

        @Override
        public void teardown() {
            if (requestQueue != null) {
                requestQueue.stop();
                requestQueue.cancelAll(new RequestQueue.RequestFilter() {
					
                    @Override
                    public boolean apply(Request<?> request) {
                        return true; 
                    }
                });
                requestQueue = null;
            }
        }
    }
	
	private Context context;

	public MageUrlLoader(RequestQueue requestQueue, Context context) {
		super(requestQueue);
		this.context = context.getApplicationContext();
	}
	
	@Override
    public StreamLoader getStreamLoader(URL url, int width, int height) {
		String s = url.toString();
		String token = PreferenceHelper.getInstance(context).getValue(R.string.tokenKey);
		s += "?access_token=" + token + "&size=" + (width < height ? height : width);
		try {
			url = new URL(s);
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		}
		return super.getStreamLoader(url, width, height);
    }

    @Override
    public String getId(URL url) {
        return url.toString();
    }

}
