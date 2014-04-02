package mil.nga.giat.mage.sdk.datastore.location;

import java.util.Collection;

import mil.nga.giat.mage.sdk.datastore.user.User;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "locations")
public class Location {

	@DatabaseField(generatedId = true)
	private Long pk_id;

	@DatabaseField(unique = true)
	private String remote_id;
	
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User user;

	@DatabaseField(canBeNull = false, version = true)
	private long lastModified;
	
	@DatabaseField(canBeNull = false)
	private boolean dirty;

	@DatabaseField
	private String type;

	@ForeignCollectionField(eager = true)
	private Collection<LocationProperty> properties;

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private LocationGeometry locationGeometry;
	
	public Location() {
		// ORMLite needs a no-arg constructor
	}

	public Location(String type, User user, Collection<LocationProperty> properties, LocationGeometry locationGeometry) {
		this(null, user, System.currentTimeMillis(), type, properties, locationGeometry);
		this.setDirty(true);
	}

	public Location(String remoteId, User user, long lastModified, String type, Collection<LocationProperty> properties, LocationGeometry locationGeometry) {
		super();
		this.remote_id = remoteId;
		this.user = user;
		this.lastModified = lastModified;
		this.type = type;
		this.properties = properties;
		this.locationGeometry = locationGeometry;
		this.setDirty(false);
	}

	public Long getPk_id() {
		return pk_id;
	}

	public String getRemote_id() {
		return remote_id;
	}

	public void setRemote_id(String remote_id) {
		this.remote_id = remote_id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isDirty() {
		return dirty;
	}

	public Collection<LocationProperty> getProperties() {
		return properties;
	}

	public void setProperties(Collection<LocationProperty> properties) {
		this.properties = properties;
	}

	public LocationGeometry getLocationGeometry() {
		return locationGeometry;
	}

	public void setLocationGeometry(LocationGeometry geometry) {
		this.locationGeometry = geometry;
	}

	@Override
	public String toString() {
		return "Location [pk_id=" + pk_id + ", type=" + type + ", properties=" + properties + ", locationGeometry=" + locationGeometry + "]";
	}

}
