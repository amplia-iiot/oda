package es.amplia.oda.datastreams.snmp.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import es.amplia.oda.datastreams.snmp.configuration.SnmpDatastreamsEntry;

import java.util.List;

public class SnmpDatastreamsManager implements AutoCloseable{

    SnmpClientsFinder clientsFinder;
    ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager;
    ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager;

    public SnmpDatastreamsManager(SnmpClientsFinder clientsFinder,
                                  ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager,
                                  ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager) {
        this.clientsFinder = clientsFinder;
        this.datastreamsGetterRegistrationManager = datastreamsGetterRegistrationManager;
        this.datastreamsSetterRegistrationManager = datastreamsSetterRegistrationManager;
    }

    public void loadConfiguration(List<SnmpDatastreamsEntry> snmpDatastreamsConfiguration) {
        datastreamsGetterRegistrationManager.unregister();
        datastreamsSetterRegistrationManager.unregister();

        for (SnmpDatastreamsEntry entry : snmpDatastreamsConfiguration) {
            datastreamsGetterRegistrationManager.register(createSnmpDatastreamsGetter(entry));
            //datastreamsSetterRegistrationManager.register(createSnmpDatastreamsSetter(entry));
        }
    }

    public SnmpDatastreamsGetter createSnmpDatastreamsGetter(SnmpDatastreamsEntry snmpDatastreamConf) {
        return new SnmpDatastreamsGetter(clientsFinder, snmpDatastreamConf.getOID(), snmpDatastreamConf.getDataType().getClass(),
                snmpDatastreamConf.getDatastreamId(), snmpDatastreamConf.getDeviceId(), snmpDatastreamConf.getFeed());
    }

    public SnmpDatastreamsSetter createSnmpDatastreamsSetter(SnmpDatastreamsEntry snmpDatastreamConf) {
        return new SnmpDatastreamsSetter(clientsFinder, snmpDatastreamConf.getOID(), snmpDatastreamConf.getDataType(),
                snmpDatastreamConf.getDatastreamId(), snmpDatastreamConf.getDeviceId());
    }

    @Override
    public void close(){
        datastreamsGetterRegistrationManager.unregister();
        datastreamsSetterRegistrationManager.unregister();
    }
}
