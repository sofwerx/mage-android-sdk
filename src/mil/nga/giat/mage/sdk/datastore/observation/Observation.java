package mil.nga.giat.mage.sdk.datastore.observation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mil.nga.giat.mage.sdk.datastore.common.Geometry;
import mil.nga.giat.mage.sdk.datastore.common.Property;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName="observations")
public class Observation {

    @DatabaseField(generatedId = true)
    private Long pk_id;
    
    @DatabaseField
    private String remote_id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private State state;
    
    @DatabaseField(canBeNull = false,foreign = true, foreignAutoRefresh = true)
    private Geometry geometry;
    
    @ForeignCollectionField(eager = true)
    Collection<Property> properties;
    
    @ForeignCollectionField(eager = true)
    Collection<Attachment> attachments;
    
	public Observation() {
        // ORMLite needs a no-arg constructor 
    }
    public Observation(String pRemoteId, State pState, Geometry pGeometry, Collection<Property> pProperties,Collection<Attachment> pAttachments) {        
        this.remote_id = pRemoteId;
        this.state = pState;
        this.geometry = pGeometry;
        this.properties = pProperties;
        this.attachments = pAttachments;
    }

    public Long getPk_id() {
		return pk_id;
	}
	public void setPk_id(Long pk_id) {
		this.pk_id = pk_id;
	}
	
	public String getRemote_id() {
		return remote_id;
	}
	public void setRemote_id(String remote_id) {
		this.remote_id = remote_id;
	}
	
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}

	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	
	public Collection<Property> getProperties() {
		return properties;
	}
	public void setProperties(Collection<Property> properties) {
		this.properties = properties;
	}

	public Collection<Attachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(Collection<Attachment> attachments) {
		this.attachments = attachments;
	}

	/**
	 * A convenience method used for returning an Observation's properties in a more useful 
	 * data-structure.
	 * @return
	 */
	public Map<String, String> getPropertiesMap() {
		
		Map<String, String> propertiesMap = new HashMap<String, String>();
		
		if(properties != null) {
			for(Property property : properties) {
				propertiesMap.put(property.getKey(), property.getValue());
			}
		}
		
		return propertiesMap;
	}
	
	/** 
	 * A convenience method used for setting an Observation's properties with a Map
	 * (instead of a Collection).
	 * @param propertiesMap A Map of ALL the properties to be set.
	 */
	public void setPropertiesMap(Map<String,String> propertiesMap) {
		Collection<Property> properties = new ArrayList<Property>();
		
		if(propertiesMap != null) {
			for(String key : propertiesMap.keySet()) {
				properties.add(new Property(key,propertiesMap.get(key)));
			}
		}
		
		setProperties(properties);
	}
			
	@Override
	public String toString() {
		return "Observation [pk_id=" + pk_id + ", remote_id=" + remote_id
				+ ", state=" + state + ", geometry=" + geometry
				+ ", properties=" + properties + ", attachments=" + attachments
				+ "]";
	}
	
}