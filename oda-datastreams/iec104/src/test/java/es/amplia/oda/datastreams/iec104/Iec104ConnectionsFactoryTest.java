package es.amplia.oda.datastreams.iec104;

import com.google.common.util.concurrent.ListenableFuture;
import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.datastreams.iec104.configuration.Iec104DatastreamsConfiguration;
import org.eclipse.neoscada.protocol.iec60870.client.Client;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.SocketAddress;
import java.util.*;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class Iec104ConnectionsFactoryTest {


    @Mock
    private Client mockedClient;
    @Mock
    private SocketAddress mockedAddress;
    @Mock
    private ListenableFuture<Void> mockedFuture;
    @Mock
    private ScadaTableTranslator mockedScadaTables;

    @InjectMocks
    private Iec104ConnectionsFactory testIec104ConnectionsFactory;

    char[] TEST_QUALITY_BITS = {1, 1, 1, 1};
    boolean TEST_QUALITY_BITS_NOTIFY = false;


    @Test
    public void testCreateConnections()
    {
        Iec104DatastreamsConfiguration conf1 = Iec104DatastreamsConfiguration.builder()
                .deviceId("testDevice1").ipAddress("0.0.0.1").ipPort(2024)
                .commonAddress(1).build();

        Iec104DatastreamsConfiguration conf2 = Iec104DatastreamsConfiguration.builder()
                .deviceId("testDevice2").ipAddress("0.0.0.2").ipPort(2025)
                .commonAddress(80).build();

        List<Iec104DatastreamsConfiguration> configurations = Arrays.asList(conf1, conf2);

        // condition
        when(mockedScadaTables.getRecollectionDeviceIds()).thenReturn(Arrays.asList("testDeviceIdSignal1", "testDeviceIdSignal2"));
        testIec104ConnectionsFactory.createConnections(configurations, TEST_QUALITY_BITS, TEST_QUALITY_BITS_NOTIFY);

        // checks
        Map<SocketAddress, Client> clients  = (Map<SocketAddress, Client>) Whitebox.getInternalState(testIec104ConnectionsFactory,"clients");
        Assert.assertEquals(configurations.size(), clients.values().size());
        Assert.assertEquals(2, testIec104ConnectionsFactory.getConnectionsDeviceList().size());
        Map<String, Iec104Cache> caches = (Map<String, Iec104Cache>) Whitebox.getInternalState(testIec104ConnectionsFactory,"caches");
        // there must be four caches, one for each connection and two for the deviceIds of scadaTables
        Assert.assertEquals(4, caches.keySet().size());
        Assert.assertEquals(4, testIec104ConnectionsFactory.getDeviceList().size());
        Assert.assertTrue(caches.containsKey("testDeviceIdSignal1"));
        Assert.assertTrue(caches.containsKey("testDeviceIdSignal2"));
        Assert.assertTrue(caches.containsKey("testDevice1"));
        Assert.assertTrue(caches.containsKey("testDevice2"));

        // get cache for one of the signal deviceIds
        Assert.assertNotNull(testIec104ConnectionsFactory.getCache("testDeviceIdSignal1"));

        // check connection for testDevice1 has been created
        Assert.assertNotNull(testIec104ConnectionsFactory.getConnection("testDevice1"));

        // check commonAddress
        Assert.assertEquals(Integer.valueOf(1), testIec104ConnectionsFactory.getCommonAddress("testDevice1"));
        Assert.assertEquals(Integer.valueOf(80), testIec104ConnectionsFactory.getCommonAddress("testDevice2"));

    }

    @Test
    public void testConnect() throws InterruptedException
    {
        // conditions
        Map<SocketAddress, Client> clients = new HashMap();
        clients.put(mockedAddress, mockedClient);
        Whitebox.setInternalState(testIec104ConnectionsFactory,"clients", clients);
        Whitebox.setInternalState(testIec104ConnectionsFactory,"connInitialDelay", 0);
        Whitebox.setInternalState(testIec104ConnectionsFactory,"connRetryDelay", 10);
        when(mockedClient.connect()).thenReturn(mockedFuture);

        // call method to test
        testIec104ConnectionsFactory.connect();

        Thread.sleep(100); // Añadimos la espera para eliminar la aleatoriedad del test producida por el hilo generado con el connInitialDelay aunque esté con valor 0

        // assertions
        verify(mockedClient, times(1)).connect();
    }

    @Test
    public void testDisconnect() throws Exception {
        // conditions
        Map<SocketAddress, Client> clients = new HashMap();
        clients.put(mockedAddress, mockedClient);
        Whitebox.setInternalState(testIec104ConnectionsFactory,"clients", clients);

        // call method to test
        testIec104ConnectionsFactory.disconnect();

        // assertions
        verify(mockedClient, times(1)).close();
    }

}
