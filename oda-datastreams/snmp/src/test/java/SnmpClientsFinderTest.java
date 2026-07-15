import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SnmpClientsFinderTest {

    private static final String TEST_DEVICE_ID_VALUE = "testDevice";
    private static final String TEST_DEVICE_ID_VALUE_NO_MATCH = "noMatch";


    @Mock
    private BundleContext mockedContext;
    @Mock
    private SnmpClient mockedSnmpClient;

    private MockedConstruction<ServiceLocatorOsgi> serviceLocatorCons;
    private ServiceLocatorOsgi<SnmpClient> mockedSnmpClientLocator;

    SnmpClientsFinder snmpClientsFinder;

    @Before
    @SuppressWarnings("unchecked")
    public void start() {
        serviceLocatorCons = mockConstruction(ServiceLocatorOsgi.class);

        snmpClientsFinder = new SnmpClientsFinder(mockedContext);

        mockedSnmpClientLocator = serviceLocatorCons.constructed().get(0);
    }

    @After
    public void tearDown() {
        serviceLocatorCons.close();
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
        Mockito.when(mockedSnmpClientLocator.findAll()).thenReturn(snmpClients);
        Mockito.when(mockedSnmpClient.getDeviceId()).thenReturn(TEST_DEVICE_ID_VALUE);

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
