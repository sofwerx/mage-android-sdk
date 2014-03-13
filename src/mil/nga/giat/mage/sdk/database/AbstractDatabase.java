package mil.nga.giat.mage.sdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * If you want to CRUD any database, your class should extends this, and not
 * {@link SQLiteOpenHelper}. This class will also track the {@link Context}
 * passed in.
 * 
 * @author wiedemannse
 * 
 */
public abstract class AbstractDatabase extends SQLiteOpenHelper {

	private Context mContext;

	public final Context getContext() {
		return mContext;
	}

	public AbstractDatabase(Context context, String name, int version) {
		super(context, name, null, version);
		mContext = context;
	}
	

}
