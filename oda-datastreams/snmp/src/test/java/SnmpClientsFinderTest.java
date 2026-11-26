import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void start() {
        serviceLocatorCons = mockConstruction(ServiceLocatorOsgi.class);

        snmpClientsFinder = new SnmpClientsFinder(mockedContext);

        mockedSnmpClientLocator = serviceLocatorCons.constructed().get(0);
    }

    @AfterEach
    public void tearDown() {
        serviceLocatorCons.close();
    }

    @Test
    public void getSnmpClientNoMatchTest() {
        SnmpClient actualSnmpClient = snmpClientsFinder.getSnmpClient(TEST_DEVICE_ID_VALUE_NO_MATCH);
        Assertions.assertNull(actualSnmpClient);
    }

    @Test
    public void getSnmpClientTest() {
        List<SnmpClient> snmpClients = new ArrayList<>();
        snmpClients.add(mockedSnmpClient);
        Mockito.when(mockedSnmpClientLocator.findAll()).thenReturn(snmpClients);
        Mockito.when(mockedSnmpClient.getDeviceId()).thenReturn(TEST_DEVICE_ID_VALUE);

        SnmpClient actualSnmpClient = snmpClientsFinder.getSnmpClient(TEST_DEVICE_ID_VALUE);
        Assertions.assertNotNull(actualSnmpClient);
        Assertions.assertEquals(TEST_DEVICE_ID_VALUE, actualSnmpClient.getDeviceId());
    }

    @Test
    public void closeTest() {
        snmpClientsFinder.close();
        verify(mockedSnmpClientLocator).close();
    }
}
