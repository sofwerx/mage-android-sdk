package mil.nga.giat.mage.sdk.jackson.deserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryDeserializer {
    
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public Geometry parseGeometry(JsonParser jsonParser) throws Exception {
        String typeName = null;
        ArrayNode coordinates = null;
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String name = jsonParser.getCurrentName();
            if ("type".equals(name)) {
                jsonParser.nextToken();
                typeName = jsonParser.getText();
            } else if ("coordinates".equals(name)) {
                jsonParser.nextToken();
                coordinates = jsonParser.readValueAsTree();
            } else {
                jsonParser.skipChildren();
            }
        }

        Geometry geometry = null;
        if (typeName.equals("Point")) {
            geometry = geometryFactory.createPoint(new Coordinate(coordinates.get(0).asDouble(), coordinates.get(1).asDouble()));
        } else if (typeName.equals("MultiPoint")) {
            geometry = geometryFactory.createMultiPoint(parseLineString(coordinates));
        } else if (typeName.equals("LineString")) {
            geometry = geometryFactory.createLineString(parseLineString(coordinates));
        } else if (typeName.equals("MultiLineString")) {
            geometry = geometryFactory.createMultiLineString(parseLineStrings(coordinates));
        } else if (typeName.equals("Polygon")) {
            geometry = parsePolygonCoordinates(coordinates);
        } else if (typeName.equals("MultiPolygon")) {
            geometry = geometryFactory.createMultiPolygon(parsePolygons(coordinates));
        } else if (typeName.equals("GeometryCollection")) {
            geometry = geometryFactory.createGeometryCollection(parseGeometries(coordinates));
        }
        
        return geometry;
    }
    
    private Coordinate parseCoordinate(JsonNode coordinate) throws Exception {
        return new Coordinate(coordinate.get(0).asDouble(), coordinate.get(1).asDouble());
    }
    
    private Coordinate[] parseLineString(JsonNode array) throws Exception {
        Coordinate[] points = new Coordinate[array.size()];
        for (int i = 0; i < array.size(); ++i) {
            points[i] = parseCoordinate(array.get(i));
        }
        return points;
    }
    
    private LineString[] parseLineStrings(JsonNode array) throws Exception {
        LineString[] strings = new LineString[array.size()];
        for (int i = 0; i != array.size(); ++i) {
            strings[i] = geometryFactory.createLineString(parseLineString(array.get(i)));
        }
        return strings;
    }
    
    private Polygon parsePolygonCoordinates(JsonNode arrayOfRings) throws Exception {
        return geometryFactory.createPolygon(parseExteriorRing(arrayOfRings), parseInteriorRings(arrayOfRings));
    }

    private Geometry[] parseGeometries(JsonNode arrayOfGeoms) throws Exception {
        Geometry[] items = new Geometry[arrayOfGeoms.size()];
        for (int i = 0; i != arrayOfGeoms.size(); ++i) {
            items[i] = parseGeometry(arrayOfGeoms.get(i).traverse());
        }
        return items;
    }

    private Polygon[] parsePolygons(JsonNode arrayOfPolygons) throws Exception {
        Polygon[] polygons = new Polygon[arrayOfPolygons.size()];
        for (int i = 0; i != arrayOfPolygons.size(); i++) {
            polygons[i] = parsePolygonCoordinates(arrayOfPolygons.get(i));
        }
        return polygons;
    }

    private LinearRing parseExteriorRing(JsonNode arrayOfRings) throws Exception {
        return geometryFactory.createLinearRing(parseLineString(arrayOfRings.get(0)));
    }

    private LinearRing[] parseInteriorRings(JsonNode arrayOfRings) throws Exception {
        LinearRing rings[] = new LinearRing[arrayOfRings.size() - 1];
        for (int i = 1; i < arrayOfRings.size(); i++) {
            rings[i - 1] = geometryFactory.createLinearRing(parseLineString(arrayOfRings.get(i)));
        }
        return rings;
    }
}