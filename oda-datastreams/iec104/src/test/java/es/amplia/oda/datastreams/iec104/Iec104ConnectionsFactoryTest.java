package es.amplia.oda.datastreams.iec104;

import com.google.common.util.concurrent.ListenableFuture;
import es.amplia.oda.datastreams.iec104.configuration.Iec104DatastreamsConfiguration;
import org.eclipse.neoscada.protocol.iec60870.client.Client;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class Iec104ConnectionsFactoryTest {


    @Mock
    private Client mockedClient;
    @Mock
    private ListenableFuture<Void> mockedFuture;

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

        testIec104ConnectionsFactory.createConnections(configurations);

        // checks
        List<Client> clients = (List<Client>) Whitebox.getInternalState(testIec104ConnectionsFactory,"clients");
        Assert.assertEquals(configurations.size(), clients.size());

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
