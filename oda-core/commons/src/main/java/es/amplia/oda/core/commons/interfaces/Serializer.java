package es.amplia.oda.core.commons.interfaces;

import java.io.IOException;

/**
 * Interface to implement by all classes providing serialization and deserialization
 */
public interface Serializer {

    /**
     * Parse a value to a byte array correspondent to the specialized serializer format
     * If any problem occur it returns null
     *
     * @param value Object to parse
     * @return Byte array of value in serializer specialized format format
     * @throws IOException If something goes wrong, throw IOException. Should be for bad input data
     */
    byte[] serialize(Object value) throws IOException;

    /**
     * Parse a byte array of the correspondent specialized serializer format to the target object type
     * If any problem occur it returns null
     *
     * @param value Byte array of the correspondent specialized serializer format
     * @param type Target type parameter class to parse the byte array
     * @param <T> Target type to parse the byte array
     * @return T instance correspondent to the parsed byte array
     * @throws IOException If something goes wrong, throw IOException. Should be for bad input data
     */
    <T> T deserialize(byte[] value, Class<T> type) throws IOException;
}
