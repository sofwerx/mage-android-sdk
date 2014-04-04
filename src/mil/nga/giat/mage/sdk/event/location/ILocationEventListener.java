package mil.nga.giat.mage.sdk.event.location;

import java.util.Collection;

import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.event.IEventListener;

public interface ILocationEventListener extends IEventListener<Location> {

	public void onLocationCreated(final Collection<Location> observations);
	
	public void onLocationUpdated(final Location observation);
	
	public void onLocationDeleted(final Location observation);
}
