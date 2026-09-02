package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.utils.DatastreamsGettersFinderImpl;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinderImpl;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import es.amplia.oda.dispatcher.opengate.OperationProcessor;
import es.amplia.oda.operation.api.CustomOperation;
import es.amplia.oda.operation.api.osgi.proxies.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OpenGateOperationProcessorFactoryImplTest {

    @Mock
    private BundleContext mockedContext;

    private OpenGateOperationProcessorFactoryImpl testFactory;

    private MockedConstruction<OperationRefreshInfoProxy> refreshInfoCons;
    private MockedConstruction<OperationGetDeviceParametersProxy> getDeviceParametersCons;
    private MockedConstruction<OperationSetDeviceParametersProxy> setDeviceParametersCons;
    private MockedConstruction<OperationUpdateProxy> updateCons;
    private MockedConstruction<OperationSetClockProxy> setClockCons;
    private MockedConstruction<OperationSynchronizeClockProxy> synchronizeClockCons;
    private MockedConstruction<ServiceLocatorOsgi> serviceLocatorCons;
    private MockedConstruction<DatastreamsGettersFinderImpl> gettersFinderCons;
    private MockedConstruction<DatastreamsSettersFinderImpl> settersFinderCons;

    private final List<List<?>> refreshInfoArgs = new ArrayList<>();
    private final List<List<?>> getDeviceParametersArgs = new ArrayList<>();
    private final List<List<?>> setDeviceParametersArgs = new ArrayList<>();
    private final List<List<?>> updateArgs = new ArrayList<>();
    private final List<List<?>> setClockArgs = new ArrayList<>();
    private final List<List<?>> synchronizeClockArgs = new ArrayList<>();
    private final List<List<?>> serviceLocatorArgs = new ArrayList<>();

    private OperationRefreshInfoProxy mockedRefreshInfo;
    private OperationGetDeviceParametersProxy mockedGetDeviceParameters;
    private OperationSetDeviceParametersProxy mockedSetDeviceParameters;
    private OperationUpdateProxy mockedUpdate;
    private OperationSetClockProxy mockedSetClockEquipment;
    private OperationSynchronizeClockProxy mockedSynchronizeClock;
    private ServiceLocatorOsgi mockedOperationServiceLocator;

    @BeforeEach
    public void setUp() {
        refreshInfoCons = mockConstruction(OperationRefreshInfoProxy.class,
                (mock, mctx) -> refreshInfoArgs.add(new ArrayList<>(mctx.arguments())));
        getDeviceParametersCons = mockConstruction(OperationGetDeviceParametersProxy.class,
                (mock, mctx) -> getDeviceParametersArgs.add(new ArrayList<>(mctx.arguments())));
        setDeviceParametersCons = mockConstruction(OperationSetDeviceParametersProxy.class,
                (mock, mctx) -> setDeviceParametersArgs.add(new ArrayList<>(mctx.arguments())));
        updateCons = mockConstruction(OperationUpdateProxy.class,
                (mock, mctx) -> updateArgs.add(new ArrayList<>(mctx.arguments())));
        setClockCons = mockConstruction(OperationSetClockProxy.class,
                (mock, mctx) -> setClockArgs.add(new ArrayList<>(mctx.arguments())));
        synchronizeClockCons = mockConstruction(OperationSynchronizeClockProxy.class,
                (mock, mctx) -> synchronizeClockArgs.add(new ArrayList<>(mctx.arguments())));
        serviceLocatorCons = mockConstruction(ServiceLocatorOsgi.class,
                (mock, mctx) -> serviceLocatorArgs.add(new ArrayList<>(mctx.arguments())));
        gettersFinderCons = mockConstruction(DatastreamsGettersFinderImpl.class);
        settersFinderCons = mockConstruction(DatastreamsSettersFinderImpl.class);

        testFactory = new OpenGateOperationProcessorFactoryImpl(mockedContext);

        mockedRefreshInfo = refreshInfoCons.constructed().get(0);
        mockedGetDeviceParameters = getDeviceParametersCons.constructed().get(0);
        mockedSetDeviceParameters = setDeviceParametersCons.constructed().get(0);
        mockedUpdate = updateCons.constructed().get(0);
        mockedSetClockEquipment = setClockCons.constructed().get(0);
        mockedSynchronizeClock = synchronizeClockCons.constructed().get(0);
        mockedOperationServiceLocator = serviceLocatorCons.constructed().get(0);
    }

    @AfterEach
    public void tearDown() {
        settersFinderCons.close();
        gettersFinderCons.close();
        serviceLocatorCons.close();
        synchronizeClockCons.close();
        setClockCons.close();
        updateCons.close();
        setDeviceParametersCons.close();
        getDeviceParametersCons.close();
        refreshInfoCons.close();
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals(1, refreshInfoCons.constructed().size());
        assertEquals(mockedContext, refreshInfoArgs.get(0).get(0));
        assertEquals(1, getDeviceParametersCons.constructed().size());
        assertEquals(mockedContext, getDeviceParametersArgs.get(0).get(0));
        assertEquals(1, setDeviceParametersCons.constructed().size());
        assertEquals(mockedContext, setDeviceParametersArgs.get(0).get(0));
        assertEquals(1, updateCons.constructed().size());
        assertEquals(mockedContext, updateArgs.get(0).get(0));
        assertEquals(1, setClockCons.constructed().size());
        assertEquals(mockedContext, setClockArgs.get(0).get(0));
        assertEquals(1, synchronizeClockCons.constructed().size());
        assertEquals(mockedContext, synchronizeClockArgs.get(0).get(0));
        assertEquals(mockedContext, serviceLocatorArgs.get(0).get(0));
        assertEquals(CustomOperation.class, serviceLocatorArgs.get(0).get(1));
    }

    @Test
    public void testCreateOperationProcessor() throws Exception {
        List<List<?>> refreshInfoProcessorArgs = new ArrayList<>();
        List<List<?>> getDeviceParametersProcessorArgs = new ArrayList<>();
        List<List<?>> setDeviceParametersProcessorArgs = new ArrayList<>();
        List<List<?>> updateProcessorArgs = new ArrayList<>();
        List<List<?>> setClockProcessorArgs = new ArrayList<>();
        List<List<?>> synchronizeClockProcessorArgs = new ArrayList<>();
        List<List<?>> customOperationProcessorArgs = new ArrayList<>();
        List<List<?>> openGateOperationProcessorArgs = new ArrayList<>();
        try (MockedConstruction<RefreshInfoProcessor> refreshInfoProcessorCons =
                     mockConstruction(RefreshInfoProcessor.class,
                             (mock, mctx) -> refreshInfoProcessorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<GetDeviceParametersProcessor> getDeviceParametersProcessorCons =
                     mockConstruction(GetDeviceParametersProcessor.class,
                             (mock, mctx) -> getDeviceParametersProcessorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SetDeviceParametersProcessor> setDeviceParametersProcessorCons =
                     mockConstruction(SetDeviceParametersProcessor.class,
                             (mock, mctx) -> setDeviceParametersProcessorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<UpdateProcessor> updateProcessorCons = mockConstruction(UpdateProcessor.class,
                     (mock, mctx) -> updateProcessorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SetClockEquipmentProcessor> setClockProcessorCons =
                     mockConstruction(SetClockEquipmentProcessor.class,
                             (mock, mctx) -> setClockProcessorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SynchronizeClockProcessor> synchronizeClockProcessorCons =
                     mockConstruction(SynchronizeClockProcessor.class,
                             (mock, mctx) -> synchronizeClockProcessorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<CustomOperationProcessor> customOperationProcessorCons =
                     mockConstruction(CustomOperationProcessor.class,
                             (mock, mctx) -> customOperationProcessorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<OpenGateOperationProcessor> openGateOperationProcessorCons =
                     mockConstruction(OpenGateOperationProcessor.class,
                             (mock, mctx) -> openGateOperationProcessorArgs.add(new ArrayList<>(mctx.arguments())))) {

            OperationProcessor operationProcessor = testFactory.createOperationProcessor();

            assertEquals(openGateOperationProcessorCons.constructed().get(0), operationProcessor);
            assertEquals(1, refreshInfoProcessorCons.constructed().size());
            assertEquals(mockedRefreshInfo, refreshInfoProcessorArgs.get(0).get(0));
            assertEquals(1, getDeviceParametersProcessorCons.constructed().size());
            assertEquals(mockedGetDeviceParameters, getDeviceParametersProcessorArgs.get(0).get(0));
            assertEquals(1, setDeviceParametersProcessorCons.constructed().size());
            assertEquals(mockedSetDeviceParameters, setDeviceParametersProcessorArgs.get(0).get(0));
            assertEquals(1, updateProcessorCons.constructed().size());
            assertEquals(mockedUpdate, updateProcessorArgs.get(0).get(0));
            assertEquals(1, setClockProcessorCons.constructed().size());
            assertEquals(mockedSetClockEquipment, setClockProcessorArgs.get(0).get(0));
            assertEquals(1, synchronizeClockProcessorCons.constructed().size());
            assertEquals(mockedSynchronizeClock, synchronizeClockProcessorArgs.get(0).get(0));
            assertEquals(1, customOperationProcessorCons.constructed().size());
            assertEquals(mockedOperationServiceLocator, customOperationProcessorArgs.get(0).get(0));
            assertEquals(1, openGateOperationProcessorCons.constructed().size());
            assertTrue(openGateOperationProcessorArgs.get(0).get(0) instanceof Map);
            assertEquals(customOperationProcessorCons.constructed().get(0),
                    openGateOperationProcessorArgs.get(0).get(1));
        }
    }

    @Test
    public void testClose() {
        testFactory.close();

        verify(mockedRefreshInfo).close();
        verify(mockedGetDeviceParameters).close();
        verify(mockedSetDeviceParameters).close();
        verify(mockedUpdate).close();
        verify(mockedSetClockEquipment).close();
        verify(mockedSynchronizeClock).close();
        verify(mockedOperationServiceLocator).close();
    }
}
