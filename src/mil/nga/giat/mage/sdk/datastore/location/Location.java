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
    Collection<Property> properties;
	
    @DatabaseField(canBeNull = false,foreign = true, foreignAutoRefresh = true)
    private Geometry geometry;
    
    public Location() {
    	// ORMLite needs a no-arg constructor 
    }
	public Location(String type, Collection<Property> properties,
			Geometry geometry) {
		super();
		this.type = type;
		this.properties = properties;
		this.geometry = geometry;
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

	public Collection<Property> getProperties() {
		return properties;
	}

	public void setProperties(Collection<Property> properties) {
		this.properties = properties;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
    
}
