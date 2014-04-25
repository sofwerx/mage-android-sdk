package mil.nga.giat.mage.sdk.jackson.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class Deserializer {

    protected void skipField(JsonParser jsonParser) throws JsonParseException, IOException {
        JsonToken token = jsonParser.getCurrentToken();
        if (token == JsonToken.FIELD_NAME) {
            token = jsonParser.nextToken();
            
            if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) {
                jsonParser.skipChildren();
            }
        }
    }
}