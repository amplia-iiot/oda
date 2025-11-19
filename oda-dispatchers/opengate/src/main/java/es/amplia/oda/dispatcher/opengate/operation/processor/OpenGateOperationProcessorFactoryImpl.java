package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinder;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinderImpl;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinderImpl;
import es.amplia.oda.core.commons.utils.OsgiContext;
import es.amplia.oda.core.commons.utils.ServiceLocator;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import es.amplia.oda.dispatcher.opengate.OpenGateOperationProcessorFactory;
import es.amplia.oda.dispatcher.opengate.OperationProcessor;
import es.amplia.oda.operation.api.CustomOperation;
import es.amplia.oda.operation.api.engine.OperationEngineProxy;
import es.amplia.oda.operation.api.osgi.proxies.*;

import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;

import static es.amplia.oda.dispatcher.opengate.operation.processor.DiscoverProcessor.DISCOVER_OPERATION_NAME;
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
    private final OperationDiscoverProxy operationDiscover;
    private final ServiceLocator<CustomOperation> operationServiceLocator;
    private final OsgiContext osgiContext;
    private final DatastreamsGettersFinder datastreamsGettersFinder;
    private final DatastreamsSettersFinder datastreamsSettersFinder;
    private final OperationEngineProxy operationEngine;


    public OpenGateOperationProcessorFactoryImpl(BundleContext bundleContext) {
        this.operationRefreshInfo = new OperationRefreshInfoProxy(bundleContext);
        this.operationGetDeviceParameters = new OperationGetDeviceParametersProxy(bundleContext);
        this.operationSetDeviceParameters = new OperationSetDeviceParametersProxy(bundleContext);
        this.operationUpdate = new OperationUpdateProxy(bundleContext);
        this.operationSetClockEquipment = new OperationSetClockProxy(bundleContext);
        this.operationSynchronizeClock = new OperationSynchronizeClockProxy(bundleContext);
        this.operationDiscover = new OperationDiscoverProxy(bundleContext);
        this.operationServiceLocator = new ServiceLocatorOsgi<>(bundleContext, CustomOperation.class);
        ServiceLocator<DatastreamsGetter> datastreamsGettersLocator =
                new ServiceLocatorOsgi<>(bundleContext, DatastreamsGetter.class);
        datastreamsGettersFinder = new DatastreamsGettersFinderImpl(datastreamsGettersLocator);
        ServiceLocator<DatastreamsSetter> datastreamsSettersLocator =
                new ServiceLocatorOsgi<>(bundleContext, DatastreamsSetter.class);
        datastreamsSettersFinder = new DatastreamsSettersFinderImpl(datastreamsSettersLocator);
        operationEngine = new OperationEngineProxy(bundleContext);
        this.osgiContext = new OsgiContext(bundleContext, datastreamsGettersFinder, datastreamsSettersFinder);
    }

    @Override
    public OperationProcessor createOperationProcessor() {
        return new OpenGateOperationProcessor(createCatalogueOperationProcessors(),
                createCustomOperationProcessor());
    }

    private Map<String, OperationProcessor> createCatalogueOperationProcessors() {
        Map<String, OperationProcessor> catalogueOperationProcessors = new HashMap<>();
        catalogueOperationProcessors.put(REFRESH_INFO_OPERATION_NAME,
                new RefreshInfoProcessor(operationRefreshInfo));
        catalogueOperationProcessors.put(GET_DEVICE_PARAMETERS_OPERATION_NAME,
                new GetDeviceParametersProcessor(operationGetDeviceParameters));
        catalogueOperationProcessors.put(SET_DEVICE_PARAMETERS_OPERATION_NAME,
                new SetDeviceParametersProcessor(operationSetDeviceParameters));
        catalogueOperationProcessors.put(UPDATE_OPERATION_NAME,
                new UpdateProcessor(operationUpdate));
        catalogueOperationProcessors.put(SET_CLOCK_EQUIPMENT_OPERATION_NAME,
                new SetClockEquipmentProcessor(operationSetClockEquipment));
        catalogueOperationProcessors.put(SYNCHRONIZE_CLOCK_OPERATION_NAME,
                new SynchronizeClockProcessor(operationSynchronizeClock));
        catalogueOperationProcessors.put(DISCOVER_OPERATION_NAME,
                new DiscoverProcessor(operationDiscover));
        return catalogueOperationProcessors;
    }

    private OperationProcessor createCustomOperationProcessor() {
        return new CustomOperationProcessor(operationServiceLocator, osgiContext, operationEngine);
    }

    @Override
    public void close() {
        operationRefreshInfo.close();
        operationGetDeviceParameters.close();
        operationSetDeviceParameters.close();
        operationUpdate.close();
        operationSetClockEquipment.close();
        operationSynchronizeClock.close();
        operationDiscover.close();
        operationServiceLocator.close();
        datastreamsGettersFinder.close();
        datastreamsSettersFinder.close();
        operationEngine.close();
        osgiContext.close();
    }
}
