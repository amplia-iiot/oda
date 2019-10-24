package es.amplia.oda.datastreams.adc.configuration;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.datastreams.adc.DatastreamsRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static es.amplia.oda.datastreams.adc.configuration.DatastreamsAdcConfigurationHandler.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class DatastreamsAdcConfigurationHandlerTest {

	private static final int TEST_INDEX_1 = 1;
	private static final String TEST_DATASTREAM_1 = "test1";
	private static final int TEST_INDEX_2 = 2;
	private static final String TEST_DATASTREAM_2 = "test2";


	@Mock
	DatastreamsRegistry mockedRegistry;
	@Mock
	AdcService mockedService;
	@InjectMocks
	DatastreamsAdcConfigurationHandler testHandler;

	@Mock
	AdcChannel EXT_ADC0;
	@Mock
	AdcChannel EXT_ADC1;


	@Test
	public void loadConfiguration() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(TEST_DATASTREAM_1,
				CHANNEL_PIN_PROPERTY_NAME + ":" + TEST_INDEX_1 + "," + GETTER_PROPERTY_NAME + ": true"
						+ "," + MINIMUM_PROPERTY_NAME + ": 0" + "," + MAXIMUM_PROPERTY_NAME + ": 10");
		props.put(TEST_DATASTREAM_2,
				CHANNEL_PIN_PROPERTY_NAME + ":" + TEST_INDEX_2 + "," + EVENT_PROPERTY_NAME + ": true"
						+ "," + MINIMUM_PROPERTY_NAME + ": 0" + "," + MAXIMUM_PROPERTY_NAME + ": 10"
						+ "," + GETTER_PROPERTY_NAME + ": false");

		testHandler.loadConfiguration(props);

		Map<String, AdcChannelDatastreamConfiguration> configuration = getCurrentConfiguration();
		assertEquals(2, configuration.size());
		assertTrue(configuration.containsKey(TEST_DATASTREAM_1));
		AdcChannelDatastreamConfiguration conf = configuration.get(TEST_DATASTREAM_1);
		assertEquals(TEST_INDEX_1, conf.getChannelPin());
		assertEquals(TEST_DATASTREAM_1, conf.getDatastreamId());
		assertTrue(conf.isGetter());
		assertFalse(conf.isEvent());
		assertTrue(configuration.containsKey(TEST_DATASTREAM_2));
		conf = configuration.get(TEST_DATASTREAM_2);
		assertEquals(TEST_INDEX_2, conf.getChannelPin());
		assertEquals(TEST_DATASTREAM_2, conf.getDatastreamId());
		assertFalse(conf.isGetter());
		assertTrue(conf.isEvent());
	}

	@SuppressWarnings("unchecked")
	private Map<String, AdcChannelDatastreamConfiguration> getCurrentConfiguration() {
		return (Map<String, AdcChannelDatastreamConfiguration>)
				Whitebox.getInternalState(testHandler, "currentConfiguration");
	}

	@Test
	public void loadConfigurationWithNoDatastreamIdIllegalArgumentExceptionIsCaught() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(Integer.toString(TEST_INDEX_1), GETTER_PROPERTY_NAME + ": true");

		testHandler.loadConfiguration(props);

		Map<String, AdcChannelDatastreamConfiguration> configuration = getCurrentConfiguration();
		assertTrue(configuration.isEmpty());
	}

	@Test
	public void loadDefaultConfiguration() {
		when(EXT_ADC0.getName()).thenReturn(TEST_DATASTREAM_1);
		when(EXT_ADC1.getName()).thenReturn(TEST_DATASTREAM_2);
		Map<Integer,AdcChannel> availableChannels = new HashMap<>();
		availableChannels.put(TEST_INDEX_1, EXT_ADC0);
		availableChannels.put(TEST_INDEX_2, EXT_ADC1);
		when(mockedService.getAvailableChannels()).thenReturn(availableChannels);

		testHandler.loadDefaultConfiguration();

		Map<String, AdcChannelDatastreamConfiguration> configuration =
				getCurrentConfiguration();
		assertEquals(2, configuration.size());
		assertTrue(configuration.containsKey(TEST_DATASTREAM_1));
		AdcChannelDatastreamConfiguration conf = configuration.get(TEST_DATASTREAM_1);
		assertEquals(TEST_INDEX_1, conf.getChannelPin());
		assertEquals(TEST_DATASTREAM_1, conf.getDatastreamId());
		assertTrue(conf.isGetter());
		assertFalse(conf.isEvent());
		assertTrue(configuration.containsKey(TEST_DATASTREAM_2));
		conf = configuration.get(TEST_DATASTREAM_2);
		assertEquals(TEST_INDEX_2, conf.getChannelPin());
		assertEquals(TEST_DATASTREAM_2, conf.getDatastreamId());
		assertTrue(conf.isGetter());
		assertFalse(conf.isEvent());
	}

	@Test
	public void applyConfiguration() {
		Map<String, AdcChannelDatastreamConfiguration> configuration = new HashMap<>();
		configuration.put(TEST_DATASTREAM_1, new AdcChannelDatastreamConfiguration(TEST_DATASTREAM_1, TEST_INDEX_1,
				true, false, 0, 1));
		configuration.put(TEST_DATASTREAM_2, new AdcChannelDatastreamConfiguration(TEST_DATASTREAM_2, TEST_INDEX_2,
				false, true, 0, 1));

		Whitebox.setInternalState(testHandler, "currentConfiguration", configuration);

		testHandler.applyConfiguration();

		verify(mockedRegistry).close();
		verify(mockedRegistry).addAdcDatastreamGetter(eq(TEST_INDEX_1), eq(TEST_DATASTREAM_1), eq(0.), eq(1.));
		verify(mockedRegistry).addAdcDatastreamEvent(eq(TEST_INDEX_2), eq(TEST_DATASTREAM_2));
	}
}
