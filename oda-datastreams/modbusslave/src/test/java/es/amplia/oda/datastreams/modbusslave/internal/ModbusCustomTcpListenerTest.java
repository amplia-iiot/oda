package es.amplia.oda.datastreams.modbusslave.internal;

import es.amplia.oda.core.commons.interfaces.StateManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.mockito.Mockito.mockConstruction;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ModbusCustomTcpListenerTest {

    private static final String TEST_DEVICE_IP = "1.2.3.4";
    private static final int TEST_SLAVE_ADDRESS = 1;
    private static final String TEST_DEVICE_ID = "deviceId";

    private final Map<String, CustomModbusRequestHandler> requestHandlers = new HashMap<>();

    @Mock
    StateManager mockedStateManager;
    @Mock
    Socket mockedSocket;
    @Mock
    InetAddress mockedInetAddress;

    private final List<ServerSocket> constructedServerSockets = new CopyOnWriteArrayList<>();

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
        Mockito.when(mockedSocket.getInetAddress()).thenReturn(mockedInetAddress);
        Mockito.when(mockedInetAddress.getHostAddress()).thenReturn(TEST_DEVICE_IP);

        // call method
        // execute in a new thread because it's an infinite loop
        // the ServerSocket construction mock must be created in the same thread that runs the listener
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try (MockedConstruction<ServerSocket> ignored = mockConstruction(ServerSocket.class,
                    (mock, mctx) -> {
                        Mockito.when(mock.accept()).thenReturn(mockedSocket);
                        constructedServerSockets.add(mock);
                    })) {
                customTcpListener.run();
            }
        });

        // wait 1 second and then cancel thread
        Thread.sleep(1000);
        future.cancel(true);

        // set timeout to not wait indefinitely for threads to join when stopping
        customTcpListener.setTimeout(1000);
        customTcpListener.stop();

        Mockito.verify(constructedServerSockets.get(0), Mockito.atLeastOnce()).accept();
    }
}
