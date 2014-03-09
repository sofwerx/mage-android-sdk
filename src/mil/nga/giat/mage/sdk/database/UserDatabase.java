package mil.nga.giat.mage.sdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * TODO: Work in progress.  Local storage CRUD for users and their locations.
 * 
 * @author wiedemannse
 *
 */
public class UserDatabase extends AbstractDatabase {

	private static final String DATABASE_NAME = "users.db";
	private static final int DATABASE_VERSION = 1;

	public UserDatabase(Context context) {
		super(context, DATABASE_NAME, DATABASE_VERSION);
	}

	public static final String USERS_TABLE = "users";
	public static final String USERS_COLUMN_PK_ID = "pk_id";
	public static final String USERS_COLUMN_ID = "_id";
	public static final String USERS_COLUMN_EMAIL = "email";
	public static final String USERS_COLUMN_FIRSTNAME = "firstname";
	public static final String USERS_COLUMN_LASTNAME = "lastname";
	public static final String USERS_COLUMN_USERNAME = "username";
	public static final String USERS_COLUMN_ROLE = "role";
	
	public static final String CREATE_USERS_TABLE = new StringBuilder("CREATE TABLE ")
	.append(USERS_TABLE)
	.append(" (").append(USERS_COLUMN_PK_ID).append(" INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL, ")
	.append(USERS_COLUMN_ID).append(" TEXT NOT NULL, ")
	.append(USERS_COLUMN_EMAIL).append(" TEXT NOT NULL, ")
	.append(USERS_COLUMN_FIRSTNAME).append(" TEXT NOT NULL, ")
	.append(USERS_COLUMN_LASTNAME).append(" TEXT NOT NULL, ")
	.append(USERS_COLUMN_USERNAME).append(" TEXT NOT NULL, ")
	.append(USERS_COLUMN_ROLE).append(" TEXT NOT NULL)").toString();

	public static final String USER_LOCATIONS_TABLE = "user_locations";
	public static final String USER_LOCATIONS_COLUMN_PK_ID = "pk_id";
	public static final String USER_LOCATIONS_COLUMN_ID = "_id";
	public static final String USER_LOCATIONS_COLUMN_USER_ID = "user_id";
	public static final String USER_LOCATIONS_COLUMN_TIMESTAMP = "timestamp";
	public static final String USER_LOCATIONS_COLUMN_LATITUDE = "latitude";
	public static final String USER_LOCATIONS_COLUMN_LONGITUDE = "longitude";
	public static final String USER_LOCATIONS_COLUMN_ACCURACY = "accuracy";
	public static final String USER_LOCATIONS_COLUMN_ALTITUDE = "altitude";
	public static final String USER_LOCATIONS_COLUMN_HEADING = "heading";
	public static final String USER_LOCATIONS_COLUMN_SPEED = "speed";
	
	public static final String CREATE_USER_LOCATIONS_TABLE = new StringBuilder("CREATE TABLE ")
	.append(USER_LOCATIONS_TABLE)
	.append(" (").append(USER_LOCATIONS_COLUMN_PK_ID).append(" INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL, ")
	.append(USER_LOCATIONS_COLUMN_ID).append(" TEXT NOT NULL, ")
	.append(USER_LOCATIONS_COLUMN_USER_ID).append(" TEXT NOT NULL, ")
	.append(USER_LOCATIONS_COLUMN_TIMESTAMP).append(" TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP, ")
	.append(USER_LOCATIONS_COLUMN_LATITUDE).append(" TEXT NOT NULL, ")
	.append(USER_LOCATIONS_COLUMN_LONGITUDE).append(" TEXT NOT NULL, ")
	.append(USER_LOCATIONS_COLUMN_ACCURACY).append(" DOUBLE NOT NULL, ")
	.append(USER_LOCATIONS_COLUMN_ALTITUDE).append(" DOUBLE NOT NULL, ")
	.append(USER_LOCATIONS_COLUMN_HEADING).append(" DOUBLE, ")
	.append(USER_LOCATIONS_COLUMN_SPEED).append(" DOUBLE)").toString();
	
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_USERS_TABLE);
		db.execSQL(CREATE_USER_LOCATIONS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + USER_LOCATIONS_TABLE);
		onCreate(db);
	}
	
	/**
	 * FIXME: TEST DEMO ONLY.  Use DAO.
	 */
	public void addUser(String name) {
		ContentValues values = new ContentValues();
		values.put(USERS_COLUMN_ID, name);
		values.put(USERS_COLUMN_EMAIL, name);
		values.put(USERS_COLUMN_FIRSTNAME, name);
		values.put(USERS_COLUMN_LASTNAME, name);
		values.put(USERS_COLUMN_USERNAME, name);
		values.put(USERS_COLUMN_ROLE, name);

		SQLiteDatabase db = this.getWritableDatabase();
		long userPkId = db.insert(USERS_TABLE, null, values);
		db.close();
		
		String selectQuery = "SELECT * FROM " + USERS_TABLE;
	    db = getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    if (cursor.moveToFirst()) {
	        do {
	            System.out.println(cursor.getString(0));
	            System.out.println(cursor.getString(4));
	        } while (cursor.moveToNext());
	    }
		db.close();
	}

}
