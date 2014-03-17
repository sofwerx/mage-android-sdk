package mil.nga.giat.mage.sdk.event;

import java.util.EventListener;

public interface IEventListener<T> extends EventListener {
	void onComplete(T item);

	void onError(Throwable error);
}
