package es.amplia.oda.statemanager.inmemory.derby;

import es.amplia.oda.core.commons.statemanager.SQLStatements;

public class DerbyStatements implements SQLStatements {
	@Override
	public String getProtocolUrlDatabase() {
		return "jdbc:derby:";
	}

	@Override
	public String getDriverClassName() {
		return "org.apache.derby.jdbc.EmbeddedDriver";
	}

	@Override
	public String getExtraOptions() {
		return ";create=true";
	}

	@Override
	public String getCreateStateTableStatement() {
		return "CREATE TABLE state (" +
				"\"deviceId\" varchar(255), " +
				"\"datastreamId\" varchar(255), " +
				"\"at\" bigint, " +
				"\"value\" varchar(5000), " +
				"\"type\" varchar(255), " +
				"\"status\" varchar(255), " +
				"\"error\" varchar(255), " +
				"\"sent\" boolean" +
				")";
	}

	@Override
	public String getIdIndexStatement() {
		return "CREATE INDEX idx_datastream ON state (\"deviceId\", \"datastreamId\")";
	}

	@Override
	public String getTimeIndexStatement() {
		return "CREATE INDEX idx_at ON state (\"at\")";
	}

	@Override
	public String getObtainStoredDataStatement() {
		return "SELECT * FROM state ORDER BY \"at\" DESC";
	}

	@Override
	public String getInsertNewDataRowStatement() {
		return "INSERT INTO state (\"deviceId\", \"datastreamId\", \"at\", \"value\", \"type\", \"status\", \"error\", \"sent\") VALUES (?,?,?,?,?,?,?,?)";
	}

	@Override
	public String getCountRowsOfADatastreamStatement() {
		return "SELECT COUNT(*) as count FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=?";
	}

	@Override
	public String getSelectRowNOfADatastreamStatement() {
		return "SELECT \"at\" FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=? ORDER BY \"at\" DESC OFFSET ? ROWS FETCH NEXT 1 ROW ONLY";
	}

	@Override
	public String getDeleteOverloadDataFromADatastreamStatement() {
		return "DELETE FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=? AND \"at\"<=?";
	}

	@Override
	public String getDeleteOlderDataFromDatabaseStatement() {
		return "DELETE FROM state WHERE \"at\"<?";
	}

	@Override
	public String getUpdateSentData() {
		return "UPDATE state SET \"sent\"=true WHERE \"deviceId\"=? AND \"datastreamId\"=? AND \"at\"=?";
	}

	@Override
	public String getUpdateIsDataSent() {
		return "SELECT \"at\", \"sent\" FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=?";
	}

	@Override
	public String getQueryToGetTables() {
		return "select st.tablename  from sys.systables st LEFT OUTER join sys.sysschemas ss on (st.schemaid = ss.schemaid) where ss.schemaname ='APP'";
	}
}
