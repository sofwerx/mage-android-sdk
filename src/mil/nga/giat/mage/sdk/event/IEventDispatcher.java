package mil.nga.giat.mage.sdk.event;

import java.util.List;

/**
 * Part of a small event framework. Used to pass events to different parts of
 * the mdk. When locations are saved, when tokens expire, etc...
 * 
 * @author wiedemannse
 * 
 * @param <T>
 */
public interface IEventDispatcher<T> {

	/**
	 * Returns a collection of stuff the listener has missed out on.
	 * 
	 * @param listener
	 * @return
	 * @throws Exception
	 */
	public List<T> addListener(IEventListener<T> listener) throws Exception;

	/**
	 * Removes the listerner
	 * 
	 * @param listener
	 * @return
	 * @throws Exception
	 */
	public boolean removeListener(IEventListener<T> listener) throws Exception;
}
