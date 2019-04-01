package es.amplia.oda.core.commons.interfaces;

import java.io.IOException;

public interface Serializer {

	enum SERIALIZER_TYPE {
		JSON,
		CBOR
	}

	static final String TYPE_PROPERTY_NAME = "type";

	/**
	 * Parse a value to bytestring in CBOR format.
	 * If any problem occur it returns null
	 *
	 * @param value Object to parse
	 * @return Bytestring of value in CBOR format
	 * @throws IOException If something goes wrong, throw IOException. Should be for bad input data
	 */
	byte[] serialize(Object value) throws IOException;

	/**
	 * Parse a bytestring to the target object type
	 * If any problem occur it returns null
	 *
	 * @param value Bytestring of value in CBOR format
	 * @param type Type target to parse value
	 * @param <T> ~
	 * @return Object of T type parsed from value
	 * @throws IOException If something goes wrong, throw IOException. Should be for bad input data
	 */
	<T> T deserialize(byte[] value, Class<T> type) throws IOException;
}
