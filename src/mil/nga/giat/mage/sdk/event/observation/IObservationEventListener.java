package mil.nga.giat.mage.sdk.event.observation;

import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.event.IEventListener;

public interface IObservationEventListener extends IEventListener<Observation> {

	public void onObservationCreated(Observation observation);
	
	public void onObservationDeleted(Observation observation);
}
