package es.amplia.oda.operation.api.engine;

import java.util.Map;

import es.amplia.oda.core.commons.utils.OsgiContext;
import es.amplia.oda.core.commons.utils.operation.response.Response;

public interface OperationEngine {
    Response engine(String operationName, String deviceId, String operationId, Map<String, Object> params, OsgiContext ctx) throws OperationNotFound;

	void reloadAllOperations();

	void stop();
}
