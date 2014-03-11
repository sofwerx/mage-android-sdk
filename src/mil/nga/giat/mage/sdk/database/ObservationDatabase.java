package mil.nga.giat.mage.sdk.database;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class ObservationDatabase extends AbstractDatabase {

	

	public ObservationDatabase(Context context) {
		super(context, DATABASE_NAME, DATABASE_VERSION);
	}
	
	private static final String DATABASE_NAME = "observations.db";
	private static final int DATABASE_VERSION = 1;
	
	//observations table
	private static final String OBSERVATIONS_TABLE = "observations";
	private static final List<DatabaseColumn> OBSERVATION_COLUMNS = Arrays
			.asList(new DatabaseColumn("remote_id" , ColumnType.INTEGER),
					new DatabaseColumn("state_id"  , ColumnType.INTEGER));
		
	//state_lu table
	private static final String STATE_LU_TABLE = "state_lu";
	private static final List<DatabaseColumn> STATE_COLUMNS = Arrays
			.asList(new DatabaseColumn("state" , ColumnType.TEXT));
					
 	//properties table
 	private static final String PROPERTIES_TABLE  = "properties";
 	private static final List<DatabaseColumn> PROPERTY_COLUMNS = Arrays
			.asList(new DatabaseColumn("observation_id" , ColumnType.INTEGER),
					new DatabaseColumn("key"            , ColumnType.TEXT),
					new DatabaseColumn("value"          , ColumnType.TEXT));
 	
 	//geometries table
 	private static final String GEOMETRIES_TABLE  = "geometries";
 	private static final List<DatabaseColumn> GEOMETRY_COLUMNS = Arrays
			.asList(new DatabaseColumn("observation_id"   , ColumnType.INTEGER),
					new DatabaseColumn("coordinates"      , ColumnType.TEXT),
					new DatabaseColumn("geometry_type_id" , ColumnType.TEXT));
 	
	//geometry_type_lu table
	private static final String GEOMETRY_TYPE_LU_TABLE = "geometry_type_lu";
	private static final List<DatabaseColumn> GEOMETRY_TYPE_COLUMNS = Arrays
			.asList(new DatabaseColumn("type"  , ColumnType.TEXT));
 	
	//attachments 
	private static final String ATTACHMENTS_TABLE = "attachments";
	private static final List<DatabaseColumn> ATTACHMENT_COLUMNS = Arrays
			.asList(new DatabaseColumn("observation_id" , ColumnType.INTEGER),
					new DatabaseColumn("content_type"   , ColumnType.TEXT),
					new DatabaseColumn("size"           , ColumnType.INTEGER),
					new DatabaseColumn("name"           , ColumnType.INTEGER),
					new DatabaseColumn("local_path"     , ColumnType.INTEGER),
					new DatabaseColumn("remote_path"    , ColumnType.INTEGER)); 	
 	
	@Override
	public void onCreate(SQLiteDatabase db) {	
		db.execSQL((createTable(OBSERVATIONS_TABLE, OBSERVATION_COLUMNS)));
		db.execSQL((createTable(STATE_LU_TABLE    , STATE_COLUMNS)));
		db.execSQL((createTable(PROPERTIES_TABLE, PROPERTY_COLUMNS)));
		db.execSQL((createTable(GEOMETRIES_TABLE, GEOMETRY_COLUMNS)));
		db.execSQL((createTable(GEOMETRY_TYPE_LU_TABLE, GEOMETRY_TYPE_COLUMNS)));
		db.execSQL((createTable(ATTACHMENTS_TABLE, ATTACHMENT_COLUMNS)));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL((dropTable(OBSERVATIONS_TABLE)));
		db.execSQL((dropTable(STATE_LU_TABLE)));
		db.execSQL((dropTable(PROPERTIES_TABLE)));
		db.execSQL((dropTable(GEOMETRIES_TABLE)));
		db.execSQL((dropTable(GEOMETRY_TYPE_LU_TABLE)));
		db.execSQL((dropTable(ATTACHMENTS_TABLE)));
	}


	
}
