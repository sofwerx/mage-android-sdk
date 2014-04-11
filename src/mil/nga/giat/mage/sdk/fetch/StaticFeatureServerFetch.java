package mil.nga.giat.mage.sdk.fetch;

import java.util.List;

import mil.nga.giat.mage.sdk.datastore.layer.Layer;
import mil.nga.giat.mage.sdk.datastore.layer.LayerHelper;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeatureHelper;
import mil.nga.giat.mage.sdk.exceptions.LayerException;
import mil.nga.giat.mage.sdk.exceptions.StaticFeatureException;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import android.content.Context;
import android.util.Log;

public class StaticFeatureServerFetch extends AbstractServerFetch {

	public StaticFeatureServerFetch(Context context) {
		super(context);
	}

	private static final String LOG_NAME = StaticFeatureServerFetch.class.getName();
	
	public void fetch() {

		StaticFeatureHelper staticFeatureHelper = StaticFeatureHelper.getInstance(mContext);
		LayerHelper layerHelper = LayerHelper.getInstance(mContext);
		
		List<Layer> layers = MageServerGetRequests.getLayers(mContext);
		try {
			layerHelper.createAll(layers);
			
			// get ALL the layers
			layers = layerHelper.readAll();
			for (Layer layer : layers) {
				if(layer.getType().equalsIgnoreCase("external")) {
					try {
						staticFeatureHelper.createAll(MageServerGetRequests.getStaticFeatures(mContext, layer));
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
}
