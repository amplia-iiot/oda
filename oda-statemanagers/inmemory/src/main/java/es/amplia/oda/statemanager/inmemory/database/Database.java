package es.amplia.oda.statemanager.inmemory.database;

import es.amplia.oda.core.commons.statemanager.DatabaseException;
import es.amplia.oda.core.commons.statemanager.StateManagerDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.List;

public class Database implements StateManagerDatabase {
	private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
	String path;

	Connection connection;

	public Database(String database) {
		path = database;
	}

	@Override
	public void connect() {
		try {
			connection = DriverManager.getConnection(path);
		} catch (SQLException throwables) {
			LOGGER.error("Couldn't connect to the database.");
			throw new DatabaseException("Error trying to connect to the database");
		}
	}

	@Override
	public void disconnect() {
		try {
			connection.close();
		} catch (SQLException throwables) {
			LOGGER.error("Couldn't disconnect to the database. Is database still in its place?");
			throw new DatabaseException("Error trying to disconnect to the database");
		}
	}

	@Override
	public boolean exists() {
		File database = new File(path);
		return database.exists();
	}

	@Override
	public void create() {
		try {
			connection = DriverManager.getConnection(path);
			execute("CREATE TABLE IF NOT EXISTS state (" +
						"deviceId text," +
						"datastreamId text," +
						"at integer," +
						"value text," +
						"type text," +
						"status blob," +
						"error text" +
					");");
		} catch (SQLException throwables) {
			LOGGER.error("Couldn't create the database ");
			throw new DatabaseException("Error trying to create the database");
		}
	}

	@Override
	public ResultSet query(String sql) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			return stmt.executeQuery(sql);
		} catch (SQLException throwables) {
			LOGGER.error("Impossible execute the query: {}", sql);
			throw new DatabaseException("Error trying to execute a query. It's possible that the selected table " +
					"doesn't exists or the specified fields aren't present in the table");
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException throwables) {
					LOGGER.error("Couldn't close a statement of the query.");
				}
			}
		}
	}

	@Override
	public Object preparedQuery(String sql, List<Object> parameters) {
		PreparedStatement prstmt = null;
		try {
			prstmt = connection.prepareStatement(sql);
			for (int i = 0; i < parameters.size(); i++) {
				prstmt.setObject(i, parameters.get(i));
			}
			return prstmt.executeQuery(sql);
		} catch (SQLException throwables) {
			LOGGER.error("Impossible execute the prepared query: {}", sql);
			throw new DatabaseException("Error trying to execute a prepared query. It's possible that the selected table " +
					"doesn't exists or the specified fields aren't present in the table");
		} finally {
			if (prstmt != null) {
				try {
					prstmt.close();
				} catch (SQLException throwables) {
					LOGGER.error("Couldn't close a prepared statement of the query.");
				}
			}
		}
	}

	@Override
	public int update(String sql) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			return stmt.executeUpdate(sql);
		} catch (SQLException throwables) {
			LOGGER.error("Impossible execute the update: {}", sql);
			throw new DatabaseException("Error trying to execute a update.");
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException throwables) {
					LOGGER.error("Couldn't close a statement of the update.");
				}
			}
		}
	}

	@Override
	public int preparedUpdate(String sql, List<Object> parameters) {
		PreparedStatement prstmt = null;
		try {
			prstmt = connection.prepareStatement(sql);
			for (int i = 0; i < parameters.size(); i++) {
				prstmt.setObject(i, parameters.get(i));
			}
			return prstmt.executeUpdate(sql);
		} catch (SQLException throwables) {
			LOGGER.error("Impossible execute the prepared update: {}", sql);
			throw new DatabaseException("Error trying to execute a prepared update.");
		} finally {
			if (prstmt != null) {
				try {
					prstmt.close();
				} catch (SQLException throwables) {
					LOGGER.error("Couldn't close a prepared statement of the update.");
				}
			}
		}
	}

	@Override
	public Object execute(String sql) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			return stmt.execute(sql);
		} catch (SQLException throwables) {
			LOGGER.error("Impossible execute the operation: {}", sql);
			throw new DatabaseException("Error trying to execute an operation.");
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException throwables) {
					LOGGER.error("Couldn't close a statement of the operation.");
				}
			}
		}
	}

	@Override
	public Object preparedExecute(String sql, List<Object> parameters) {
		PreparedStatement prstmt = null;
		try {
			prstmt = connection.prepareStatement(sql);
			for (int i = 0; i < parameters.size(); i++) {
				prstmt.setObject(i, parameters.get(i));
			}
			return prstmt.execute(sql);
		} catch (SQLException throwables) {
			LOGGER.error("Impossible execute the prepared operation: {}", sql);
			throw new DatabaseException("Error trying to execute a prepared operation.");
		} finally {
			if (prstmt != null) {
				try {
					prstmt.close();
				} catch (SQLException throwables) {
					LOGGER.error("Couldn't close a prepared statement of the operation.");
				}
			}
		}
	}
}
