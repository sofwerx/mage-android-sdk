package mil.nga.giat.mage.sdk.gson.deserializer;

import java.lang.reflect.Type;

import mil.nga.giat.mage.sdk.datastore.observation.Attachment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class AttachmentDeserializer implements JsonDeserializer<Attachment> {
	
	public static Gson getGsonBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Attachment.class, new AttachmentDeserializer());
		return gsonBuilder.create();
	}

	@Override
	public Attachment deserialize(JsonElement json, Type type, JsonDeserializationContext c) throws JsonParseException {
		JsonObject feature = json.getAsJsonObject();
		
		Attachment attachment = new Attachment();
		attachment.setContentType(feature.get("contentType").getAsString());
		attachment.setRemoteId(feature.get("id").getAsString());
		attachment.setName(feature.get("name").getAsString());
		attachment.setUrl(feature.get("url").getAsString());
		attachment.setSize(feature.get("size").getAsLong());

		return attachment;
	}
	
	
}
