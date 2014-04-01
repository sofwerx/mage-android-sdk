package mil.nga.giat.mage.sdk.event.user;

import mil.nga.giat.mage.sdk.event.IEventListener;

public interface IUserEventListener extends IEventListener<Void> {

	public void onTokenExpired();
}
