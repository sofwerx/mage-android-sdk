package mil.nga.giat.mage.sdk.event.location;

import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.event.IEventListener;

public interface ILocationEventListener extends IEventListener<Location> {

	public void onLocationCreated(Location observation);
	
	public void onLocationDeleted(Location observation);
}
