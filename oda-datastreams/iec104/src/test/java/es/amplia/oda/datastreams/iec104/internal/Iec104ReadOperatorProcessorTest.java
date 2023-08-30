package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.master.Iec104ClientModule;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.datastreams.iec104.Iec104ConnectionsFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.Timer;

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Iec104ReadOperatorProcessor.class)
public class Iec104ReadOperatorProcessorTest {

    @Mock
    private Iec104ConnectionsFactory mockedConnectionsFactory;
    @Mock
    private ScadaTableTranslator mockScadaTranslator;
    @Mock
    private Iec104ClientModule mockClient;
    @InjectMocks
    private Iec104ReadOperatorProcessor readOperatorProcessor;

    private final CollectedValue expectedValue = new CollectedValue(System.currentTimeMillis(), 1);


    @Test
    public void testRead(){

        ScadaTableTranslator.ScadaInfo scadaInfo = new ScadaTableTranslator.ScadaInfo(106001, "M_ME_NC_1", null);
        Iec104Cache cache = new Iec104Cache();
        cache.add("M_ME_NC_1", 1, 106001);

        // conditions
        when(mockedConnectionsFactory.getCache(anyString())).thenReturn(cache);
        when(mockScadaTranslator.translate(any())).thenReturn(scadaInfo);

        CollectedValue readValue = readOperatorProcessor.read("deviceId1", "datastreamId1");

        Assert.assertEquals(expectedValue.getValue(), readValue.getValue());
    }

    @Test
    public void testUpdateGetterPolling(){
        readOperatorProcessor.updateGetterPolling(100, 200);

        // conditions
        when(mockedConnectionsFactory.getDeviceList()).thenReturn(Collections.singletonList("deviceId1"));
        when(mockedConnectionsFactory.getConnection("deviceId1")).thenReturn(mockClient);
        when(mockedConnectionsFactory.getCommonAddress("deviceId1")).thenReturn(1);
        when(mockClient.isConnected()).thenReturn(true);


        Timer timer = (Timer) Whitebox.getInternalState(readOperatorProcessor,"timer");
        Assert.assertNotNull(timer);
    }

}
