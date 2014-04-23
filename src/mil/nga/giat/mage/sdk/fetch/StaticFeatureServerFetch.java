package mil.nga.giat.mage.sdk.fetch;

import java.util.Collection;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.layer.Layer;
import mil.nga.giat.mage.sdk.datastore.layer.LayerHelper;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeatureHelper;
import mil.nga.giat.mage.sdk.exceptions.LayerException;
import mil.nga.giat.mage.sdk.exceptions.StaticFeatureException;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class StaticFeatureServerFetch extends AbstractServerFetch {

	public StaticFeatureServerFetch(Context context) {
		super(context);
	}

	private static final String LOG_NAME = StaticFeatureServerFetch.class.getName();

	private Boolean isCanceled = Boolean.FALSE;

	public void fetch() {
	    fetch(false);
	}
	
	public void fetch(boolean force) {
        Editor sp = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
	    
		StaticFeatureHelper staticFeatureHelper = StaticFeatureHelper.getInstance(mContext);
		LayerHelper layerHelper = LayerHelper.getInstance(mContext);

        sp.putString(mContext.getString(R.string.haveLayersBeenFetchedOnceKey), "false").commit();
		Log.d(LOG_NAME, "Pulling static layers.");
		Collection<Layer> layers = MageServerGetRequests.getStaticLayers(mContext);
		try {
		    if (force) {
		        layerHelper.deleteAllStaticLayers();
		    }
		    
			layerHelper.createAll(layers);

			// set this flag for the layer manager
			sp.putString(mContext.getString(R.string.haveLayersBeenFetchedOnceKey), "true").commit();

			// get ALL the layers
			layers = layerHelper.readAll();

			for (Layer layer : layers) {
				if (isCanceled) {
					break;
				}
				if (layer.getType().equalsIgnoreCase("external") && (force || !layer.isLoaded())) {
				    try {
				        Log.i(LOG_NAME, "Loading static features for layer " + layer.getName());
				        staticFeatureHelper.createAll(layer);
				        Log.i(LOG_NAME, "Loaded static features for layer " + layer.getName());

				    } catch (StaticFeatureException e) {
				        Log.e(LOG_NAME, "Problem creating static features.", e);
				        continue;
				    }
				}
			}
		} catch (LayerException e) {
			Log.e(LOG_NAME, "Problem creating layers.", e);
		}
	}

	public void destroy() {
		isCanceled = true;
	}
}