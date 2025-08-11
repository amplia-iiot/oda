package es.amplia.oda.hardware.modbus;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.osgi.proxies.CounterManagerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.hardware.modbus.configuration.ModbusMasterConfigurationUpdateHandler;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterFactory;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ModbusMasterManager modbusMasterManager;
    private ConfigurableBundle configurableBundle;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Modbus Hardware Bundle");
        ServiceRegistrationManager<ModbusMaster> modbusRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, ModbusMaster.class);
        modbusMasterManager = new ModbusMasterManager(modbusRegistrationManager);
        ModbusMasterFactory modbusMasterFactory = new ModbusMasterFactory();
        ModbusMasterConfigurationUpdateHandler configHandler =
                new ModbusMasterConfigurationUpdateHandler(modbusMasterManager, modbusMasterFactory);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        // create counters
        new ModbusCounters(new CounterManagerProxy(bundleContext));

        LOGGER.info("Modbus Hardware Bundle starting");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Modbus Hardware Bundle");
        modbusMasterManager.close();
        configurableBundle.close();
        LOGGER.info("Modbus Hardware Bundle stopped");
    }
}
