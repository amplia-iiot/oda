package es.amplia.oda.hardware.snmp.configuration;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.hardware.snmp.internal.SnmpClientFactory;
import es.amplia.oda.hardware.snmp.internal.SnmpClientManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SnmpConfigurationUpdateHandlerTest {

    private static final String VERSION_PROPERTY_NAME = "version";
    private static final String IP_PROPERTY_NAME = "ip";
    private static final String PORT_PROPERTY_NAME = "port";
    private static final String LISTEN_PORT_PROPERTY_NAME = "listenPort";
    private static final String COMMUNITY_PROPERTY_NAME = "community";
    private static final String CONTEXT_NAME_PROPERTY_NAME = "contextName";
    private static final String SECURITY_NAME_PROPERTY_NAME = "securityName";
    private static final String AUTH_PASSPHRASE_PROPERTY_NAME = "authPassphrase";
    private static final String PRIV_PASSPHRASE_PROPERTY_NAME = "privPassphrase";
    private static final String AUTH_PROTOCOL_PROPERTY_NAME = "authProtocol";
    private static final String PRIV_PROTOCOL_PROPERTY_NAME = "privProtocol";

    private static final String TEST_DEVICE_ID_VALUE = "testDevice";
    private static final int TEST_VERSION_1_VALUE = 1;
    private static final int TEST_VERSION_2_VALUE = 2;
    private static final int TEST_VERSION_3_VALUE = 3;
    private static final int TEST_PORT_VALUE = 12345;
    private static final int TEST_LISTEN_PORT_VALUE = 12346;
    private static final String TEST_IP_VALUE = "127.0.0.1";
    private static final String TEST_COMMUNITY_VALUE = "public";
    private static final String TEST_CONTEXT_NAME_VALUE = "public";
    private static final String TEST_SECURITY_NAME_VALUE = "simulator";
    private static final String TEST_AUTH_PASSPHRASE_VALUE = "auctoritas";
    private static final String TEST_PRIV_PASSPHRASE_VALUE = "privatus";
    private static final String TEST_AUTH_PROTOCOL_VALUE = "MD5";
    private static final String TEST_PRIV_PROTOCOL_VALUE = "DES";


    private static final SnmpClientConfig TEST_SNMP_V1_COMPLETE_CONFIGURATION =
            new SnmpClientConfig(TEST_DEVICE_ID_VALUE, TEST_IP_VALUE, TEST_PORT_VALUE, TEST_LISTEN_PORT_VALUE,
                    TEST_VERSION_1_VALUE, new SnmpClientOptions(TEST_COMMUNITY_VALUE));

    private static final SnmpClientConfig TEST_SNMP_V2_COMPLETE_CONFIGURATION =
            new SnmpClientConfig(TEST_DEVICE_ID_VALUE, TEST_IP_VALUE, TEST_PORT_VALUE, TEST_LISTEN_PORT_VALUE,
                    TEST_VERSION_2_VALUE, new SnmpClientOptions(TEST_COMMUNITY_VALUE));

    private static final SnmpClientConfig TEST_SNMP_V3_COMPLETE_CONFIGURATION =
            new SnmpClientConfig(TEST_DEVICE_ID_VALUE, TEST_IP_VALUE, TEST_PORT_VALUE, TEST_LISTEN_PORT_VALUE,
                    TEST_VERSION_3_VALUE, new SnmpClientV3Options(TEST_SECURITY_NAME_VALUE, TEST_AUTH_PASSPHRASE_VALUE,
                    TEST_PRIV_PASSPHRASE_VALUE, TEST_CONTEXT_NAME_VALUE, TEST_AUTH_PROTOCOL_VALUE, TEST_PRIV_PROTOCOL_VALUE));

    @Mock
    SnmpClientManager mockedSnmpClientManager;
    @Mock
    SnmpClientFactory mockedSnmpClientFactory;
    @InjectMocks
    private SnmpConfigurationUpdateHandler testConfigHandler;

    @Test
    public void testLoadSnmpV1CompleteConfiguration() {
        Dictionary<String, String> snmpCompleteConfiguration = new Hashtable<>();

        String snmpConfig = VERSION_PROPERTY_NAME + ":" + TEST_VERSION_1_VALUE + ","
                + IP_PROPERTY_NAME + ":" + TEST_IP_VALUE + ","
                + PORT_PROPERTY_NAME + ":" + TEST_PORT_VALUE + ","
                + LISTEN_PORT_PROPERTY_NAME + ":" + TEST_LISTEN_PORT_VALUE + ","
                + COMMUNITY_PROPERTY_NAME + ":" + TEST_COMMUNITY_VALUE;
        snmpCompleteConfiguration.put(TEST_DEVICE_ID_VALUE, snmpConfig);

        testConfigHandler.loadConfiguration(snmpCompleteConfiguration);

        verify(mockedSnmpClientFactory).createSnmpClient(TEST_SNMP_V1_COMPLETE_CONFIGURATION);
    }

    @Test
    public void testLoadSnmpV2CompleteConfiguration() {
        Dictionary<String, String> snmpCompleteConfiguration = new Hashtable<>();

        String snmpConfig = VERSION_PROPERTY_NAME + ":" + TEST_VERSION_2_VALUE + ","
                + IP_PROPERTY_NAME + ":" + TEST_IP_VALUE + ","
                + PORT_PROPERTY_NAME + ":" + TEST_PORT_VALUE + ","
                + LISTEN_PORT_PROPERTY_NAME + ":" + TEST_LISTEN_PORT_VALUE + ","
                + COMMUNITY_PROPERTY_NAME + ":" + TEST_COMMUNITY_VALUE;
        snmpCompleteConfiguration.put(TEST_DEVICE_ID_VALUE, snmpConfig);

        testConfigHandler.loadConfiguration(snmpCompleteConfiguration);

        verify(mockedSnmpClientFactory).createSnmpClient(TEST_SNMP_V2_COMPLETE_CONFIGURATION);
    }

    @Test
    public void testLoadSnmpV3CompleteConfiguration() {
        Dictionary<String, String> snmpCompleteConfiguration = new Hashtable<>();

        String snmpConfig = VERSION_PROPERTY_NAME + ":" + TEST_VERSION_3_VALUE + ","
                + IP_PROPERTY_NAME + ":" + TEST_IP_VALUE + ","
                + PORT_PROPERTY_NAME + ":" + TEST_PORT_VALUE + ","
                + LISTEN_PORT_PROPERTY_NAME + ":" + TEST_LISTEN_PORT_VALUE + ","
                + CONTEXT_NAME_PROPERTY_NAME + ":" + TEST_CONTEXT_NAME_VALUE + ","
                + SECURITY_NAME_PROPERTY_NAME + ":" + TEST_SECURITY_NAME_VALUE + ","
                + AUTH_PASSPHRASE_PROPERTY_NAME + ":" + TEST_AUTH_PASSPHRASE_VALUE + ","
                + PRIV_PASSPHRASE_PROPERTY_NAME + ":" + TEST_PRIV_PASSPHRASE_VALUE + ","
                + AUTH_PROTOCOL_PROPERTY_NAME + ":" + TEST_AUTH_PROTOCOL_VALUE + ","
                + PRIV_PROTOCOL_PROPERTY_NAME + ":" + TEST_PRIV_PROTOCOL_VALUE;
        snmpCompleteConfiguration.put(TEST_DEVICE_ID_VALUE, snmpConfig);

        testConfigHandler.loadConfiguration(snmpCompleteConfiguration);

        verify(mockedSnmpClientFactory).createSnmpClient(TEST_SNMP_V3_COMPLETE_CONFIGURATION);
    }

    @Test
    public void testMissingConfigParameter() {
        Dictionary<String, String> snmpConfiguration = new Hashtable<>();

        String snmpConfig = VERSION_PROPERTY_NAME + ":" + 1 + ","
                + IP_PROPERTY_NAME + ":" + TEST_IP_VALUE + ","
                + PORT_PROPERTY_NAME + ":" + TEST_PORT_VALUE;
        snmpConfiguration.put(TEST_DEVICE_ID_VALUE, snmpConfig);

        testConfigHandler.loadConfiguration(snmpConfiguration);
    }

    @Test
    public void testLoadSnmpWrongVersionConfiguration() {
        Dictionary<String, String> snmpConfiguration = new Hashtable<>();

        String snmpConfig = VERSION_PROPERTY_NAME + ":" + 25 + ","
                + IP_PROPERTY_NAME + ":" + TEST_IP_VALUE + ","
                + PORT_PROPERTY_NAME + ":" + TEST_PORT_VALUE + ","
                + LISTEN_PORT_PROPERTY_NAME + ":" + TEST_LISTEN_PORT_VALUE + ","
                + COMMUNITY_PROPERTY_NAME + ":" + TEST_COMMUNITY_VALUE;
        snmpConfiguration.put(TEST_DEVICE_ID_VALUE, snmpConfig);

        testConfigHandler.loadConfiguration(snmpConfiguration);
    }

    @Test
    public void testApplyConfiguration() {
        Dictionary<String, String> snmpConfiguration = new Hashtable<>();

        String snmpConfig = VERSION_PROPERTY_NAME + ":" + TEST_VERSION_1_VALUE + ","
                + IP_PROPERTY_NAME + ":" + TEST_IP_VALUE + ","
                + PORT_PROPERTY_NAME + ":" + TEST_PORT_VALUE + ","
                + LISTEN_PORT_PROPERTY_NAME + ":" + TEST_LISTEN_PORT_VALUE + ","
                + COMMUNITY_PROPERTY_NAME + ":" + TEST_COMMUNITY_VALUE;
        snmpConfiguration.put(TEST_DEVICE_ID_VALUE, snmpConfig);

        testConfigHandler.loadConfiguration(snmpConfiguration);
        testConfigHandler.applyConfiguration();

        List<SnmpClient> expectedSnmpClients = new ArrayList<>();
        verify(mockedSnmpClientManager).loadConfiguration(expectedSnmpClients);
    }
}
