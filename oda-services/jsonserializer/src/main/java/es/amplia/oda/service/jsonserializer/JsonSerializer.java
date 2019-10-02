package es.amplia.oda.service.jsonserializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import es.amplia.oda.core.commons.interfaces.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

class JsonSerializer implements Serializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public byte[] serialize(Object value) throws IOException {
        return MAPPER.writeValueAsBytes(value);
    }

    @Override
    public <T> T deserialize(byte[] value, Class<T> type) throws IOException {
        return MAPPER.readValue(value, type);
    }
}
