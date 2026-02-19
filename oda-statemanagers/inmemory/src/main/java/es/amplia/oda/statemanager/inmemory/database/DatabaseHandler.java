package es.amplia.oda.statemanager.inmemory.database;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.statemanager.DatabaseException;
import es.amplia.oda.core.commons.statemanager.SQLStatements;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.Scheduler;
import es.amplia.oda.statemanager.inmemory.derby.DerbyStatements;
import org.apache.derby.iapi.services.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DatabaseHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);

	String path;
	Serializer serializer;
	private final int maxHistoricalData;
	private final long forgetTime;

	Connection connection;
	DatatypesUtils datatypesUtils;
	SQLStatements statements = new DerbyStatements();

	// statement where queries are stored before commit
	List<String> insertStatementsToCommit = new ArrayList<>();
	List<String> updateStatementsToCommit = new ArrayList<>();
	List<String> deleteStatementsToCommit = new ArrayList<>();
	String questionMarkRegex = "\\?";


	public DatabaseHandler(String databasePath, Serializer serializer, Scheduler scheduler, int maxData, long forgetTime, long forgetPeriod) {
		this.path = databasePath;
		this.serializer = serializer;
		this.maxHistoricalData = maxData;
		this.forgetTime = forgetTime;
		this.datatypesUtils = new DatatypesUtils(serializer);
		if (!exists()) {
			createDatabase();
		} else {
			connectDatabase();
		}

		// create a periodical task to commit data to database
		scheduler.clear();
		scheduler.schedule(this::commitAllToDatabase, forgetPeriod, forgetPeriod, TimeUnit.SECONDS);
    }

	private void createDatabase() {
		try {
			LOGGER.info("Creating the local database");
			Class.forName(statements.getDriverClassName());
			connection = DriverManager.getConnection(statements.getProtocolUrlDatabase() + path + statements.getExtraOptions());
			update(statements.getCreateStateTableStatement());
			update(statements.getTimeIndexStatement());
			update(statements.getDeleteIndexStatement());
		} catch (SQLException e) {
			LOGGER.error("Error trying to create the database: {}", e.getSQLState());
			restartTryOfConnect();
		} catch (ClassNotFoundException e) {
			LOGGER.error("Class of the current database library not found: {}", e.getMessage());
			restartTryOfConnect();
		}
	}

	private void connectDatabase() {
		Statement stmt;
		try {
			LOGGER.info("Starting connection with the local database");
			Class.forName(statements.getDriverClassName());
			connection = DriverManager.getConnection(statements.getProtocolUrlDatabase() + path + statements.getExtraOptions());
			stmt = connection.createStatement();
			ResultSet resultSet = stmt.executeQuery(statements.getQueryToGetTables());
			if (resultSet.next() && resultSet.getString(1).equalsIgnoreCase("state")) {
				LOGGER.info("Table state exists, connection was established");
			} else {
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
		if(FileUtil.removeDirectory(f)) {
			createDatabase();
		} else {
			LOGGER.error("Impossible to create the database. Check that database path configuration is alright");
		}
	}

	public synchronized Map<DatastreamInfo, List<DatastreamValue>> collectDataFromDatabase() {
		deleteOldHistoricDataAtStart();
		deleteHistoricMaxDataAtStart();
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			String partialStatement = statements.getObtainStoredDataStatement();
			ResultSet result = stmt.executeQuery(partialStatement);
			LOGGER.trace("Executing query {}", partialStatement);
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

	private Map<DatastreamInfo, List<DatastreamValue>> generateMapOfData(ResultSet result) throws SQLException {
		Map<DatastreamInfo, List<DatastreamValue>> map = new HashMap<>();
		while (!result.isClosed() && result.next()) {
			try {
				// during DatastreamValue process, it is saved in the database, so if it exists in the database it means it has been processed
				DatastreamValue value = new DatastreamValue(result.getString(1), result.getString(2),
						result.getString(3), result.getLong(4), 
						datatypesUtils.parseStoredData(result.getString(6), result.getString(7)),
						parseStatus(result.getString(8)), result.getString(9),
						result.getBoolean(10), true);
				value.setDate(result.getLong(5));
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

	private void commitAllToDatabase() {

		// add query to remove by oldest date
		deleteOldHistoricData();

		List<String> statementsToCommit = new ArrayList<>(this.insertStatementsToCommit);
		commitBatchToDatabase(statementsToCommit);
		// remove commited SQLs
		this.insertStatementsToCommit.removeAll(statementsToCommit);

		statementsToCommit.clear();
		statementsToCommit = new ArrayList<>(this.updateStatementsToCommit);
		commitBatchToDatabase(statementsToCommit);
		this.updateStatementsToCommit.removeAll(statementsToCommit);

		statementsToCommit.clear();
		statementsToCommit = new ArrayList<>(this.deleteStatementsToCommit);
		commitBatchToDatabase(statementsToCommit);
		this.deleteStatementsToCommit.removeAll(statementsToCommit);
	}

	private void commitBatchToDatabase(List<String> statementsToCommit) {
		int batchSize = 200;
		try {
			// disabling autocommit and commit all queries at once, reduces executing time
			connection.setAutoCommit(false);
			Statement stmt = connection.createStatement();

			for (int i = 0; i < statementsToCommit.size(); i++) {
				String sql = statementsToCommit.get(i);
				LOGGER.debug("Executing query: {}", sql);
				stmt.addBatch(sql);

				// it is better to do small batches than one big batch
				if (i % batchSize == 0) {
					stmt.executeBatch();
				}
			}

			// do final batch
			stmt.executeBatch();
			// commit update
			connection.commit();

		} catch (SQLException e) {
			LOGGER.error("Error executing batch query to database: ", e);
		}
	}

	private synchronized void update(String sql) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			LOGGER.trace("Executing query {} ", sql);
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

		// prepare string sql
		String insertSql = statements.getInsertNewDataRowStatement();

		String className = datatypesUtils.getClassNameOf(value.getValue());
		if(className == null) {
			return false;
		}
		String data = new String(serializer.serialize(value.getValue()), StandardCharsets.UTF_8);

		// replace parameters
		insertSql = insertSql.replaceFirst(questionMarkRegex, "'" + value.getDeviceId() + "'");
		insertSql = insertSql.replaceFirst(questionMarkRegex, "'" + value.getDatastreamId() + "'");
		insertSql = insertSql.replaceFirst(questionMarkRegex, "'" + (value.getFeed() != null ? value.getFeed() : "null") + "'");
		insertSql = insertSql.replaceFirst(questionMarkRegex, String.valueOf(value.getAt()));
		insertSql = insertSql.replaceFirst(questionMarkRegex, String.valueOf(value.getDate()));

		insertSql = insertSql.replaceFirst(questionMarkRegex, "'" + data + "'");
		insertSql = insertSql.replaceFirst(questionMarkRegex, "'" + className + "'");
		insertSql = insertSql.replaceFirst(questionMarkRegex, "'" + value.getStatus().toString() + "'");
		insertSql = insertSql.replaceFirst(questionMarkRegex, "'" + (value.getError() != null ? value.getError() : "null") + "'");
		insertSql = insertSql.replaceFirst(questionMarkRegex, String.valueOf(value.getSent()));

		this.insertStatementsToCommit.add(insertSql);
		return true;
	}

	public void updateDataAsSent(String deviceId, String datastreamId, long at) {
		// prepare string sql
		String updateSql = statements.getUpdateSentData();

		// replace parameters
		updateSql = updateSql.replaceFirst(questionMarkRegex, "'" + deviceId + "'");
		updateSql = updateSql.replaceFirst(questionMarkRegex, "'" + datastreamId + "'");
		updateSql = updateSql.replaceFirst(questionMarkRegex, String.valueOf(at));

		this.updateStatementsToCommit.add(updateSql);
	}

	public void deleteExcessiveHistoricMaxData(String deviceId, String datastreamId, long maxDateToRetain) {
		LOGGER.trace("Erasing historic data in database by maxData parameter for deviceId {} and datastreamId {}", deviceId, datastreamId);

		// prepare string sql
		String deleteSql = statements.getDeleteOverloadDataFromADatastreamStatement();
		// replace parameters
		deleteSql = deleteSql.replaceFirst(questionMarkRegex, "'" + deviceId + "'");
		deleteSql = deleteSql.replaceFirst(questionMarkRegex, "'" + datastreamId + "'");
		deleteSql = deleteSql.replaceFirst(questionMarkRegex, String.valueOf(maxDateToRetain));

		this.deleteStatementsToCommit.add(deleteSql);
	}

	private void deleteOldHistoricData() {
		// delete by datastream datetime
		// remove datastreams whose datetime it's older than forgettime
		long maxTimeToRetain = System.currentTimeMillis() - (this.forgetTime * 1000);

		LOGGER.trace("Erasing historic data in database with date inferior to {} by forgetTime parameter", maxTimeToRetain);

		// prepare string sql
		String deleteSql = statements.getDeleteOlderDataFromDatabaseStatement();
		// replace parameters
		deleteSql = deleteSql.replaceFirst(questionMarkRegex, String.valueOf(maxTimeToRetain));
		this.deleteStatementsToCommit.add(deleteSql);
	}

	private void deleteOldHistoricDataAtStart() {
		// delete by datastream datetime
		// remove datastreams whose datetime it's older than forgettime
		long maxTimeToRetain = System.currentTimeMillis() - (this.forgetTime * 1000);

		LOGGER.trace("Erasing historic data in database with date inferior to {} by forgetTime parameter", maxTimeToRetain);

		// prepare and execute query
		List<Object> params = Collections.singletonList(maxTimeToRetain);
		String sql = statements.getDeleteOlderDataFromDatabaseStatement();
		PreparedStatement prstmt = null;

		try {
			prstmt = connection.prepareStatement(sql);
			for (int i = 1; i <= params.size(); i++) {
				datatypesUtils.insertParameter(prstmt, i, params.get(i - 1));
			}
			LOGGER.trace("Executing query {} with parameters {}", sql, params);
			prstmt.executeUpdate();
		} catch (SQLException throwables) {
			LOGGER.error("Impossible execute the update: {} with values {}", sql, params);
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

	private void deleteHistoricMaxDataAtStart() {
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			// first get the combinations of datastreamId and deviceId that have more values than maxHistoricalData
			String partialStatement = statements.getExcessHistoricDataFromDatabaseStatement();
			stmt = connection.prepareStatement(partialStatement);
			stmt.setInt(1, maxHistoricalData);
			result = stmt.executeQuery();
			LOGGER.trace("Executing query {} with parameters {}", partialStatement, maxHistoricalData);
			// parse SQL results
			Map<String, String> existingValues = new HashMap<>();
			while (result.next()) {
				String deviceId = result.getString("deviceId");
				String datastreamId = result.getString("datastreamId");
				existingValues.put(deviceId, datastreamId);
			}
			// for every combination of deviceId and datastreamId, erase max data
			for (Map.Entry<String, String> pair : existingValues.entrySet()) {
				String deviceId = pair.getKey();
				String datastreamId = pair.getValue();
				LOGGER.trace("Erasing historic data in database by maxData parameter for deviceId {} and datastreamId {}", deviceId, datastreamId);

				// get date to maintain
				partialStatement = statements.getSelectRowNOfADatastreamStatement();
				stmt = connection.prepareStatement(partialStatement);
				stmt.setString(1, deviceId);
				stmt.setString(2, datastreamId);
				stmt.setInt(3, maxHistoricalData - 1);
				result = stmt.executeQuery();
				LOGGER.trace("Executing query {} with parameters {}, {}, {}", partialStatement, deviceId, datastreamId, maxHistoricalData - 1);
				if (result.next()) {
					long date = result.getLong("date");
					result.close();
					stmt.close();
					List<Object> params = new ArrayList<>();
					params.add(deviceId);
					params.add(datastreamId);
					params.add(date);
					String sql = statements.getDeleteOverloadDataFromADatastreamStatement();
					stmt = connection.prepareStatement(sql);
					for (int i = 1; i <= params.size(); i++) {
						datatypesUtils.insertParameter(stmt, i, params.get(i - 1));
					}
					LOGGER.trace("Executing query {} with parameters {}", sql, params);
					stmt.executeUpdate();
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException("Error trying to execute an update: " + e.getSQLState());
		} finally {
			try {
				if (result != null) {
					result.close();
				}
				if (stmt != null) {
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