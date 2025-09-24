package es.amplia.oda.datastreams.snmp;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.SnmpTranslator;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.snmp.configuration.SnmpDatastreamsConfigurationHandler;
import es.amplia.oda.datastreams.snmp.internal.SnmpDatastreamsManager;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@Slf4j
public class Activator implements BundleActivator {

    private ConfigurableBundle configurableBundle;
    private SnmpClientsFinder snmpClientsFinder;
    private SnmpDatastreamsManager snmpDatastreamsManager;

    @Override
    public void start(BundleContext bundleContext) {
        log.info("Starting SNMP Datastreams bundle");

        // initiate manager of snmp clients
        snmpClientsFinder = new SnmpClientsFinder(bundleContext);

        // create snmp data streams getter and setters register service
        ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsGetter.class);
        ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsSetter.class);
        ServiceRegistrationManager<SnmpTranslator> snmpTranslatorRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, SnmpTranslator.class);

        // create datastreams manager
        snmpDatastreamsManager = new SnmpDatastreamsManager(snmpClientsFinder, datastreamsGetterRegistrationManager,
                datastreamsSetterRegistrationManager, snmpTranslatorRegistrationManager);

        // make bundle configurable
        SnmpDatastreamsConfigurationHandler configHandler = new SnmpDatastreamsConfigurationHandler(snmpDatastreamsManager);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        log.info("Started SNMP Datastreams bundle");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        log.info("Stopping SNMP Datastreams bundle");

        configurableBundle.close();
        snmpClientsFinder.close();
        snmpDatastreamsManager.close();

        log.info("Stopped SNMP Datastreams bundle");
    }

}
