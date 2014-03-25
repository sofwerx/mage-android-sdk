package mil.nga.giat.mage.sdk.event;

/**
 * Part of a small event framework. Used to pass events to different parts of
 * the mdk. When locations are saved, when tokens expire, etc...
 * 
 * @author wiedemannse
 * 
 * @param <T>
 */
public interface IEventDispatcher<T> {

	public boolean addListener(IEventListener<T> listener);

	public boolean removeListener(IEventListener<T> listener);
}
