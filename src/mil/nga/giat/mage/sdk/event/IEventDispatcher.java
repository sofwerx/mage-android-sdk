package mil.nga.giat.mage.sdk.event;

public interface IEventDispatcher<T> {

	public boolean addListener(IEventListener<T> listener);

	public boolean removeListener(IEventListener<T> listener);
}
