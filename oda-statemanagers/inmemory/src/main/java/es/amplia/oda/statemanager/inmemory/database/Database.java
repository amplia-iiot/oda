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

	private static final String PROTOCOL_URL_DATABASE_LIBRARY = "jdbc:derby:";
	private static final String EXTRA_OPTIONS_URL = ";create=true";
	
	String path;

	Connection connection = null;

	public Database(String database) {
		path = database;
	}

	@Override
	public synchronized void connect() {
		try {
			LOGGER.info("Starting connection with the local database");
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			connection = DriverManager.getConnection(PROTOCOL_URL_DATABASE_LIBRARY + path + EXTRA_OPTIONS_URL);
			LOGGER.info("Connection with the database achieved");
		} catch (SQLException | ClassNotFoundException e) {
			LOGGER.error("Exception caught trying to connect to the database {}", e.getMessage());
		}
	}

	@Override
	public synchronized void disconnect() {
		try {
			LOGGER.info("Disconnecting with the local database");
			connection.close();
			LOGGER.info("Disconnection with the database achieved");
		} catch (SQLException throwables) {
			LOGGER.error("Couldn't disconnect to the database. Is database still in its place? {}", throwables.getNextException().getSQLState());
			throw new DatabaseException("Error trying to disconnect to the database");
		}
	}

	@Override
	public synchronized boolean exists() {
		File database = new File(path);
		return database.exists();
	}

	@Override
	public synchronized void create() {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			connection = DriverManager.getConnection(PROTOCOL_URL_DATABASE_LIBRARY + path + EXTRA_OPTIONS_URL);
			update("CREATE TABLE state (" +
					"\"deviceId\" varchar(255), " +
					"\"datastreamId\" varchar(255), " +
					"\"at\" bigint, " +
					"\"value\" varchar(5000)," +
					"\"type\" varchar(255)," +
					"\"status\" varchar(255)," +
					"\"error\" varchar(255)" +
					")");
			update("CREATE INDEX idx_datastream ON state (\"deviceId\", \"datastreamId\")");
			update("CREATE INDEX idx_at ON state (\"at\")");
		} catch (SQLException e) {
			LOGGER.error("Error trying to create the database: {}", e.getSQLState());
		} catch (ClassNotFoundException e) {
			LOGGER.error("Class of the current database library not found: {}", e.getMessage());
		}
	}

	@Override
	public synchronized ResultSet query(String sql) {
		Statement stmt;
		try {
			stmt = connection.createStatement();
			return stmt.executeQuery(sql);
		} catch (SQLException throwables) {
			throw new DatabaseException("Error trying to execute a query: " + throwables.getSQLState());
		}
	}

	@Override
	public synchronized ResultSet preparedQuery(String sql, List<Object> parameters) {
		PreparedStatement prstmt;
		try {
			prstmt = connection.prepareStatement(sql);
			for (int i = 1; i <= parameters.size(); i++) {
				prstmt = insertParameter(prstmt, i, parameters.get(i-1));
			}
			return prstmt.executeQuery();
		} catch (SQLException throwables) {
			throw new DatabaseException("Error trying to execute a query: " + throwables.getSQLState());
		}
	}

	@Override
	public synchronized int update(String sql) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			return stmt.executeUpdate(sql);
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
	public synchronized int preparedUpdate(String sql, List<Object> parameters) {
		PreparedStatement prstmt = null;
		try {
			prstmt = connection.prepareStatement(sql);
			for (int i = 1; i <= parameters.size(); i++) {
				prstmt = insertParameter(prstmt, i, parameters.get(i-1));
			}
			return prstmt.executeUpdate();
		} catch (SQLException throwables) {
			LOGGER.error("Impossible execute the query: {}", sql);
			throw new DatabaseException("Error trying to execute a query. It's possible that the selected table " +
					"doesn't exists or the specified fields aren't present in the table");
		} finally {
			if (prstmt != null) {
				try {
					prstmt.close();
				} catch (SQLException throwables) {
					LOGGER.error("Couldn't close a statement of the query.");
				}
			}
		}
	}

	@Override
	public synchronized Object execute(String sql) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			return stmt.execute(sql);
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
	public synchronized Object preparedExecute(String sql, List<Object> parameters) {
		PreparedStatement prstmt = null;
		try {
			prstmt = connection.prepareStatement(sql);
			for (int i = 1; i <= parameters.size(); i++) {
				prstmt = insertParameter(prstmt, i, parameters.get(i-1));
			}
			return prstmt.execute();
		} catch (SQLException throwables) {
			LOGGER.error("Impossible execute the query: {}", sql);
			throw new DatabaseException("Error trying to execute a query. It's possible that the selected table " +
					"doesn't exists or the specified fields aren't present in the table");
		} finally {
			if (prstmt != null) {
				try {
					prstmt.close();
				} catch (SQLException throwables) {
					LOGGER.error("Couldn't close a statement of the query.");
				}
			}
		}
	}

	private PreparedStatement insertParameter(PreparedStatement ps, int pos, Object parameter) throws SQLException {
		if (parameter == null) {
			ps.setNull(pos, 2);
			return ps;
		}
		switch (parameter.getClass().getName()) {
			case "short":
				ps.setShort(pos, (short)parameter);
				break;
			case "java.lang.Short":
				ps.setShort(pos, (Short)parameter);
				break;
			case "int":
				ps.setInt(pos, (int)parameter);
				break;
			case "java.lang.Integer":
				ps.setInt(pos, (Integer)parameter);
				break;
			case "long":
				ps.setLong(pos, (long)parameter);
				break;
			case "java.lang.Long":
				ps.setLong(pos, (Long)parameter);
				break;
			case "float":
				ps.setFloat(pos, (float)parameter);
				break;
			case "java.lang.Float":
				ps.setFloat(pos, (Float)parameter);
				break;
			case "double":
				ps.setDouble(pos, (double)parameter);
				break;
			case "java.lang.Double":
				ps.setDouble(pos, (Double)parameter);
				break;
			case "boolean":
				ps.setBoolean(pos, (boolean)parameter);
				break;
			case "java.lang.Boolean":
				ps.setBoolean(pos, (Boolean)parameter);
				break;
			case "byte":
				ps.setByte(pos, (byte)parameter);
				break;
			case "java.lang.Byte":
				ps.setInt(pos, (Byte)parameter);
				break;
			case "java.lang.String":
				ps.setString(pos, (String)parameter);
				break;
			case "[B":
				ps.setBytes(pos, (byte[])parameter);
				break;
			case "javax.sql.rowset.serial.SerialBlob":
				ps.setBlob(pos, (Blob)parameter);
				break;
		}
		return ps;
	}

	public void closeQuery(ResultSet rs) {
		try {
			rs.getStatement().close();
			rs.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}
}
