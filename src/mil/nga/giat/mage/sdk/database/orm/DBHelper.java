package mil.nga.giat.mage.sdk.database.orm;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DBHelper extends OrmLiteSqliteOpenHelper {

	private static DBHelper helperInstance;

	private static final String DATABASE_NAME = "mage.db";
	private static final String LOG_NAME = "mage.log";
	private static final int DATABASE_VERSION = 1;

	//DAOS
	private Dao<Observation, Integer> observationDao = null;
	private Dao<State, Integer> stateDao = null;
	private Dao<Geometry, Integer> geometryDao = null;
	private Dao<GeometryType, Integer> geometryTypeDao = null;
	private Dao<Property, Integer> propertyDao = null;
	private Dao<Attachment, Integer> attachmentDao = null;

	
	
	
	public static DBHelper getInstance(Context context) {
		if (helperInstance == null) {
			helperInstance = new DBHelper(context);
		}
		return helperInstance;
	}

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Observation.class);
			TableUtils.createTable(connectionSource, State.class);
			TableUtils.createTable(connectionSource, Geometry.class);
			TableUtils.createTable(connectionSource, GeometryType.class);
			TableUtils.createTable(connectionSource, Property.class);
			TableUtils.createTable(connectionSource, Attachment.class);
		} 
		catch (Exception e) {
			Log.e(LOG_NAME, "could not create table Observation", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		//drop tables
		
	}

	@Override
	public void close() {
		super.close();
		helperInstance = null;
	}
	
	public Dao<Observation, Integer> getObservationDao() throws SQLException {
		if (observationDao == null) {
			observationDao = getDao(Observation.class);
		}
		return observationDao;
	}
	
	public Dao<State, Integer> getStateDao() throws SQLException {
		if (stateDao == null) {
			stateDao = getDao(State.class);
		}
		return stateDao;
	}
	
	public Dao<Geometry, Integer> getGeometryDao() throws SQLException {
		if (geometryDao == null) {
			geometryDao = getDao(Geometry.class);
		}
		return geometryDao;
	}
	
	public Dao<GeometryType, Integer> getGeometryTypeDao() throws SQLException {
		if (geometryTypeDao == null) {
			geometryTypeDao = getDao(GeometryType.class);
		}
		return geometryTypeDao;
	}
	
	public Dao<Property, Integer> getPropertyDao() throws SQLException {
		if (propertyDao == null) {
			propertyDao = getDao(Property.class);
		}
		return propertyDao;
	}
	
	public Dao<Attachment, Integer> getAttachmentDao() throws SQLException {
		if (attachmentDao == null) {
			attachmentDao = getDao(Attachment.class);
		}
		return attachmentDao;
	}

}
