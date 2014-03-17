package mil.nga.giat.mage.sdk.datastore.location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="permissions")
public class Permission {

    @DatabaseField(generatedId = true)
    private Long pk_id;
	
    @DatabaseField
    private String permission;

	public Permission() {
        // ORMLite needs a no-arg constructor 
    }
	public Permission(String permission) {
		this.permission = permission;
	}

	public Long getPk_id() {
		return pk_id;
	}

	public void setPk_id(Long pk_id) {
		this.pk_id = pk_id;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}
    
}
