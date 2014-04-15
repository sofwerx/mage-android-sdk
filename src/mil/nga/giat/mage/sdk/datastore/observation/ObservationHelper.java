package mil.nga.giat.mage.sdk.datastore.observation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;

import mil.nga.giat.mage.sdk.datastore.DaoHelper;
import mil.nga.giat.mage.sdk.event.IEventDispatcher;
import mil.nga.giat.mage.sdk.event.IObservationEventListener;
import mil.nga.giat.mage.sdk.exceptions.ObservationException;
import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * A utility class for accessing {@link Observation} data from the physical data
 * model. The details of ORM DAOs and Lazy Loading should not be exposed past
 * this class.
 * 
 * @author travis
 * 
 */
public class ObservationHelper extends DaoHelper<Observation> implements IEventDispatcher<IObservationEventListener> {

	private static final String LOG_NAME = ObservationHelper.class.getName();

	private final Dao<Observation, Long> observationDao;
	private final Dao<ObservationGeometry, Long> observationGeometryDao;
	private final Dao<ObservationProperty, Long> observationPropertyDao;
	private final Dao<Attachment, Long> attachmentDao;

	private Collection<IObservationEventListener> listeners = new ArrayList<IObservationEventListener>();
	
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
		super(pContext);
		try {
			// Set up DAOs
			observationDao = daoStore.getObservationDao();
			observationGeometryDao = daoStore.getObservationGeometryDao();
			observationPropertyDao = daoStore.getObservationPropertyDao();
			attachmentDao = daoStore.getAttachmentDao();
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to communicate with Observation database.", sqle);

			throw new IllegalStateException("Unable to communicate with Observation database.", sqle);
		}

	}

	@Override
	public Observation create(Observation pObservation) throws ObservationException {

		Observation createdObservation;

		// Now we try and create the Observation structure.
		try {

			// create Observation geometry.
			observationGeometryDao.create(pObservation.getObservationGeometry());

			// create the Observation.
			createdObservation = observationDao.createIfNotExists(pObservation);

			// create Observation properties.
			Collection<ObservationProperty> properties = pObservation.getProperties();
			if (properties != null) {
				for (ObservationProperty property : properties) {
					property.setObservation(createdObservation);
					observationPropertyDao.create(property);
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
		ConcurrentSkipListSet<Observation> observations = new ConcurrentSkipListSet<Observation>();
		observations.add(createdObservation);
		for (IObservationEventListener listener : listeners) {
			listener.onObservationCreated(observations);
		}
		return createdObservation;
	}

	@Override
	public Observation read(String pRemoteId) throws ObservationException {
		Observation observation = null;
		try {
			List<Observation> results = observationDao.queryBuilder().where().eq("remote_id", pRemoteId).query();
			if (results != null && results.size() > 0) {
				observation = results.get(0);
			}
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
			throw new ObservationException("Unable to query for existance for remote_id = '" + pRemoteId + "'", sqle);
		}

		return observation;
	}
	
	public void update(Observation pObservation) throws ObservationException {
		try {
			observationGeometryDao.update(pObservation.getObservationGeometry());
			observationDao.update(pObservation);

			Collection<ObservationProperty> properties = pObservation.getProperties();
			if (properties != null) {
				for (ObservationProperty property : properties) {
					property.setObservation(pObservation);
					observationPropertyDao.createOrUpdate(property);
				}
			}

			Collection<Attachment> attachments = pObservation.getAttachments();
			if (attachments != null) {
				for (Attachment attachment : attachments) {
					attachment.setObservation(pObservation);
					attachmentDao.createOrUpdate(attachment);
				}
			}

		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "There was a problem updating the observation: " + pObservation + ".", sqle);
			throw new ObservationException("There was a problem updating the observation: " + pObservation + ".", sqle);
		}
		
		// fire the event
		for (IObservationEventListener listener : listeners) {
			listener.onObservationUpdated(pObservation);
		}
	}

	/**
	 * We have to realign all the foreign ids so the update works correctly
	 * 
	 * @param pNewObservation
	 * @param pOldObservation
	 * @throws ObservationException
	 */
	public Observation update(Observation pNewObservation, Observation pOldObservation) throws ObservationException {
		pNewObservation.setId(pOldObservation.getId());

		if (pNewObservation.getObservationGeometry() != null && pOldObservation.getObservationGeometry() != null) {
			pNewObservation.getObservationGeometry().setPk_id(pOldObservation.getObservationGeometry().getPk_id());
		}

		// FIXME : make this run faster?
		for (ObservationProperty op : pNewObservation.getProperties()) {
			for (ObservationProperty oop : pOldObservation.getProperties()) {
				if (op.getKey().equalsIgnoreCase(oop.getKey())) {
					op.setPk_id(oop.getPk_id());
					break;
				}
			}
		}

		// FIXME : make this run faster?
		for (Attachment a : pNewObservation.getAttachments()) {
			for (Attachment oa : pOldObservation.getAttachments()) {
				if (a.getRemoteId() != null && a.getRemoteId().equalsIgnoreCase(oa.getRemoteId())) {
					a.setId(oa.getId());
					break;
				}
			}
		}

		update(pNewObservation);
		return pNewObservation;
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
	public Observation readByPrimaryKey(Long pPrimaryKey) throws ObservationException {
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

	public Collection<Observation> readAll() throws ObservationException {
		ConcurrentSkipListSet<Observation> observations = new ConcurrentSkipListSet<Observation>();
		try {
			observations.addAll(observationDao.queryForAll());
		} catch (SQLException sqle) {
			Log.e(LOG_NAME, "Unable to read Observations", sqle);
			throw new ObservationException("Unable to read Observations.", sqle);
		}
		return observations;
	}

	/**
	 * Gets the latest last modified date.  Used when fetching.
	 * 
	 * @return
	 */
	public Date getLatestRemoteLastModified() {
		Date lastModifiedDate = new Date(0);
		QueryBuilder<Observation, Long> queryBuilder = observationDao.queryBuilder();

		try {
			queryBuilder.where().isNotNull("remote_id");
			queryBuilder.where().eq("dirty", false);
			queryBuilder.orderBy("last_modified", false);
			queryBuilder.limit(1L);
			Observation o = observationDao.queryForFirst(queryBuilder.prepare());
			if (o != null) {
				lastModifiedDate = o.getLastModified();
			}
		} catch (SQLException se) {
			Log.w(LOG_NAME, "Could not get last_modified date.");
		}

		return lastModifiedDate;
	}

	/**
	 * Gets a List of Observations from the datastore that are dirty (i.e.
	 * should be synced with the server).
	 * 
	 * @return
	 */
	public List<Observation> getDirty() {
		QueryBuilder<Observation, Long> queryBuilder = observationDao.queryBuilder();
		List<Observation> observations = new ArrayList<Observation>();

		try {
			queryBuilder.where().eq("dirty", true);
			observations = observationDao.query(queryBuilder.prepare());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_NAME, "Could not get dirty Observations.");
		}
		return observations;
	}
	
	/**
	 * A List of {@link Attachment} from the datastore that are dirty (i.e.
	 * should be synced with the server).
	 * 
	 * @return
	 */
	public List<Attachment> getDirtyAttachments() {
		QueryBuilder<Attachment, Long> queryBuilder = attachmentDao.queryBuilder();
		List<Attachment> attachments = new ArrayList<Attachment>();

		try {
			queryBuilder.where().eq("dirty", true);
			attachments = attachmentDao.query(queryBuilder.prepare());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_NAME, "Could not get dirty Observations.");
		}
		return attachments;
	}
	
	/**
	 * Deletes an Observation. This will also delete an Observation's child
	 * Attachments, child Properties and Geometry data.
	 * 
	 * @param pPrimaryKey
	 * @throws OrmException
	 */
	public void delete(Long pPrimaryKey) throws ObservationException {
		try {
			// read the full Observation in
			Observation observation = observationDao.queryForId(pPrimaryKey);

			// delete Observation properties.
			Collection<ObservationProperty> properties = observation.getProperties();
			if (properties != null) {
				for (ObservationProperty property : properties) {
					observationPropertyDao.deleteById(property.getPk_id());
				}
			}

			// delete Observation attachments.
			Collection<Attachment> attachments = observation.getAttachments();
			if (attachments != null) {
				for (Attachment attachment : attachments) {
					attachmentDao.deleteById(attachment.getId());
				}
			}

			// delete Geometry (but not corresponding GeometryType).
			observationGeometryDao.deleteById(observation.getObservationGeometry().getPk_id());

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
	public boolean addListener(final IObservationEventListener listener) throws ObservationException {
		boolean status = listeners.add(listener);
		
		new Callable<Object>() {
			@Override
			public Object call() throws ObservationException {
				listener.onObservationCreated(readAll());
				return null;
			}
		}.call();
		return status;
	}

	@Override
	public boolean removeListener(IObservationEventListener listener) {
		return listeners.remove(listener);
	}

}

