package es.amplia.oda.datastreams.opcua;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import java.util.List;

public class OpcUaDatastreamsManager implements AutoCloseable {

    private final OpcUaDatastreamsFactory opcUaDatastreamsFactory;
    private final ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager;
    private final ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager;
    private final ScadaTableTranslator translator;

    OpcUaDatastreamsManager(OpcUaDatastreamsFactory iec104DatastreamsFactory,
                             ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager,
                             ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager,
                             ScadaTableTranslator translator) {
        this.opcUaDatastreamsFactory = iec104DatastreamsFactory;
        this.datastreamsGetterRegistrationManager = datastreamsGetterRegistrationManager;
        this.datastreamsSetterRegistrationManager = datastreamsSetterRegistrationManager;
        this.translator = translator;
    }

    public void updateDatastreams() {
        // Una vez que tenemos el número de dispositivos en el sistema ya podemos crear los datastreams Getter y Setters correspondientes
        List<String> recollectionDatastreamIds = this.translator.getRecollectionDatastreamsIds();
        if (recollectionDatastreamIds!= null && !recollectionDatastreamIds.isEmpty()) {
            recollectionDatastreamIds.forEach(this::createAndRegisterOpcUaDatastreams);
        }
    }

    private void createAndRegisterOpcUaDatastreams(String datastreamId) {
        //if (isWriteableDatastream(datastreamId)) {
            createAndRegisterOpcUaDatastreamsSetter(datastreamId);
        //}
        createAndRegisterOpcUaDatastreamsGetter(datastreamId);
    }

    private void createAndRegisterOpcUaDatastreamsGetter(String datastreamId) {
        DatastreamsGetter datastreamsGetter =
                opcUaDatastreamsFactory.createOpcUaDatastreamsGetter(datastreamId);

        if (datastreamsGetter != null) {
            datastreamsGetterRegistrationManager.register(datastreamsGetter);
        }
    }

    private void createAndRegisterOpcUaDatastreamsSetter(String datastreamId) {
        DatastreamsSetter datastreamsSetter =
                opcUaDatastreamsFactory.createOpcUaDatastreamsSetter(datastreamId);

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
