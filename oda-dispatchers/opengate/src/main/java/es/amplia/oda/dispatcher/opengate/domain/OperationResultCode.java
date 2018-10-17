package es.amplia.oda.dispatcher.opengate.domain;

public enum OperationResultCode {
    SUCCESSFUL,
    OPERATION_PENDING,
    ERROR_IN_PARAM,
    NOT_SUPPORTED,
    ALREADY_IN_PROGRESS,
    ERROR_PROCESSING,
    ERROR_TIMEOUT,
    TIMEOUT_CANCELLED,
    CANCELLED,
    CANCELLED_INTERNAL
}
