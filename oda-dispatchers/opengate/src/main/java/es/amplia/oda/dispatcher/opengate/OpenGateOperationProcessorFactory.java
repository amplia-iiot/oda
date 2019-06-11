package es.amplia.oda.dispatcher.opengate;

public interface OpenGateOperationProcessorFactory extends AutoCloseable {
    OperationProcessor createOperationProcessor();
    @Override
    void close();
}
