package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsGetterFactory;

import java.util.List;

class SimulatedDatastreamsManager implements AutoCloseable {

    private final SimulatedDatastreamsGetterFactory factory;
    private final ServiceRegistrationManager<DatastreamsGetter> registrationManager;


    SimulatedDatastreamsManager(SimulatedDatastreamsGetterFactory factory,
                                ServiceRegistrationManager<DatastreamsGetter> registrationManager) {
        this.factory = factory;
        this.registrationManager = registrationManager;
    }

    void loadConfiguration(List<SimulatedDatastreamsConfiguration> configuration) {
        configuration.stream()
                .map(this::createDatastreamsGetter)
                .forEach(this::registerDatastreamsGetter);
    }

    private DatastreamsGetter createDatastreamsGetter(SimulatedDatastreamsConfiguration conf) {
        return factory.createSimulatedDatastreamsGetter(conf.getDatastreamId(), conf.getDeviceId(), conf.getMinValue(),
                conf.getMaxValue(), conf.getMaxDifferenceBetweenMeasurements());
    }

    private void registerDatastreamsGetter(DatastreamsGetter datastreamsGetter) {
        registrationManager.register(datastreamsGetter);
    }

    @Override
    public void close() {
        registrationManager.unregister();
    }
}
