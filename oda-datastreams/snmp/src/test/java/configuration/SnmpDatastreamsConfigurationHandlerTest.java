package configuration;

import es.amplia.oda.core.commons.snmp.SnmpEntry;
import es.amplia.oda.datastreams.snmp.configuration.SnmpDatastreamsConfigurationHandler;
import es.amplia.oda.datastreams.snmp.internal.SnmpDatastreamsManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SnmpDatastreamsConfigurationHandlerTest {

    private static final String DATATYPE_PROPERTY_NAME = "dataType";
    private static final String DATASTREAM_PROPERTY_NAME = "datastream";
    private static final String FEED_PROPERTY_NAME = "feed";

    private static final String TEST_DEVICE_ID_VALUE = "testDevice";
    private static final String TEST_OID_1_VALUE = "1.3.6.1.2.1.1.7.0";
    private static final String TEST_DATATYPE_VALUE = "String";
    private static final String TEST_DATASTREAM_VALUE = "testDatastreamId1";
    private static final String TEST_FEED_VALUE = "feed";


    @Mock
    private SnmpDatastreamsManager mockedSnmpDatastreamsManager;
    @InjectMocks
    private SnmpDatastreamsConfigurationHandler testConfigHandler;

    @Test
    public void testLoadCompleteConfiguration() {
        Dictionary<String, String> snmpDatastreamsCompleteConfiguration = new Hashtable<>();

        String snmpConfig = DATATYPE_PROPERTY_NAME + ":" + TEST_DATATYPE_VALUE + ","
                + DATASTREAM_PROPERTY_NAME + ":" + TEST_DATASTREAM_VALUE + ","
                + FEED_PROPERTY_NAME + ":" + TEST_FEED_VALUE;
        snmpDatastreamsCompleteConfiguration.put(TEST_OID_1_VALUE + "," + TEST_DEVICE_ID_VALUE, snmpConfig);

        testConfigHandler.loadConfiguration(snmpDatastreamsCompleteConfiguration);
        testConfigHandler.applyConfiguration();

        verify(mockedSnmpDatastreamsManager).loadConfiguration(Mockito.anyListOf(SnmpEntry.class));
    }

    @Test
    public void testConfigurationMissingProperty() {
        Dictionary<String, String> snmpDatastreamsCompleteConfiguration = new Hashtable<>();

        String snmpConfig = DATATYPE_PROPERTY_NAME + ":" + TEST_DATATYPE_VALUE + ","
                + FEED_PROPERTY_NAME + ":" + TEST_FEED_VALUE;
        snmpDatastreamsCompleteConfiguration.put(TEST_OID_1_VALUE + "," + TEST_DEVICE_ID_VALUE, snmpConfig);

        testConfigHandler.loadConfiguration(snmpDatastreamsCompleteConfiguration);
    }
}
