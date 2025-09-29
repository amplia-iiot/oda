import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SnmpClientsFinder.class)
public class SnmpClientsFinderTest {

    private static final String TEST_DEVICE_ID_VALUE = "testDevice";
    private static final String TEST_DEVICE_ID_VALUE_NO_MATCH = "noMatch";


    @Mock
    private BundleContext mockedContext;
    @Mock
    private SnmpClient mockedSnmpClient;
    @Mock
    private ServiceLocatorOsgi<SnmpClient> mockedSnmpClientLocator;

    SnmpClientsFinder snmpClientsFinder;

    @Before
    public void start() throws Exception {
        // conditions
        PowerMockito.whenNew(ServiceLocatorOsgi.class)
                .withArguments(any(BundleContext.class), any(SnmpClient.class))
                .thenReturn(mockedSnmpClientLocator);

        snmpClientsFinder = new SnmpClientsFinder(mockedContext);
    }

    @Test
    public void getSnmpClientNoMatchTest() {
        SnmpClient actualSnmpClient = snmpClientsFinder.getSnmpClient(TEST_DEVICE_ID_VALUE_NO_MATCH);
        Assert.assertNull(actualSnmpClient);
    }

    @Test
    public void getSnmpClientTest() {
        List<SnmpClient> snmpClients = new ArrayList<>();
        snmpClients.add(mockedSnmpClient);
        PowerMockito.when(mockedSnmpClientLocator.findAll()).thenReturn(snmpClients);
        PowerMockito.when(mockedSnmpClient.getDeviceId()).thenReturn(TEST_DEVICE_ID_VALUE);

        SnmpClient actualSnmpClient = snmpClientsFinder.getSnmpClient(TEST_DEVICE_ID_VALUE);
        Assert.assertNotNull(actualSnmpClient);
        Assert.assertEquals(TEST_DEVICE_ID_VALUE, actualSnmpClient.getDeviceId());
    }

    @Test
    public void closeTest() {
        snmpClientsFinder.close();
        verify(mockedSnmpClientLocator).close();
    }
}
