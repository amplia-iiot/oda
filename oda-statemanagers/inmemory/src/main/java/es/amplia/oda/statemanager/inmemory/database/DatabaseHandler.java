package es.amplia.oda.statemanager.inmemory.database;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.statemanager.DatabaseException;
import es.amplia.oda.core.commons.statemanager.SQLStatements;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.statemanager.inmemory.derby.DerbyStatements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class DatabaseHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);

	String path;
	Serializer serializer;
	private final int maxHistoricalData;
	private final long forgetTime;

	Connection connection;
	DatatypesUtils datatypesUtils;
	SQLStatements statements = new DerbyStatements();

	public DatabaseHandler(String databasePath, Serializer serializer, int maxData, long forgetTime) {
		this.path = databasePath;
		this.serializer = serializer;
		this.maxHistoricalData = maxData;
		this.forgetTime = forgetTime;
		this.datatypesUtils = new DatatypesUtils(serializer);
		if(!exists()) {
			createDatabase();
		} else {
			connectDatabase();
		}
	}

	private void createDatabase() {
		try {
			LOGGER.info("Creating the local database");
			Class.forName(statements.getDriverClassName());
			connection = DriverManager.getConnection(statements.getProtocolUrlDatabase() + path + statements.getExtraOptions());
			update(statements.getCreateStateTableStatement());
			update(statements.getIdIndexStatement());
			update(statements.getTimeIndexStatement());
		} catch (SQLException e) {
			LOGGER.error("Error trying to create the database: {}", e.getSQLState());
			restartTryOfConnect();
		} catch (ClassNotFoundException e) {
			LOGGER.error("Class of the current database library not found: {}", e.getMessage());
			restartTryOfConnect();
		}
	}

	private void connectDatabase() {
		try {
			LOGGER.info("Starting connection with the local database");
			Class.forName(statements.getDriverClassName());
			connection = DriverManager.getConnection(statements.getProtocolUrlDatabase() + path + statements.getExtraOptions());
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet resultSet = metadata.getTables(null, null, "state", null);
			if(resultSet.next()) {
				LOGGER.info("Table state exists, connection was established");
			}
			else {
				restartTryOfConnect();
			}
			LOGGER.info("Connection with the database achieved");
		} catch (SQLException | ClassNotFoundException e) {
			LOGGER.error("Exception caught trying to connect to the database {}", e.getMessage());
			restartTryOfConnect();
		}
	}

	private void restartTryOfConnect() {
		File f = new File(path);
		if(f.delete()) {
			createDatabase();
		} else {
			LOGGER.error("Impossible to create the database. Check that database path configuration is alright");
		}
	}

	public synchronized Map<DatastreamInfo, List<DatastreamValue>> collectDataFromDatabase() {
		deleteOldHistoricData();
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet result = stmt.executeQuery(statements.getObtainStoredDataStatement());
			Map<DatastreamInfo, List<DatastreamValue>> map = generateMapOfData(result);
			stmt.close();
			result.close();
			return map;
		} catch (SQLException e) {
			throw new DatabaseException("Error trying to execute a query: " + e.getSQLState());
		} finally {
			try {
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.warn("Couldn't close a statement of the query.");
			}
		}
	}

	private int countHistoricalData(String deviceId, String datastreamId) {
		PreparedStatement prstmt = null;
		ResultSet result = null;
		try {
			prstmt = connection.prepareStatement(statements.getCountRowsOfADatastreamStatement());
			datatypesUtils.insertParameter(prstmt, 1, deviceId);
			datatypesUtils.insertParameter(prstmt, 2, datastreamId);
			result = prstmt.executeQuery();
			if(result.next()) {
				return result.getInt("count");
			}
			else {
				throw new SQLException();
			}
		} catch (SQLException e) {
			throw new DatabaseException("Error trying to execute a query: " + e.getSQLState());
		} finally {
			try {
				if(result != null) {
					result.close();
				}
				if(prstmt != null) {
					prstmt.close();
				}
			} catch (SQLException e) {
				LOGGER.warn("Couldn't close a statement of the query.");
			}
		}

	}

	public Map<Long, Boolean> getDatapointsSentValue(String deviceId, String datastreamId) {
		deleteOldHistoricData();
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = connection.prepareStatement(statements.getUpdateIsDataSent());
			stmt.setString(1, deviceId);
			stmt.setString(2, datastreamId);
			result = stmt.executeQuery();
			Map<Long, Boolean> datapoints = new HashMap<>();
			while (result.next()) {
				long at = result.getLong("at");
				boolean sent = result.getBoolean("sent");
				datapoints.put(at, sent);
			}
			return datapoints;
		} catch (SQLException e) {
			throw new DatabaseException("Error trying to execute an update: " + e.getSQLState());
		} finally {
			try {
				if(result != null) {
					result.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.warn("Couldn't close a statement of the update.");
				try {
					stmt.close();
				} catch (SQLException ignored) {
					LOGGER.warn("Couldn't close a statement of the update.");
				}
			}
		}
	}

	private Map<DatastreamInfo, List<DatastreamValue>> generateMapOfData(ResultSet result) throws SQLException {
		Map<DatastreamInfo, List<DatastreamValue>> map = new HashMap<>();
		while (!result.isClosed() && result.next()) {
			try {
				DatastreamValue value = new DatastreamValue(result.getString(1), result.getString(2), result.getLong(3), datatypesUtils.parseStoredData(result.getString(4), result.getString(5)), parseStatus(result.getString(6)), result.getString(7), result.getBoolean(8));
				DatastreamInfo info = new DatastreamInfo(result.getString(1), result.getString(2));
				List<DatastreamValue> values = map.get(info);
				if (values == null) {
					values = new ArrayList<>();
				}
				values.add(value);
				map.put(info, values);
			} catch(NullPointerException | IOException e) {
				LOGGER.warn("Error trying to load a data: {}", e.getMessage());
			}
		}
		return map;
	}

	private synchronized void update(String sql) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			LOGGER.error("Error trying to update the table: {}", e.getSQLState());
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.warn("Couldn't close a statement of the update.");
				}
			}
		}
	}

	public synchronized boolean insertNewRow(DatastreamValue value) throws IOException {
		List<Object> values = new ArrayList<>();
		values.add(value.getDeviceId());
		values.add(value.getDatastreamId());
		values.add(value.getAt());
		String className = datatypesUtils.getClassNameOf(value.getValue());
		if(className == null) {
			return false;
		}
		String data = new String(serializer.serialize(value.getValue()), StandardCharsets.UTF_8);
		values.add(data);
		values.add(className);
		values.add(value.getStatus().toString());
		values.add(value.getError());
		values.add(value.isSent());
		try {
			int rows = countHistoricalData(value.getDeviceId(), value.getDatastreamId());
			if (rows >= maxHistoricalData) {
				boolean deleted = deleteExcessiveHistoricData(value.getDeviceId(), value.getDatastreamId());
				if (!deleted) {
					throw new SQLException();
				}
			}
		} catch (SQLException e) {
			return false;
		}
		int changes = preparedUpdate(statements.getInsertNewDataRowStatement(), values);
		return changes == 1;
	}

	public synchronized int preparedUpdate(String sql, List<Object> parameters) {
		PreparedStatement prstmt = null;
		try {
			prstmt = connection.prepareStatement(sql);
			for (int i = 1; i <= parameters.size(); i++) {
				datatypesUtils.insertParameter(prstmt, i, parameters.get(i-1));
			}
			return prstmt.executeUpdate();
		} catch (SQLException throwables) {
			LOGGER.error("Impossible execute the update: {}", sql);
			throw new DatabaseException("Error trying to execute a query. It's possible that the selected table " +
					"doesn't exists or the specified fields aren't present in the table");
		} finally {
			if (prstmt != null) {
				try {
					prstmt.close();
				} catch (SQLException throwables) {
					LOGGER.warn("Couldn't close a statement of the update.");
				}
			}
		}
	}

	public void updateDataAsSent(String deviceId, String datastreamId, long at) {
		List<Object> params = new ArrayList<>();
		params.add(deviceId);
		params.add(datastreamId);
		params.add(at);
		preparedUpdate(statements.getUpdateSentData(), params);
	}

	private boolean deleteExcessiveHistoricData(String deviceId, String datastreamId) {
		PreparedStatement prstmt = null;
		ResultSet result = null;
		try {
			prstmt = connection.prepareStatement(statements.getSelectRowNOfADatastreamStatement());
			prstmt.setString(1, deviceId);
			prstmt.setString(2, datastreamId);
			prstmt.setInt(3, maxHistoricalData - 1);
			result = prstmt.executeQuery();
			if (result.next()) {
				long at = result.getLong("at");
				result.close();
				prstmt.close();
				List<Object> params = new ArrayList<>();
				params.add(deviceId);
				params.add(datastreamId);
				params.add(at);
				int removed = preparedUpdate(statements.getDeleteOverloadDataFromADatastreamStatement(), params);
				return removed >= 1;
			}
		} catch (SQLException e) {
			LOGGER.error("Error trying to delete excesive amount of data: {}", e.getSQLState());
		} finally {
			try {
				if (result != null) {
					result.close();
				}
				if (prstmt != null) {
					prstmt.close();
				}
			} catch (SQLException e) {
				try {
					prstmt.close();
				} catch (SQLException ex) {
					LOGGER.error("Error trying to close the statement on deleting: {}", ex.getSQLState());
				}
				LOGGER.error("Error trying to close the statement on deleting: {}", e.getSQLState());
			}
		}
		return false;
	}

	private void deleteOldHistoricData() {
		long time = System.currentTimeMillis();
		time -= (this.forgetTime * 1000);
		List<Object> params = new ArrayList<>();
		params.add(time);
		preparedUpdate(statements.getDeleteOlderDataFromDatabaseStatement(), params);
	}

	public synchronized boolean exists() {
		File database = new File(path);
		return database.exists();
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

	public void close() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			LOGGER.warn("Error trying to close the connection to the database");
		}
	}
}