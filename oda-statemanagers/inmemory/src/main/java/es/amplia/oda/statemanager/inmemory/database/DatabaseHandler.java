package es.amplia.oda.statemanager.inmemory.database;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DatabaseHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);

	private static final String SHORT_PRIMITIVE_TYPE_NAME  = "short";
	private static final String FLOAT_PRIMITIVE_TYPE_NAME  = "float";
	private static final String DOUBLE_PRIMITIVE_TYPE_NAME  = "double";
	private static final String BOOLEAN_PRIMITIVE_TYPE_NAME  = "boolean";

	String path;

	Serializer serializer;
	private final int maxHistoricalData;
	private final long forgetTime;

	Database database;

	public DatabaseHandler(String databasePath, Serializer serializer, int maxData, long forgetTime) {
		this.path = databasePath;
		this.serializer = serializer;
		this.maxHistoricalData = maxData;
		this.forgetTime = forgetTime;
		this.database = new Database(databasePath);
		if(!database.exists()) {
			this.database.create();
		} else {
			this.database.connect();
		}
	}

	public synchronized Map<DatastreamInfo, List<DatastreamValue>> collectDataFromDatabase() throws SQLException {
		deleteOldHistoricData();
		ResultSet result = database.query("SELECT * FROM state ORDER BY \"at\" DESC");
		Map<DatastreamInfo, List<DatastreamValue>> map = new HashMap<>();
		while (!result.isClosed() && result.next()) {
			try {
				DatastreamValue value = new DatastreamValue(result.getString(1), result.getString(2), result.getLong(3), parseStoredData(result.getString(4), result.getString(5)), parseStatus(result.getString(6)), result.getString(7));
				DatastreamInfo info = new DatastreamInfo(result.getString(1), result.getString(2));
				List<DatastreamValue> values = map.get(info);
				if (values == null) {
					values = new ArrayList<>();
				}
				values.add(value);
				map.put(info, values);
			} catch(NullPointerException | IOException e) {
				LOGGER.error("Error trying to load a data: {}", e.getMessage());
			}
		}
		this.database.closeQuery(result);
		return map;
	}

	private Object parseStoredData(String value, String type) throws IOException {
		switch (type) {
			case SHORT_PRIMITIVE_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), short.class);
			case "Short":
				return serializer.deserialize(value.getBytes(), Short.class);
			case "int":
				return serializer.deserialize(value.getBytes(), int.class);
			case "Integer":
				return serializer.deserialize(value.getBytes(), Integer.class);
			case "long":
				return serializer.deserialize(value.getBytes(), long.class);
			case "Long":
				return serializer.deserialize(value.getBytes(), Long.class);
			case FLOAT_PRIMITIVE_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), float.class);
			case "Float":
				return serializer.deserialize(value.getBytes(), Float.class);
			case DOUBLE_PRIMITIVE_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), double.class);
			case "Double":
				return serializer.deserialize(value.getBytes(), Double.class);
			case BOOLEAN_PRIMITIVE_TYPE_NAME:
				return serializer.deserialize(value.getBytes(), boolean.class);
			case "Boolean":
				return serializer.deserialize(value.getBytes(), Boolean.class);
			case "char":
				return serializer.deserialize(value.getBytes(), char.class);
			case "Character":
				return serializer.deserialize(value.getBytes(), Character.class);
			case "byte":
				return serializer.deserialize(value.getBytes(), byte.class);
			case "Byte":
				return serializer.deserialize(value.getBytes(), Byte.class);
			case "charArray":
				return serializer.deserialize(value.getBytes(), char[].class);
			case "byteArray":
				return serializer.deserialize(value.getBytes(), byte[].class);
			case "String":
				return serializer.deserialize(value.getBytes(), String.class);
			case "Array":
				return serializer.deserialize(value.getBytes(), ArrayList.class);
			default:
				return null;
		}
	}

	public synchronized boolean insertNewRow(DatastreamValue value) throws IOException {
		String sql = "INSERT INTO state (\"deviceId\", \"datastreamId\", \"at\", \"value\", \"type\", \"status\", \"error\") VALUES (?,?,?,?,?,?,?)";
		List<Object> values = new ArrayList<>();
		values.add(value.getDeviceId());
		values.add(value.getDatastreamId());
		values.add(value.getAt());
		String className = getClassNameOf(value.getValue());
		if(className == null) {
			return false;
		}
		String data = new String(serializer.serialize(value.getValue()), StandardCharsets.UTF_8);
		values.add(data);
		values.add(className);
		values.add(value.getStatus().toString());
		values.add(value.getError());
		try {
			int rows = countHistoricalData(value.getDeviceId(), value.getDatastreamId());
			if (rows >= maxHistoricalData) {
				boolean deleted = deleteOverloadHistoricData(value.getDeviceId(), value.getDatastreamId());
				if (!deleted) {
					throw new SQLException();
				}
			}
		} catch (SQLException throwables) {
			return false;
		}
		int changes = this.database.preparedUpdate(sql, values);
		return changes == 1;
	}

	private int countHistoricalData(String deviceId, String datastreamId) throws SQLException {
		String sql = "SELECT COUNT(*) as count FROM state WHERE \"deviceId\"='" + deviceId + "' AND \"datastreamId\"='" + datastreamId + "' ORDER BY 'at' DESC";
		ResultSet result = this.database.preparedQuery(sql, new ArrayList<>());
		if(result.next()) {
			int res = result.getInt("count");
			this.database.closeQuery(result);
			return res;
		}
		else {
			throw new SQLException();
		}
	}

	private boolean deleteOverloadHistoricData(String deviceId, String datastreamId) throws SQLException {
		String sql = "SELECT * FROM state WHERE \"datastreamId\"='" + datastreamId + "' AND \"deviceId\"='" + deviceId + "' ORDER BY \"at\" DESC OFFSET " + (maxHistoricalData -1) + " ROWS FETCH NEXT 1 ROW ONLY";
		ResultSet result = this.database.preparedQuery(sql, new ArrayList<>());
		if(result.next()) {
			long at = result.getLong("at");
			this.database.closeQuery(result);
			String remove = "DELETE FROM state WHERE \"deviceId\"='" + deviceId + "' AND \"datastreamId\"='" + datastreamId + "' AND \"at\"<=" + at;
			return this.database.update(remove) >= 1;
		}
		else {
			throw new SQLException();
		}
	}

	private void deleteOldHistoricData() {
		long time = System.currentTimeMillis();
		time -= (this.forgetTime * 1000);
		String remove = "DELETE FROM state WHERE ' AND \"at\"<" + time;
		this.database.update(remove);
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
			case "java.util.ArrayList":
				return "Array";
			default:
				return null;
		}
	}

	public boolean exists() {
		return this.database.exists();
	}

	public DatastreamValue.Status parseStatus(String status) {
		switch (status) {
			case "Successful":
				return DatastreamValue.Status.OK;
			case "Not found":
				return DatastreamValue.Status.NOT_FOUND;
			case "Error processing":
				return DatastreamValue.Status.PROCESSING_ERROR;
			default:
				return null;
		}
	}
}