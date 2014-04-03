package mil.nga.giat.mage.sdk.datastore.observation;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "observation_properties")
public class ObservationProperty {

	@DatabaseField(generatedId = true)
	private Long pk_id;

	@DatabaseField(canBeNull = false, uniqueCombo = true)
	private String key;

	@DatabaseField(canBeNull = false)
	private String value;

	@DatabaseField(foreign = true, uniqueCombo = true)
	private Observation observation;

	public ObservationProperty() {
		// ORMLite needs a no-arg constructor
	}

	public ObservationProperty(String pKey, String pValue) {
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

	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}

	@Override
	public String toString() {
		return "ObservationProperty [pk_id=" + pk_id + ", key=" + key + ", value=" + value + ", observation=" + observation.getId() + "]";
	}

}
