package es.amplia.oda.service.jsonserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.amplia.oda.core.commons.interfaces.Serializer;

import java.io.IOException;

public class JsonSerializer implements Serializer {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public byte[] serialize(Object value) throws IOException {
		return MAPPER.writeValueAsBytes(value);
	}

	@Override
	public <T> T deserialize(byte[] value, Class<T> type) throws IOException {
		return MAPPER.readValue(value, type);
	}
}
