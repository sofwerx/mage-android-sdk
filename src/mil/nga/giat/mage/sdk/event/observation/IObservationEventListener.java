package mil.nga.giat.mage.sdk.event.observation;

import java.util.Collection;

import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.event.IEventListener;

public interface IObservationEventListener extends IEventListener<Observation> {

	public void onObservationCreated(final Collection<Observation> observations);
	
	public void onObservationUpdated(final Observation observation);
	
	public void onObservationDeleted(final Observation observation);
}
