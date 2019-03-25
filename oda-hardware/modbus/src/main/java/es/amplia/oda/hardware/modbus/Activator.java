package es.amplia.oda.hardware.modbus;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.hardware.modbus.configuration.ModbusMasterConfigurationUpdateHandler;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterFactory;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private ModbusMasterManager modbusMasterManager;
    private ConfigurableBundle configurableBundle;

    @Override
    public void start(BundleContext bundleContext) {
        ServiceRegistrationManager<ModbusMaster> modbusRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, ModbusMaster.class);
        modbusMasterManager = new ModbusMasterManager(modbusRegistrationManager);
        ModbusMasterFactory modbusMasterFactory = new ModbusMasterFactory();
        ModbusMasterConfigurationUpdateHandler configHandler =
                new ModbusMasterConfigurationUpdateHandler(modbusMasterManager, modbusMasterFactory);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
    }

    @Override
    public void stop(BundleContext bundleContext) {
        modbusMasterManager.close();
        configurableBundle.close();
    }
}
