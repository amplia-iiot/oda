package es.amplia.oda.statemanager.inmemory.database;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.core.commons.utils.DatastreamValue;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHandler {
	private static final String SHORT_PRIMITIVE_TYPE_NAME  = "short";
	private static final String FLOAT_PRIMITIVE_TYPE_NAME  = "float";
	private static final String DOUBLE_PRIMITIVE_TYPE_NAME  = "double";
	private static final String BOOLEAN_PRIMITIVE_TYPE_NAME  = "boolean";

	String path;

	Serializer serializer;

	Database database;

	public DatabaseHandler(String databasePath, Serializer serializer) {
		this.path = databasePath;
		this.serializer = serializer;
		this.database = new Database(databasePath);
		if(database.exists()) {
			this.database.connect();
		} else {
			this.database.create();
		}
	}

	public Map<DatastreamInfo, List<DatastreamValue>> collectDataFromDatabase() throws SQLException, IOException {
		ResultSet result = database.query("SELECT * FROM state ORDER BY at DESC");
		Map<DatastreamInfo, List<DatastreamValue>> map = new HashMap<>();
		while(result.next()) {
			DatastreamValue value = new DatastreamValue(result.getString(1), result.getString(2), result.getInt(3), parseStoredData(result.getString(4), result.getString(5)), serializer.deserialize(result.getBlob(6).getBytes(0, (int)result.getBlob(6).length()), DatastreamValue.Status.class), result.getString(7));
			DatastreamInfo info = new DatastreamInfo(result.getString(1), result.getString(2));
			List<DatastreamValue> values = map.get(info);
			if (values == null) {
				values = new ArrayList<>();
			}
			values.add(value);
			map.put(info, values);
		}
		return map;
	}

	private Object parseStoredData(String value, String type) {
		switch (type) {
			case SHORT_PRIMITIVE_TYPE_NAME:
				return Short.parseShort(value);
			case "Short":
				return Short.valueOf(value);
			case "int":
				return Integer.parseInt(value);
			case "Integer":
				return Integer.valueOf(value);
			case "long":
				return Long.parseLong(value);
			case "Long":
				return Long.valueOf(value);
			case FLOAT_PRIMITIVE_TYPE_NAME:
				return Float.parseFloat(value);
			case "Float":
				return Float.valueOf(value);
			case DOUBLE_PRIMITIVE_TYPE_NAME:
				return Double.parseDouble(value);
			case "Double":
				return Double.valueOf(value);
			case BOOLEAN_PRIMITIVE_TYPE_NAME:
				return Boolean.parseBoolean(value);
			case "Boolean":
				return Boolean.valueOf(value);
			case "char":
				return value.charAt(0);
			case "Character":
				return (Character) value.charAt(0);
			case "byte":
				return value.getBytes()[0];
			case "Byte":
				return Byte.valueOf(value);
			case "charArray":
				return value.toCharArray();
			case "byteArray":
				return value.getBytes();
			case "String":
				return value;
			default:
				return null;
		}
	}

	public boolean insertNewRow(DatastreamValue value) throws IOException {
		String sql = "INSERT INTO state (deviceId, datastreamId, at, value, type, status, error) VALUES (?,?,?,?,?,?,?);";
		List<Object> values = new ArrayList<>();
		values.add(value.getDatastreamId());
		values.add(value.getDeviceId());
		values.add(value.getAt());
		values.add(value.getValue().toString());
		values.add(getClassNameOf(value.getValue()));
		values.add(new OdaBlob(serializer.serialize(value.getStatus())));
		values.add(value.getError());
		int changes = this.database.preparedUpdate(sql, values);
		return changes == 1;
	}

	private String getClassNameOf(Object value) {
		if (value.getClass() == char[].class) {
			return "charArray";
		}
		if (value.getClass() == byte[].class) {
			return "byteArray";
		}
		switch (value.getClass().getName()) {
			case SHORT_PRIMITIVE_TYPE_NAME:
				return SHORT_PRIMITIVE_TYPE_NAME;
			case "java.lang.Short":
				return "Short";
			case "int":
				return "int";
			case "java.lang.Integer":
				return "Integer";
			case "long":
				return "long";
			case "java.lang.Long":
				return "Long";
			case FLOAT_PRIMITIVE_TYPE_NAME:
				return FLOAT_PRIMITIVE_TYPE_NAME;
			case "java.lang.Float":
				return "Float";
			case DOUBLE_PRIMITIVE_TYPE_NAME:
				return DOUBLE_PRIMITIVE_TYPE_NAME;
			case "java.lang.Double":
				return "Double";
			case BOOLEAN_PRIMITIVE_TYPE_NAME:
				return BOOLEAN_PRIMITIVE_TYPE_NAME;
			case "java.lang.Boolean":
				return "Boolean";
			case "char":
				return "char";
			case "java.lang.Character":
				return "Character";
			case "byte":
				return "byte";
			case "java.lang.Byte":
				return "Byte";
			case "java.lang.String":
				return "String";
			default:
				return null;
		}
	}

	public boolean exists() {
		return this.database.exists();
	}
}
