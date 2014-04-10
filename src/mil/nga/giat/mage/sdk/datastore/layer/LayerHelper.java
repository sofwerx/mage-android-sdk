package mil.nga.giat.mage.sdk.datastore.layer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.mage.sdk.datastore.DaoHelper;
import mil.nga.giat.mage.sdk.exceptions.LayerException;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

/**
 * A utility class for accessing {@link Layer} data from the physical data
 * model. The details of ORM DAOs and Lazy Loading should not be exposed past
 * this class.
 * 
 * @author wiedemannse
 * 
 */
public class LayerHelper extends DaoHelper<Layer> {

	private static final String LOG_NAME = LayerHelper.class.getName();

	private final Dao<Layer, Long> layerDao;

	/**
	 * Singleton.
	 */
	private static LayerHelper mLayerHelper;

	/**
	 * Use of a Singleton here ensures that an excessive amount of DAOs are not
	 * created.
	 * 
	 * @param context
	 *            Application Context
	 * @return A fully constructed and operational LocationHelper.
	 */
	public static LayerHelper getInstance(Context context) {
		if (mLayerHelper == null) {
			mLayerHelper = new LayerHelper(context);
		}
		return mLayerHelper;
	}

	/**
	 * Only one-per JVM. Singleton.
	 * 
	 * @param context
	 */
	private LayerHelper(Context context) {
		super(context);

		try {
			layerDao = daoStore.getLayerDao();
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to communicate with Layers database.", sqle);

			throw new IllegalStateException("Unable to communicate with Layers database.", sqle);
		}

	}

	public List<Layer> readAll() throws LayerException {
		List<Layer> locations = new ArrayList<Layer>();
		try {
			locations = layerDao.queryForAll();
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to read Layers", sqle);
			throw new LayerException("Unable to read Layers.", sqle);
		}
		return locations;
	}

	@Override
	public Layer create(Layer pLayer) throws LayerException {

		Layer createdLayer = null;
		try {
			createdLayer = layerDao.createIfNotExists(pLayer);
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "There was a problem creating the layer: " + pLayer + ".", sqle);
			throw new LayerException("There was a problem creating the layer: " + pLayer + ".", sqle);
		}

		return createdLayer;
	}
	
	public List<Layer> createAll(List<Layer> pLayers) throws LayerException {

		List<Layer> createdLayers = new ArrayList<Layer>();
		for (Layer layer : pLayers) {
			try {
				if(read(layer.getRemoteId()) == null) {
					createdLayers.add(layerDao.createIfNotExists(layer));					
				}
			} catch (SQLException sqle) {
				Log.e(LOG_NAME, "There was a problem creating the layer: " + layer + ".", sqle);
				// TODO  Throw exception?
			}
		}
		
		// TODO: fire event

		return createdLayers;
	}

	@Override
	public Layer read(String pRemoteId) throws LayerException {
		Layer layer = null;
		try {
			List<Layer> results = layerDao.queryBuilder().where().eq("remote_id", pRemoteId).query();
			if (results != null && results.size() > 0) {
				layer = results.get(0);
			}
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
			throw new LayerException("Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
		}

		return layer;
	}
}
