package mil.nga.giat.mage.sdk.database.orm;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "geometry_type_lu")
public class GeometryType {

	@DatabaseField(generatedId = true)
	private Long pk_id;
	
	@DatabaseField
	private String type;
	
	public GeometryType() {
		// ORMLite needs a no-arg constructor
	}	
	public GeometryType(String pType) {        
        this.type = pType;
    }
	
	public Long getPk_id() {
		return pk_id;
	}
	public void setPk_id(Long pk_id) {
		this.pk_id = pk_id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
