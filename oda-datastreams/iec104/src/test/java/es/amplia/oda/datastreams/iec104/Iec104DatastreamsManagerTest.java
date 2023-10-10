package es.amplia.oda.datastreams.iec104;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.iec104.configuration.Iec104DatastreamsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class Iec104DatastreamsManagerTest {

    private static final int TEST_INIT_PO0L = 100;
    private static final int TEST_PO0L = 200;

    @Mock
    private Iec104DatastreamsFactory mockedDatastreamsFactory;
    @Mock
    private Iec104ConnectionsFactory mockedConnectionsFactory;
    @Mock
    private ScadaTableTranslator mockedScadaTables;
    @Mock
    private ServiceRegistrationManager mockedRegistrationManager;
    @InjectMocks
    private Iec104DatastreamsManager testIec104DatastreamsManager;

    @Mock
    private DatastreamsGetter mockedDatastreamsGetter;
    @Mock
    private DatastreamsSetter mockedDatastreamsSetter;

    @Test
    public void testLoadConfiguration() {
        Iec104DatastreamsConfiguration conf1 = Iec104DatastreamsConfiguration.builder()
                .deviceId("testDevice1").ipAddress("0.0.0.1").ipPort(2024)
                .commonAddress(1).build();

        Iec104DatastreamsConfiguration conf2 = Iec104DatastreamsConfiguration.builder()
                .deviceId("testDevice2").ipAddress("0.0.0.2").ipPort(2025)
                .commonAddress(80).build();

        List<Iec104DatastreamsConfiguration> configurations = Arrays.asList(conf1, conf2);

        // conditions
        when(mockedScadaTables.getRecollectionDatastreamsIds()).thenReturn(Arrays.asList("datastreamId1", "datastreamId2"));
        when(mockedDatastreamsFactory.createIec104DatastreamsGetter(anyString())).thenReturn(mockedDatastreamsGetter);
        when(mockedDatastreamsFactory.createIec104DatastreamsSetter(anyString())).thenReturn(mockedDatastreamsSetter);

        // launch method to test
        testIec104DatastreamsManager.loadConfiguration(configurations, TEST_INIT_PO0L, TEST_PO0L);

        // verifications
        verify(mockedRegistrationManager, times(2)).unregister();
        verify(mockedConnectionsFactory).createConnections(configurations);
        verify(mockedDatastreamsFactory).updateGetterPolling(TEST_INIT_PO0L, TEST_PO0L);
        verify(mockedDatastreamsFactory).createIec104DatastreamsGetter("datastreamId1");
        verify(mockedDatastreamsFactory).createIec104DatastreamsGetter("datastreamId2");
        verify(mockedRegistrationManager, times(2)).register(mockedDatastreamsGetter);
        verify(mockedRegistrationManager, times(0)).register(mockedDatastreamsSetter);
    }
}
