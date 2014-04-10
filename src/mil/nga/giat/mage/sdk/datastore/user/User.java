package mil.nga.giat.mage.sdk.datastore.user;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User {

	@DatabaseField(generatedId = true)
	private Long pk_id;

	@DatabaseField(unique = true, columnName = "remote_id")
	private String remoteId;

	@DatabaseField
	private String email;

	@DatabaseField
	private String firstname;

	@DatabaseField
	private String lastname;

	@DatabaseField(canBeNull = false, unique = true)
	private String username;

	@DatabaseField(canBeNull = false)
	private Boolean isCurrentUser = Boolean.FALSE;

	@DatabaseField(canBeNull = false, columnName = "fetched_date")
	private Date fetchedDate = new Date(0);

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Role role;

	public User() {
		// ORMLite needs a no-arg constructor
	}

	public User(String remoteId, String email, String firstname, String lastname, String username, Role role) {
		super();
		this.remoteId = remoteId;
		this.email = email;
		this.firstname = firstname;
		this.lastname = lastname;
		this.username = username;
		this.role = role;
	}

	public Long getPk_id() {
		return pk_id;
	}

	public void setPk_id(Long pk_id) {
		this.pk_id = pk_id;
	}

	public String getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(String remoteId) {
		this.remoteId = remoteId;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getUsername() {
		return username;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Boolean isCurrentUser() {
		return isCurrentUser;
	}

	public void setCurrentUser(Boolean isCurrentUser) {
		this.isCurrentUser = isCurrentUser;
	}

	public Date getFetchedDate() {
		return fetchedDate;
	}

	public void setFetchedDate(Date fetchedDate) {
		this.fetchedDate = fetchedDate;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
