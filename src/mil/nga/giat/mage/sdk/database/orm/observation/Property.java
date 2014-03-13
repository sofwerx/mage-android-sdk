package mil.nga.giat.mage.sdk.database.orm.observation;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "properties")
public class Property {

	@DatabaseField(generatedId = true)
	private Long pk_id;
	
	@DatabaseField
	private String key;
	
	@DatabaseField
	private String value;

    
	@DatabaseField (foreign = true)
    private Observation observation;
	
	public Property() {
		// ORMLite needs a no-arg constructor
	}
	
	public Property(String pKey, String pValue) {
		this.key = pKey;
		this.value = pValue;
		//leave this out for now.  Messes up child object creation.
		//this.observation = pObservation;
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
	
	public Observation getObservation() {
		return observation;
	}
	public void setObservation(Observation observation) {
		this.observation = observation;
	}

	@Override
	public String toString() {
		return "Property [pk_id=" + pk_id + ", key=" + key + ", value=" + value
				+ ", observation=" + observation.getPk_id() + "]";
	}
	
}
