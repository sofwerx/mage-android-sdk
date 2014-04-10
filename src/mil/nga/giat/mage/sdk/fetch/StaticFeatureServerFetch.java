package mil.nga.giat.mage.sdk.fetch;

import mil.nga.giat.mage.sdk.datastore.layer.Layer;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeature;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeatureHelper;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import android.content.Context;
import android.util.Log;

public class StaticFeatureServerFetch extends AbstractServerFetch {

	public StaticFeatureServerFetch(Context context) {
		super(context);
	}

	private static final String LOG_NAME = StaticFeatureServerFetch.class.getName();
	
	// FIXME : testing!!!!
	public void fetch() throws Exception {

		StaticFeatureHelper staticFeatureHelper = StaticFeatureHelper.getInstance(mContext);
		
		for (Layer layer : MageServerGetRequests.getLayers(mContext)) {
			if(layer.getType().equalsIgnoreCase("external")) {
				for (StaticFeature staticFeature : MageServerGetRequests.getStaticFeatures(mContext, layer)) {
					Log.i(LOG_NAME, staticFeatureHelper.create(staticFeature).toString());
				}
			}
		}
	}
}
