package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.OpenGateOperationProcessorFactory;
import es.amplia.oda.dispatcher.opengate.OperationProcessor;
import es.amplia.oda.operation.api.osgi.proxies.*;

import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;

import static es.amplia.oda.dispatcher.opengate.operation.processor.GetDeviceParametersProcessor.GET_DEVICE_PARAMETERS_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.RefreshInfoProcessor.REFRESH_INFO_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.SetDeviceParametersProcessor.SET_DEVICE_PARAMETERS_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.SetClockEquipmentProcessor.SET_CLOCK_EQUIPMENT_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.SynchronizeClockProcessor.SYNCHRONIZE_CLOCK_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.UpdateProcessor.UPDATE_OPERATION_NAME;

public class OpenGateOperationProcessorFactoryImpl implements OpenGateOperationProcessorFactory {

    private final OperationRefreshInfoProxy operationRefreshInfo;
    private final OperationGetDeviceParametersProxy operationGetDeviceParameters;
    private final OperationSetDeviceParametersProxy operationSetDeviceParameters;
    private final OperationUpdateProxy operationUpdate;
    private final OperationSetClockProxy operationSetClockEquipment;
    private final OperationSynchronizeClockProxy operationSynchronizeClock;
    private final Serializer serializer;

    public OpenGateOperationProcessorFactoryImpl(BundleContext bundleContext, Serializer serializer) {
        this.operationRefreshInfo = new OperationRefreshInfoProxy(bundleContext);
        this.operationGetDeviceParameters = new OperationGetDeviceParametersProxy(bundleContext);
        this.operationSetDeviceParameters = new OperationSetDeviceParametersProxy(bundleContext);
        this.operationUpdate = new OperationUpdateProxy(bundleContext);
        this.operationSetClockEquipment = new OperationSetClockProxy(bundleContext);
        this.operationSynchronizeClock = new OperationSynchronizeClockProxy(bundleContext);
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
        catalogueOperationProcessors.put(SET_CLOCK_EQUIPMENT_OPERATION_NAME,
                new SetClockEquipmentProcessor(serializer, operationSetClockEquipment));
        catalogueOperationProcessors.put(SYNCHRONIZE_CLOCK_OPERATION_NAME,
                new SynchronizeClockProcessor(serializer, operationSynchronizeClock));
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
        operationSetClockEquipment.close();
        operationSynchronizeClock.close();
    }
}
