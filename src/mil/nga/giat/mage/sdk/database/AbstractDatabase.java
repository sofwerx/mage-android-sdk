package mil.nga.giat.mage.sdk.database;

import java.util.List;

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
	
	/**
	 * Utility method for generating a create table sql command.
	 * @param tableName The name of the table.
	 * @param columns A List of columns to create.
	 * @return SQL query
	 */
	public String createTable(String tableName, List<DatabaseColumn> columns) {
		
		StringBuilder query = new StringBuilder("CREATE TABLE ");
		query.append(tableName);
		query.append(" (pk_id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL");
		for(DatabaseColumn column : columns) {
			query.append("," + column.name + " " + column.type);
		}		
		query.append(" )");
		return query.toString();		
	}
	
	/**
	 * Simple utility method for generating a safe drop table command.
	 * @param tableName The table to drop
	 * @return SQL query
	 */
	public String dropTable(String tableName) {
		return "DROP TABLE IF EXISTS " + tableName;
	}
	
	/**
	 * Simple enumeration for capturing database column types
	 * @author travis
	 *
	 */
	protected enum ColumnType {TEXT,DOUBLE,INTEGER};
	
	/**
	 * Simple container for a database column
	 * @author travis
	 *
	 */
	protected static class DatabaseColumn {
		
		final String name;
		final ColumnType type;
		
		public DatabaseColumn(String pName, ColumnType pType) {
			this.name = pName;
			this.type = pType;
		}
		
	}

}
