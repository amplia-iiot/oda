package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.utils.operation.response.OperationResponse;

/**
 * Interface to be implemented by all components able to dispatch response to
 * process them and send them through a connector.
 */
public interface ResponseDispatcher {
    /**
     * Publish a Response for sending it through a connector.
     * @param response Response for a connector.
     */
    void publishResponse(OperationResponse response);
}
