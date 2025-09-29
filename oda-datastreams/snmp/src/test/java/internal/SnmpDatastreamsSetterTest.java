package internal;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import es.amplia.oda.datastreams.snmp.internal.SnmpDatastreamsSetter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

import java.lang.reflect.Type;
import java.util.List;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SnmpDatastreamsSetterTest {

    private static final String TEST_DEVICE_ID_VALUE = "testDevice";
    private static final String TEST_OID_VALUE = "1.3.6.1.2.1.1.7.0";
    private static final String TEST_DATATYPE_VALUE = "String";
    private static final String TEST_DATASTREAM_VALUE = "testDatastreamId1";
    private static final String TEST_WRITE_VALUE = "write";


    @Mock
    SnmpClient mockedSnmpClient;
    @Mock
    SnmpClientsFinder mockedSnmpClientsFinder;

    private SnmpDatastreamsSetter datastreamSetter;

    @Before
    public void start(){
        datastreamSetter = new SnmpDatastreamsSetter(mockedSnmpClientsFinder, TEST_OID_VALUE, TEST_DATATYPE_VALUE,
                TEST_DATASTREAM_VALUE, TEST_DEVICE_ID_VALUE);
    }

    @Test
    public void getDatastreamIdTest(){
        String actualDatastreamId = datastreamSetter.getDatastreamIdSatisfied();
        Assert.assertEquals(TEST_DATASTREAM_VALUE, actualDatastreamId);
    }

    @Test
    public void getDevicesTest(){
        List<String> actualDevices = datastreamSetter.getDevicesIdManaged();
        Assert.assertEquals(TEST_DEVICE_ID_VALUE, actualDevices.get(0));
    }

    @Test
    public void getDatastreamTypeTest(){
        Type datastreamType = datastreamSetter.getDatastreamType();
        Assert.assertTrue(datastreamType.getTypeName().contains(TEST_DATATYPE_VALUE));
    }

    @Test
    public void setValueTest() {
        PowerMockito.when(mockedSnmpClientsFinder.getSnmpClient(Mockito.any())).thenReturn(mockedSnmpClient);

        datastreamSetter.set(TEST_DEVICE_ID_VALUE, TEST_WRITE_VALUE);

        verify(mockedSnmpClient).setValue(TEST_OID_VALUE, TEST_DATATYPE_VALUE, TEST_WRITE_VALUE);
    }
}
