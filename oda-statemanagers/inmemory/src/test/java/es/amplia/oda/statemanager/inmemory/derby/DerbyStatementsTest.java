package es.amplia.oda.statemanager.inmemory.derby;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DerbyStatementsTest {
	DerbyStatements statements = new DerbyStatements();

	@Test
	public void getProtocolUrlDatabaseTest() {
		assertEquals("jdbc:derby:", statements.getProtocolUrlDatabase());
	}

	@Test
	public void getDriverClassNameTest() {
		assertEquals("org.apache.derby.jdbc.EmbeddedDriver", statements.getDriverClassName());
	}

	@Test
	public void getExtraOptionsTest() {
		assertEquals(";create=true", statements.getExtraOptions());
	}

	@Test
	public void getCreateStateTableStatementTest() {
		assertEquals("CREATE TABLE state (" +
				"\"deviceId\" varchar(255), " +
				"\"datastreamId\" varchar(255), " +
				"\"feed\" varchar(255), " +
				"\"at\" bigint, " +
				"\"value\" varchar(5000), " +
				"\"type\" varchar(255), " +
				"\"status\" varchar(255), " +
				"\"error\" varchar(255), " +
				"\"sent\" boolean" +
				")",
				statements.getCreateStateTableStatement());
	}

	@Test
	public void getIdIndexStatementTest() {
		assertEquals("CREATE INDEX idx_datastream ON state (\"deviceId\", \"datastreamId\")",
				statements.getIdIndexStatement());
	}

	@Test
	public void getTimeIndexStatementTest() {
		assertEquals("CREATE INDEX idx_at ON state (\"at\")",
				statements.getTimeIndexStatement());
	}

	@Test
	public void getObtainStoredDataStatementTest() {
		assertEquals("SELECT * FROM state ORDER BY \"at\" DESC",
				statements.getObtainStoredDataStatement());
	}

	@Test
	public void getInsertNewDataRowStatementTest() {
		assertEquals("INSERT INTO state (\"deviceId\", \"datastreamId\", \"feed\", \"at\", \"value\", \"type\", \"status\", \"error\", \"sent\") VALUES (?,?,?,?,?,?,?,?,?)",
				statements.getInsertNewDataRowStatement());
	}

	@Test
	public void getCountRowsOfADatastreamStatementTest() {
		assertEquals("SELECT COUNT(*) as count FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=?",
				statements.getCountRowsOfADatastreamStatement());
	}

	@Test
	public void getSelectRowNOfADatastreamStatementTest() {
		assertEquals("SELECT \"at\" FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=? ORDER BY \"at\" DESC OFFSET ? ROWS FETCH NEXT 1 ROW ONLY",
				statements.getSelectRowNOfADatastreamStatement());
	}

	@Test
	public void getDeleteOverloadDataFromADatastreamStatementTest() {
		assertEquals("DELETE FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=? AND \"at\" < ?",
				statements.getDeleteOverloadDataFromADatastreamStatement());
	}

	@Test
	public void getDeleteOlderDataFromDatabaseStatementTest() {
		assertEquals("DELETE FROM state WHERE \"at\"<?",
				statements.getDeleteOlderDataFromDatabaseStatement());
	}

	@Test
	public void getUpdateSentDataTest() {
		assertEquals("UPDATE state SET \"sent\"=true WHERE \"deviceId\"=? AND \"datastreamId\"=? AND \"at\"=?",
				statements.getUpdateSentData());
	}

	@Test
	public void getUpdateIsDataSentTest() {
		assertEquals("SELECT \"at\", \"sent\" FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=? AND \"sent\"=false",
				statements.getUpdateIsDataSent());
	}

	@Test
	public void getExcessHistoricDataTest() {
		assertEquals(	"SELECT \"deviceId\", \"datastreamId\" FROM " +
						" (SELECT \"deviceId\", \"datastreamId\", COUNT(\"datastreamId\") AS numValues FROM STATE " +
						"GROUP BY \"deviceId\", \"datastreamId\") AS historicData " +
						"WHERE numValues > ?" ,
				statements.getExcessHistoricDataFromDatabaseStatement());
	}


}
