package es.amplia.oda.connector.dnp3.configuration;

import es.amplia.oda.connector.dnp3.DNP3Connector;

import com.automatak.dnp3.DNP3Exception;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DNP3ConnectorConfigurationHandler.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class DNP3ConnectorConfigurationHandlerTest {

    private static final String TEST_CHANNEL_ID = "testChannel";
    private static final String TEST_OUTSTATION_ID = "testOutstation";
    private static final String TEST_IP_ADDRESS = "TEST_IP_ADDRESS";
    private static final int TEST_IP_PORT = 20001;
    private static final int TEST_LOCAL_DEVICE_DNP3_ADDRESS = 4;
    private static final int TEST_REMOTE_DEVICE_DNP3_ADDRESS = 5;
    private static final int TEST_EVENT_BUFFER_SIZE = 10;
    private static final boolean TEST_UNSOLICITED_RESPONSE = true;
    private static final int TEST_LOG_LEVEL = 10;
    private static final boolean TEST_ENABLED = true;
    private static final DNP3ConnectorConfiguration TEST_CONFIGURATION = DNP3ConnectorConfiguration.builder()
            .channelIdentifier(TEST_CHANNEL_ID)
            .outstationIdentifier(TEST_OUTSTATION_ID).ipAddress(TEST_IP_ADDRESS).ipPort(TEST_IP_PORT)
            .localDeviceDNP3Address(TEST_LOCAL_DEVICE_DNP3_ADDRESS)
            .remoteDeviceDNP3Address(TEST_REMOTE_DEVICE_DNP3_ADDRESS).eventBufferSize(TEST_EVENT_BUFFER_SIZE)
            .unsolicitedResponse(TEST_UNSOLICITED_RESPONSE).logLevel(TEST_LOG_LEVEL).enable(TEST_ENABLED)
            .build();

    private static final String CURRENT_CONFIGURATION_FIELD_NAME = "currentConfiguration";

    private Dictionary<String,String> createDictionaryInstance() {
        return new Hashtable<>();
    }

    @Mock
    private DNP3Connector mockedConnector;
    @InjectMocks
    private DNP3ConnectorConfigurationHandler testConfigHandler;

    @Test
    public void testLoadDefaultConfiguration() {
        testConfigHandler.loadDefaultConfiguration();

        DNP3ConnectorConfiguration configuration =
                Whitebox.getInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME);

        assertEquals(DNP3ConnectorConfiguration.DEFAULT_CHANNEL_IDENTIFIER, configuration.getChannelIdentifier());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_OUTSTATION_IDENTIFIER,
                     configuration.getOutstationIdentifier());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_IP_ADDRESS, configuration.getIpAddress());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_IP_PORT, configuration.getIpPort());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_LOCAL_DEVICE_DNP3_ADDRESS,
                configuration.getLocalDeviceDNP3Address());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_REMOTE_DEVICE_DNP3_ADDRESS,
                     configuration.getRemoteDeviceDNP3Address());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_UNSOLICITED_RESPONSE,
                     configuration.isUnsolicitedResponse());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_EVENT_BUFFER_SIZE, configuration.getEventBufferSize());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_LOG_LEVEL, configuration.getLogLevel());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_ENABLE, configuration.isEnable());
    }

    @Test
    public void testLoadConfigurationComplete() {
        Dictionary<String, String> props = createDictionaryInstance();

        props.put(DNP3ConnectorConfigurationHandler.CHANNEL_IDENTIFIER_PROPERTY_NAME, TEST_CHANNEL_ID);
        props.put(DNP3ConnectorConfigurationHandler.OUTSTATION_IDENTIFIER_PROPERTY_NAME, TEST_OUTSTATION_ID);
        props.put(DNP3ConnectorConfigurationHandler.IP_ADDRESS_PROPERTY_NAME, TEST_IP_ADDRESS);
        props.put(DNP3ConnectorConfigurationHandler.IP_PORT_PROPERTY_NAME, String.valueOf(TEST_IP_PORT));
        props.put(DNP3ConnectorConfigurationHandler.LOCAL_DEVICE_DNP_ADDRESS_PROPERTY_NAME,
                  String.valueOf(TEST_LOCAL_DEVICE_DNP3_ADDRESS));
        props.put(DNP3ConnectorConfigurationHandler.REMOTE_DEVICE_DNP_ADDRESS_PROPERTY_NAME,
                  String.valueOf(TEST_REMOTE_DEVICE_DNP3_ADDRESS));
        props.put(DNP3ConnectorConfigurationHandler.UNSOLICITED_RESPONSE_PROPERTY_NAME,
                  String.valueOf(TEST_UNSOLICITED_RESPONSE));
        props.put(DNP3ConnectorConfigurationHandler.EVENT_BUFFER_SIZE_PROPERTY_NAME,
                  String.valueOf(TEST_EVENT_BUFFER_SIZE));
        props.put(DNP3ConnectorConfigurationHandler.LOG_LEVEL_PROPERTY_NAME, String.valueOf(TEST_LOG_LEVEL));
        props.put(DNP3ConnectorConfigurationHandler.ENABLE_PROPERTY_NAME, String.valueOf(TEST_ENABLED));

        testConfigHandler.loadConfiguration(props);

        DNP3ConnectorConfiguration configuration =
                Whitebox.getInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME);

        assertEquals(TEST_CHANNEL_ID, configuration.getChannelIdentifier());
        assertEquals(TEST_OUTSTATION_ID, configuration.getOutstationIdentifier());
        assertEquals(TEST_IP_ADDRESS, configuration.getIpAddress());
        assertEquals(TEST_IP_PORT, configuration.getIpPort());
        assertEquals(TEST_LOCAL_DEVICE_DNP3_ADDRESS, configuration.getLocalDeviceDNP3Address());
        assertEquals(TEST_REMOTE_DEVICE_DNP3_ADDRESS, configuration.getRemoteDeviceDNP3Address());
        assertEquals(TEST_UNSOLICITED_RESPONSE, configuration.isUnsolicitedResponse());
        assertEquals(TEST_EVENT_BUFFER_SIZE, configuration.getEventBufferSize());
        assertEquals(TEST_LOG_LEVEL, configuration.getLogLevel());
        assertEquals(TEST_ENABLED, configuration.isEnable());
    }

    @Test
    public void testLoadConfigurationIncomplete() {
        Dictionary<String, String> props = createDictionaryInstance();

        props.put(DNP3ConnectorConfigurationHandler.IP_ADDRESS_PROPERTY_NAME, TEST_IP_ADDRESS);
        props.put(DNP3ConnectorConfigurationHandler.IP_PORT_PROPERTY_NAME, String.valueOf(TEST_IP_PORT));
        props.put(DNP3ConnectorConfigurationHandler.LOCAL_DEVICE_DNP_ADDRESS_PROPERTY_NAME,
                String.valueOf(TEST_LOCAL_DEVICE_DNP3_ADDRESS));
        props.put(DNP3ConnectorConfigurationHandler.REMOTE_DEVICE_DNP_ADDRESS_PROPERTY_NAME,
                String.valueOf(TEST_REMOTE_DEVICE_DNP3_ADDRESS));

        testConfigHandler.loadConfiguration(props);

        DNP3ConnectorConfiguration configuration =
                Whitebox.getInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME);

        assertEquals(DNP3ConnectorConfiguration.DEFAULT_CHANNEL_IDENTIFIER, configuration.getChannelIdentifier());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_OUTSTATION_IDENTIFIER,
                     configuration.getOutstationIdentifier());
        assertEquals(TEST_IP_ADDRESS, configuration.getIpAddress());
        assertEquals(TEST_IP_PORT, configuration.getIpPort());
        assertEquals(TEST_LOCAL_DEVICE_DNP3_ADDRESS, configuration.getLocalDeviceDNP3Address());
        assertEquals(TEST_REMOTE_DEVICE_DNP3_ADDRESS, configuration.getRemoteDeviceDNP3Address());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_UNSOLICITED_RESPONSE,
                     configuration.isUnsolicitedResponse());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_EVENT_BUFFER_SIZE, configuration.getEventBufferSize());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_LOG_LEVEL, configuration.getLogLevel());
        assertEquals(DNP3ConnectorConfiguration.DEFAULT_ENABLE, configuration.isEnable());
    }

    @Test(expected = NumberFormatException.class)
    public void testLoadConfigurationNumberFormatException() {
        Dictionary<String, String> props = createDictionaryInstance();
        String wrongIpPort = "badFormat";

        props.put(DNP3ConnectorConfigurationHandler.CHANNEL_IDENTIFIER_PROPERTY_NAME, TEST_CHANNEL_ID);
        props.put(DNP3ConnectorConfigurationHandler.OUTSTATION_IDENTIFIER_PROPERTY_NAME, TEST_OUTSTATION_ID);
        props.put(DNP3ConnectorConfigurationHandler.IP_ADDRESS_PROPERTY_NAME, TEST_IP_ADDRESS);
        props.put(DNP3ConnectorConfigurationHandler.IP_PORT_PROPERTY_NAME, wrongIpPort);

        testConfigHandler.loadConfiguration(props);

        fail("Exception must be thrown");
    }

    @Test
    public void testApplyConfigurationConnectorEnabled() throws DNP3Exception {
        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, TEST_CONFIGURATION);

        testConfigHandler.applyConfiguration();

        verify(mockedConnector).loadConfiguration(eq(TEST_CONFIGURATION));
        verify(mockedConnector).init();
    }

    @Test
    public void testApplyConfigurationConnectorDisabled() throws DNP3Exception {
        DNP3ConnectorConfiguration disabledConfiguration = DNP3ConnectorConfiguration.builder().enable(false).build();

        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, disabledConfiguration);

        testConfigHandler.applyConfiguration();

        verify(mockedConnector).loadConfiguration(eq(disabledConfiguration));
        verify(mockedConnector, never()).init();
    }

    @Test(expected = Exception.class)
    public void testApplyConfigurationException() throws DNP3Exception {
        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, TEST_CONFIGURATION);

        doThrow(new DNP3Exception("")).when(mockedConnector).loadConfiguration(any(DNP3ConnectorConfiguration.class));

        testConfigHandler.applyConfiguration();

        fail("DNP3 exception must be thrown");
    }
}