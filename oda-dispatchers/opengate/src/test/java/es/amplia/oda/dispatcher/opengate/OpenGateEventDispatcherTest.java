package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OpenGateEventDispatcherTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_HOST_ID = "host";
    private static final String[] TEST_PATH = new String[]{"deviceA", "deviceB"};
    private static final String[] TEST_PATH_WITH_HOST = new String[]{"host","deviceA", "deviceB"};
    private static final Long TEST_AT = System.currentTimeMillis();
    private static final int TEST_VALUE = 20;

    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private JsonWriter mockedJsonWriter;
    @Mock
    private OpenGateConnector mockedConnector;
    @InjectMocks
    private OpenGateEventDispatcher testEventDispatcher;

    @Test
    public void testPublish() {
        Event eventToTest = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_AT, TEST_VALUE);
        Datapoint datapoint = new Datapoint(TEST_AT, TEST_VALUE);
        Datastream datastream = new Datastream(TEST_DATASTREAM_ID, Collections.singleton(datapoint));
        OutputDatastream output =
                new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH_WITH_HOST,
                        Collections.singleton(datastream));
        byte[] payload = new byte[]{1, 2, 3, 4};

        when(mockedJsonWriter.dumpOutput(any())).thenReturn(payload);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_ID);

        testEventDispatcher.publish(eventToTest);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedJsonWriter).dumpOutput(eq(output));
        verify(mockedConnector).uplink(eq(payload));
    }

    @Test
    public void testPublishWithEmptyDeviceIdAndNullPath() {
        Event eventToTest = new Event(TEST_DATASTREAM_ID, "", null, TEST_AT, TEST_VALUE);
        Datapoint datapoint = new Datapoint(TEST_AT, TEST_VALUE);
        Datastream datastream = new Datastream(TEST_DATASTREAM_ID, Collections.singleton(datapoint));
        OutputDatastream output = new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, null,
                Collections.singleton(datastream));
        byte[] payload = new byte[]{1, 2, 3, 4};

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedJsonWriter.dumpOutput(any())).thenReturn(payload);

        testEventDispatcher.publish(eventToTest);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedJsonWriter).dumpOutput(eq(output));
        verify(mockedConnector).uplink(eq(payload));
    }

    @Test
    public void testPublishReduceBandwidthMode() {
        Event eventToTest = new Event(TEST_DATASTREAM_ID, "", null, TEST_AT, TEST_VALUE);
        Datapoint datapoint = new Datapoint(null, TEST_VALUE);
        Datastream datastream = new Datastream(TEST_DATASTREAM_ID, Collections.singleton(datapoint));
        OutputDatastream output = new OutputDatastream(OPENGATE_VERSION, null, null, Collections.singleton(datastream));
        byte[] payload = new byte[]{1, 2, 3, 4};

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedJsonWriter.dumpOutput(any())).thenReturn(payload);

        testEventDispatcher.setReduceBandwidthMode(true);
        testEventDispatcher.publish(eventToTest);

        verify(mockedJsonWriter).dumpOutput(eq(output));
        verify(mockedConnector).uplink(eq(payload));
    }
}