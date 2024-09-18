package es.amplia.oda.core.commons.statemanager;

public interface SQLStatements {
	String getProtocolUrlDatabase();

	String getDriverClassName();

	String getExtraOptions();

	String getCreateStateTableStatement();

	String getIdIndexStatement();

	String getTimeIndexStatement();

	String getObtainStoredDataStatement();

	String getInsertNewDataRowStatement();

	String getCountRowsOfADatastreamStatement();

	String getSelectRowNOfADatastreamStatement();

	String getDeleteOverloadDataFromADatastreamStatement();

	String getDeleteOlderDataFromDatabaseStatement();

	String getUpdateSentData();

	String getQueryToGetTables();

	String getExcessHistoricDataFromDatabaseStatement();
}