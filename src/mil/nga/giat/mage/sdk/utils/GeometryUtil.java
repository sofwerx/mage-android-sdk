package mil.nga.giat.mage.sdk.utils;

import java.util.HashMap;
import java.util.Map;

public class GeometryUtil {

	
	public enum POINT{LAT,LON};
	
	
	public static String generate(Double pLat, Double pLon) {		
		return "[" + pLat + "," + pLon + "]";
	}
	
	/**
	 * Utility for parsing a point out of a String in the following format:
	 * [11.11111,22.22222]
	 * @return
	 */
	public static Map<POINT,Double> parse(String pPoint) {	
		String[] latLon = pPoint.split(",");
		Double lat = Double.parseDouble(latLon[0].substring(1));
		Double lon = Double.parseDouble(latLon[1].substring(0, latLon[1].length() - 1));
		Map<POINT,Double> returnMap = new HashMap<GeometryUtil.POINT, Double>();
		returnMap.put(POINT.LAT, lat);
		returnMap.put(POINT.LON, lon);		
		return returnMap;
	}
	
	
	
}
