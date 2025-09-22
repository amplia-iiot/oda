package es.amplia.oda.datastreams.snmp;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.snmp.configuration.SnmpDatastreamsConfigurationHandler;
import es.amplia.oda.datastreams.snmp.internal.SnmpDatastreamsManager;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.List;

@Slf4j
public class Activator implements BundleActivator {

    private ConfigurableBundle configurableBundle;
    private SnmpClientsFinder snmpClientsFinder;
    private ServiceListenerBundle<SnmpClient> snmpClientListenerBundle;
    private SnmpDatastreamsManager snmpDatastreamsManager;

    @Override
    public void start(BundleContext bundleContext) {
        log.info("Starting SNMP Datastreams bundle");

        // initiate manager of snmp clients
        snmpClientsFinder = new SnmpClientsFinder(bundleContext);

        // create snmp data streams getter and setters
        ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsGetter.class);
        ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsSetter.class);

        // create datastreams manager
        snmpDatastreamsManager = new SnmpDatastreamsManager(snmpClientsFinder, datastreamsGetterRegistrationManager,
                datastreamsSetterRegistrationManager);

        // make bundle configurable
        SnmpDatastreamsConfigurationHandler configHandler = new SnmpDatastreamsConfigurationHandler(snmpDatastreamsManager);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        // listen for configuration changes in Snmp Hardware bundles
        snmpClientListenerBundle = new ServiceListenerBundle<>(bundleContext, SnmpClient.class, this::onServiceChanged);
        onServiceChanged();

        log.info("Started SNMP Datastreams bundle");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        log.info("Stopping SNMP Datastreams bundle");

        configurableBundle.close();
        snmpClientsFinder.close();
        snmpClientListenerBundle.close();
        snmpDatastreamsManager.close();

        log.info("Stopped SNMP Datastreams bundle");
    }

    void onServiceChanged() {
        //configHandler.applyConfiguration();

        List<SnmpClient> clients = snmpClientsFinder.getAllSnmpClients();
        for (SnmpClient client : clients) {
            log.info("DeviceId retrieved from client trough OSGI = {}", client.getDeviceId());
        }
    }
}
