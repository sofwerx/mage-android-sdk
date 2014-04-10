package mil.nga.giat.mage.sdk.event;

import java.util.Collection;

import mil.nga.giat.mage.sdk.datastore.location.Location;

public interface ILocationEventListener extends IEventListener<Location> {

	public void onLocationCreated(final Collection<Location> location);
	
	public void onLocationUpdated(final Location location);
	
	public void onLocationDeleted(final String pUserLocalId);
}
