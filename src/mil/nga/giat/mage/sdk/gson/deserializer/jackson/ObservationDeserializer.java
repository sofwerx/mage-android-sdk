package mil.nga.giat.mage.sdk.gson.deserializer.jackson;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.datastore.common.State;
import mil.nga.giat.mage.sdk.datastore.observation.Attachment;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationGeometry;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationProperty;
import mil.nga.giat.mage.sdk.utils.DateUtility;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;

public class ObservationDeserializer {

    private static final String LOG_NAME = ObservationDeserializer.class.getName();
    
    private static JsonFactory factory = new JsonFactory();
    private static ObjectMapper mapper = new ObjectMapper();
    private GeometryDeserializer geometryDeserializer = new GeometryDeserializer();
    private DateFormat iso8601Format = DateUtility.getISO8601();
    
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
                    observations.add(parseObservation(jsonParser));
                }
            }
        }

        jsonParser.close();
        return observations;
    }

    private Observation parseObservation(JsonParser jsonParser) throws Exception {
        Observation o = new Observation();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String name = jsonParser.getCurrentName();
            if ("id".equals(name)) {
                jsonParser.nextToken();
                o.setDirty(false);
                o.setRemoteId(jsonParser.getText());
            } else if ("userId".equals(name)) {
                jsonParser.nextToken();
                o.setUserId(jsonParser.getText());
            } else if ("deviceId".equals(name)) {
                jsonParser.nextToken();
                o.setDeviceId(jsonParser.getText());
            } else if ("lastModified".equals(name)) {
                jsonParser.nextToken();
                try {
                    Date d = iso8601Format.parse(jsonParser.getText());
                    o.setLastModified(d);
                } catch (ParseException e) {
                    Log.e(LOG_NAME, "Problem paring date.");
                }
            } else if ("url".equals(name)) {
                jsonParser.nextToken();
                o.setUrl(jsonParser.getText());
            } else if ("state".equals(name)) {
                jsonParser.nextToken();
                o.setState(parseState(jsonParser));
            } else if ("geometry".equals(name)) {
                jsonParser.nextToken();
                Geometry g = geometryDeserializer.parseGeometry(jsonParser);
                o.setObservationGeometry(new ObservationGeometry(g));
            } else if ("properties".equals(name)) {
                jsonParser.nextToken();
                o.setProperties(parseProperties(jsonParser));
            } else if ("attachments".equals(name)) {
                jsonParser.nextToken();
                o.setAttachments(parseAttachments(jsonParser));
            } else {
                jsonParser.skipChildren();
            }
        }

        return o;
    }

    private State parseState(JsonParser jsonParser) throws Exception {
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

    private Collection<ObservationProperty> parseProperties(JsonParser jsonParser) throws Exception {
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

    private Collection<Attachment> parseAttachments(JsonParser jsonParser) throws Exception {
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
                } else {
                    jsonParser.skipChildren();
                }
            }
            a.setDirty(false);
            attachments.add(a);
        }

        return attachments;
    }
}