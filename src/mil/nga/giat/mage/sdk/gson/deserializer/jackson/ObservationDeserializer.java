package mil.nga.giat.mage.sdk.gson.deserializer.jackson;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mil.nga.giat.mage.sdk.datastore.common.State;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationGeometry;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationProperty;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class ObservationDeserializer {

    private static JsonFactory factory = new JsonFactory();
    private static ObjectMapper mapper = new ObjectMapper();
    private final GeometryFactory geometryFactory = new GeometryFactory();
    
    static {
        factory.setCodec(mapper);
    }
    
    public List<Observation> parseObservations(InputStream is) throws Exception {
        JsonParser jsonParser = factory.createParser(is);
        
        List<Observation> observations = new ArrayList<Observation>();

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String name = jsonParser.getCurrentName();
            if ("features".equals(name)) {
                jsonParser.nextToken();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    observations.add(parserObservation(jsonParser));
                }
            }
        }

        jsonParser.close();
        return observations;
    }

    public Observation parserObservation(JsonParser jsonParser) throws Exception {
        Observation o = new Observation();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String name = jsonParser.getCurrentName();
            if ("id".equals(name)) {
                jsonParser.nextToken();
                o.setRemoteId(jsonParser.getText());
            } else if ("url".equals(name)) {
                jsonParser.nextToken();
                o.setUrl(jsonParser.getText());
            } else if ("state".equals(name)) {
                jsonParser.nextToken();
                o.setState(parseState(jsonParser));
            } else if ("geometry".equals(name)) {
                jsonParser.nextToken();
                o.setObservationGeometry(new ObservationGeometry(parseGeometry(jsonParser)));
            } else if ("properties".equals(name)) {
                jsonParser.nextToken();
                o.setProperties(parseProperties(jsonParser));
            } else if ("attachments".equals(name)) {
                jsonParser.nextToken();
                o.setAttachments(parseAttachments(jsonParser));
            }
        }

        return o;
    }

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

    public State parseState(JsonParser jsonParser) throws Exception {
        State state = null;

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String name = jsonParser.getCurrentName();
            if ("name".equals(name)) {
                jsonParser.nextToken();
                state = State.ACTIVE;
            }
        }

        return state;
    }

    public Collection<ObservationProperty> parseProperties(JsonParser jsonParser) throws Exception {
        Collection<ObservationProperty> properties = new ArrayList<ObservationProperty>();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String key = jsonParser.getCurrentName();
            JsonToken token = jsonParser.nextToken();
            if (token == JsonToken.START_OBJECT) {
                jsonParser.skipChildren();
            } else {
                String value = jsonParser.getText();
                properties.add(new ObservationProperty(key, value));
            }
        }

        return properties;
    }

    public Collection<Attachment> parseAttachments(JsonParser jsonParser) throws Exception {
        Collection<Attachment> attachments = new ArrayList<Attachment>();
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            Attachment a = new Attachment();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String name = jsonParser.getCurrentName();
                if ("id".equals(name)) {
                    jsonParser.nextToken();
                    a.setRemoteId(jsonParser.getText());
                } else if ("contentType".equals(name)) {
                    jsonParser.nextToken();
                    a.setContentType(jsonParser.getText());
                } else if ("size".equals(name)) {
                    jsonParser.nextToken();
                    a.setSize(jsonParser.getLongValue());
                } else if ("name".equals(name)) {
                    jsonParser.nextToken();
                    a.setName(jsonParser.getText());
                } else if ("relativePath".equals(name)) {
                    jsonParser.nextToken();
                    a.setRemotePath(jsonParser.getText());
                } else if ("url".equals(name)) {
                    jsonParser.nextToken();
                    a.setUrl(jsonParser.getText());
                }
            }
            a.setDirty(false);
            attachments.add(a);
        }

        return attachments;
    }
}