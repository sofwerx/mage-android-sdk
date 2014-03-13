package mil.nga.giat.mage.sdk.database.orm;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "state_lu")
public class State {

	@DatabaseField(generatedId = true)
	private Long pk_id;

	@DatabaseField
	private String state;

	public State() {
		// ORMLite needs a no-arg constructor
	}	
	public State(String pState) {        
        this.state = pState;
    }
	
	public Long getPk_id() {
		return pk_id;
	}
	public void setPk_id(Long pk_id) {
		this.pk_id = pk_id;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

}
