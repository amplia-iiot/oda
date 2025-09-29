package internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import es.amplia.oda.datastreams.snmp.internal.SnmpDatastreamsGetter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(MockitoJUnitRunner.class)
public class SnmpDatastreamsGetterTest {

    private static final String TEST_DEVICE_ID_VALUE = "testDevice";
    private static final String TEST_OID_VALUE = "1.3.6.1.2.1.1.7.0";
    private static final String TEST_DATATYPE_VALUE = "String";
    private static final String TEST_DATASTREAM_VALUE = "testDatastreamId1";
    private static final String TEST_FEED_VALUE = "feed";
    private static final String TEST_RETURN_VALUE = "returned";


    @Mock
    SnmpClient mockedSnmpClient;
    @Mock
    SnmpClientsFinder mockedSnmpClientsFinder;

    private SnmpDatastreamsGetter datastreamGetter;

    @Before
    public void start(){
        datastreamGetter = new SnmpDatastreamsGetter(mockedSnmpClientsFinder, TEST_OID_VALUE, TEST_DATATYPE_VALUE.getClass(),
                TEST_DATASTREAM_VALUE, TEST_DEVICE_ID_VALUE, TEST_FEED_VALUE);
    }

    @Test
    public void getDatastreamIdTest(){
        String actualDatastreamId = datastreamGetter.getDatastreamIdSatisfied();
        Assert.assertEquals(TEST_DATASTREAM_VALUE, actualDatastreamId);
    }

    @Test
    public void getDevicesTest(){
        List<String> actualDevices = datastreamGetter.getDevicesIdManaged();
        Assert.assertEquals(TEST_DEVICE_ID_VALUE, actualDevices.get(0));
    }

    @Test
    public void getCollectedValueTest() throws ExecutionException, InterruptedException {
        PowerMockito.when(mockedSnmpClientsFinder.getSnmpClient(Mockito.any())).thenReturn(mockedSnmpClient);
        PowerMockito.when(mockedSnmpClient.getValue(Mockito.any())).thenReturn(TEST_RETURN_VALUE);

        CompletableFuture<DatastreamsGetter.CollectedValue> actualCollectedValue = datastreamGetter.get(TEST_DEVICE_ID_VALUE);

        Assert.assertEquals(TEST_RETURN_VALUE, actualCollectedValue.get().getValue().toString());
    }
}
