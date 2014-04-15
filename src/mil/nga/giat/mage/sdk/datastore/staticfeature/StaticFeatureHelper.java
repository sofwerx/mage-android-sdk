package mil.nga.giat.mage.sdk.datastore.staticfeature;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.DaoHelper;
import mil.nga.giat.mage.sdk.datastore.layer.Layer;
import mil.nga.giat.mage.sdk.event.IEventDispatcher;
import mil.nga.giat.mage.sdk.event.IStaticFeatureEventListener;
import mil.nga.giat.mage.sdk.exceptions.StaticFeatureException;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

public class StaticFeatureHelper extends DaoHelper<StaticFeature> implements IEventDispatcher<IStaticFeatureEventListener> {

	private static final String LOG_NAME = StaticFeatureHelper.class.getName();

	private final Dao<StaticFeature, Long> staticFeatureDao;
	private final Dao<StaticFeatureGeometry, Long> staticFeatureGeometryDao;

	private Collection<IStaticFeatureEventListener> listeners = new ArrayList<IStaticFeatureEventListener>();

	/**
	 * Singleton.
	 */
	private static StaticFeatureHelper mStaticFeatureHelper;

	/**
	 * Use of a Singleton here ensures that an excessive amount of DAOs are not
	 * created.
	 * 
	 * @param context
	 *            Application Context
	 * @return A fully constructed and operational StaticFeatureHelper.
	 */
	public static StaticFeatureHelper getInstance(Context context) {
		if (mStaticFeatureHelper == null) {
			mStaticFeatureHelper = new StaticFeatureHelper(context);
		}
		return mStaticFeatureHelper;
	}

	/**
	 * Only one-per JVM. Singleton.
	 * 
	 * @param pContext
	 */
	private StaticFeatureHelper(Context pContext) {
		super(pContext);
		try {
			// Set up DAOs
			staticFeatureDao = daoStore.getStaticFeatureDao();
			staticFeatureGeometryDao = daoStore.getStaticFeatureGeometryDao();
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to communicate with StaticFeature database.", sqle);

			throw new IllegalStateException("Unable to communicate with StaticFeature database.", sqle);
		}
	}

	@Override
	public StaticFeature create(StaticFeature pStaticFeature) throws StaticFeatureException {

		StaticFeature createdStaticFeature;
		try {
			staticFeatureGeometryDao.create(pStaticFeature.getStaticFeatureGeometry());
			createdStaticFeature = staticFeatureDao.createIfNotExists(pStaticFeature);

		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "There was a problem creating the static feature: " + pStaticFeature + ".", sqle);
			throw new StaticFeatureException("There was a problem creating the static feature: " + pStaticFeature + ".", sqle);
		}

		return createdStaticFeature;
	}

	/**
	 * Set of layers that features were added to, or already belonged to.
	 * 
	 * @param pStaticFeatures
	 * @return
	 * @throws StaticFeatureException
	 */
	public Collection<Layer> createAll(Collection<StaticFeature> pStaticFeatures) throws StaticFeatureException {
		Set<Layer> layers = new HashSet<Layer>();
		for (StaticFeature staticFeature : pStaticFeatures) {
			try {
				if (read(staticFeature.getRemoteId()) == null) {
					staticFeatureGeometryDao.create(staticFeature.getStaticFeatureGeometry());
					staticFeature = staticFeatureDao.createIfNotExists(staticFeature);
					//Log.d(LOG_NAME, "created static feature: " + staticFeature);
				}
				layers.add(staticFeature.getLayer());
			} catch (SQLException sqle) {
				Log.e(LOG_NAME, "There was a problem creating the static feature: " + staticFeature + ".", sqle);
				continue;
				// TODO Throw exception?
			}
		}

		// fire the event
		for (IStaticFeatureEventListener listener : listeners) {
			listener.onStaticFeaturesCreated(layers);
		}

		return layers;
	}

	public Boolean haveLayersBeenFetchedOnce(final Context context) {
		return PreferenceHelper.getInstance(context).getValue(R.string.haveLayersBeenFetchedOnceKey, Boolean.class, R.string.haveLayersBeenFetchedOnceDefaultValue);
	}

	@Override
	public StaticFeature read(String pRemoteId) throws StaticFeatureException {
		StaticFeature staticFeature = null;
		try {
			List<StaticFeature> results = staticFeatureDao.queryBuilder().where().eq("remote_id", pRemoteId).query();
			if (results != null && results.size() > 0) {
				staticFeature = results.get(0);
			}
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
			throw new StaticFeatureException("Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
		}

		return staticFeature;
	}

	public List<StaticFeature> readAll(String pLayerId) throws StaticFeatureException {
		List<StaticFeature> staticFeatures = new ArrayList<StaticFeature>();
		try {
			List<StaticFeature> results = staticFeatureDao.queryBuilder().where().eq("layer_id", pLayerId).query();
			if (results != null) {
				staticFeatures.addAll(results);
			}
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to query for features with layer id = '" + pLayerId + "'", sqle);
			throw new StaticFeatureException("Unable to query for features with layer id = '" + pLayerId + "'", sqle);
		}

		return staticFeatures;
	}

	@Override
	public boolean addListener(IStaticFeatureEventListener listener) {
		return listeners.add(listener);
	}

	@Override
	public boolean removeListener(IStaticFeatureEventListener listener) {
		return listeners.remove(listener);
	}
}
