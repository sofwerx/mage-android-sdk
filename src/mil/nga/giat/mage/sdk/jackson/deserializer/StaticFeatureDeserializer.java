package mil.nga.giat.mage.sdk.jackson.deserializer;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mil.nga.giat.mage.sdk.datastore.layer.Layer;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeature;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeatureGeometry;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeatureProperty;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;

public class StaticFeatureDeserializer extends Deserializer {
    
    private static JsonFactory factory = new JsonFactory();
    private static ObjectMapper mapper = new ObjectMapper();
    private GeometryDeserializer geometryDeserializer = new GeometryDeserializer();
    
    static {
        factory.setCodec(mapper);
    }
    
    public List<StaticFeature> parseStaticFeatures(InputStream is, Layer layer) throws Exception {
        List<StaticFeature> features = new ArrayList<StaticFeature>();
        
        JsonParser jsonParser = factory.createParser(is);
        jsonParser.nextToken();
        
        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) return features;

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String name = jsonParser.getCurrentName();
            if ("features".equals(name)) {
                jsonParser.nextToken();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    StaticFeature feature = parseFeature(jsonParser);
                    feature.setLayer(layer);
                    features.add(feature);
                }
            } else {
                skipField(jsonParser);
            }
        }

        jsonParser.close();
        return features;
    }

    private StaticFeature parseFeature(JsonParser jsonParser) throws Exception {
        StaticFeature o = new StaticFeature();
        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) return o;

        
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
            } else {
                skipField(jsonParser);
            }
        }

        return o;
    }

    private Collection<StaticFeatureProperty> parseProperties(JsonParser jsonParser)  throws Exception{
        Collection<StaticFeatureProperty> properties = new ArrayList<StaticFeatureProperty>();
        return parseProperties(jsonParser, properties, "");
    }
    
    private Collection<StaticFeatureProperty> parseProperties(JsonParser jsonParser, Collection<StaticFeatureProperty> properties, String keyPrefix) throws Exception {
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String key = keyPrefix + jsonParser.getCurrentName().toLowerCase();
            JsonToken token = jsonParser.nextToken();
            if (token == JsonToken.START_OBJECT) {
                parseProperties(jsonParser, properties, key);
            } else {
                String value = jsonParser.getText();
                properties.add(new StaticFeatureProperty(key, value));
                Log.i("static features", "static feature key inserted: " + key);
            }
        }
        
        return properties;
    }

}