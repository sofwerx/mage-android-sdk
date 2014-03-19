package mil.nga.giat.mage.sdk.datastore.location;

import java.util.Collection;

import mil.nga.giat.mage.sdk.datastore.common.Geometry;
import mil.nga.giat.mage.sdk.datastore.common.Property;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="locations")
public class Location {

	
	@DatabaseField(generatedId = true)
    private Long pk_id;
	
	@DatabaseField
    private String type;
	
	@ForeignCollectionField(eager = true)
    Collection<LocationProperty> properties;
	
    @DatabaseField(canBeNull = false,foreign = true, foreignAutoRefresh = true)
    private LocationGeometry locationGeometry;
    
    public Location() {
    	// ORMLite needs a no-arg constructor 
    }
	public Location(String type, Collection<LocationProperty> properties,
			LocationGeometry locationGeometry) {
		super();
		this.type = type;
		this.properties = properties;
		this.locationGeometry = locationGeometry;
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
		return "Location [pk_id=" + pk_id + ", type=" + type + ", properties="
				+ properties + ", locationGeometry=" + locationGeometry + "]";
	}
   
	
	
}
