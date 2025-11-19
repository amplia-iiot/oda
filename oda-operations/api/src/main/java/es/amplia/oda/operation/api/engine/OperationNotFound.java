package es.amplia.oda.operation.api.engine;

public class OperationNotFound extends RuntimeException {

    public OperationNotFound (String message) {
        super(message);
    }
}
