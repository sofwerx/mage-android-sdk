package mil.nga.giat.mage.sdk.datastore.common;

import java.io.Serializable;

/**
 * Interface for geometries. Point, line, polygon, etc.
 * 
 * @author wiedemannse
 * 
 */
public interface Geometry extends Serializable {
	String toGeoJSON();

	GeometryType getType();
}
