package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.datastreams.iec104.Iec104ConnectionsFactory;
import es.amplia.oda.datastreams.iec104.Iec104DatastreamsFactory;

public class Iec104DatastreamsFactoryImpl implements Iec104DatastreamsFactory {

    private final Iec104ReadOperatorProcessor readOperatorProcessor;
    private final Iec104WriteOperatorProcessor writeOperatorProcessor;
    private final Iec104ConnectionsFactory connectionsFactory;


    public Iec104DatastreamsFactoryImpl(ScadaTableTranslator scadaTranslator, Iec104ConnectionsFactory connectionsFactory) {
        this.connectionsFactory = connectionsFactory;
        this.readOperatorProcessor = new Iec104ReadOperatorProcessor(scadaTranslator, connectionsFactory);
        this.writeOperatorProcessor = new Iec104WriteOperatorProcessor(scadaTranslator, connectionsFactory);
    }

    @Override
    public Iec104DatastreamsGetter createIec104DatastreamsGetter(String datastreamId) {
        return new Iec104DatastreamsGetter(datastreamId, connectionsFactory.getDeviceList(), readOperatorProcessor);
    }

    @Override
    public Iec104DatastreamsSetter createIec104DatastreamsSetter(String datastreamId) {
        return new Iec104DatastreamsSetter(datastreamId, connectionsFactory.getDeviceList(), writeOperatorProcessor);
    }

    @Override
    public void updateGetterPolling(int polling) {
        readOperatorProcessor.updateGetterPolling(polling);
    }

}
