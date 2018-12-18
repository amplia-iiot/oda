package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.osgi.proxies.ModbusMasterProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.modbus.configuration.ModbusDatastreamsConfigurationUpdateHandler;

import es.amplia.oda.datastreams.modbus.internal.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private ModbusMasterProxy modbusMaster;
    private ModbusDatastreamsManager modbusDatastreamsManager;
    private ConfigurableBundle configurableBundle;
    private ServiceListenerBundle<ModbusMaster> modbusMasterListenerBundle;


    @Override
    public void start(BundleContext bundleContext) {
        modbusMaster = new ModbusMasterProxy(bundleContext);
        ModbusDatastreamsFactory modbusDatastreamsFactory = new ModbusDatastreamsFactoryImpl(modbusMaster);
        ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsGetter.class);
        ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsSetter.class);
        modbusDatastreamsManager =
                new ModbusDatastreamsManager(modbusDatastreamsFactory, datastreamsGetterRegistrationManager,
                        datastreamsSetterRegistrationManager);
        ModbusDatastreamsConfigurationUpdateHandler configHandler =
                new ModbusDatastreamsConfigurationUpdateHandler(modbusDatastreamsManager);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        modbusMasterListenerBundle =
                new ServiceListenerBundle<>(bundleContext, ModbusMaster.class, this::onServiceChanged);
        onServiceChanged();
    }

    void onServiceChanged() {
        modbusMaster.connect();
    }

    @Override
    public void stop(BundleContext bundleContext) {
        modbusMasterListenerBundle.close();
        configurableBundle.close();
        modbusDatastreamsManager.close();
        modbusMaster.disconnect();
        modbusMaster.close();
    }
}
