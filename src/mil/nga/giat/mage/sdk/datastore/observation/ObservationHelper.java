package mil.nga.giat.mage.sdk.datastore.observation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mil.nga.giat.mage.sdk.datastore.DBHelper;
import mil.nga.giat.mage.sdk.event.IEventDispatcher;
import mil.nga.giat.mage.sdk.event.IEventListener;
import mil.nga.giat.mage.sdk.event.observation.IObservationEventListener;
import mil.nga.giat.mage.sdk.exceptions.ObservationException;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

/**
 * A utility class for accessing {@link Observation} data from the physical data
 * model. The details of ORM DAOs and Lazy Loading should not be exposed past
 * this class.
 * 
 * @author travis
 * 
 */
public class ObservationHelper implements IEventDispatcher<Observation> {

	private static final String LOG_NAME = ObservationHelper.class.getName();

	// required DBHelper and DAOs for handing CRUD operations for Observations
	private DBHelper helper;
	private Dao<Observation, Long> observationDao;
	private Dao<ObservationGeometry, Long> geometryDao;
	private Dao<ObservationProperty, Long> propertyDao;
	private Dao<Attachment, Long> attachmentDao;

	private List<IObservationEventListener> listeners = new ArrayList<IObservationEventListener>();
	
	/**
	 * Singleton.
	 */
	private static ObservationHelper mObservationHelper;

	/**
	 * Use of a Singleton here ensures that an excessive amount of DAOs are not
	 * created.
	 * 
	 * @param context
	 *            Application Context
	 * @return A fully constructed and operational ObservationHelper.
	 */
	public static ObservationHelper getInstance(Context context) {
		if (mObservationHelper == null) {
			mObservationHelper = new ObservationHelper(context);
		}
		return mObservationHelper;
	}

	/**
	 * Only one-per JVM. Singleton.
	 * 
	 * @param pContext
	 */
	private ObservationHelper(Context pContext) {

		helper = DBHelper.getInstance(pContext);
		try {
			// Set up DAOs
			observationDao = helper.getObservationDao();
			geometryDao = helper.getGeometryDao();
			propertyDao = helper.getPropertyDao();
			attachmentDao = helper.getAttachmentDao();
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to communicate " + "with Observation database.", sqle);

			// Fatal Error!
			throw new IllegalStateException("Unable to communicate " + "with Observation database.", sqle);
		}

	}

	/**
	 * This utility method abstracts the complexities of persisting a new
	 * Observation. All the caller needs to to is construct an Observation
	 * object and call the appropriate setters.
	 * 
	 * @param pObservation
	 *            A constructed Observation.
	 * @return A fully constructed Observation complete with database primary
	 *         keys.
	 * @throws ObservationException
	 *             If the Observation being created violates any database
	 *             constraints.
	 */
	public Observation createObservation(Observation pObservation) throws ObservationException {

		Observation createdObservation;

		// Now we try and create the Observation structure.
		try {

			// create Observation geometry.
			geometryDao.create(pObservation.getObservationGeometry());

			// create the Observation.
			createdObservation = observationDao.createIfNotExists(pObservation);

			// create Observation properties.
			Collection<ObservationProperty> properties = pObservation.getProperties();
			if (properties != null) {
				for (ObservationProperty property : properties) {
					property.setObservation(createdObservation);
					propertyDao.create(property);
				}
			}

			// create Observation attachments.
			Collection<Attachment> attachments = pObservation.getAttachments();
			if (attachments != null) {
				for (Attachment attachment : attachments) {
					attachment.setObservation(createdObservation);
					attachmentDao.create(attachment);
				}
			}

		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "There was a problem creating the observation: " + pObservation + ".", sqle);
			throw new ObservationException("There was a problem creating the observation: " + pObservation + ".", sqle);
		}
		
		// fire the event
		for (IObservationEventListener listener : listeners) {
			listener.onObservationCreated(createdObservation);
		}
		return createdObservation;

	}

	/**
	 * Read an Observation from the data-store
	 * 
	 * @param pPrimaryKey
	 *            The primary key of the Obervation to read.
	 * @return A fully constructed Observation.
	 * @throws OrmException
	 *             If there was an error reading the Observation from the
	 *             database.
	 */
	public Observation readObservation(Long pPrimaryKey) throws ObservationException {
		Observation observation;
		try {
			// NOTE: Observation Collections are set up to be 'eager'. Any
			// future collections
			// added to the Observation class needs to be eager as well.
			observation = observationDao.queryForId(pPrimaryKey);
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to read Observation: " + pPrimaryKey, sqle);
			throw new ObservationException("Unable to read Observation: " + pPrimaryKey, sqle);
		}
		return observation;
	}

	/**
	 * Deletes an Observation. This will also delete an Observation's child
	 * Attachments, child Properties and Geometry data.
	 * 
	 * @param pPrimaryKey
	 * @throws OrmException
	 */
	public void deleteObservation(Long pPrimaryKey) throws ObservationException {
		try {
			// read the full Observation in
			Observation observation = observationDao.queryForId(pPrimaryKey);

			// delete Observation properties.
			Collection<ObservationProperty> properties = observation.getProperties();
			if (properties != null) {
				for (ObservationProperty property : properties) {
					propertyDao.deleteById(property.getPk_id());
				}
			}

			// delete Observation attachments.
			Collection<Attachment> attachments = observation.getAttachments();
			if (attachments != null) {
				for (Attachment attachment : attachments) {
					attachmentDao.deleteById(attachment.getPk_id());
				}
			}

			// delete Geometry (but not corresponding GeometryType).
			geometryDao.deleteById(observation.getObservationGeometry().getPk_id());

			// finally, delete the Observation.
			observationDao.deleteById(pPrimaryKey);
			
			for (IObservationEventListener listener : listeners) {
				listener.onObservationDeleted(observation);
			}
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to delete Observation: " + pPrimaryKey, sqle);
			throw new ObservationException("Unable to delete Observation: " + pPrimaryKey, sqle);
		}
	}

	@Override
	public boolean addListener(IEventListener<Observation> listener) {
		return listeners.add((IObservationEventListener) listener);
	}

	@Override
	public boolean removeListener(IEventListener<Observation> listener) {
		return listeners.remove((IObservationEventListener)listener);
	}
}
