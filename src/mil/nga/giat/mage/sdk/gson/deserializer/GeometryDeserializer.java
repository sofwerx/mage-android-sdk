package mil.nga.giat.mage.sdk.gson.deserializer;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryDeserializer implements JsonDeserializer<Geometry> {

	private final GeometryFactory geometryFactory = new GeometryFactory();

	public static Gson getGsonBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Geometry.class, new GeometryDeserializer());
		return gsonBuilder.create();
	}
	
	@Override
	public Geometry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext arg2) throws JsonParseException {
		return parseGeometry(json);
	}

	private Geometry parseGeometry(JsonElement json) {
		Geometry geometry = null;
		JsonObject root = json.getAsJsonObject();
		String typeName = root.get("type").getAsString();
		if (typeName.equals("Point")) {
			geometry = geometryFactory.createPoint(parseCoordinate(root.get("coordinates").getAsJsonArray()));
		} else if (typeName.equals("MultiPoint")) {
			geometry = geometryFactory.createMultiPoint(parseLineString(root.get("coordinates").getAsJsonArray()));
		} else if (typeName.equals("LineString")) {
			geometry = geometryFactory.createLineString(parseLineString(root.get("coordinates").getAsJsonArray()));
		} else if (typeName.equals("MultiLineString")) {
			geometry = geometryFactory.createMultiLineString(parseLineStrings(root.get("coordinates").getAsJsonArray()));
		} else if (typeName.equals("Polygon")) {
			geometry = parsePolygonCoordinates(root.get("coordinates").getAsJsonArray());
		} else if (typeName.equals("MultiPolygon")) {
			geometry = geometryFactory.createMultiPolygon(parsePolygons(root.get("coordinates").getAsJsonArray()));
		} else if (typeName.equals("GeometryCollection")) {
			geometry = geometryFactory.createGeometryCollection(parseGeometries(root.get("geometries").getAsJsonArray()));
		} else {
			throw new UnsupportedOperationException();
		}
		return geometry;
	}

	private Geometry[] parseGeometries(JsonArray arrayOfGeoms) {
		Geometry[] items = new Geometry[arrayOfGeoms.size()];
		for (int i = 0; i != arrayOfGeoms.size(); ++i) {
			items[i] = parseGeometry(arrayOfGeoms.get(i).getAsJsonObject());
		}
		return items;
	}

	private Polygon parsePolygonCoordinates(JsonArray arrayOfRings) {
		return geometryFactory.createPolygon(parseExteriorRing(arrayOfRings), parseInteriorRings(arrayOfRings));
	}

	private Polygon[] parsePolygons(JsonArray arrayOfPolygons) {
		Polygon[] polygons = new Polygon[arrayOfPolygons.size()];
		for (int i = 0; i != arrayOfPolygons.size(); i++) {
			polygons[i] = parsePolygonCoordinates(arrayOfPolygons.get(i).getAsJsonArray());
		}
		return polygons;
	}

	private LinearRing parseExteriorRing(JsonArray arrayOfRings) {
		return geometryFactory.createLinearRing(parseLineString(arrayOfRings.get(0).getAsJsonArray()));
	}

	private LinearRing[] parseInteriorRings(JsonArray arrayOfRings) {
		LinearRing rings[] = new LinearRing[arrayOfRings.size() - 1];
		for (int i = 1; i < arrayOfRings.size(); i++) {
			rings[i - 1] = geometryFactory.createLinearRing(parseLineString(arrayOfRings.get(i).getAsJsonArray()));
		}
		return rings;
	}

	private Coordinate parseCoordinate(JsonArray array) {
		return new Coordinate(array.get(0).getAsDouble(), array.get(1).getAsDouble());
	}

	private Coordinate[] parseLineString(JsonArray array) {
		Coordinate[] points = new Coordinate[array.size()];
		for (int i = 0; i != array.size(); ++i) {
			points[i] = parseCoordinate(array.get(i).getAsJsonArray());
		}
		return points;
	}

	private LineString[] parseLineStrings(JsonArray array) {
		LineString[] strings = new LineString[array.size()];
		for (int i = 0; i != array.size(); ++i) {
			strings[i] = geometryFactory.createLineString(parseLineString(array.get(i).getAsJsonArray()));
		}
		return strings;
	}
}