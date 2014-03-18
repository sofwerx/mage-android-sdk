package mil.nga.giat.mage.sdk.datastore.location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="permissions")
public class Permission {

    @DatabaseField(generatedId = true)
    private Long pk_id;
	
    @DatabaseField
    private String permission;

    @DatabaseField(canBeNull = false,foreign = true)
    private Role role;
    
	public Permission() {
        // ORMLite needs a no-arg constructor 
    }
	public Permission(String permission, Role role) {
		this.permission = permission;
		this.role = role;
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
	
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
    
	
	
}
