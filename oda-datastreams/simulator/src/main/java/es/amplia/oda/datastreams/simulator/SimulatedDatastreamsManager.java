package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsGetterFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class SimulatedDatastreamsManager implements AutoCloseable {

    private final SimulatedDatastreamsGetterFactory factory;
    private final ServiceRegistrationManager<DatastreamsGetter> registrationManager;

    private final Map<Class, Function<SimulatedDatastreamsConfiguration, DatastreamsGetter>> datastreamsGetterCreators =
            new HashMap<>();

    SimulatedDatastreamsManager(SimulatedDatastreamsGetterFactory factory,
                                ServiceRegistrationManager<DatastreamsGetter> registrationManager) {
        this.factory = factory;
        this.registrationManager = registrationManager;
        registerDatastreamsGetterCreators();
    }

    private void registerDatastreamsGetterCreators() {
        datastreamsGetterCreators.put(ConstantDatastreamConfiguration.class, this::createConstantDatastreamsGetter);
        datastreamsGetterCreators.put(RandomDatastreamConfiguration.class, this::createRandomDatastreamsGetter);
    }

    void loadConfiguration(List<SimulatedDatastreamsConfiguration> configuration) {
        configuration.stream()
                .map(this::createDatastreamsGetter)
                .forEach(this::registerDatastreamsGetter);
    }

    private DatastreamsGetter createDatastreamsGetter(SimulatedDatastreamsConfiguration conf) {
        return datastreamsGetterCreators.get(conf.getClass()).apply(conf);
    }

    private DatastreamsGetter createConstantDatastreamsGetter(SimulatedDatastreamsConfiguration configuration) {
        ConstantDatastreamConfiguration conf = (ConstantDatastreamConfiguration) configuration;
        return factory.createConstantDatastreamsGetter(conf.getDatastreamId(), conf.getDeviceId(), conf.getValue());
    }

    private DatastreamsGetter createRandomDatastreamsGetter(SimulatedDatastreamsConfiguration configuration) {
        RandomDatastreamConfiguration conf = (RandomDatastreamConfiguration) configuration;
        return factory.createRandomDatastreamsGetter(conf.getDatastreamId(), conf.getDeviceId(), conf.getMinValue(),
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
