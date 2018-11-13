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
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DNP3Connector.class, DNP3ManagerFactory.class })
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

    @Mock
    private DNP3LogHandler mockedLogHandler;
    @Mock
    private DNP3Manager mockedManager;
    @Mock
    private DNP3ChannelListener mockedListener;
    @Mock
    private Channel mockedChannel;
    @Mock
    private ScadaCommandHandler mockedCommandHandler;
    @Mock
    private Outstation mockedOutstation;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(DNP3LogHandler.class).withAnyArguments().thenReturn(mockedLogHandler);
        PowerMockito.mockStatic(DNP3ManagerFactory.class);
        PowerMockito.when(DNP3ManagerFactory.createManager(any(LogHandler.class))).thenReturn(mockedManager);

        testConnector = new DNP3Connector(mockedTableInfo, mockedDispatcher, mockedScadaConnectorRegistrationManager);
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(testConnector);
        PowerMockito.verifyNew(DNP3LogHandler.class).withArguments(any(Logger.class));
        PowerMockito.verifyStatic(DNP3ManagerFactory.class);
        DNP3ManagerFactory.createManager(eq(mockedLogHandler));
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

        PowerMockito.whenNew(DNP3ChannelListener.class).withAnyArguments().thenReturn(mockedListener);
        when(mockedManager.addTCPServer(anyString(), anyInt(), any(ChannelRetry.class), anyString(), anyInt(),
                any(ChannelListener.class))).thenReturn(mockedChannel);
        when(mockedChannel.addOutstation(anyString(), any(CommandHandler.class), any(OutstationApplication.class),
                any(OutstationStackConfig.class))).thenReturn(mockedOutstation);

        testConnector.loadConfiguration(testConfiguration);

        PowerMockito.verifyNew(DNP3ChannelListener.class).withNoArguments();
        verify(mockedManager).addTCPServer(eq(TEST_CHANNEL_ID), eq(TEST_LOG_LEVEL), any(ChannelRetry.class),
                                           eq(TEST_IP_ADDRESS), eq(TEST_IP_PORT), eq(mockedListener));
        verify(mockedTableInfo).getNumBinaryInputs();
        verify(mockedTableInfo).getNumDoubleBinaryInputs();
        verify(mockedTableInfo).getNumAnalogInputs();
        verify(mockedTableInfo).getNumCounters();
        verify(mockedTableInfo).getNumFrozenCounters();
        verify(mockedTableInfo).getNumBinaryOutputs();
        verify(mockedTableInfo).getNumAnalogOutputs();
        assertNotNull(Whitebox.getInternalState(testConnector, "outstationStackConfig"));

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

        PowerMockito.whenNew(DNP3ChannelListener.class).withAnyArguments().thenReturn(mockedListener);
        when(mockedManager.addTCPServer(anyString(), anyInt(), any(ChannelRetry.class), anyString(), anyInt(),
                any(ChannelListener.class))).thenReturn(mockedChannel);
        PowerMockito.whenNew(ScadaCommandHandler.class).withAnyArguments().thenReturn(mockedCommandHandler);
        when(mockedChannel.addOutstation(anyString(), any(CommandHandler.class), any(OutstationApplication.class),
                any(OutstationStackConfig.class))).thenReturn(mockedOutstation);

        testConnector.loadConfiguration(testConfiguration);
        testConnector.init();

        verify(mockedScadaConnectorRegistrationManager).unregister();
        verify(oldMockedOutstation).shutdown();
        verify(oldMockedChannel).shutdown();
        PowerMockito.verifyNew(DNP3ChannelListener.class).withNoArguments();
        verify(mockedManager).addTCPServer(eq(TEST_CHANNEL_ID), eq(TEST_LOG_LEVEL), any(ChannelRetry.class),
                eq(TEST_IP_ADDRESS), eq(TEST_IP_PORT), any(ChannelListener.class));
    }

    @Test
    public void testInit() throws Exception {
        OutstationStackConfig mockedOutstationStackConfig = mock(OutstationStackConfig.class);

        Whitebox.setInternalState(testConnector, CHANNEL_FIELD_NAME, mockedChannel);
        Whitebox.setInternalState(testConnector, "outstationIdentifier", TEST_OUTSTATION_ID);
        Whitebox.setInternalState(testConnector, "outstationStackConfig", mockedOutstationStackConfig);

        PowerMockito.whenNew(ScadaCommandHandler.class).withAnyArguments().thenReturn(mockedCommandHandler);
        when(mockedChannel.addOutstation(anyString(), any(CommandHandler.class), any(OutstationApplication.class),
                any(OutstationStackConfig.class))).thenReturn(mockedOutstation);

        testConnector.init();

        PowerMockito.verifyNew(ScadaCommandHandler.class).withArguments(eq(mockedDispatcher));
        verify(mockedChannel).addOutstation(eq(TEST_OUTSTATION_ID), eq(mockedCommandHandler),
                any(OutstationApplication.class), eq(mockedOutstationStackConfig));
        verify(mockedOutstation).enable();
        verify(mockedScadaConnectorRegistrationManager).register(eq(testConnector));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testUplinkBoolean() throws Exception {
        boolean testValue = true;
        String testType = "type";
        OutstationChangeSet mockedChangeSet = mock(OutstationChangeSet.class);
        BinaryInput mockedBinaryInput = mock(BinaryInput.class);

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);
        PowerMockito.whenNew(OutstationChangeSet.class).withNoArguments().thenReturn(mockedChangeSet);
        PowerMockito.whenNew(BinaryInput.class).withAnyArguments().thenReturn(mockedBinaryInput);

        testConnector.uplink(TEST_INDEX, testValue, testType, TEST_TIMESTAMP);

        verify(mockedListener).isOpen();
        PowerMockito.verifyNew(OutstationChangeSet.class).withNoArguments();
        PowerMockito.verifyNew(OutstationChangeSet.class).withNoArguments();
        PowerMockito.verifyNew(BinaryInput.class).withArguments(eq(testValue), eq(TEST_DATA_QUALITY), eq(TEST_TIMESTAMP));
        mockedChangeSet.update(eq(mockedBinaryInput), eq(TEST_INDEX));
        mockedOutstation.apply(eq(mockedChangeSet));
    }

    @Test
    public void testUplinkTrueAsString() throws Exception {
        String testValue = Boolean.TRUE.toString();
        String testType = "type";
        OutstationChangeSet mockedChangeSet = mock(OutstationChangeSet.class);
        BinaryInput mockedBinaryInput = mock(BinaryInput.class);

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);
        PowerMockito.whenNew(OutstationChangeSet.class).withNoArguments().thenReturn(mockedChangeSet);
        PowerMockito.whenNew(BinaryInput.class).withAnyArguments().thenReturn(mockedBinaryInput);

        testConnector.uplink(TEST_INDEX, testValue, testType, TEST_TIMESTAMP);

        verify(mockedListener).isOpen();
        PowerMockito.verifyNew(OutstationChangeSet.class).withNoArguments();
        PowerMockito.verifyNew(OutstationChangeSet.class).withNoArguments();
        PowerMockito.verifyNew(BinaryInput.class).withArguments(eq(Boolean.TRUE), eq(TEST_DATA_QUALITY), eq(TEST_TIMESTAMP));
        mockedChangeSet.update(eq(mockedBinaryInput), eq(TEST_INDEX));
        mockedOutstation.apply(eq(mockedChangeSet));
    }

    @Test
    public void testUplinkFalseAsString() throws Exception {
        String testValue = Boolean.FALSE.toString();
        String testType = "type";
        OutstationChangeSet mockedChangeSet = mock(OutstationChangeSet.class);
        BinaryInput mockedBinaryInput = mock(BinaryInput.class);

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);
        PowerMockito.whenNew(OutstationChangeSet.class).withNoArguments().thenReturn(mockedChangeSet);
        PowerMockito.whenNew(BinaryInput.class).withAnyArguments().thenReturn(mockedBinaryInput);

        testConnector.uplink(TEST_INDEX, testValue, testType, TEST_TIMESTAMP);

        verify(mockedListener).isOpen();
        PowerMockito.verifyNew(OutstationChangeSet.class).withNoArguments();
        PowerMockito.verifyNew(OutstationChangeSet.class).withNoArguments();
        PowerMockito.verifyNew(BinaryInput.class).withArguments(eq(Boolean.FALSE), eq(TEST_DATA_QUALITY), eq(TEST_TIMESTAMP));
        mockedChangeSet.update(eq(mockedBinaryInput), eq(TEST_INDEX));
        mockedOutstation.apply(eq(mockedChangeSet));
    }

    @Test
    public void testUplinkInt() throws Exception {
        int testValue = 1;
        String testType = "type";
        OutstationChangeSet mockedChangeSet = mock(OutstationChangeSet.class);
        AnalogInput mockedAnalogInput = mock(AnalogInput.class);

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);


        when(mockedListener.isOpen()).thenReturn(true);
        PowerMockito.whenNew(OutstationChangeSet.class).withNoArguments().thenReturn(mockedChangeSet);
        PowerMockito.whenNew(AnalogInput.class).withAnyArguments().thenReturn(mockedAnalogInput);

        testConnector.uplink(TEST_INDEX, testValue, testType, TEST_TIMESTAMP);

        verify(mockedListener).isOpen();
        PowerMockito.verifyNew(OutstationChangeSet.class).withNoArguments();
        PowerMockito.verifyNew(AnalogInput.class)
                .withArguments(eq((double) testValue), eq(TEST_DATA_QUALITY), eq(TEST_TIMESTAMP));
        mockedChangeSet.update(eq(mockedAnalogInput), eq(TEST_INDEX));
        mockedOutstation.apply(eq(mockedChangeSet));
    }

    @Test
    public void testUplinkDouble() throws Exception {
        double testValue = 18.50;
        String testType = "type";
        OutstationChangeSet mockedChangeSet = mock(OutstationChangeSet.class);
        AnalogInput mockedAnalogInput = mock(AnalogInput.class);

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);
        PowerMockito.whenNew(OutstationChangeSet.class).withNoArguments().thenReturn(mockedChangeSet);
        PowerMockito.whenNew(AnalogInput.class).withAnyArguments().thenReturn(mockedAnalogInput);

        testConnector.uplink(TEST_INDEX, testValue, testType, TEST_TIMESTAMP);

        verify(mockedListener).isOpen();
        PowerMockito.verifyNew(OutstationChangeSet.class).withNoArguments();
        PowerMockito.verifyNew(AnalogInput.class).withArguments(eq(testValue), eq(TEST_DATA_QUALITY), eq(TEST_TIMESTAMP));
        mockedChangeSet.update(eq(mockedAnalogInput), eq(TEST_INDEX));
        mockedOutstation.apply(eq(mockedChangeSet));
    }

    @Test
    public void testUplinkDoubleAsString() throws Exception {
        double testValue = 18.50;
        String testValueAsString = Double.toString(testValue);
        String testType = "type";
        OutstationChangeSet mockedChangeSet = mock(OutstationChangeSet.class);
        AnalogInput mockedAnalogInput = mock(AnalogInput.class);

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);
        PowerMockito.whenNew(OutstationChangeSet.class).withNoArguments().thenReturn(mockedChangeSet);
        PowerMockito.whenNew(AnalogInput.class).withAnyArguments().thenReturn(mockedAnalogInput);

        testConnector.uplink(TEST_INDEX, testValueAsString, testType, TEST_TIMESTAMP);

        verify(mockedListener).isOpen();
        PowerMockito.verifyNew(OutstationChangeSet.class).withNoArguments();
        PowerMockito.verifyNew(AnalogInput.class).withArguments(eq(testValue), eq(TEST_DATA_QUALITY), eq(TEST_TIMESTAMP));
        mockedChangeSet.update(eq(mockedAnalogInput), eq(TEST_INDEX));
        mockedOutstation.apply(eq(mockedChangeSet));
    }

    @Test
    public void testUplinkDoubleAsStringNotValid() throws Exception {
        String testValueAsString = "18.50la";
        String testType = "type";
        OutstationChangeSet mockedChangeSet = mock(OutstationChangeSet.class);
        AnalogInput mockedAnalogInput = mock(AnalogInput.class);

        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, mockedListener);
        Whitebox.setInternalState(testConnector, OUTSTATION_FIELD_NAME, mockedOutstation);

        when(mockedListener.isOpen()).thenReturn(true);
        PowerMockito.whenNew(OutstationChangeSet.class).withNoArguments().thenReturn(mockedChangeSet);
        PowerMockito.whenNew(AnalogInput.class).withAnyArguments().thenReturn(mockedAnalogInput);

        testConnector.uplink(TEST_INDEX, testValueAsString, testType, TEST_TIMESTAMP);

        verify(mockedListener).isOpen();
        verify(mockedOutstation, never()).apply(any(ChangeSet.class));
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
        Whitebox.setInternalState(testConnector, CHANNEL_LISTENER_FIELD_NAME, null);

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