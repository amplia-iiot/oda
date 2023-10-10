package es.amplia.oda.datastreams.iec104;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.iec104.configuration.Iec104DatastreamsConfiguration;
import java.util.List;

public class Iec104DatastreamsManager implements AutoCloseable {

    private final Iec104DatastreamsFactory iec104DatastreamsFactory;
    private final ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager;
    private final ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager;
    private final Iec104ConnectionsFactory iec104ConnectionsFactory;
    private final ScadaTableTranslator translator;


    Iec104DatastreamsManager(Iec104DatastreamsFactory iec104DatastreamsFactory,
                             ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager,
                             ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager,
                             Iec104ConnectionsFactory iec104ConnectionsFactory, ScadaTableTranslator translator) {
        this.iec104DatastreamsFactory = iec104DatastreamsFactory;
        this.datastreamsGetterRegistrationManager = datastreamsGetterRegistrationManager;
        this.datastreamsSetterRegistrationManager = datastreamsSetterRegistrationManager;
        this.iec104ConnectionsFactory = iec104ConnectionsFactory;
        this.translator = translator;
    }

    public void loadConfiguration(List<Iec104DatastreamsConfiguration> currentIEC104DatastreamsConfigurations,
                                  int initialPolling, int polling) {
        close();

        // Primero creamos las conexiones para saber el número de dispositivos que tenemos en el sistema
        this.iec104ConnectionsFactory.createConnections(currentIEC104DatastreamsConfigurations);

        // Una vez que tenemos el número de dispositivos en el sistema ya podemos crear los datastreams Getter y Setters correspondientes
        this.translator.getRecollectionDatastreamsIds().forEach(this::createAndRegisterIEC104Datastreams);

        this.iec104ConnectionsFactory.connect();
        this.iec104DatastreamsFactory.updateGetterPolling(initialPolling, polling);
    }

    private void createAndRegisterIEC104Datastreams(String datastreamId) {
        /*if (isWriteableDatastream(datastreamId)) {
            createAndRegisterIec104DatastreamsSetter(datastreamId);
        }*/
        createAndRegisterIec104DatastreamsGetter(datastreamId);
    }

    private void createAndRegisterIec104DatastreamsGetter(String datastreamId) {
        DatastreamsGetter datastreamsGetter =
                iec104DatastreamsFactory.createIec104DatastreamsGetter(datastreamId);

        if (datastreamsGetter != null) {
            datastreamsGetterRegistrationManager.register(datastreamsGetter);
        }
    }

    private void createAndRegisterIec104DatastreamsSetter(String datastreamId) {
        DatastreamsSetter datastreamsSetter =
                iec104DatastreamsFactory.createIec104DatastreamsSetter(datastreamId);

        if (datastreamsSetter != null) {
            datastreamsSetterRegistrationManager.register(datastreamsSetter);
        }
    }

    @Override
    public void close() {
        datastreamsGetterRegistrationManager.unregister();
        datastreamsSetterRegistrationManager.unregister();
    }
}
