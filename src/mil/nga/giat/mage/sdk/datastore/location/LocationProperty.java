package mil.nga.giat.mage.sdk.datastore.location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "location_properties")
public class LocationProperty {

	@DatabaseField(generatedId = true)
	private Long pk_id;
	
	@DatabaseField
	private String key;
	
	@DatabaseField
	private String value;

	@DatabaseField (foreign = true)
    private Location location;
	
	public LocationProperty() {
		// ORMLite needs a no-arg constructor
	}
	
	public LocationProperty(String pKey, String pValue) {
		this.key = pKey;
		this.value = pValue;		
	}
	
	public Long getPk_id() {
		return pk_id;
	}
	public void setPk_id(Long pk_id) {
		this.pk_id = pk_id;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "Property [pk_id=" + pk_id + ", key=" + key + ", value=" + value
				+ ", observation=" + location.getPk_id() + "]";
	}
	
}
