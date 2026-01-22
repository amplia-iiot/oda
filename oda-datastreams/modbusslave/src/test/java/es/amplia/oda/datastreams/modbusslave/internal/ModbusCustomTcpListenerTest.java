package es.amplia.oda.datastreams.modbusslave.internal;

import es.amplia.oda.core.commons.interfaces.StateManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ModbusCustomTCPListener.class)
public class ModbusCustomTcpListenerTest {

    private static final String TEST_DEVICE_IP = "1.2.3.4";
    private static final int TEST_SLAVE_ADDRESS = 1;
    private static final String TEST_DEVICE_ID = "deviceId";

    private final Map<String, CustomModbusRequestHandler> requestHandlers = new HashMap<>();

    @Mock
    StateManager mockedStateManager;
    @Mock
    ServerSocket mockedServerSocket;
    @Mock
    Socket mockedSocket;
    @Mock
    InetAddress mockedInetAddress;

    @Before
    public void prepare(){
        CustomModbusRequestHandler modbusRequestHandler = new CustomModbusRequestHandler(TEST_DEVICE_ID, TEST_DEVICE_IP,
                TEST_SLAVE_ADDRESS, mockedStateManager);
        requestHandlers.put(TEST_DEVICE_IP, modbusRequestHandler);
    }

    @Test
    public void handleRequestTest() throws Exception {
        // conditions
        ModbusCustomTCPListener customTcpListener = new ModbusCustomTCPListener(1, requestHandlers);
        PowerMockito.whenNew(ServerSocket.class).withAnyArguments().thenReturn(mockedServerSocket);
        PowerMockito.when(mockedServerSocket.accept()).thenReturn(mockedSocket);
        PowerMockito.when(mockedSocket.getInetAddress()).thenReturn(mockedInetAddress);
        PowerMockito.when(mockedInetAddress.getHostAddress()).thenReturn(TEST_DEVICE_IP);
        PowerMockito.when(mockedInetAddress.getHostAddress()).thenReturn(TEST_DEVICE_IP);

        // call method
        // execute in a new thread because it's an infinite loop
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(customTcpListener);

        // wait 1 second and then cancel thread
        Thread.sleep(1000);
        future.cancel(true);

        // set timeout to not wait indefinitely for threads to join when stopping
        customTcpListener.setTimeout(1000);
        customTcpListener.stop();

        Mockito.verify(mockedServerSocket, Mockito.atLeastOnce()).accept();
    }
}
