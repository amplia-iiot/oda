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

import java.util.*;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class Iec104ConnectionsFactoryTest {


    @Mock
    private Client mockedClient;
    @Mock
    private ListenableFuture<Void> mockedFuture;
    @Mock
    private ScadaTableTranslator mockedScadaTables;

    @InjectMocks
    private Iec104ConnectionsFactory testIec104ConnectionsFactory;

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
        when(mockedScadaTables.getDeviceIds()).thenReturn(Arrays.asList("testDeviceIdSignal1", "testDeviceIdSignal2"));
        testIec104ConnectionsFactory.createConnections(configurations);

        // checks
        List<Client> clients = (List<Client>) Whitebox.getInternalState(testIec104ConnectionsFactory,"clients");
        Assert.assertEquals(configurations.size(), clients.size());
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
    public void testConnect()
    {
        // conditions
        List<Client> clients = new ArrayList<>();
        clients.add(mockedClient);
        Whitebox.setInternalState(testIec104ConnectionsFactory,"clients", clients);
        when(mockedClient.connect()).thenReturn(mockedFuture);

        // call method to test
        testIec104ConnectionsFactory.connect();

        // assertions
        verify(mockedClient, times(1)).connect();
    }

    @Test
    public void testDisconnect() throws Exception {
        // conditions
        List<Client> clients = new ArrayList<>();
        clients.add(mockedClient);
        Whitebox.setInternalState(testIec104ConnectionsFactory,"clients", clients);

        // call method to test
        testIec104ConnectionsFactory.disconnect();

        // assertions
        verify(mockedClient, times(1)).close();
    }

}
