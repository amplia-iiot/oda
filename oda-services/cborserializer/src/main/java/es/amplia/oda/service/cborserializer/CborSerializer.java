package es.amplia.oda.service.cborserializer;

import es.amplia.oda.core.commons.interfaces.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import java.io.IOException;

class CborSerializer implements Serializer {

    private static final ObjectMapper MAPPER = new ObjectMapper(new CBORFactory());

    @Override
    public byte[] serialize(Object value) throws IOException {
        return MAPPER.writeValueAsBytes(value);
    }

    @Override
    public <T> T deserialize(byte[] value, Class<T> type) throws IOException {
        return MAPPER.readValue(value, type);
    }
}
