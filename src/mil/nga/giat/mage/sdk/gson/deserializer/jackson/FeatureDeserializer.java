package mil.nga.giat.mage.sdk.gson.deserializer.jackson;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeature;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeatureGeometry;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeatureProperty;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;

public class FeatureDeserializer {
    
    private static JsonFactory factory = new JsonFactory();
    private static ObjectMapper mapper = new ObjectMapper();
    private GeometryDeserializer geometryDeserializer = new GeometryDeserializer();
    
    static {
        factory.setCodec(mapper);
    }
    
    public List<StaticFeature> parseObservations(InputStream is) throws Exception {
        JsonParser jsonParser = factory.createParser(is);
        
        List<StaticFeature> features = new ArrayList<StaticFeature>();

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String name = jsonParser.getCurrentName();
            if ("features".equals(name)) {
                jsonParser.nextToken();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    features.add(parseFeature(jsonParser));
                }
            }
        }

        jsonParser.close();
        return features;
    }

    private StaticFeature parseFeature(JsonParser jsonParser) throws Exception {
        StaticFeature o = new StaticFeature();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String name = jsonParser.getCurrentName();
            if ("id".equals(name)) {
                jsonParser.nextToken();
                o.setRemoteId(jsonParser.getText());
            } else if ("geometry".equals(name)) {
                jsonParser.nextToken();
                Geometry g = geometryDeserializer.parseGeometry(jsonParser);
                o.setStaticFeatureGeometry(new StaticFeatureGeometry(g));
            } else if ("properties".equals(name)) {
                jsonParser.nextToken();
                o.setProperties(parseProperties(jsonParser));
            }
        }

        return o;
    }

    private Collection<StaticFeatureProperty> parseProperties(JsonParser jsonParser) throws Exception {
        Collection<StaticFeatureProperty> properties = new ArrayList<StaticFeatureProperty>();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String key = jsonParser.getCurrentName();
            JsonToken token = jsonParser.nextToken();
            if (token == JsonToken.START_OBJECT) {
                jsonParser.skipChildren();
            } else {
                String value = jsonParser.getText();
                properties.add(new StaticFeatureProperty(key, value));
            }
        }

        return properties;
    }

}