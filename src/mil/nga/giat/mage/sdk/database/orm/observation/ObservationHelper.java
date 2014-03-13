package mil.nga.giat.mage.sdk.database.orm.observation;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.giat.mage.sdk.database.orm.DBHelper;
import mil.nga.giat.mage.sdk.database.orm.OrmException;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

/**
 * A utility class for accessing Observation data from the physical data model.
 * The details of ORM DAOs and Lazy Loading should not be exposed past this
 * class.
 * 
 * @author travis
 * 
 */
public class ObservationHelper {

	private static final String LOG_NAME = "mage.observation.log";

	// required DBHelper and DAOs for handing CRUD operations for Observations
	private DBHelper helper;
	private Dao<Observation, Long> observationDao;
	private Dao<State, Long> stateDao;
	private Dao<Geometry, Long> geometryDao;
	private Dao<GeometryType, Long> geometryTypeDao;
	private Dao<Property, Long> propertyDao;
	private Dao<Attachment, Long> attachmentDao;

	/**
	 * This Map can be used to ensure that a valid STATE is used when performing CRUD 
	 * operations on Observations.
	 */
	public static final Map<String, State> STATE_LOOKUP_MAP = new HashMap<String, State>(); 
	
	/**
	 * This Map can be used to ensure that a valid GEOMETRY TYPE is used when performing CRUD 
	 * operations on Observations.
	 */
	public static final Map<String, GeometryType> GEOMETRY_TYPE_LOOKUP_MAP = new HashMap<String, GeometryType>();
	
	/**
	 * Singleton.
	 */
	private static ObservationHelper mObservationHelper;
	
	/**
	 * Use of a Singleton here ensures that an excessive amount of DAOs are not created.
	 * @param context Application Context
	 * @return A fully constructed and operational ObservationHelper.
	 */
	public static ObservationHelper getInstance(Context context) {
		if(mObservationHelper == null) {
			mObservationHelper = new ObservationHelper(context);
		}
		return mObservationHelper;
	}
	
	/**
	 * Only one-per JVM.  Singleton.
	 * @param pContext
	 */
	private ObservationHelper(Context pContext) {

		helper = DBHelper.getInstance(pContext);
		try {
			//Set up DAOs
			observationDao = helper.getObservationDao();
			stateDao = helper.getStateDao();
			geometryDao = helper.getGeometryDao();
			geometryTypeDao = helper.getGeometryTypeDao();
			propertyDao = helper.getPropertyDao();
			attachmentDao = helper.getAttachmentDao();
						
			//initialize state values
			List<State> states = stateDao.queryForAll();
			for(State state : states) {
				STATE_LOOKUP_MAP.put(state.getState(), state);				
			}
			
			//initialize geometry type values;
			List<GeometryType> geometryTypes = geometryTypeDao.queryForAll();
			for(GeometryType geometryType : geometryTypes) {
				GEOMETRY_TYPE_LOOKUP_MAP.put(geometryType.getType(), geometryType);
			}
			
			
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to communicate "
					+ "with Observation database.", sqle);

			//Fatal Error!
			throw new IllegalStateException("Unable to communicate "
					+ "with Observation database.", sqle);
		}		

	}

	/**
	 * This utility method abstracts the complexities of persisting a new Observation.
	 * All the caller needs to to is construct an Observation object and call the
	 * appropriate setters.
	 * @param pObservation A constructed Observation.
	 * @return A fully constructed Observation complete with database primary keys.
	 * @throws OrmException If the Observation being created violates any database
	 *                      constraints.
	 */
	public Observation createObservation(Observation pObservation) throws OrmException {
				
		Observation createdObservation;
		
		//Validate State.  ORM will now allow a null State
		State state = pObservation.getState();
		if(STATE_LOOKUP_MAP.containsKey(state.getState())) {
			pObservation.setState(STATE_LOOKUP_MAP.get(state.getState()));
		}
		else {			
			Log.w(LOG_NAME,
					"The state: '" + state.getState()
							+ "' is invalid.  Valid states are "
							+ STATE_LOOKUP_MAP.keySet());
		}
		
		//Validate GeometryType.  ORM will not allow a null Geometry.
		GeometryType geometryType = pObservation.getGeometry().getGeometryType();
		if(GEOMETRY_TYPE_LOOKUP_MAP.containsKey(geometryType.getType())) {
			//This sets the GeometryType to the value already persisted in the lookup table.
			//We don't want duplicates.
			pObservation.getGeometry().setGeometryType(GEOMETRY_TYPE_LOOKUP_MAP.get(geometryType.getType()));
		}
		else {
			Log.w(LOG_NAME, "The geometry type: '" + geometryType.getType()
					+ "' is invalid.  Valid geometry types are "
					+ GEOMETRY_TYPE_LOOKUP_MAP.keySet());
		}
		
		//Now we try and create the Observation structure.  
		try {
			
			//create Observation geometry.
			geometryDao.create(pObservation.getGeometry());
			
			//create the Observation.
			createdObservation = observationDao.createIfNotExists(pObservation);
			
			//create Observation properties.
			Collection<Property> properties = pObservation.getProperties();
			if (properties != null) {
				for (Property property : properties) {
					property.setObservation(createdObservation);
					propertyDao.create(property);
				}
			}
			
			//create Observation attachments.
			Collection<Attachment> attachments = pObservation.getAttachments();
			if (attachments != null) {
				for (Attachment attachment : attachments) {
					attachment.setObservation(createdObservation);
					attachmentDao.create(attachment);
				}
			}		
			
		}
		catch(SQLException sqle) {
			Log.e(LOG_NAME,"There was a problem creating the observation: " + pObservation + ".",sqle);
			throw new OrmException("There was a problem creating the observation: " + pObservation + ".",sqle);
		}
		
		return createdObservation;
		
	}
	
	/**
	 * Read an Observation from the data-store
	 * @param pPrimaryKey The primary key of the Obervation to read.
	 * @return A fully constructed Observation.
	 * @throws OrmException If there was an error reading the Observation from the database.
	 */
	public Observation readObservation(Long pPrimaryKey) throws OrmException {
		Observation observation;
		try {
			//NOTE: Observation Collections are set up to be 'eager'.  Any future collections
			//added to the Observation class needs to be eager as well.
			observation =  observationDao.queryForId(pPrimaryKey);
		}
		catch(SQLException sqle) {
			Log.e(LOG_NAME,"Unable to read Observation: " + pPrimaryKey,sqle);
			throw new OrmException("Unable to read Observation: " + pPrimaryKey,sqle);
		}
		return observation;
	}

	/**
	 * Deletes an Observation.  This will also delete an Observation's child Attachments,
	 * child Properties and Geometry data.
	 * @param pPrimaryKey
	 * @throws OrmException
	 */
	public void deleteObservation(Long pPrimaryKey) throws OrmException {
		try {
			
			//read the full Observation in
			Observation observation = observationDao.queryForId(pPrimaryKey);
			
			//delete Observation properties.
			Collection<Property> properties = observation.getProperties();
			if (properties != null) {
				for (Property property : properties) {					
					propertyDao.deleteById(property.getPk_id());
				}
			}
			
			//delete Observation attachments.
			Collection<Attachment> attachments = observation.getAttachments();
			if (attachments != null) {
				for (Attachment attachment : attachments) {					
					attachmentDao.deleteById(attachment.getPk_id());
				}
			}
			
			//delete Geometry (but not corresponding GeometryType).
			geometryDao.deleteById(observation.getGeometry().getPk_id());
			
			//finally, delete the Observation.
			observationDao.deleteById(pPrimaryKey);
		}
		catch(SQLException sqle) {
			Log.e(LOG_NAME,"Unable to delete Observation: " + pPrimaryKey,sqle);
			throw new OrmException("Unable to delete Observation: " + pPrimaryKey,sqle);
		}
	}
	
}
