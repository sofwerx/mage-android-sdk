package mil.nga.giat.mage.sdk.exceptions;

/**
 * A generic Location exception.
 * @author travis
 *
 */
public class LocationException extends Exception {

	private static final long serialVersionUID = 1L;

	public LocationException() {
		super();
	}

	public LocationException(String message) {
		super(message);
	}

	public LocationException(String message, Throwable cause) {
		super(message, cause);
	}

	public LocationException(Throwable cause) {
		super(cause);
	}

}
