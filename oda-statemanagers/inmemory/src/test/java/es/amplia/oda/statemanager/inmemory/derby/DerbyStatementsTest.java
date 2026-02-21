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
				"\"date\" bigint, " +
				"\"value\" varchar(5000), " +
				"\"type\" varchar(255), " +
				"\"status\" varchar(255), " +
				"\"error\" varchar(255), " +
				"\"sent\" boolean" +
				")",
				statements.getCreateStateTableStatement());
	}

	@Test
	public void getTimeIndexStatementTest() {
		assertEquals("CREATE INDEX idx_date ON state (\"date\")",
				statements.getTimeIndexStatement());
	}

	@Test
	public void getObtainStoredDataStatementTest() {
		assertEquals("SELECT * FROM state ORDER BY \"date\" DESC",
				statements.getObtainStoredDataStatement());
	}

	@Test
	public void getInsertNewDataRowStatementTest() {
		assertEquals("INSERT INTO state (\"deviceId\", \"datastreamId\", \"feed\", \"at\", \"date\", \"value\", \"type\", \"status\", \"error\", \"sent\") VALUES (?,?,?,?,?,?,?,?,?,?)",
				statements.getInsertNewDataRowStatement());
	}

	@Test
	public void getDeleteOverloadDataFromADatastreamStatementTest() {
		assertEquals("DELETE FROM state WHERE \"deviceId\"=? AND \"datastreamId\"=? AND \"date\"<?",
				statements.getDeleteOverloadDataFromADatastreamStatement());
	}

	@Test
	public void getDeleteOlderDataFromDatabaseStatementTest() {
		assertEquals("DELETE FROM state WHERE \"date\"<?",
				statements.getDeleteOlderDataFromDatabaseStatement());
	}

	@Test
	public void getUpdateSentDataTest() {
		assertEquals("UPDATE state SET \"sent\"=true WHERE \"deviceId\"=? AND \"datastreamId\"=? AND \"date\"=?",
				statements.getUpdateSentData());
	}


}
