package es.amplia.oda.core.commons.statemanager;

import java.sql.ResultSet;
import java.util.List;

public interface StateManagerDatabase {
	void connect();
	void disconnect();
	boolean exists();
	void create();
	ResultSet query(String sql);
	ResultSet preparedQuery(String sql, List<Object> parameters);
	int update(String sql);
	int preparedUpdate(String sql, List<Object> parameters);
	Object execute(String sql);
	Object preparedExecute(String sql, List<Object> parameters);
}
