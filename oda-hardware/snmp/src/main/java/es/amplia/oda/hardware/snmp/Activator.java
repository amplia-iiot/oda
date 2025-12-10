package es.amplia.oda.hardware.snmp;

import es.amplia.oda.core.commons.osgi.proxies.CounterManagerProxy;
import es.amplia.oda.core.commons.osgi.proxies.SnmpTranslatorProxy;
import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.hardware.snmp.configuration.SnmpConfigurationUpdateHandler;
import es.amplia.oda.hardware.snmp.internal.SnmpClientFactory;
import es.amplia.oda.hardware.snmp.internal.SnmpClientManager;
import es.amplia.oda.hardware.snmp.internal.SnmpTrapListener;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@Slf4j
public class Activator implements BundleActivator {

    private ConfigurableBundle configurableBundle;
    SnmpClientManager snmpManager;
    SnmpTranslatorProxy snmpTranslatorProxy;
    StateManagerProxy stateManagerProxy;

    @Override
    public void start(BundleContext bundleContext) {
        log.info("Starting SNMP Hardware bundle");

        ServiceRegistrationManager<SnmpClient> snmpRegistrationManager = new ServiceRegistrationManagerOsgi<>(bundleContext, SnmpClient.class);

        // create snmp manager
        snmpManager = new SnmpClientManager(snmpRegistrationManager);
        SnmpClientFactory snmpFactory = new SnmpClientFactory();

        // create and register needed proxies
        snmpTranslatorProxy = new SnmpTranslatorProxy(bundleContext);
        stateManagerProxy = new StateManagerProxy(bundleContext);

        // make bundle configurable
        SnmpConfigurationUpdateHandler configHandler = new SnmpConfigurationUpdateHandler(snmpManager, snmpFactory,
                snmpTranslatorProxy, stateManagerProxy);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        // create counters
        new SnmpCounters(new CounterManagerProxy(bundleContext));

        log.info("Started SNMP Hardware bundle");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        log.info("Stopping SNMP Hardware bundle");

        // close snmp trap listener
        SnmpTrapListener.closeListener();

        snmpManager.close();
        configurableBundle.close();
        snmpTranslatorProxy.close();
        stateManagerProxy.close();

        log.info("Stopped SNMP Hardware bundle");
    }
}
