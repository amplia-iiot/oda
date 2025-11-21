package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.hardware.snmp.configuration.SnmpClientConfig;
import es.amplia.oda.hardware.snmp.configuration.SnmpClientOptions;
import es.amplia.oda.hardware.snmp.configuration.SnmpClientV3Options;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;

@RunWith(MockitoJUnitRunner.class)
public class SnmpClientFactoryTest {

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
    private static final String TEST_IP_VALUE = "127.0.0.1";
    private static final String TEST_COMMUNITY_VALUE = "public";
    private static final String TEST_CONTEXT_NAME_VALUE = "public";
    private static final String TEST_SECURITY_NAME_VALUE = "simulator";
    private static final String TEST_AUTH_PASSPHRASE_VALUE = "auctoritas";
    private static final String TEST_PRIV_PASSPHRASE_VALUE = "privatus";
    private static final String TEST_AUTH_PROTOCOL_VALUE = "MD5";
    private static final String TEST_PRIV_PROTOCOL_VALUE = "DES";

    private static final SnmpClientConfig TEST_SNMP_V1_COMPLETE_CONFIGURATION =
            new SnmpClientConfig(TEST_DEVICE_ID_VALUE, TEST_IP_VALUE, TEST_PORT_VALUE, TEST_VERSION_1_VALUE,
                    new SnmpClientOptions(TEST_COMMUNITY_VALUE));

    private static final SnmpClientConfig TEST_SNMP_V2_COMPLETE_CONFIGURATION =
            new SnmpClientConfig(TEST_DEVICE_ID_VALUE, TEST_IP_VALUE, TEST_PORT_VALUE, TEST_VERSION_2_VALUE,
                    new SnmpClientOptions(TEST_COMMUNITY_VALUE));

    private static final SnmpClientConfig TEST_SNMP_V3_COMPLETE_CONFIGURATION =
            new SnmpClientConfig(TEST_DEVICE_ID_VALUE, TEST_IP_VALUE, TEST_PORT_VALUE, TEST_VERSION_3_VALUE,
                    new SnmpClientV3Options(TEST_SECURITY_NAME_VALUE, TEST_AUTH_PASSPHRASE_VALUE,
                    TEST_PRIV_PASSPHRASE_VALUE, TEST_CONTEXT_NAME_VALUE, TEST_AUTH_PROTOCOL_VALUE, TEST_PRIV_PROTOCOL_VALUE));


    @Mock
    private Snmp mockedSnmp;
    @Mock
    private Snmp mockedSnmpClient;
    @InjectMocks
    private SnmpClientFactory testClientFactory;


    @Test
    public void createClientV1Test(){
        SnmpClientImpl expectedSnmpClient = new SnmpClientImpl(mockedSnmp, TEST_VERSION_1_VALUE, new CommunityTarget(), TEST_DEVICE_ID_VALUE);

        SnmpClient actualSnmpClient = testClientFactory.createSnmpClient(TEST_SNMP_V1_COMPLETE_CONFIGURATION);
        // disconnect to allow other test to use same ports
        actualSnmpClient.disconnect();

        Assert.assertEquals(actualSnmpClient.getDeviceId(), expectedSnmpClient.getDeviceId());
    }

    @Test
    public void createClientV3Test() throws Exception {
        SnmpClientImpl expectedSnmpClient = new SnmpClientImpl(mockedSnmp, TEST_VERSION_3_VALUE, new UserTarget(),
                TEST_CONTEXT_NAME_VALUE, TEST_DEVICE_ID_VALUE);

        PowerMockito.whenNew(Snmp.class).withNoArguments().thenReturn(mockedSnmpClient);

        SnmpClient actualSnmpClient = testClientFactory.createSnmpClient(TEST_SNMP_V3_COMPLETE_CONFIGURATION);
        // disconnect to allow other test to use same ports
        actualSnmpClient.disconnect();

        Assert.assertEquals(actualSnmpClient.getDeviceId(), expectedSnmpClient.getDeviceId());
    }

    @Test
    public void createClientV3WrongAuthProtocolTest(){

        SnmpClientConfig snmpClientConfig = new SnmpClientConfig(TEST_DEVICE_ID_VALUE, TEST_IP_VALUE, TEST_PORT_VALUE,
                TEST_VERSION_3_VALUE, new SnmpClientV3Options(TEST_SECURITY_NAME_VALUE, TEST_AUTH_PASSPHRASE_VALUE,
                TEST_PRIV_PASSPHRASE_VALUE, TEST_CONTEXT_NAME_VALUE, "NotExist", TEST_PRIV_PROTOCOL_VALUE));

        SnmpClient actualSnmpClient = testClientFactory.createSnmpClient(snmpClientConfig);

        Assert.assertNull(actualSnmpClient);
    }

    @Test
    public void createClientV3WrongPrivProtocolTest(){

        SnmpClientConfig snmpClientConfig = new SnmpClientConfig(TEST_DEVICE_ID_VALUE, TEST_IP_VALUE, TEST_PORT_VALUE,
                TEST_VERSION_3_VALUE, new SnmpClientV3Options(TEST_SECURITY_NAME_VALUE, TEST_AUTH_PASSPHRASE_VALUE,
                TEST_PRIV_PASSPHRASE_VALUE, TEST_CONTEXT_NAME_VALUE, TEST_AUTH_PROTOCOL_VALUE, "NotExist"));

        SnmpClient actualSnmpClient = testClientFactory.createSnmpClient(snmpClientConfig);

        Assert.assertNull(actualSnmpClient);
    }
}
