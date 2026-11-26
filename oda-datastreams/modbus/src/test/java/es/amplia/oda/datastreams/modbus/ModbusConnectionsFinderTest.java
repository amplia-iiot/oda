package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ModbusConnectionsFinderTest {

    private final String TEST_DEVICE_ID = "testDevice";


    @Mock
    private BundleContext mockedContext;
    @Mock
    private ModbusMaster mockedModbusMaster;
    private final List<ModbusMaster> mockedModbusConnectionsList = new ArrayList<>();
    private ServiceLocatorOsgi<ModbusMaster> mockedModbusConnectionsLocator;

    private MockedConstruction<ServiceLocatorOsgi> mockedLocatorCons;

    private ModbusConnectionsFinder testConnectionsFinder;

    @BeforeEach
    public void setUp() throws Exception {

        // conditions
        mockedLocatorCons = mockConstruction(ServiceLocatorOsgi.class,
                (mock, mctx) -> when(mock.findAll()).thenReturn(mockedModbusConnectionsList));

        when(mockedModbusMaster.getDeviceId()).thenReturn(TEST_DEVICE_ID);

        // ini class to test
        testConnectionsFinder = new ModbusConnectionsFinder(mockedContext);
        mockedModbusConnectionsLocator = mockedLocatorCons.constructed().get(0);

        // add mockedModbusMaster to list of connections
        mockedModbusConnectionsList.add(mockedModbusMaster);

    }

    @AfterEach
    public void tearDown() {
        mockedLocatorCons.close();
    }

    @Test
    public void testGetModbusConnectionWithId() {

        ModbusMaster connectionFound = testConnectionsFinder.getModbusConnectionWithId(TEST_DEVICE_ID);

        verify(mockedModbusConnectionsLocator).findAll();
        verify(mockedModbusMaster).getDeviceId();
        Assertions.assertEquals(connectionFound, mockedModbusMaster);
    }

    @Test
    public void testGetModbusConnectionWithIdNoMatch() {

        when(mockedModbusMaster.getDeviceId()).thenReturn("NoMatch");

        ModbusMaster connectionFound = testConnectionsFinder.getModbusConnectionWithId(TEST_DEVICE_ID);

        verify(mockedModbusConnectionsLocator).findAll();
        verify(mockedModbusMaster).getDeviceId();
        Assertions.assertNull(connectionFound);
    }

    @Test
    public void testModbusConnect() {
        testConnectionsFinder.connect();

        verify(mockedModbusConnectionsLocator).findAll();
        verify(mockedModbusMaster).connect();
    }

    @Test
    public void testModbusDisconnect() {
        testConnectionsFinder.disconnect();

        verify(mockedModbusConnectionsLocator).findAll();
        verify(mockedModbusMaster).disconnect();
    }

    @Test
    public void testModbusConnectionFinderClose() {
        testConnectionsFinder.close();

        verify(mockedModbusConnectionsLocator).close();
    }
}
