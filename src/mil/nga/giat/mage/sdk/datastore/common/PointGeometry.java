package mil.nga.giat.mage.sdk.datastore.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A point, stores a lat, lon
 * 
 * @author wiedemannse
 * 
 */
public class PointGeometry implements Geometry {

	private static final long serialVersionUID = -4037181327435449086L;

	private double latitude;
	private double longitude;

	public PointGeometry(double lat, double lon) {
		latitude = lat;
		longitude = lon;
	}

	public PointGeometry(String geoJSON) {
		String regexDouble = "-?\\d*\\.?\\d+";
		Pattern pattern = Pattern.compile(".*?(" + regexDouble + ")\\s*,\\s*(" + regexDouble + ").*");
		Matcher matcher = pattern.matcher(geoJSON);
		if (matcher.matches()) {
			latitude = Double.parseDouble(matcher.group(2));
			longitude = Double.parseDouble(matcher.group(1));
		}
	}

	@Override
	public String toGeoJSON() {
		return "[" + latitude + "," + longitude + "]";
	}

	@Override
	public GeometryType getType() {
		return GeometryType.POINT;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return "PointGeometry [latitude=" + getLatitude() + ", longitude=" + getLongitude() + "]";
	}

}
