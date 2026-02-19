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
				"\"feed\" varchar(255), " +
				"\"at\" bigint, " +
				"\"date\" bigint, " +
				"\"value\" varchar(5000), " +
				"\"type\" varchar(255), " +
				"\"status\" varchar(255), " +
				"\"error\" varchar(255), " +
				"\"sent\" boolean" +
				")";
	}

	@Override
	public String getTimeIndexStatement() {
		return "CREATE INDEX idx_date ON state (\"date\")";
	}

	@Override
	public String getDeleteIndexStatement() {return "CREATE INDEX idx_datastream_delete ON state (\"deviceId\", \"datastreamId\", \"date\")";}

	@Override
	public String getObtainStoredDataStatement() {
		return "SELECT * FROM state ORDER BY \"date\" DESC";
	}

	@Override
	public String getInsertNewDataRowStatement() {
		return "INSERT INTO state (\"deviceId\", \"datastreamId\", \"feed\", \"at\", \"date\", \"value\", \"type\", \"status\", \"error\", \"sent\") VALUES (?,?,?,?,?,?,?,?,?,?)";
	}

	@Override
	public String getSelectRowNOfADatastreamStatement() {
		return "SELECT \"date\" FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=? ORDER BY \"date\" DESC OFFSET ? ROWS FETCH NEXT 1 ROW ONLY";
	}

	@Override
	public String getDeleteOverloadDataFromADatastreamStatement() {
		return "DELETE FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=? AND \"date\" < ?";
	}

	@Override
	public String getDeleteOlderDataFromDatabaseStatement() {
		return "DELETE FROM state WHERE \"date\"<?";
	}

	@Override
	public String getUpdateSentData() {
		return "UPDATE state SET \"sent\"=true WHERE \"deviceId\"=? AND \"datastreamId\"=? AND \"date\"=?";
	}

	@Override
	public String getQueryToGetTables() {
		return "select st.tablename  from sys.systables st LEFT OUTER join sys.sysschemas ss on (st.schemaid = ss.schemaid) where ss.schemaname ='APP'";
	}

	@Override
	public String getExcessHistoricDataFromDatabaseStatement() {
		return "SELECT \"deviceId\", \"datastreamId\" FROM " +
				" (SELECT \"deviceId\", \"datastreamId\", COUNT(\"datastreamId\") AS numValues FROM STATE " +
				"GROUP BY \"deviceId\", \"datastreamId\") AS historicData " +
				"WHERE numValues > ?";
	}
}
