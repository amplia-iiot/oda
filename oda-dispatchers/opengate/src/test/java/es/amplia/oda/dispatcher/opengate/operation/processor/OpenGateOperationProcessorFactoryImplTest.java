package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.OperationProcessor;
import es.amplia.oda.operation.api.osgi.proxies.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OpenGateOperationProcessorFactoryImpl.class)
public class OpenGateOperationProcessorFactoryImplTest {

    @Mock
    private BundleContext mockedContext;
    @Mock
    private Serializer mockedSerializer;

    private OpenGateOperationProcessorFactoryImpl testFactory;

    @Mock
    private OperationRefreshInfoProxy mockedRefreshInfo;
    @Mock
    private OperationGetDeviceParametersProxy mockedGetDeviceParameters;
    @Mock
    private OperationSetDeviceParametersProxy mockedSetDeviceParameters;
    @Mock
    private OperationUpdateProxy mockedUpdate;
    @Mock
    private OperationSetClockProxy mockedSetClockEquipment;
    @Mock
    private OperationSynchronizeClockProxy mockedSynchronizeClock;
    @Mock
    private RefreshInfoProcessor mockedRefreshInfoProcessor;
    @Mock
    private GetDeviceParametersProcessor mockedGetDeviceParamsProcessor;
    @Mock
    private SetDeviceParametersProcessor mockedSetDeviceParamsProcessor;
    @Mock
    private UpdateProcessor mockedUpdateProcessor;
    @Mock
    private SetClockEquipmentProcessor mockedSetClockEquipmentProcessor;
    @Mock
    private SynchronizeClockProcessor mockedSynchronizeClockProcessor;
    @Mock
    private UnsupportedOperationProcessor mockedUnsupportedProcessor;
    @Mock
    private OpenGateOperationProcessor mockedOpenGateOperationProcessor;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(OperationRefreshInfoProxy.class).withAnyArguments().thenReturn(mockedRefreshInfo);
        PowerMockito.whenNew(OperationGetDeviceParametersProxy.class).withAnyArguments()
                .thenReturn(mockedGetDeviceParameters);
        PowerMockito.whenNew(OperationSetDeviceParametersProxy.class).withAnyArguments()
                .thenReturn(mockedSetDeviceParameters);
        PowerMockito.whenNew(OperationUpdateProxy.class).withAnyArguments().thenReturn(mockedUpdate);
        PowerMockito.whenNew(OperationSetClockProxy.class).withAnyArguments().thenReturn(mockedSetClockEquipment);
        PowerMockito.whenNew(OperationSynchronizeClockProxy.class).withAnyArguments().thenReturn(mockedSynchronizeClock);

        testFactory = new OpenGateOperationProcessorFactoryImpl(mockedContext, mockedSerializer);
    }

    @Test
    public void testConstructor() throws Exception {
        PowerMockito.verifyNew(OperationRefreshInfoProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OperationGetDeviceParametersProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OperationSetDeviceParametersProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OperationUpdateProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OperationSetClockProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OperationSynchronizeClockProxy.class).withArguments(eq(mockedContext));
    }

    @Test
    public void testCreateOperationProcessor() throws Exception {
        PowerMockito.whenNew(RefreshInfoProcessor.class).withAnyArguments().thenReturn(mockedRefreshInfoProcessor);
        PowerMockito.whenNew(GetDeviceParametersProcessor.class).withAnyArguments()
                .thenReturn(mockedGetDeviceParamsProcessor);
        PowerMockito.whenNew(SetDeviceParametersProcessor.class).withAnyArguments()
                .thenReturn(mockedSetDeviceParamsProcessor);
        PowerMockito.whenNew(UpdateProcessor.class).withAnyArguments().thenReturn(mockedUpdateProcessor);
        PowerMockito.whenNew(SetClockEquipmentProcessor.class)
                .withAnyArguments().thenReturn(mockedSetClockEquipmentProcessor);
        PowerMockito.whenNew(SynchronizeClockProcessor.class).withAnyArguments()
                .thenReturn(mockedSynchronizeClockProcessor);
        PowerMockito.whenNew(UnsupportedOperationProcessor.class).withAnyArguments()
                .thenReturn(mockedUnsupportedProcessor);
        PowerMockito.whenNew(OpenGateOperationProcessor.class).withAnyArguments()
                .thenReturn(mockedOpenGateOperationProcessor);

        OperationProcessor operationProcessor = testFactory.createOperationProcessor();

        assertEquals(mockedOpenGateOperationProcessor, operationProcessor);
        PowerMockito.verifyNew(RefreshInfoProcessor.class).withArguments(eq(mockedSerializer), eq(mockedRefreshInfo));
        PowerMockito.verifyNew(GetDeviceParametersProcessor.class)
                .withArguments(eq(mockedSerializer), eq(mockedGetDeviceParameters));
        PowerMockito.verifyNew(SetDeviceParametersProcessor.class)
                .withArguments(eq(mockedSerializer), eq(mockedSetDeviceParameters));
        PowerMockito.verifyNew(UpdateProcessor.class).withArguments(eq(mockedSerializer), eq(mockedUpdate));
        PowerMockito.verifyNew(SetClockEquipmentProcessor.class).withArguments(eq(mockedSerializer), eq(mockedSetClockEquipment));
        PowerMockito.verifyNew(UnsupportedOperationProcessor.class).withArguments(eq(mockedSerializer));
        PowerMockito.verifyNew(OpenGateOperationProcessor.class)
                .withArguments(any(Map.class), eq(mockedUnsupportedProcessor));
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
    }
}