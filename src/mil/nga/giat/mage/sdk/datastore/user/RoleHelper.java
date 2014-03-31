package mil.nga.giat.mage.sdk.datastore.user;

import java.sql.SQLException;
import java.util.List;

import mil.nga.giat.mage.sdk.datastore.DBHelper;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.exceptions.ObservationException;
import mil.nga.giat.mage.sdk.exceptions.RoleException;
import mil.nga.giat.mage.sdk.exceptions.UserException;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

/**
 * 
 * A utility class for accessing {@link Role} data from the physical data model.
 * The details of ORM DAOs and Lazy Loading should not be exposed past this
 * class.
 * 
 * @author wiedemannse
 * 
 */
public class RoleHelper {

	private static final String LOG_NAME = RoleHelper.class.getName();

	// required DBHelper and DAOs for handing CRUD operations for Users
	private final DBHelper helper;
	private final Dao<Role, Long> roleDao;

	/**
	 * Singleton.
	 */
	private static RoleHelper mRoleHelper;

	/**
	 * Use of a Singleton here ensures that an excessive amount of DAOs are not
	 * created.
	 * 
	 * @param context
	 *            Application Context
	 * @return A fully constructed and operational {@link RoleHelper}.
	 */
	public static RoleHelper getInstance(Context context) {
		if (mRoleHelper == null) {
			mRoleHelper = new RoleHelper(context);
		}
		return mRoleHelper;
	}

	/**
	 * Only one-per JVM. Singleton.
	 * 
	 * @param pContext
	 */
	private RoleHelper(Context pContext) {

		helper = DBHelper.getInstance(pContext);

		try {
			roleDao = helper.getRoleDao();
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to communicate with Role database.", sqle);

			// Fatal Error!
			throw new IllegalStateException("Unable to communicate with Role database.", sqle);
		}

	}

	public Role create(Role pRole) throws UserException {

		Role createdRole;

		try {
			createdRole = roleDao.createIfNotExists(pRole);

		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "There was a problem creating the role: " + pRole);
			throw new UserException("There was a problem creating the role: " + pRole, sqle);
		}

		return createdRole;
	}
	
	/**
	 * Does a record already exist in the local DB?
	 * 
	 * @param pRemoteId
	 *            The remote ID assigned to an observation by an external entity
	 *            (server).
	 * @return If the Role exists locally.
	 * @throws ObservationException
	 *             Unable to read Observation from the database
	 */
	public Boolean exists(String pRemoteId) throws RoleException {

		Boolean exists = Boolean.FALSE;

		try {
			List<Role> results = roleDao.queryBuilder().where().eq("remote_id", pRemoteId).query();
			if (results != null && results.size() > 0) {
				exists = Boolean.TRUE;
			}
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
			throw new RoleException("Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
		}
		return exists;
	}

}
