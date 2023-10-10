package es.amplia.oda.dispatcher.scada;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;

import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.core.commons.utils.Event;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScadaEventDispatcherTest {

    @Mock
    private ScadaTableTranslator mockedTranslator;
    @Mock
    private ScadaConnector mockedConnector;
    @InjectMocks
    private ScadaEventDispatcher testDispatcher;

    @Test
    public void testPublish() {
        String datastreamId = "testDatastream";
        String deviceId = "testDevice";
        Object value = 10;
        long timestamp = System.currentTimeMillis();
        Event event = new Event(datastreamId, deviceId, null, null, timestamp, value);
        ScadaInfo returnedInfo = new ScadaInfo(1, 1);
        DatastreamInfo dsInfo = new DatastreamInfo(deviceId, datastreamId);

        when(mockedTranslator.translate(any(), anyBoolean())).thenReturn(returnedInfo);
        when(mockedTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(value);

        testDispatcher.publish(Collections.singletonList(event));

        verify(mockedTranslator).translate(eq(dsInfo), eq(false));
        verify(mockedConnector).uplink(eq(returnedInfo.getIndex()), eq(value), eq(returnedInfo.getType()), eq(timestamp));
    }

    @Test
    public void testPublishDataNotFoundExceptionCaught() {
        String datastreamId = "testDatastream";
        String deviceId = "testDevice";
        Object value = 10;
        long timestamp = System.currentTimeMillis();
        Event event = new Event(datastreamId, deviceId, null, null, timestamp, value);
        DatastreamInfo dsInfo = new DatastreamInfo(deviceId, datastreamId);

        when(mockedTranslator.translate(any(), anyBoolean())).thenThrow(new DataNotFoundException(""));

        testDispatcher.publish(Collections.singletonList(event));

        verify(mockedTranslator).translate(eq(dsInfo), eq(false));
        verify(mockedConnector, never()).uplink(anyInt(), any(), any(), anyLong());
    }
}