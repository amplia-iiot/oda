package es.amplia.oda.hardware.snmp;

import es.amplia.oda.core.commons.osgi.proxies.CounterManagerProxy;
import es.amplia.oda.core.commons.osgi.proxies.SnmpTranslatorProxy;
import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.hardware.snmp.configuration.SnmpConfigurationUpdateHandler;
import es.amplia.oda.hardware.snmp.internal.SnmpClientFactory;
import es.amplia.oda.hardware.snmp.internal.SnmpClientManager;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@Slf4j
public class Activator implements BundleActivator {

    private ConfigurableBundle configurableBundle;
    SnmpClientManager snmpManager;
    SnmpTranslatorProxy snmpTranslatorProxy;

    @Override
    public void start(BundleContext bundleContext) {
        log.info("Starting SNMP Hardware bundle");

        ServiceRegistrationManager<SnmpClient> snmpRegistrationManager = new ServiceRegistrationManagerOsgi<>(bundleContext, SnmpClient.class);

        // create and register translation proxy to translate traps received
        snmpTranslatorProxy = new SnmpTranslatorProxy(bundleContext);
        snmpManager = new SnmpClientManager(snmpRegistrationManager);
        SnmpClientFactory snmpFactory = new SnmpClientFactory(snmpTranslatorProxy);

        // make bundle configurable
        SnmpConfigurationUpdateHandler configHandler = new SnmpConfigurationUpdateHandler(snmpManager, snmpFactory);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        // create counters
        new SnmpCounters(new CounterManagerProxy(bundleContext));

        log.info("Started SNMP Hardware bundle");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        log.info("Stopping SNMP Hardware bundle");

        snmpManager.close();
        configurableBundle.close();
        snmpTranslatorProxy.close();

        log.info("Stopped SNMP Hardware bundle");
    }
}
