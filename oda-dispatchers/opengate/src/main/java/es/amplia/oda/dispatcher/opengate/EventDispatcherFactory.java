package es.amplia.oda.dispatcher.opengate;

public interface EventDispatcherFactory {
    EventCollector createEventCollector(boolean reducedOutput);
}
