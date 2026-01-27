package internal;

import es.amplia.oda.core.commons.snmp.SnmpEntry;
import es.amplia.oda.datastreams.snmp.internal.SnmpDatastreamsTranslator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SnmpDatastreamTranslatorTest {

    private static final String TEST_DEVICE_ID_VALUE = "testDevice";
    private static final String TEST_OID_VALUE = "1.3.6.1.2.1.1.7.0";
    private static final String TEST_OID_VALUE_2 = "1.3.6.1.2.1.1.8.0";
    private static final String TEST_DATATYPE_VALUE = "String";
    private static final String TEST_DATASTREAM_VALUE = "testDatastreamId1";
    private static final String TEST_FEED_VALUE = "feed";
    private static final String TEST_PUBLISH_TYPE_VALUE = "dispatcher";


    List<SnmpEntry> snmpEntries = new ArrayList<>();

    SnmpDatastreamsTranslator snmpDatastreamsTranslator;

    @Before
    public void start(){
        SnmpEntry snmpEntry = new SnmpEntry(TEST_OID_VALUE, TEST_DATATYPE_VALUE, TEST_DATASTREAM_VALUE,
                TEST_DEVICE_ID_VALUE, TEST_FEED_VALUE, TEST_PUBLISH_TYPE_VALUE);
        snmpEntries.add(snmpEntry);

        snmpDatastreamsTranslator = new SnmpDatastreamsTranslator(snmpEntries);
    }

    @Test
    public void translateTest(){
        SnmpEntry actualSnmpEntry = snmpDatastreamsTranslator.translate(TEST_OID_VALUE, TEST_DEVICE_ID_VALUE);

        Assert.assertEquals(TEST_DATASTREAM_VALUE, actualSnmpEntry.getDatastreamId());
        Assert.assertEquals(TEST_OID_VALUE, actualSnmpEntry.getOID());
        Assert.assertEquals(TEST_FEED_VALUE, actualSnmpEntry.getFeed());
        Assert.assertEquals(TEST_DATATYPE_VALUE, actualSnmpEntry.getDataType());
        Assert.assertEquals(TEST_DEVICE_ID_VALUE, actualSnmpEntry.getDeviceId());
    }

    @Test
    public void translateTestNoMatch(){
        SnmpEntry actualSnmpEntry = snmpDatastreamsTranslator.translate(TEST_OID_VALUE_2, TEST_DEVICE_ID_VALUE);

        Assert.assertNull(actualSnmpEntry);
    }
}
