package mil.nga.giat.mage.sdk.datastore.location;

import mil.nga.giat.mage.sdk.datastore.DBHelper;
import mil.nga.giat.mage.sdk.exceptions.UserException;
import android.content.Context;

/**
 * A utility class for accessing User data from the physical data model.
 * The details of ORM DAOs and Lazy Loading should not be exposed past this
 * class.
 * 
 * @author travis
 * 
 */
public class UserHelper {

	private static final String LOG_NAME = "mage.user.log";

	// required DBHelper and DAOs for handing CRUD operations for Users
	private DBHelper helper;
	
	/**
	 * Singleton.
	 */
	private static UserHelper mUserHelper;
	
	/**
	 * Use of a Singleton here ensures that an excessive amount of DAOs are not created.
	 * @param context Application Context
	 * @return A fully constructed and operational UserHelper.
	 */
	public static UserHelper getInstance(Context context) {
		if(mUserHelper == null) {
			mUserHelper = new UserHelper(context);
		}
		return mUserHelper;
	}
	
	/**
	 * Only one-per JVM.  Singleton.
	 * @param pContext
	 */
	private UserHelper(Context pContext) {

		helper = DBHelper.getInstance(pContext);
		

	}

	/**
	 * This utility method abstracts the complexities of persisting a new User.
	 * All the caller needs to to is construct an User object and call the
	 * appropriate setters.
	 * @param pUser A constructed User.
	 * @return A fully constructed User complete with database primary keys.
	 * @throws UserException If the User being created violates any database
	 *                      constraints.
	 */
	public User createUser(User pUser) throws UserException {
		
		return null;
		
	}
	
	
}
