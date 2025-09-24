package es.amplia.oda.datastreams.snmp.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.SnmpTranslator;
import es.amplia.oda.core.commons.snmp.SnmpEntry;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;

import java.util.List;

public class SnmpDatastreamsManager implements AutoCloseable{

    SnmpClientsFinder clientsFinder;
    ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager;
    ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager;
    ServiceRegistrationManager<SnmpTranslator> snmpTranslatorRegistrationManager;


    public SnmpDatastreamsManager(SnmpClientsFinder clientsFinder,
                                  ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager,
                                  ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager,
                                  ServiceRegistrationManager<SnmpTranslator> snmpTranslatorRegistrationManager) {
        this.clientsFinder = clientsFinder;
        this.datastreamsGetterRegistrationManager = datastreamsGetterRegistrationManager;
        this.datastreamsSetterRegistrationManager = datastreamsSetterRegistrationManager;
        this.snmpTranslatorRegistrationManager = snmpTranslatorRegistrationManager;
    }

    public void loadConfiguration(List<SnmpEntry> snmpDatastreamsConfiguration) {
        datastreamsGetterRegistrationManager.unregister();
        datastreamsSetterRegistrationManager.unregister();
        snmpTranslatorRegistrationManager.unregister();

        // create datastreamGetters and datastreamSetter
        for (SnmpEntry entry : snmpDatastreamsConfiguration) {
            datastreamsGetterRegistrationManager.register(createSnmpDatastreamsGetter(entry));
            //datastreamsSetterRegistrationManager.register(createSnmpDatastreamsSetter(entry));
        }

        // create translation service for snmp traps
        snmpTranslatorRegistrationManager.register(new SnmpDatastreamsTranslator(snmpDatastreamsConfiguration));
    }

    public SnmpDatastreamsGetter createSnmpDatastreamsGetter(SnmpEntry snmpDatastreamConf) {
        return new SnmpDatastreamsGetter(clientsFinder, snmpDatastreamConf.getOID(), snmpDatastreamConf.getDataType().getClass(),
                snmpDatastreamConf.getDatastreamId(), snmpDatastreamConf.getDeviceId(), snmpDatastreamConf.getFeed());
    }

    public SnmpDatastreamsSetter createSnmpDatastreamsSetter(SnmpEntry snmpDatastreamConf) {
        return new SnmpDatastreamsSetter(clientsFinder, snmpDatastreamConf.getOID(), snmpDatastreamConf.getDataType(),
                snmpDatastreamConf.getDatastreamId(), snmpDatastreamConf.getDeviceId());
    }

    @Override
    public void close(){
        datastreamsGetterRegistrationManager.unregister();
        datastreamsSetterRegistrationManager.unregister();
        snmpTranslatorRegistrationManager.unregister();
    }
}
