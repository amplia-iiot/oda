package es.amplia.oda.datastreams.modbusslave;

import es.amplia.oda.core.commons.osgi.proxies.CounterManagerProxy;
import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.datastreams.modbusslave.configuration.ModbusSlaveConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbusslave.internal.ModbusSlaveManager;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


@Slf4j
public class Activator implements BundleActivator {

    // to make bundle configurable
    private ConfigurableBundle configurableBundle;
    private StateManagerProxy stateManager;
    private ModbusSlaveManager modbusSlaveManager;

    // Bundle start
    public void start(BundleContext bundleContext) {
        log.info("Starting bundle modbus slave");

        stateManager = new StateManagerProxy(bundleContext);
        modbusSlaveManager = new ModbusSlaveManager(stateManager);

        // make bundle configurable
        ModbusSlaveConfigurationUpdateHandler configHandler = new ModbusSlaveConfigurationUpdateHandler(modbusSlaveManager);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        // create counters
        new ModbusSlaveCounters(new CounterManagerProxy(bundleContext));

        log.info("Started bundle modbus slave");
    }

    // Bundle stop
    public void stop(BundleContext bundleContext) {
        log.info("Stopping bundle modbus slave");

        modbusSlaveManager.close();
        stateManager.close();
        configurableBundle.close();

        log.info("Stopped bundle modbus slave");
    }

}
