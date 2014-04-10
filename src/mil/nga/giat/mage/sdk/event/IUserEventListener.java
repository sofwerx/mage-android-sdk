package mil.nga.giat.mage.sdk.event;


public interface IUserEventListener extends IEventListener<Void> {

	public void onTokenExpired();
}
