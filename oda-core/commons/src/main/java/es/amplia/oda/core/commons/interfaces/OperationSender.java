package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.utils.operation.request.OperationRequest;

public interface OperationSender {

    void downlink(OperationRequest<Object> operation);

}
