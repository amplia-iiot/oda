package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.io.IOException;
import java.util.*;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SchedulerImplTest {
    private static final String ID1 = "Id1";
    private static final String ID2 = "Id2";

    private static final long AT1 = 100;
    private static final long AT2 = 200;
    private static final Integer ID1_DEV1_VAL1 = 12;
    private static final Integer ID1_DEV1_VAL2 = 24;
    private static final Integer ID1_DEV2_VAL1 = 77;
    private static final Integer ID1_DEV2_VAL2 = 88;
    private static final String ID2_DEV2_VAL1 = "a";
    private static final String ID2_DEV2_VAL2 = "b";
    private static final String DEVICE1 = "Device1";
    private static final String DEVICE2 = "Device2";
    private static final String HOST = "host";
    private static final String[] PATH_DEVICE1 = {"a","path"};
    private static final String[] PATH_DEVICE1_WITH_HOST = {"host","a","path"};
    private static final String[] PATH_DEVICE2 = {"another","path"};
    private static final String[] PATH_DEVICE2_WITH_HOST = {"host","another","path"};
    
    private static final List<Event> COLLECTED_VALUES_FOR_ID1_DEV1 = Arrays.asList(
            new Event(ID1, DEVICE1, PATH_DEVICE1, AT1, ID1_DEV1_VAL1),
            new Event(ID1, DEVICE1, PATH_DEVICE1, AT2, ID1_DEV1_VAL2)
    );
    private static final List<Event> COLLECTED_VALUES_FOR_ID1_DEV2 = Arrays.asList(
            new Event(ID1, DEVICE2, PATH_DEVICE2, AT1, ID1_DEV2_VAL1),
            new Event(ID1, DEVICE2, PATH_DEVICE2, AT2, ID1_DEV2_VAL2)
    );
    private static final List<Event> COLLECTED_VALUES_FOR_ID2_DEV1 = Arrays.asList(
            new Event(ID2, DEVICE1, PATH_DEVICE1, AT1, ID2_DEV2_VAL1),
            new Event(ID2, DEVICE1, PATH_DEVICE1, AT2, ID2_DEV2_VAL2)
    );
    private static final List<Event> COLLECTED_VALUES_FOR_ID2_DEV2 = Arrays.asList(
            new Event(ID2, DEVICE2, PATH_DEVICE2, AT1, ID2_DEV2_VAL1),
            new Event(ID2, DEVICE2, PATH_DEVICE2, AT2, ID2_DEV2_VAL2)
    );

    @Mock
    private DeviceInfoProvider deviceInfoProvider;
    @Mock
    private EventCollector collector;
    @Mock
    private OpenGateConnector connector;
    @Mock
    private Serializer serializer;
    @Mock
    private IOException exception;
    
    private SchedulerImpl schedulerImpl;


    @SafeVarargs
    private static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        schedulerImpl = new SchedulerImpl(deviceInfoProvider, collector, connector, serializer);
    }

    @Test
    public void runForGetsValuesToSendFromCollector() {
        schedulerImpl.runFor(asSet(ID1));
        
        verify(collector).getAndCleanCollectedValues(ID1);
    }

    @Test
    public void runForSendsADatastreamForEveryIdInTheParametersWithAllDatapointsRecollected() throws IOException {
        byte[] testByteArray = { 0x1, 0x2, 0x3, 0x4 };
        List<OutputDatastream> expectedOutputStreams = iotDataBuilder()
                .iotData(DEVICE1, PATH_DEVICE1_WITH_HOST)
                .datastream(ID1)
                .datapoint(AT1, ID1_DEV1_VAL1)
                .datapoint(AT2, ID1_DEV1_VAL2)
                .buildCurrentList();
        ArgumentCaptor<OutputDatastream> outputDatastreamsCaptor = ArgumentCaptor.forClass(OutputDatastream.class);

        when(collector.getAndCleanCollectedValues(ID1)).thenReturn(COLLECTED_VALUES_FOR_ID1_DEV1);
        when(serializer.serialize(any())).thenReturn(testByteArray);
        when(deviceInfoProvider.getDeviceId()).thenReturn(HOST);

        schedulerImpl.runFor(asSet(ID1));

        verify(deviceInfoProvider, atLeastOnce()).getDeviceId();
        for (OutputDatastream os : expectedOutputStreams) {
            verify(serializer).serialize(outputDatastreamsCaptor.capture());
            OutputDatastream capturedDatastream = outputDatastreamsCaptor.getValue();
            verifyEqualsOutput(os, capturedDatastream);
        }
        verify(connector, times(expectedOutputStreams.size())).uplink(eq(testByteArray));
    }

    @Test
    public void datastreamsOfTheSameDeviceAreJoinedInTheSameIotData() throws IOException {
        byte[] testByteArray = { 0x1, 0x2, 0x3, 0x4 };
        List<OutputDatastream> expectedOutputStreams = iotDataBuilder().
                iotData(DEVICE1, PATH_DEVICE1_WITH_HOST)
                .datastream(ID2)
                .datapoint(AT1, ID2_DEV2_VAL1)
                .datapoint(AT2, ID2_DEV2_VAL2)
                .datastream(ID1)
                .datapoint(AT1, ID1_DEV1_VAL1)
                .datapoint(AT2, ID1_DEV1_VAL2)
                .buildCurrentList();
        ArgumentCaptor<OutputDatastream> outputDatastreamsCaptor = ArgumentCaptor.forClass(OutputDatastream.class);

        when(collector.getAndCleanCollectedValues(ID1)).thenReturn(COLLECTED_VALUES_FOR_ID1_DEV1);
        when(collector.getAndCleanCollectedValues(ID2)).thenReturn(COLLECTED_VALUES_FOR_ID2_DEV1);
        when(serializer.serialize(any())).thenReturn(testByteArray);
        when(deviceInfoProvider.getDeviceId()).thenReturn(HOST);

        schedulerImpl.runFor(asSet(ID1,ID2));

        verify(deviceInfoProvider, atLeastOnce()).getDeviceId();
        for (OutputDatastream os : expectedOutputStreams) {
            verify(serializer).serialize(outputDatastreamsCaptor.capture());
            OutputDatastream capturedDatastream = outputDatastreamsCaptor.getValue();
            verifyEqualsOutput(os, capturedDatastream);
        }
        verify(serializer, times(expectedOutputStreams.size())).serialize(any());
        verify(connector, times(expectedOutputStreams.size())).uplink(eq(testByteArray));
    }
    
    @Test
    public void datastreamsOfDifferentDevicesAreNotJoinedInTheSameIotData() throws IOException {
        byte[] testByteArray = { 0x1, 0x2, 0x3, 0x4 };
        List<OutputDatastream> expectedOutputStreams = iotDataBuilder()
                .iotData(DEVICE1, PATH_DEVICE1_WITH_HOST)
                .datastream(ID1)
                .datapoint(AT1, ID1_DEV1_VAL1)
                .datapoint(AT2, ID1_DEV1_VAL2)
                .iotData(DEVICE2, PATH_DEVICE2_WITH_HOST)
                .datastream(ID2)
                .datapoint(AT1, ID2_DEV2_VAL1)
                .datapoint(AT2, ID2_DEV2_VAL2)
                .buildCurrentList();
        ArgumentCaptor<OutputDatastream> outputDatastreamsCaptor = ArgumentCaptor.forClass(OutputDatastream.class);

        when(collector.getAndCleanCollectedValues(ID1)).thenReturn(COLLECTED_VALUES_FOR_ID1_DEV1);
        when(collector.getAndCleanCollectedValues(ID2)).thenReturn(COLLECTED_VALUES_FOR_ID2_DEV2);
        when(serializer.serialize(any())).thenReturn(testByteArray);
        when(deviceInfoProvider.getDeviceId()).thenReturn(HOST);
        
        schedulerImpl.runFor(asSet(ID1,ID2));

        verify(deviceInfoProvider, atLeastOnce()).getDeviceId();

        verify(serializer, atLeast(1)).serialize(outputDatastreamsCaptor.capture());
        OutputDatastream capturedDatastream;
        for (int i = expectedOutputStreams.size() - 1; i >= 0; i--) {
            capturedDatastream = outputDatastreamsCaptor.getValue();
            verifyEqualsOutput(expectedOutputStreams.get(i), capturedDatastream);
            outputDatastreamsCaptor.getAllValues().remove(i);
        }
        verify(serializer, times(expectedOutputStreams.size())).serialize(any());
        verify(connector, times(expectedOutputStreams.size())).uplink(eq(testByteArray));
    }
    
    @Test
    public void ifThereAreNoCollectedValuesForAnId_NothingIsSent() {
        when(collector.getAndCleanCollectedValues(ID1)).thenReturn(null);
        
        schedulerImpl.runFor(asSet(ID1));
        
        verify(connector, never()).uplink(any());
    }
    
    @Test
    public void datapointsOfDifferentDevicesAreSentInDifferentMessages() throws IOException {
        List<Event> COLLECTED_VALUES_FOR_ID1_FOR_DEV1_AND_DEV2 = new ArrayList<>();
        COLLECTED_VALUES_FOR_ID1_FOR_DEV1_AND_DEV2.addAll(COLLECTED_VALUES_FOR_ID1_DEV1);
        COLLECTED_VALUES_FOR_ID1_FOR_DEV1_AND_DEV2.addAll(COLLECTED_VALUES_FOR_ID1_DEV2);
        byte[] testByteArray = { 0x1, 0x2, 0x3, 0x4 };

        List<OutputDatastream> expectedOutputStreams = iotDataBuilder()
                .iotData(DEVICE1, PATH_DEVICE1_WITH_HOST)
                .datastream(ID1)
                .datapoint(AT1, ID1_DEV1_VAL1)
                .datapoint(AT2, ID1_DEV1_VAL2)
                .iotData(DEVICE2, PATH_DEVICE2_WITH_HOST)
                .datastream(ID1)
                .datapoint(AT1, ID1_DEV2_VAL1)
                .datapoint(AT2, ID1_DEV2_VAL2)
                .buildCurrentList();
        ArgumentCaptor<OutputDatastream> outputDatastreamsCaptor = ArgumentCaptor.forClass(OutputDatastream.class);

        when(collector.getAndCleanCollectedValues(ID1)).thenReturn(COLLECTED_VALUES_FOR_ID1_FOR_DEV1_AND_DEV2);
        when(deviceInfoProvider.getDeviceId()).thenReturn(HOST);
        when(serializer.serialize(any())).thenReturn(testByteArray);

        schedulerImpl.runFor(asSet(ID1));

        verify(deviceInfoProvider, atLeastOnce()).getDeviceId();


        verify(serializer, atLeast(1)).serialize(outputDatastreamsCaptor.capture());
        OutputDatastream capturedDatastream;
        for (int i = expectedOutputStreams.size() - 1; i >= 0; i--) {
            capturedDatastream = outputDatastreamsCaptor.getValue();
            verifyEqualsOutput(expectedOutputStreams.get(i), capturedDatastream);
            outputDatastreamsCaptor.getAllValues().remove(i);
        }
        verify(serializer, times(expectedOutputStreams.size())).serialize(any());
        verify(connector, times(expectedOutputStreams.size())).uplink(eq(testByteArray));
    }
    
    private IotDataBuilderT iotDataBuilder() {
        return new IotDataBuilderT();
    }
    
    private class IotDataBuilderT {
        private final List<OutputDatastream> currentList = new ArrayList<>();
        private OutputDatastream currentIotData;
        private Datastream currentDatastream;
        
        IotDataBuilderT iotData(String device, String[] path) {
            currentIotData = new OutputDatastream(OPENGATE_VERSION, device, path, new HashSet<>());
            currentList.add(currentIotData);
            return this;
        }

        IotDataBuilderT datastream(String id) {
            currentDatastream = new Datastream(id, new HashSet<>());
            currentIotData.getDatastreams().add(currentDatastream);
            return this;
        }

        IotDataBuilderT datapoint(long at, Object value) {
            currentDatastream.getDatapoints().add(new Datapoint(at, value));
            return this;
        }

        List<OutputDatastream> buildCurrentList() {
            return currentList;
        }
    }

    private void verifyEqualsOutput(OutputDatastream os, OutputDatastream capturedDatastream) {
        assertEquals(os.getDevice(), capturedDatastream.getDevice());
        assertEquals(os.getVersion(), capturedDatastream.getVersion());
        assertArrayEquals(os.getPath(), capturedDatastream.getPath());
        for (Iterator it1 = os.getDatastreams().iterator(), it2 = capturedDatastream.getDatastreams().iterator();
             it1.hasNext() && it2.hasNext(); ) {
            Datastream d1 = (Datastream) it1.next();
            Datastream d2 = (Datastream) it2.next();
            assertEquals(d1.getId(), d2.getId());
            for (Iterator it3 = d1.getDatapoints().iterator(), it4 = d2.getDatapoints().iterator();
                 it3.hasNext() && it4.hasNext(); ) {
                Datapoint dp1 = (Datapoint) it3.next();
                Datapoint dp2 = (Datapoint) it4.next();
                assertEquals(dp1.getAt(), dp2.getAt());
                assertEquals(dp1.getValue(), dp2.getValue());
            }
        }
    }
}
