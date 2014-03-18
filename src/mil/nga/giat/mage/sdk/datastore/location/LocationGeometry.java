package mil.nga.giat.mage.sdk.datastore.location;

import mil.nga.giat.mage.sdk.datastore.common.GeometryType;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "location_geometries")
public class LocationGeometry {

	@DatabaseField(generatedId = true)
	private Long pk_id;
	
	@DatabaseField
	private String coordinates;
		
	@DatabaseField(canBeNull = false,foreign = true)
    private GeometryType geometryType;
	
	public LocationGeometry() {
		// ORMLite needs a no-arg constructor
	}
	
	public LocationGeometry(String pCoordinates, GeometryType pGeometryType) {
		this.coordinates = pCoordinates;
		this.geometryType = pGeometryType;
	}
	
	public Long getPk_id() {
		return pk_id;
	}
	public void setPk_id(Long pk_id) {
		this.pk_id = pk_id;
	}

	public String getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	public GeometryType getGeometryType() {
		return geometryType;
	}
	public void setGeometryType(GeometryType geometryType) {
		this.geometryType = geometryType;
	}

	@Override
	public String toString() {
		return "LocationGeometry [pk_id=" + pk_id + ", coordinates=" + coordinates
				+ ", geometryType=" + geometryType + "]";
	}
	
}
