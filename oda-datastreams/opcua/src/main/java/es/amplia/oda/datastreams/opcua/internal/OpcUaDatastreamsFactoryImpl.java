package es.amplia.oda.datastreams.opcua.internal;

import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.opcua.OpcUaConnection;
import es.amplia.oda.datastreams.opcua.OpcUaDatastreamsFactory;

public class OpcUaDatastreamsFactoryImpl implements OpcUaDatastreamsFactory {

    private final OpcUaReadOperatorProcessor readOperatorProcessor;
    private final OpcUaWriteOperatorProcessor writeOperatorProcessor;
    private final ScadaTableTranslator scadaTranslator;


    public OpcUaDatastreamsFactoryImpl(ScadaTableTranslator scadaTranslator, OpcUaConnection connection) {
        this.scadaTranslator = scadaTranslator;
        this.readOperatorProcessor = new OpcUaReadOperatorProcessor(scadaTranslator, connection);
        this.writeOperatorProcessor = new OpcUaWriteOperatorProcessor(scadaTranslator, connection);
    }

    @Override
    public OpcUaDatastreamsGetter createOpcUaDatastreamsGetter(String datastreamId) {
        return new OpcUaDatastreamsGetter(datastreamId, scadaTranslator.getRecollectionDeviceIds(), readOperatorProcessor);
    }

    @Override
    public OpcUaDatastreamsSetter createOpcUaDatastreamsSetter(String datastreamId) {
        return new OpcUaDatastreamsSetter(datastreamId, scadaTranslator.getRecollectionDeviceIds(), writeOperatorProcessor);
    }
}
