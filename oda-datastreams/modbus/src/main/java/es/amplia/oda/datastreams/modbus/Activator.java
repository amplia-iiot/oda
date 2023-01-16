package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.modbus.configuration.ModbusDatastreamsConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbus.internal.ModbusDatastreamsFactoryImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private ModbusDatastreamsManager modbusDatastreamsManager;
    private ConfigurableBundle configurableBundle;
    private ServiceListenerBundle<ModbusMaster> modbusMasterListenerBundle;
    private ModbusConnectionsManager modbusConnectionsManager;
    private ModbusDatastreamsConfigurationUpdateHandler configHandler;


    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);


    @Override
    public void start(BundleContext bundleContext) {

        LOGGER.info("Starting up Modbus data streams bundle");

        // initiate manager of modbus connections
        modbusConnectionsManager = new ModbusConnectionsManager(bundleContext);

        // ini data streams factory
        ModbusDatastreamsFactory modbusDatastreamsFactory = new ModbusDatastreamsFactoryImpl(modbusConnectionsManager);

        // create modbus data streams getter and setters
        ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsGetter.class);
        ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsSetter.class);

        modbusDatastreamsManager =
                new ModbusDatastreamsManager(modbusDatastreamsFactory, datastreamsGetterRegistrationManager,
                        datastreamsSetterRegistrationManager);

        // modbus bundle configuration handler
        configHandler = new ModbusDatastreamsConfigurationUpdateHandler(modbusDatastreamsManager);

        // make bundle configurable
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        // listen for configuration changes in Modbus Hardware
        modbusMasterListenerBundle =
                new ServiceListenerBundle<>(bundleContext, ModbusMaster.class, this::onServiceChanged);

        LOGGER.info("Modbus data streams bundle started");

    }

    void onServiceChanged() {

        LOGGER.info("Applying new configuration for Modbus data streams");

        // update list of connections
        modbusConnectionsManager.addAllModbusConnection();

        // update datastreams
        configHandler.applyConfiguration();

        LOGGER.info("Modbus data streams new configuration applied");
    }

    @Override
    public void stop(BundleContext bundleContext) {

        LOGGER.info("Stopping Modbus data streams bundle");

        modbusMasterListenerBundle.close();
        configurableBundle.close();
        modbusDatastreamsManager.close();
        modbusConnectionsManager.close();

        LOGGER.info("Modbus data streams bundle stopped");
    }

}
