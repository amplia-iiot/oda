package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.datastreams.iec104.Iec104ConnectionsFactory;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class Iec104ReadOperatorProcessorTest {

    @Mock
    private Iec104ConnectionsFactory mockedConnectionsFactory;
    @Mock
    private ScadaTableTranslator mockScadaTranslator;
    @InjectMocks
    private Iec104ReadOperatorProcessor readOperatorProcessor;

    String TEST_FEED = "testFeed";
    String TEST_DEVICE_ID = "testDeviceId";
    String TEST_DATASTREAM_ID = "testDatastreamId";
    String TEST_EVENT_PUBLISH = "dispatcher";

    @Test
    public void testRead(){

        long valueAt = System.currentTimeMillis();
        CollectedValue expectedValue = new CollectedValue(valueAt, 1, null, TEST_FEED);
        ScadaTableTranslator.ScadaInfo scadaInfo = new ScadaTableTranslator.ScadaInfo(106001, "M_ME_NC_1");
        ScadaTableTranslator.ScadaTranslationInfo scadaTranslationInfo =
                new ScadaTableTranslator.ScadaTranslationInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED, TEST_EVENT_PUBLISH);
        Iec104Cache cache = new Iec104Cache(TEST_DEVICE_ID);
        Value<Integer> value = new Value<>(1, valueAt, null);
        cache.add("M_ME_NC_1", value, 106001);

        // conditions
        when(mockedConnectionsFactory.getCache(anyString())).thenReturn(cache);
        when(mockScadaTranslator.translate(any(), anyBoolean())).thenReturn(scadaInfo);
        when(mockScadaTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(1);
        when(mockScadaTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(scadaTranslationInfo);


        CollectedValue readValue = readOperatorProcessor.read(TEST_DEVICE_ID, TEST_DATASTREAM_ID);

        Assertions.assertEquals(expectedValue.getValue(), readValue.getValue());
        Assertions.assertEquals(expectedValue.getAt(), readValue.getAt());
        Assertions.assertEquals(expectedValue.getFeed(), readValue.getFeed());

        // test value in cache already processed is not obtained again
        CollectedValue readValue2 = readOperatorProcessor.read(TEST_DEVICE_ID, TEST_DATASTREAM_ID);
        Assertions.assertNull(readValue2);
    }

    @Test
    public void testReadNoInfoInScadaTables(){

        long valueAt = System.currentTimeMillis();
        Iec104Cache cache = new Iec104Cache(TEST_DEVICE_ID);
        Value<Integer> value = new Value<>(1, valueAt, null);
        cache.add("M_ME_NC_1", value, 106001);

        // conditions
        when(mockedConnectionsFactory.getCache(anyString())).thenReturn(cache);
        when(mockScadaTranslator.translate(any(), anyBoolean())).thenReturn(null);

        CollectedValue readValue = readOperatorProcessor.read(TEST_DEVICE_ID, TEST_DATASTREAM_ID);

        verify(mockScadaTranslator, never()).transformValue(anyInt(), any(), anyBoolean(), any());
        verify(mockScadaTranslator, never()).getTranslationInfo(any(), anyBoolean());

        Assertions.assertNull(readValue);
    }

    @Test
    public void testReadNoScadaTablesTranslation(){

        long valueAt = System.currentTimeMillis();
        CollectedValue expectedValue = new CollectedValue(valueAt, 1, null, TEST_FEED);
        ScadaTableTranslator.ScadaInfo scadaInfo = new ScadaTableTranslator.ScadaInfo(106001, "M_ME_NC_1");
        Iec104Cache cache = new Iec104Cache(TEST_DEVICE_ID);
        Value<Integer> value = new Value<>(1, valueAt, null);
        cache.add("M_ME_NC_1", value, 106001);

        // conditions
        when(mockedConnectionsFactory.getCache(anyString())).thenReturn(cache);
        when(mockScadaTranslator.translate(any(), anyBoolean())).thenReturn(scadaInfo);
        when(mockScadaTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(1);
        when(mockScadaTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(null);

        CollectedValue readValue = readOperatorProcessor.read(TEST_DEVICE_ID, TEST_DATASTREAM_ID);

        Assertions.assertEquals(expectedValue.getValue(), readValue.getValue());
        Assertions.assertEquals(expectedValue.getAt(), readValue.getAt());
        Assertions.assertNull(readValue.getFeed());
    }

    @Test
    public void testReadNoValueInCache(){

        ScadaTableTranslator.ScadaInfo scadaInfo = new ScadaTableTranslator.ScadaInfo(106001, "M_ME_NC_1");
        Iec104Cache cache = new Iec104Cache(TEST_DEVICE_ID);

        // conditions
        when(mockedConnectionsFactory.getCache(anyString())).thenReturn(cache);
        when(mockScadaTranslator.translate(any(), anyBoolean())).thenReturn(scadaInfo);

        CollectedValue readValue = readOperatorProcessor.read(TEST_DEVICE_ID, TEST_DATASTREAM_ID);

        Assertions.assertNull(readValue);
    }
}
