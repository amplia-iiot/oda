package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.simulator.configuration.ConstantDatastreamGetterConfiguration;
import es.amplia.oda.datastreams.simulator.configuration.RandomDatastreamGetterConfiguration;
import es.amplia.oda.datastreams.simulator.configuration.SetDatastreamSetterConfiguration;
import es.amplia.oda.datastreams.simulator.configuration.SimulatedDatastreamsGetterConfiguration;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsGetterFactory;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsSetterFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SimulatedDatastreamsManager implements AutoCloseable {

    private final SimulatedDatastreamsGetterFactory getterFactory;
    private final SimulatedDatastreamsSetterFactory setterFactory;
    private final ServiceRegistrationManager<DatastreamsGetter> registrationGettersManager;
    private final ServiceRegistrationManager<DatastreamsSetter> registrationSettersManager;

    private final Map<Class, Function<SimulatedDatastreamsGetterConfiguration, DatastreamsGetter>> datastreamsGetterCreators =
            new HashMap<>();
    private final Map<Class, Function<SetDatastreamSetterConfiguration, DatastreamsSetter>> datastreamsSetterCreators =
            new HashMap<>();

    SimulatedDatastreamsManager(SimulatedDatastreamsGetterFactory getterFactory,
                                SimulatedDatastreamsSetterFactory setterFactory,
                                ServiceRegistrationManager<DatastreamsGetter> registrationGettersManager,
                                ServiceRegistrationManager<DatastreamsSetter> registrationSettersManager) {
        this.getterFactory = getterFactory;
        this.setterFactory = setterFactory;
        this.registrationGettersManager = registrationGettersManager;
        this.registrationSettersManager = registrationSettersManager;
        registerDatastreamsGetterCreators();
        registerDatastreamsSetterCreators();
    }

    private void registerDatastreamsGetterCreators() {
        datastreamsGetterCreators.put(ConstantDatastreamGetterConfiguration.class, this::createConstantDatastreamsGetter);
        datastreamsGetterCreators.put(RandomDatastreamGetterConfiguration.class, this::createRandomDatastreamsGetter);
    }

    private void registerDatastreamsSetterCreators() {
        datastreamsSetterCreators.put(SetDatastreamSetterConfiguration.class, this::createSetDatastreamsSetter);
    }

    public void loadConfiguration(List<SimulatedDatastreamsGetterConfiguration> configuration, List<SetDatastreamSetterConfiguration> settersConfigured) {
        configuration.stream()
                .map(this::createDatastreamsGetter)
                .forEach(this::registerDatastreamsGetter);
        settersConfigured.stream()
                .map(this::createDatastreamsSetter)
                .forEach(this::registerDatastreamsSetter);
    }

    private DatastreamsGetter createDatastreamsGetter(SimulatedDatastreamsGetterConfiguration conf) {
        return datastreamsGetterCreators.get(conf.getClass()).apply(conf);
    }

    private DatastreamsSetter createDatastreamsSetter(SetDatastreamSetterConfiguration conf) {
        return datastreamsSetterCreators.get(conf.getClass()).apply(conf);
    }

    private DatastreamsGetter createConstantDatastreamsGetter(SimulatedDatastreamsGetterConfiguration configuration) {
        ConstantDatastreamGetterConfiguration conf = (ConstantDatastreamGetterConfiguration) configuration;
        return getterFactory.createConstantDatastreamsGetter(conf.getDatastreamId(), conf.getDeviceId(), conf.getValue());
    }

    private DatastreamsGetter createRandomDatastreamsGetter(SimulatedDatastreamsGetterConfiguration configuration) {
        RandomDatastreamGetterConfiguration conf = (RandomDatastreamGetterConfiguration) configuration;
        return getterFactory.createRandomDatastreamsGetter(conf.getDatastreamId(), conf.getDeviceId(), conf.getMinValue(),
                conf.getMaxValue(), conf.getMaxDifferenceBetweenMeasurements());
    }

    private DatastreamsSetter createSetDatastreamsSetter(SimulatedDatastreamsGetterConfiguration configuration) {
        SetDatastreamSetterConfiguration conf = (SetDatastreamSetterConfiguration) configuration;
        return setterFactory.createSetDatastreamsSetter(conf.getDatastreamId(), conf.getDeviceId());
    }

    private void registerDatastreamsGetter(DatastreamsGetter datastreamsGetter) {
        registrationGettersManager.register(datastreamsGetter);
    }

    private void registerDatastreamsSetter(DatastreamsSetter datastreamsSetter) {
        registrationSettersManager.register(datastreamsSetter);
    }

    @Override
    public void close() {
        registrationGettersManager.unregister();
        registrationSettersManager.unregister();
    }
}
