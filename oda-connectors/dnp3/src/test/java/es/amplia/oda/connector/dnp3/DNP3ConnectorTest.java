package es.amplia.oda.connector.dnp3;

import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableInfoProxy;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.connector.dnp3.configuration.DNP3ConnectorConfiguration;

import com.automatak.dnp3.*;
import com.automatak.dnp3.impl.DNP3ManagerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DNP3ConnectorTest {

    // Do not load the opendnp3 native libs
    static {
        System.setProperty("com.automatak.dnp3.nostaticload", "");
    }

    private static final String TEST_CHANNEL_ID = "testChannel";
    private static final String TEST_OUTSTATION_ID = "testOutstation";
    private static final String TEST_IP_ADDRESS = "0.0.0.0";
    private static final int TEST_IP_PORT = 20000;
    private static final int TEST_LOCAL_DEVICE_DNP_ADDRESS = 1;
    private static final int TEST_REMOTE_DEVICE_DNP_ADDRESS = 2;
    private static final int TEST_EVENT_BUFFER_SIZE = 10;
    private static final int TEST_LOG_LEVEL = 0;
    private static final byte TEST_DATA_QUALITY = 0x01;
    private static final int TEST_INDEX = 1;
    private static final long TEST_TIMESTAMP = System.currentTimeMillis();

    private static final String MANAGER_FIELD_NAME = "manager";
    private static final String CHANNEL_LISTENER_FIELD_NAME = "channelListener";
    private static final String CHANNEL_FIELD_NAME = "channel";
    private static final String OUTSTATION_FIELD_NAME = "outstation";


    @Mock
    private ScadaTableInfoProxy mockedTableInfo;
    @Mock
    private ScadaDispatcher mockedDispatcher;
    @Mock
    private ServiceRegistrationManager<ScadaConnector> mockedScadaConnectorRegistrationManager;

    private DNP3Connector testConnector;

    private DNP3LogHandler constructedLogHandler;
    private final List<LogHandler> createManagerArgs = new ArrayList<>();

    @Mock
    private DNP3Manager mockedManager;
    @Mock
    private DNP3ChannelListener mockedListener;
    @Mock
    private Channel mockedChannel;
    @Mock
    private Outstation mockedOutstation;

    @Before
    public void setUp() {
        try (MockedConstruction<DNP3LogHandler> logHandlerCons = mockConstruction(DNP3LogHandler.class);
             MockedStatic<DNP3ManagerFactory> managerFactoryStatic = mockStatic(DNP3ManagerFactory.class)) {
            managerFactoryStatic.when(() -> DNP3ManagerFactory.createManager(any(LogHandler.class)))
                    .thenAnswer(invocation -> {
                        createManagerArgs.add(invocation.getArgument(0));
                        return mockedManager;
                    });

            testConnector = new DNP3Connector(mockedTableInfo, mockedDispatcher,
                    mockedScadaConnectorRegistrationManager);

            constructedLogHandler = logHandlerCons.constructed().isEmpty() ? null
                    : logHandlerCons.constructed().get(0);
        } catch (DNP3Exception e) {
            fail("DNP3 exception creating the connector: " + e);
        }
    }

    @Test
    public void testConstructor() {
        assertNotNull(testConnector);
        assertNotNull(constructedLogHandler);
        assertEquals(1, createManagerArgs.size());
        assertEquals(constructedLogHandler, createManagerArgs.get(0));
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        DNP3ConnectorConfiguration testConfiguration = DNP3ConnectorConfiguration.builder()
                                                        .channelIdentifier(TEST_CHANNEL_ID)
                                                        .outstationIdentifier(TEST_OUTSTATION_ID)
                                                        .ipAddress(TEST_IP_ADDRESS)
                                                        .ipPort(TEST_IP_PORT)
                                                        .localDeviceDNP3Address(TEST_LOCAL_DEVICE_DNP_ADDRESS)
                                                        .remoteDeviceDNP3Address(TEST_REMOTE_DEVICE_DNP_ADDRESS)
                                                        .eventBufferSize(TEST_EVENT_BUFFER_SIZE)
                                                        .logLevel(TEST_LOG_LEVEL)
                                                        .build();

        Whitebox.setInternalState(testConnector, MANAGER_FIELD_NAME, mockedManager);

        try (MockedConstruction<DNP3ChannelListener> listenerCons = mockConstruction(DNP3ChannelListener.class)) {
            when(mockedManager.addTCPServer(anyString(), anyInt(), any(ChannelRetry.class), anyString(), anyInt(),
                    any(ChannelListener.class))).thenReturn(mockedChannel);

            testConnector.loadConfiguration(testConfiguration);

            assertEquals(1, listenerCons.constructed().size());
            verify(mockedManager).addTCPServer(eq(TEST_CHANNEL_ID), eq(TEST_LOG_LEVEL), any(ChannelRetry.class),
                                               eq(TEST_IP_ADDRESS), eq(TEST_IP_PORT),
                                               eq(listenerCons.constructed().get(0)));
            verify(mockedTableInfo).getNumBinaryInputs();
            verify(mockedTableInfo).getNumDoubleBinaryInputs();
            verify(mockedTableInfo).getNumAnalogInputs();
            verify(mockedTableInfo).getNumCounters();
            verify(mockedTableInfo).getNumFrozenCounters();
            verify(mockedTableInfo).getNumBinaryOutputs();
            verify(mockedTableInfo).getNumAnalogOutputs();
            assertNotNull(Whitebox.getInternalState(testConnector, "outstationStackConfig"));
        }
    }

    @Test
    public void testLoadConfigurationAlreadyLoadedConfiguration() throws Exception {
        DNP3ConnectorConfiguration testConfiguration = DNP3ConnectorConfiguration.builder()
                                                        .channelIdentifier(TEST_CHANNEL_ID)
                                                        .outstationIdentifier(TEST_OUTSTATION_ID)
                                                        .ipAddress(TEST_IP_ADDRESS)
                                                        .ipPort(TEST_IP_PORT)
                                                        .localDeviceDNP3Address(TEST_LOCAL_DEVICE_DNP_ADDRESS)
                                                        .remoteDeviceDNP3Address(TEST_REMOTE_DEVICE_DNP_ADDRESS)
                                                        .eventBufferSize(TEST_EVENT_BUFFER_SIZE)
                                                        .logLevel(TEST_LOG_LEVEL)
                                                        .build();
        Channel oldMockedChannel = mock(Channel.class);
        Outstation oldMockedOutstation = mock(Outstation.class);

        Whitebox.setInternalState(testConnector, MANAGER_FIELD_NAME, mockedManager);
        Whitebox.setInternalState(testConnector, CHANNEL_FIELD_NAME, oldMockedChannel);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, oldMockedOutstation);

        try (MockedConstruction<DNP3ChannelListener> listenerCons = mockConstruction(DNP3ChannelListener.class);
             MockedConstruction<ScadaCommandHandler> commandHandlerCons = mockConstruction(ScadaCommandHandler.class)) {
            when(mockedManager.addTCPServer(anyString(), anyInt(), any(ChannelRetry.class), anyString(), anyInt(),
                    any(ChannelListener.class))).thenReturn(mockedChannel);
            when(mockedChannel.addOutstation(anyString(), any(CommandHandler.class), any(OutstationApplication.class),
                    any(OutstationStackConfig.class))).thenReturn(mockedOutstation);

            testConnector.loadConfiguration(testConfiguration);
            testConnector.init();

            verify(mockedScadaConnectorRegistrationManager).unregister();
            verify(oldMockedOutstation).shutdown();
            verify(oldMockedChannel).shutdown();
            assertEquals(1, listenerCons.constructed().size());
            verify(mockedManager).addTCPServer(eq(TEST_CHANNEL_ID), eq(TEST_LOG_LEVEL), any(ChannelRetry.class),
                    eq(TEST_IP_ADDRESS), eq(TEST_IP_PORT), any(ChannelListener.class));
        }
    }

    @Test
    public void testInit() throws Exception {
        OutstationStackConfig mockedOutstationStackConfig = mock(OutstationStackConfig.class);

        Whitebox.setInternalState(testConnector, CHANNEL_FIELD_NAME, mockedChannel);
        Whitebox.setInternalState(testConnector, "outstationIdentifier", TEST_OUTSTATION_ID);
        Whitebox.setInternalState(testConnector, "outstationStackConfig", mockedOutstationStackConfig);

        List<List<?>> commandHandlerArgs = new ArrayList<>();
        try (MockedConstruction<ScadaCommandHandler> commandHandlerCons =
                     mockConstruction(ScadaCommandHandler.class,
                             (mock, mctx) -> commandHandlerArgs.add(new ArrayList<>(mctx.arguments())))) {
            when(mockedChannel.addOutstation(anyString(), any(CommandHandler.class), any(OutstationApplication.class),
                    any(OutstationStackConfig.class))).thenReturn(mockedOutstation);

            testConnector.init();

            assertEquals(1, commandHandlerCons.constructed().size());
            assertEquals(mockedDispatcher, commandHandlerArgs.get(0).get(0));
            verify(mockedChannel).addOutstation(eq(TEST_OUTSTATION_ID), eq(commandHandlerCons.constructed().get(0)),
                    any(OutstationApplication.class), eq(mockedOutstationStackConfig));
            verify(mockedOutstation).enable();
            verify(mockedScadaConnectorRegistrationManager).register(eq(testConnector));
        }
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testUplinkBoolean() {
        boolean testValue = true;
        String testType = "type";

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);

        List<List<?>> binaryInputArgs = new ArrayList<>();
        try (MockedConstruction<OutstationChangeSet> changeSetCons = mockConstruction(OutstationChangeSet.class);
             MockedConstruction<BinaryInput> binaryInputCons =
                     mockConstruction(BinaryInput.class,
                             (mock, mctx) -> binaryInputArgs.add(new ArrayList<>(mctx.arguments())))) {

            testConnector.uplink(TEST_INDEX, testValue, testType, TEST_TIMESTAMP);

            verify(mockedListener).isOpen();
            assertEquals(1, changeSetCons.constructed().size());
            assertEquals(1, binaryInputCons.constructed().size());
            assertEquals(testValue, binaryInputArgs.get(0).get(0));
            assertEquals(TEST_DATA_QUALITY, binaryInputArgs.get(0).get(1));
            assertEquals(TEST_TIMESTAMP, binaryInputArgs.get(0).get(2));
            verify(changeSetCons.constructed().get(0))
                    .update(eq(binaryInputCons.constructed().get(0)), eq(TEST_INDEX));
            verify(mockedOutstation).apply(eq(changeSetCons.constructed().get(0)));
        }
    }

    @Test
    public void testUplinkTrueAsString() {
        String testValue = Boolean.TRUE.toString();
        String testType = "type";

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);

        List<List<?>> binaryInputArgs = new ArrayList<>();
        try (MockedConstruction<OutstationChangeSet> changeSetCons = mockConstruction(OutstationChangeSet.class);
             MockedConstruction<BinaryInput> binaryInputCons =
                     mockConstruction(BinaryInput.class,
                             (mock, mctx) -> binaryInputArgs.add(new ArrayList<>(mctx.arguments())))) {

            testConnector.uplink(TEST_INDEX, testValue, testType, TEST_TIMESTAMP);

            verify(mockedListener).isOpen();
            assertEquals(1, changeSetCons.constructed().size());
            assertEquals(1, binaryInputCons.constructed().size());
            assertEquals(Boolean.TRUE, binaryInputArgs.get(0).get(0));
            assertEquals(TEST_DATA_QUALITY, binaryInputArgs.get(0).get(1));
            assertEquals(TEST_TIMESTAMP, binaryInputArgs.get(0).get(2));
            verify(changeSetCons.constructed().get(0))
                    .update(eq(binaryInputCons.constructed().get(0)), eq(TEST_INDEX));
            verify(mockedOutstation).apply(eq(changeSetCons.constructed().get(0)));
        }
    }

    @Test
    public void testUplinkFalseAsString() {
        String testValue = Boolean.FALSE.toString();
        String testType = "type";

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);

        List<List<?>> binaryInputArgs = new ArrayList<>();
        try (MockedConstruction<OutstationChangeSet> changeSetCons = mockConstruction(OutstationChangeSet.class);
             MockedConstruction<BinaryInput> binaryInputCons =
                     mockConstruction(BinaryInput.class,
                             (mock, mctx) -> binaryInputArgs.add(new ArrayList<>(mctx.arguments())))) {

            testConnector.uplink(TEST_INDEX, testValue, testType, TEST_TIMESTAMP);

            verify(mockedListener).isOpen();
            assertEquals(1, changeSetCons.constructed().size());
            assertEquals(1, binaryInputCons.constructed().size());
            assertEquals(Boolean.FALSE, binaryInputArgs.get(0).get(0));
            assertEquals(TEST_DATA_QUALITY, binaryInputArgs.get(0).get(1));
            assertEquals(TEST_TIMESTAMP, binaryInputArgs.get(0).get(2));
            verify(changeSetCons.constructed().get(0))
                    .update(eq(binaryInputCons.constructed().get(0)), eq(TEST_INDEX));
            verify(mockedOutstation).apply(eq(changeSetCons.constructed().get(0)));
        }
    }

    @Test
    public void testUplinkInt() {
        int testValue = 1;
        String testType = "type";

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);

        List<List<?>> analogInputArgs = new ArrayList<>();
        try (MockedConstruction<OutstationChangeSet> changeSetCons = mockConstruction(OutstationChangeSet.class);
             MockedConstruction<AnalogInput> analogInputCons =
                     mockConstruction(AnalogInput.class,
                             (mock, mctx) -> analogInputArgs.add(new ArrayList<>(mctx.arguments())))) {

            testConnector.uplink(TEST_INDEX, testValue, testType, TEST_TIMESTAMP);

            verify(mockedListener).isOpen();
            assertEquals(1, changeSetCons.constructed().size());
            assertEquals(1, analogInputCons.constructed().size());
            assertEquals((double) testValue, analogInputArgs.get(0).get(0));
            assertEquals(TEST_DATA_QUALITY, analogInputArgs.get(0).get(1));
            assertEquals(TEST_TIMESTAMP, analogInputArgs.get(0).get(2));
            verify(changeSetCons.constructed().get(0))
                    .update(eq(analogInputCons.constructed().get(0)), eq(TEST_INDEX));
            verify(mockedOutstation).apply(eq(changeSetCons.constructed().get(0)));
        }
    }

    @Test
    public void testUplinkDouble() {
        double testValue = 18.50;
        String testType = "type";

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);

        List<List<?>> analogInputArgs = new ArrayList<>();
        try (MockedConstruction<OutstationChangeSet> changeSetCons = mockConstruction(OutstationChangeSet.class);
             MockedConstruction<AnalogInput> analogInputCons =
                     mockConstruction(AnalogInput.class,
                             (mock, mctx) -> analogInputArgs.add(new ArrayList<>(mctx.arguments())))) {

            testConnector.uplink(TEST_INDEX, testValue, testType, TEST_TIMESTAMP);

            verify(mockedListener).isOpen();
            assertEquals(1, changeSetCons.constructed().size());
            assertEquals(1, analogInputCons.constructed().size());
            assertEquals(testValue, analogInputArgs.get(0).get(0));
            assertEquals(TEST_DATA_QUALITY, analogInputArgs.get(0).get(1));
            assertEquals(TEST_TIMESTAMP, analogInputArgs.get(0).get(2));
            verify(changeSetCons.constructed().get(0))
                    .update(eq(analogInputCons.constructed().get(0)), eq(TEST_INDEX));
            verify(mockedOutstation).apply(eq(changeSetCons.constructed().get(0)));
        }
    }

    @Test
    public void testUplinkDoubleAsString() {
        double testValue = 18.50;
        String testValueAsString = Double.toString(testValue);
        String testType = "type";

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);

        List<List<?>> analogInputArgs = new ArrayList<>();
        try (MockedConstruction<OutstationChangeSet> changeSetCons = mockConstruction(OutstationChangeSet.class);
             MockedConstruction<AnalogInput> analogInputCons =
                     mockConstruction(AnalogInput.class,
                             (mock, mctx) -> analogInputArgs.add(new ArrayList<>(mctx.arguments())))) {

            testConnector.uplink(TEST_INDEX, testValueAsString, testType, TEST_TIMESTAMP);

            verify(mockedListener).isOpen();
            assertEquals(1, changeSetCons.constructed().size());
            assertEquals(1, analogInputCons.constructed().size());
            assertEquals(testValue, analogInputArgs.get(0).get(0));
            assertEquals(TEST_DATA_QUALITY, analogInputArgs.get(0).get(1));
            assertEquals(TEST_TIMESTAMP, analogInputArgs.get(0).get(2));
            verify(changeSetCons.constructed().get(0))
                    .update(eq(analogInputCons.constructed().get(0)), eq(TEST_INDEX));
            verify(mockedOutstation).apply(eq(changeSetCons.constructed().get(0)));
        }
    }

    @Test
    public void testUplinkDoubleAsStringNotValid() {
        String testValueAsString = "18.50la";
        String testType = "type";

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);

        try (MockedConstruction<OutstationChangeSet> changeSetCons = mockConstruction(OutstationChangeSet.class);
             MockedConstruction<AnalogInput> analogInputCons = mockConstruction(AnalogInput.class)) {

            testConnector.uplink(TEST_INDEX, testValueAsString, testType, TEST_TIMESTAMP);

            verify(mockedListener).isOpen();
            verify(mockedOutstation, never()).apply(any(ChangeSet.class));
        }
    }

    @Test
    public void testUplinkConnectorNotConfigured() {
        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);

        when(mockedListener.isOpen()).thenReturn(false);

        testConnector.uplink(TEST_INDEX, 1, "type", TEST_TIMESTAMP);

        verify(mockedListener).isOpen();
        verify(mockedOutstation, never()).apply(any(ChangeSet.class));
    }

    @Test
    public void testUplinkNotScadaData() {
        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);

        testConnector.uplink(TEST_INDEX, new Object(), new Object(), TEST_TIMESTAMP);

        verify(mockedListener).isOpen();
        verify(mockedOutstation, never()).apply(any(ChangeSet.class));
    }

    @Test
    public void testIsConnected() {
        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);

        when(mockedListener.isOpen()).thenReturn(true);

        boolean connected = testConnector.isConnected();

        assertTrue(connected);
    }

    @Test
    public void testIsConnectedNullDnpChannelListener() {
        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, (Object) null);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testIsConnectedNoOpenChannelState() {
        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);

        when(mockedListener.isOpen()).thenReturn(false);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testConnector, MANAGER_FIELD_NAME, mockedManager);
        Whitebox.setInternalState(testConnector, CHANNEL_FIELD_NAME, mockedChannel);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        testConnector.close();

        verify(mockedScadaConnectorRegistrationManager).unregister();
        verify(mockedOutstation).shutdown();
        verify(mockedChannel).shutdown();
        verify(mockedManager).shutdown();
    }

    @Test
    public void testCloseNotConfigurationLoaded() {
        Whitebox.setInternalState(testConnector, MANAGER_FIELD_NAME, mockedManager);

        testConnector.close();

        verify(mockedManager).shutdown();
    }
}
