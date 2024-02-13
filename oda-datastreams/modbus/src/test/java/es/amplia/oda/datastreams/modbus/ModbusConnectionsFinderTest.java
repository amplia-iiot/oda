package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ModbusConnectionsFinder.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ModbusConnectionsFinderTest {

    private final String TEST_DEVICE_ID = "testDevice";


    @Mock
    private BundleContext mockedContext;
    @Mock
    private ModbusMaster mockedModbusMaster;
    private final List<ModbusMaster> mockedModbusConnectionsList = new ArrayList<>();
    @Mock
    private ServiceLocatorOsgi<ModbusMaster> mockedModbusConnectionsLocator;

    private ModbusConnectionsFinder testConnectionsFinder;

    @Before
    public void setUp() throws Exception {

        // conditions
        PowerMockito.whenNew(ServiceLocatorOsgi.class)
                .withArguments(any(BundleContext.class), eq(ModbusMaster.class))
                .thenReturn(mockedModbusConnectionsLocator);

        PowerMockito.when(mockedModbusConnectionsLocator.findAll()).thenReturn(mockedModbusConnectionsList);
        PowerMockito.when(mockedModbusMaster.getDeviceId()).thenReturn(TEST_DEVICE_ID);

        // ini class to test
        testConnectionsFinder = new ModbusConnectionsFinder(mockedContext);

        // add mockedModbusMaster to list of connections
        mockedModbusConnectionsList.add(mockedModbusMaster);

    }

    @Test
    public void testGetModbusConnectionWithId() {

        ModbusMaster connectionFound = testConnectionsFinder.getModbusConnectionWithId(TEST_DEVICE_ID);

        verify(mockedModbusConnectionsLocator).findAll();
        verify(mockedModbusMaster).getDeviceId();
        Assert.assertEquals(connectionFound, mockedModbusMaster);
    }

    @Test
    public void testGetModbusConnectionWithIdNoMatch() {

        PowerMockito.when(mockedModbusMaster.getDeviceId()).thenReturn("NoMatch");

        ModbusMaster connectionFound = testConnectionsFinder.getModbusConnectionWithId(TEST_DEVICE_ID);

        verify(mockedModbusConnectionsLocator).findAll();
        verify(mockedModbusMaster).getDeviceId();
        Assert.assertNull(connectionFound);
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
