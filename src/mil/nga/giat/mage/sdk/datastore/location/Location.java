package mil.nga.giat.mage.sdk.datastore.location;

import java.util.Collection;

import mil.nga.giat.mage.sdk.datastore.user.User;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "locations")
public class Location implements Comparable<Location> {

	@DatabaseField(generatedId = true)
	private Long id;

	@DatabaseField(unique = true, columnName = "remote_id")
	private String remoteId;

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
		this.remoteId = remoteId;
		this.user = user;
		this.lastModified = lastModified;
		this.type = type;
		this.properties = properties;
		this.locationGeometry = locationGeometry;
		this.setDirty(false);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(String remoteId) {
		this.remoteId = remoteId;
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
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int compareTo(Location another) {
		return new CompareToBuilder().append(this.id, another.id).append(this.remoteId, another.remoteId).toComparison();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((remoteId == null) ? 0 : remoteId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(id, other.id).append(remoteId, other.remoteId).isEquals();
	}
}
