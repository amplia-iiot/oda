package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SchedulerTest {
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
	private static final String[] PATH_DEVICE1 = {"a","path"};
	private static final String[] PATH_DEVICE2 = {"another","path"};
	
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
	DeviceInfoProvider deviceInfoProvider;
	@Mock EventCollector collector;
	@Mock OpenGateConnector connector;
	static JsonWriter jsonWriter = new JsonWriterImpl();
	
	private Scheduler scheduler;


	@SafeVarargs
	public static <T> Set<T> asSet(T... ts) {
		return new HashSet<>(Arrays.asList(ts));
	}
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		scheduler = new Scheduler(deviceInfoProvider, collector, connector, jsonWriter);
	}

	@Test
	public void runFor_getsValuesToSendFromCollector() {
		scheduler.runFor(asSet(ID1));
		
		verify(collector).getAndCleanCollectedValues(ID1);
	}

	@Test
	public void runFor_sendsADatastreamForEveryIdInTheParametersWithAllDatapointsRecolected() {
		when(collector.getAndCleanCollectedValues(ID1)).thenReturn(COLLECTED_VALUES_FOR_ID1_DEV1);
		
		scheduler.runFor(asSet(ID1));
		
		ArgumentCaptor<byte[]> iotData = ArgumentCaptor.forClass(byte[].class);
		verify(connector).uplink(iotData.capture());

		List<byte[]> expected = iotDataBuilder().
			iotData(DEVICE1, PATH_DEVICE1).
				datastream(ID1).
					datapoint(AT1, ID1_DEV1_VAL1).
					datapoint(AT2, ID1_DEV1_VAL2).
		buildBinary();
		
		List<byte[]> actual = iotData.getAllValues();
		assertThat(actual.size(), is(expected.size()));
		assertThat(actual.get(0), is(expected.get(0)));
	}

	@Test
	public void datastreamsOfTheSameDevice_AreJoinedInTheSameIotData() {
		when(collector.getAndCleanCollectedValues(ID1)).thenReturn(COLLECTED_VALUES_FOR_ID1_DEV1);
		when(collector.getAndCleanCollectedValues(ID2)).thenReturn(COLLECTED_VALUES_FOR_ID2_DEV1);
		
		scheduler.runFor(asSet(ID1,ID2));
		
		ArgumentCaptor<byte[]> iotData = ArgumentCaptor.forClass(byte[].class);
		verify(connector, times(1)).uplink(iotData.capture());
		
		List<byte[]> expected = iotDataBuilder().
			iotData(DEVICE1, PATH_DEVICE1).
				datastream(ID2).
					datapoint(AT1, ID2_DEV2_VAL1).
					datapoint(AT2, ID2_DEV2_VAL2).
				datastream(ID1).
					datapoint(AT1, ID1_DEV1_VAL1).
					datapoint(AT2, ID1_DEV1_VAL2).
		buildBinary();
		
		List<byte[]> actual = iotData.getAllValues();
		assertThat(actual.size(), is(expected.size()));
		assertThat(actual.get(0), is(expected.get(0)));
	}
	
	@Test
	public void datastreamsOfDiferentDevices_ArenotJoinedInTheSameIotData() {
		when(collector.getAndCleanCollectedValues(ID1)).thenReturn(COLLECTED_VALUES_FOR_ID1_DEV1);
		when(collector.getAndCleanCollectedValues(ID2)).thenReturn(COLLECTED_VALUES_FOR_ID2_DEV2);
		
		scheduler.runFor(asSet(ID1,ID2));
		
		ArgumentCaptor<byte[]> iotData = ArgumentCaptor.forClass(byte[].class);
		verify(connector, times(2)).uplink(iotData.capture());
		
		List<byte[]> expected = iotDataBuilder().
			iotData(DEVICE1, PATH_DEVICE1).
				datastream(ID1).
					datapoint(AT1, ID1_DEV1_VAL1).
					datapoint(AT2, ID1_DEV1_VAL2).
			iotData(DEVICE2, PATH_DEVICE2).
				datastream(ID2).
					datapoint(AT1, ID2_DEV2_VAL1).
					datapoint(AT2, ID2_DEV2_VAL2).
		buildBinary();
		
		List<byte[]> actual = iotData.getAllValues();
		assertThat(actual.size(), is(expected.size()));
		assertThat(actual.get(0), is(expected.get(0)));
		assertThat(actual.get(1), is(expected.get(1)));
	}
	
	@Test
	public void ifThereAreNoCollectedValuesForAnId_NothingIsSent() {
		when(collector.getAndCleanCollectedValues(ID1)).thenReturn(null);
		
		scheduler.runFor(asSet(ID1));
		
		verify(connector, never()).uplink(any());
	}
	
	@Test
	public void datapointsOfDifferentDevicesAreSentInDifferentMessages() {
		List<Event> COLLECTED_VALUES_FOR_ID1_FOR_DEV1_AND_DEV2 = new ArrayList<>();
		COLLECTED_VALUES_FOR_ID1_FOR_DEV1_AND_DEV2.addAll(COLLECTED_VALUES_FOR_ID1_DEV1);
		COLLECTED_VALUES_FOR_ID1_FOR_DEV1_AND_DEV2.addAll(COLLECTED_VALUES_FOR_ID1_DEV2);
		when(collector.getAndCleanCollectedValues(ID1)).thenReturn(COLLECTED_VALUES_FOR_ID1_FOR_DEV1_AND_DEV2);

		scheduler.runFor(asSet(ID1));

		ArgumentCaptor<byte[]> iotData = ArgumentCaptor.forClass(byte[].class);
		verify(connector, times(2)).uplink(iotData.capture());
		
		List<byte[]> expected = iotDataBuilder().
			iotData(DEVICE1, PATH_DEVICE1).
				datastream(ID1).
					datapoint(AT1, ID1_DEV1_VAL1).
					datapoint(AT2, ID1_DEV1_VAL2).
			iotData(DEVICE2, PATH_DEVICE2).
				datastream(ID1).
					datapoint(AT1, ID1_DEV2_VAL1).
					datapoint(AT2, ID1_DEV2_VAL2).
		buildBinary();
		
		List<byte[]> actual = iotData.getAllValues();
		assertThat(actual.size(), is(expected.size()));
		assertThat(actual.get(0), is(expected.get(0)));
		assertThat(actual.get(1), is(expected.get(1)));
	}
	
	public static IotDataBuilderT iotDataBuilder() {
		return new IotDataBuilderT();
	}
	
	public static class IotDataBuilderT {
		List<OutputDatastream> currentList = new ArrayList<OutputDatastream>();
		OutputDatastream currentIotData;
		Datastream currentDatastream;
		
		public IotDataBuilderT iotData(String device, String[] path) {
			currentIotData = new OutputDatastream("8.0", device, path, new HashSet<Datastream>());
			currentList.add(currentIotData);
			return this;
		}

		public IotDataBuilderT datastream(String id) {
			currentDatastream = new Datastream(id, new HashSet<Datapoint>());
			currentIotData.getDatastreams().add(currentDatastream);
			return this;
		}

		public IotDataBuilderT datapoint(long at, Object value) {
			currentDatastream.getDatapoints().add(new Datapoint(at, value));
			return this;
		}

		public List<OutputDatastream> build() {
			return currentList;
		}
		
		public List<byte[]> buildBinary() {
			List<byte[]> ret = new ArrayList<>();
			currentList.forEach((ds)->ret.add(jsonWriter.dumpOutput(ds)));
			return ret;
		}
	}
}
