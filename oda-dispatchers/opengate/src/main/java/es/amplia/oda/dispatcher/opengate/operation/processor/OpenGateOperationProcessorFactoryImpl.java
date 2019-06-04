package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.OpenGateOperationProcessorFactory;
import es.amplia.oda.dispatcher.opengate.OperationProcessor;
import es.amplia.oda.operation.api.osgi.proxies.OperationGetDeviceParametersProxy;
import es.amplia.oda.operation.api.osgi.proxies.OperationRefreshInfoProxy;
import es.amplia.oda.operation.api.osgi.proxies.OperationSetDeviceParametersProxy;
import es.amplia.oda.operation.api.osgi.proxies.OperationUpdateProxy;

import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;

import static es.amplia.oda.dispatcher.opengate.operation.processor.GetDeviceParametersProcessor.GET_DEVICE_PARAMETERS_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.RefreshInfoProcessor.REFRESH_INFO_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.SetDeviceParametersProcessor.SET_DEVICE_PARAMETERS_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.UpdateProcessor.UPDATE_OPERATION_NAME;

public class OpenGateOperationProcessorFactoryImpl implements OpenGateOperationProcessorFactory {

    private final OperationRefreshInfoProxy operationRefreshInfo;
    private final OperationGetDeviceParametersProxy operationGetDeviceParameters;
    private final OperationSetDeviceParametersProxy operationSetDeviceParameters;
    private final OperationUpdateProxy operationUpdate;
    private final Serializer serializer;

    public OpenGateOperationProcessorFactoryImpl(BundleContext bundleContext, Serializer serializer) {
        this.operationRefreshInfo = new OperationRefreshInfoProxy(bundleContext);
        this.operationGetDeviceParameters = new OperationGetDeviceParametersProxy(bundleContext);
        this.operationSetDeviceParameters = new OperationSetDeviceParametersProxy(bundleContext);
        this.operationUpdate = new OperationUpdateProxy(bundleContext);
        this.serializer = serializer;
    }

    @Override
    public OperationProcessor createOperationProcessor() {
        return new OpenGateOperationProcessor(createCatalogueOperationProcessors(),
                createUnsupportedOperationProcessor());
    }

    private Map<String, OperationProcessor> createCatalogueOperationProcessors() {
        Map<String, OperationProcessor> catalogueOperationProcessors = new HashMap<>();
        catalogueOperationProcessors.put(REFRESH_INFO_OPERATION_NAME,
                new RefreshInfoProcessor(serializer, operationRefreshInfo));
        catalogueOperationProcessors.put(GET_DEVICE_PARAMETERS_OPERATION_NAME,
                new GetDeviceParametersProcessor(serializer, operationGetDeviceParameters));
        catalogueOperationProcessors.put(SET_DEVICE_PARAMETERS_OPERATION_NAME,
                new SetDeviceParametersProcessor(serializer, operationSetDeviceParameters));
        catalogueOperationProcessors.put(UPDATE_OPERATION_NAME, new UpdateProcessor(serializer, operationUpdate));
        return catalogueOperationProcessors;
    }

    private OperationProcessor createUnsupportedOperationProcessor() {
        return new UnsupportedOperationProcessor(serializer);
    }

    @Override
    public void close() {
        operationRefreshInfo.close();
        operationGetDeviceParameters.close();
        operationSetDeviceParameters.close();
        operationUpdate.close();
    }
}
