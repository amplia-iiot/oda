package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.core.commons.utils.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(StateManagerProxy.class)
public class StateManagerProxyTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final DevicePattern TEST_DEVICE_PATTERN = new DevicePattern("*");
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final Object TEST_VALUE = "testValue";
    private static final Object TEST_VALUE_2 = 99.99;
    private static final Event TEST_EVENT = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, null, TEST_VALUE);


    @Mock
    private BundleContext mockedContext;
    private StateManagerProxy testProxy;

    @Mock
    private OsgiServiceProxy<StateManager> mockedProxy;
    @Mock
    private StateManager mockedStateManager;

    @Captor
    private ArgumentCaptor<Function<StateManager, CompletableFuture<DatastreamValue>>>
            datastreamValueFutureFunctionCaptor;
    @Captor
    private ArgumentCaptor<Function<StateManager, CompletableFuture<Set<DatastreamValue>>>>
            datastreamsValueFutureFunctionCaptor;
    @Captor
    private ArgumentCaptor<Consumer<StateManager>> stateManagerConsumerCaptor;


    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(OsgiServiceProxy.class).withAnyArguments().thenReturn(mockedProxy);

        testProxy = new StateManagerProxy(mockedContext);
    }

    @Test
    public void testConstructor() throws Exception {
        PowerMockito.verifyNew(OsgiServiceProxy.class).withArguments(eq(StateManager.class), eq(mockedContext));
    }

    @Test
    public void testGetDatastreamInformation() {
        testProxy.getDatastreamInformation(TEST_DEVICE_ID, TEST_DATASTREAM_ID);

        verify(mockedProxy).callFirst(datastreamValueFutureFunctionCaptor.capture());
        Function<StateManager, CompletableFuture<DatastreamValue>> capturedFunction =
                datastreamValueFutureFunctionCaptor.getValue();
        capturedFunction.apply(mockedStateManager);
        verify(mockedStateManager).getDatastreamInformation(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
    }

    @Test
    public void testGetDatastreamsInformation() {
        Set<String> datastreams = new HashSet<>(Arrays.asList(TEST_DATASTREAM_ID, TEST_DATASTREAM_ID_2));

        testProxy.getDatastreamsInformation(TEST_DEVICE_ID, datastreams);

        verify(mockedProxy).callFirst(datastreamsValueFutureFunctionCaptor.capture());
        Function<StateManager, CompletableFuture<Set<DatastreamValue>>> capturedFunction =
                datastreamsValueFutureFunctionCaptor.getValue();
        capturedFunction.apply(mockedStateManager);
        verify(mockedStateManager).getDatastreamsInformation(eq(TEST_DEVICE_ID), eq(datastreams));
    }

    @Test
    public void testGetDatastreamInformationWithDevicePattern() {
        testProxy.getDatastreamsInformation(TEST_DEVICE_PATTERN, TEST_DATASTREAM_ID);

        verify(mockedProxy).callFirst(datastreamsValueFutureFunctionCaptor.capture());
        Function<StateManager, CompletableFuture<Set<DatastreamValue>>> capturedFunction =
                datastreamsValueFutureFunctionCaptor.getValue();
        capturedFunction.apply(mockedStateManager);
        verify(mockedStateManager).getDatastreamsInformation(eq(TEST_DEVICE_PATTERN), eq(TEST_DATASTREAM_ID));
    }

    @Test
    public void testGetDatastreamsInformationWithDevicePattern() {
        Set<String> datastreams = new HashSet<>(Arrays.asList(TEST_DATASTREAM_ID, TEST_DATASTREAM_ID_2));

        testProxy.getDatastreamsInformation(TEST_DEVICE_PATTERN, datastreams);

        verify(mockedProxy).callFirst(datastreamsValueFutureFunctionCaptor.capture());
        Function<StateManager, CompletableFuture<Set<DatastreamValue>>> capturedFunction =
                datastreamsValueFutureFunctionCaptor.getValue();
        capturedFunction.apply(mockedStateManager);
        verify(mockedStateManager).getDatastreamsInformation(eq(TEST_DEVICE_PATTERN), eq(datastreams));
    }

    @Test
    public void testGetDeviceInformation() {
        testProxy.getDeviceInformation(TEST_DEVICE_ID);

        verify(mockedProxy).callFirst(datastreamsValueFutureFunctionCaptor.capture());
        Function<StateManager, CompletableFuture<Set<DatastreamValue>>> capturedFunction =
                datastreamsValueFutureFunctionCaptor.getValue();
        capturedFunction.apply(mockedStateManager);
        verify(mockedStateManager).getDeviceInformation(eq(TEST_DEVICE_ID));
    }

    @Test
    public void testSetDatastreamValue() {
        testProxy.setDatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_VALUE);

        verify(mockedProxy).callFirst(datastreamValueFutureFunctionCaptor.capture());
        Function<StateManager, CompletableFuture<DatastreamValue>> capturedFunction =
                datastreamValueFutureFunctionCaptor.getValue();
        capturedFunction.apply(mockedStateManager);
        verify(mockedStateManager).setDatastreamValue(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID), eq(TEST_VALUE));
    }

    @Test
    public void testSetDatastreamValues() {
        Map<String, Object> datastreamValues = new HashMap<>();
        datastreamValues.put(TEST_DATASTREAM_ID, TEST_VALUE);
        datastreamValues.put(TEST_DATASTREAM_ID_2, TEST_VALUE_2);

        testProxy.setDatastreamValues(TEST_DEVICE_ID, datastreamValues);

        verify(mockedProxy).callFirst(datastreamsValueFutureFunctionCaptor.capture());
        Function<StateManager, CompletableFuture<Set<DatastreamValue>>> capturedFunction =
                datastreamsValueFutureFunctionCaptor.getValue();
        capturedFunction.apply(mockedStateManager);
        verify(mockedStateManager).setDatastreamValues(eq(TEST_DEVICE_ID), eq(datastreamValues));
    }

    @Test
    public void testOnReceivedEvent() {
        testProxy.onReceivedEvents(Collections.singletonList(TEST_EVENT));

        verify(mockedProxy).consumeFirst(stateManagerConsumerCaptor.capture());
        Consumer<StateManager> consumer = stateManagerConsumerCaptor.getValue();
        consumer.accept(mockedStateManager);
        verify(mockedStateManager).onReceivedEvents(eq(Collections.singletonList(TEST_EVENT)));
    }

    @Test
    public void testClose() {
        testProxy.close();

        verify(mockedProxy).close();
    }
}