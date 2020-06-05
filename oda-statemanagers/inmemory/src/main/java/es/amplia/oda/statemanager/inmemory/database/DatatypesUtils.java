package es.amplia.oda.statemanager.inmemory.database;

import es.amplia.oda.core.commons.interfaces.Serializer;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatatypesUtils {
	private final Serializer serializer;

	static final String SHORT_OBJECT_TYPE_NAME = "Short";
	static final String INT_OBJECT_TYPE_NAME = "Integer";
	static final String LONG_OBJECT_TYPE_NAME = "Long";
	static final String FLOAT_OBJECT_TYPE_NAME = "Float";
	static final String DOUBLE_OBJECT_TYPE_NAME = "Double";
	static final String BOOLEAN_OBJECT_TYPE_NAME = "Boolean";
	static final String CHAR_OBJECT_TYPE_NAME = "Character";
	static final String BYTE_OBJECT_TYPE_NAME = "Byte";
	static final String CHAR_ARRAY_PRIMITIVE_TYPE_NAME  = "charArray";
	static final String BYTE_ARRAY_PRIMITIVE_TYPE_NAME  = "byteArray";
	static final String STRING_TYPE_NAME = "String";
	static final String ARRAY_TYPE_NAME = "Array";

	private static final String SHORT_OBJECT_CLASS_NAME  = "java.lang.Short";
	private static final String INT_OBJECT_CLASS_NAME  = "java.lang.Integer";
	private static final String LONG_OBJECT_CLASS_NAME  = "java.lang.Long";
	private static final String FLOAT_OBJECT_CLASS_NAME  = "java.lang.Float";
	private static final String DOUBLE_OBJECT_CLASS_NAME  = "java.lang.Double";
	private static final String BOOLEAN_OBJECT_CLASS_NAME  = "java.lang.Boolean";
	private static final String CHAR_OBJECT_CLASS_NAME  = "java.lang.Character";
	private static final String BYTE_OBJECT_CLASS_NAME  = "java.lang.Byte";
	private static final String STRING_CLASS_NAME  = "java.lang.String";
	private static final String ARRAY_CLASS_NAME  = "java.util.ArrayList";

	public DatatypesUtils(Serializer serializer) {
		this.serializer = serializer;
	}

	public void insertParameter(PreparedStatement ps, int pos, Object parameter) throws SQLException {
		if (parameter == null) {
			ps.setNull(pos, 2);
			return;
		}
		switch (parameter.getClass().getName()) {
			case SHORT_OBJECT_CLASS_NAME:
				ps.setShort(pos, (Short)parameter);
				break;
			case INT_OBJECT_CLASS_NAME:
				ps.setInt(pos, (Integer)parameter);
				break;
			case LONG_OBJECT_CLASS_NAME:
				ps.setLong(pos, (Long)parameter);
				break;
			case FLOAT_OBJECT_CLASS_NAME:
				ps.setFloat(pos, (Float)parameter);
				break;
			case DOUBLE_OBJECT_CLASS_NAME:
				ps.setDouble(pos, (Double)parameter);
				break;
			case BOOLEAN_OBJECT_CLASS_NAME:
				ps.setBoolean(pos, (Boolean)parameter);
				break;
			case BYTE_OBJECT_CLASS_NAME:
				ps.setByte(pos, (Byte)parameter);
				break;
			case STRING_CLASS_NAME:
				ps.setString(pos, (String)parameter);
				break;
			default:
		}
	}

	public String getClassNameOf(Object value) {
		if (value.getClass() == char[].class) {
			return CHAR_ARRAY_PRIMITIVE_TYPE_NAME;
		}
		if (value.getClass() == byte[].class) {
			return BYTE_ARRAY_PRIMITIVE_TYPE_NAME;
		}
		switch (value.getClass().getName()) {
			case SHORT_OBJECT_CLASS_NAME:
				return SHORT_OBJECT_TYPE_NAME;
			case INT_OBJECT_CLASS_NAME:
				return INT_OBJECT_TYPE_NAME;
			case LONG_OBJECT_CLASS_NAME:
				return LONG_OBJECT_TYPE_NAME;
			case FLOAT_OBJECT_CLASS_NAME:
				return FLOAT_OBJECT_TYPE_NAME;
			case DOUBLE_OBJECT_CLASS_NAME:
				return DOUBLE_OBJECT_TYPE_NAME;
			case BOOLEAN_OBJECT_CLASS_NAME:
				return BOOLEAN_OBJECT_TYPE_NAME;
			case CHAR_OBJECT_CLASS_NAME:
				return CHAR_OBJECT_TYPE_NAME;
			case BYTE_OBJECT_CLASS_NAME:
				return BYTE_OBJECT_TYPE_NAME;
			case STRING_CLASS_NAME:
				return STRING_TYPE_NAME;
			case ARRAY_CLASS_NAME:
				return ARRAY_TYPE_NAME;
			default:
				return null;
		}
	}

	public Object parseStoredData(String value, String type) throws IOException {
		switch (type) {
			case SHORT_OBJECT_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), Short.class);
			case INT_OBJECT_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), Integer.class);
			case LONG_OBJECT_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), Long.class);
			case FLOAT_OBJECT_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), Float.class);
			case DOUBLE_OBJECT_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), Double.class);
			case BOOLEAN_OBJECT_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), Boolean.class);
			case CHAR_OBJECT_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), Character.class);
			case BYTE_OBJECT_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), Byte.class);
			case CHAR_ARRAY_PRIMITIVE_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), char[].class);
			case BYTE_ARRAY_PRIMITIVE_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), byte[].class);
			case STRING_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), String.class);
			case ARRAY_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), ArrayList.class);
			default:
				return null;
		}
	}
}
