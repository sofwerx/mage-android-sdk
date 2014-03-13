package mil.nga.giat.mage.sdk.database.orm;

/**
 * A generic ORM exception.
 * 
 * @author travis
 * 
 */
public class OrmException extends Exception {

	private static final long serialVersionUID = 1L;

	public OrmException() {
		super();
	}

	public OrmException(String message) {
		super(message);
	}

	public OrmException(String message, Throwable cause) {
		super(message, cause);
	}

	public OrmException(Throwable cause) {
		super(cause);
	}

}
