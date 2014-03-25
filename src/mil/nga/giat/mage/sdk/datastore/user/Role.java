package mil.nga.giat.mage.sdk.datastore.user;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "roles")
public class Role {

	@DatabaseField(generatedId = true)
	private Long pk_id;

	@DatabaseField(canBeNull = false)
	private String name;

	@DatabaseField
	private String description;

	@ForeignCollectionField(eager = true)
	Collection<Permission> permissions;

	public Role() {
		// ORMLite needs a no-arg constructor
	}

	public Role(String name, String description, Collection<Permission> permissions) {
		super();
		this.name = name;
		this.description = description;
		this.permissions = permissions;
	}

	public Long getPk_id() {
		return pk_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Collection<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Collection<Permission> permissions) {
		this.permissions = permissions;
	}

}
