package mil.nga.giat.mage.sdk.datastore.staticfeature;

import java.sql.SQLException;
import java.util.List;

import mil.nga.giat.mage.sdk.datastore.DaoHelper;
import mil.nga.giat.mage.sdk.exceptions.ObservationException;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

public class StaticFeatureHelper extends DaoHelper<StaticFeature> {

	private static final String LOG_NAME = StaticFeatureHelper.class.getName();

	private final Dao<StaticFeature, Long> staticFeatureDao;
	private final Dao<StaticFeatureGeometry, Long> staticFeatureGeometryDao;

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
	public StaticFeature create(StaticFeature pStaticFeature) throws ObservationException {

		StaticFeature createdStaticFeature;
		try {
			staticFeatureGeometryDao.create(pStaticFeature.getStaticFeatureGeometry());
			createdStaticFeature = staticFeatureDao.createIfNotExists(pStaticFeature);

		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "There was a problem creating the static feature: " + pStaticFeature + ".", sqle);
			throw new ObservationException("There was a problem creating the static feature: " + pStaticFeature + ".", sqle);
		}

		return createdStaticFeature;
	}

	@Override
	public StaticFeature read(String pRemoteId) throws ObservationException {
		StaticFeature staticFeature = null;
		try {
			List<StaticFeature> results = staticFeatureDao.queryBuilder().where().eq("remote_id", pRemoteId).query();
			if (results != null && results.size() > 0) {
				staticFeature = results.get(0);
			}
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
			throw new ObservationException("Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
		}

		return staticFeature;
	}
}
