package es.amplia.oda.core.commons.statemanager;

public interface SQLStatements {
	String getProtocolUrlDatabase();

	String getDriverClassName();

	String getExtraOptions();

	String getCreateStateTableStatement();

	String getTimeIndexStatement();

	String getDeleteIndexStatement();

	String getObtainStoredDataStatement();

	String getInsertNewDataRowStatement();

	String getSelectRowNOfADatastreamStatement();

	String getDeleteOverloadDataFromADatastreamStatement();

	String getDeleteOlderDataFromDatabaseStatement();

	String getUpdateSentData();

	String getQueryToGetTables();

	String getExcessHistoricDataFromDatabaseStatement();
}