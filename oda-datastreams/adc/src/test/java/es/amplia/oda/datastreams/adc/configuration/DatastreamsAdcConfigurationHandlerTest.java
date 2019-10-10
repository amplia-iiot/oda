package es.amplia.oda.datastreams.adc.configuration;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
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
		props.put(Integer.toString(TEST_INDEX_1),
				PIN_TYPE_PROPERTY_NAME + ":" + ADC_CHANNEL_TYPE_NAME + "," + DATASTREAM_ID_PROPERTY_NAME + ":" +
						TEST_DATASTREAM_1 + "," + GETTER_PROPERTY_NAME + ": true");
		props.put(Integer.toString(TEST_INDEX_2),
				PIN_TYPE_PROPERTY_NAME + ":" + ADC_CHANNEL_TYPE_NAME + "," + DATASTREAM_ID_PROPERTY_NAME + ":" +
						TEST_DATASTREAM_2 + "," + EVENT_PROPERTY_NAME + ": true");

		testHandler.loadConfiguration(props);

		Map<Integer, AdcChannelDatastreamConfiguration> configuration = getCurrentConfiguration();
		assertEquals(2, configuration.size());
		assertTrue(configuration.containsKey(TEST_INDEX_1));
		AdcChannelDatastreamConfiguration conf = configuration.get(TEST_INDEX_1);
		assertEquals(TEST_INDEX_1, conf.getChannelPin());
		assertEquals(ADC_CHANNEL_TYPE_NAME, conf.getPinType());
		assertEquals(TEST_DATASTREAM_1, conf.getDatastreamId());
		assertTrue(conf.isGetter());
		assertFalse(conf.isEvent());
		assertTrue(configuration.containsKey(TEST_INDEX_2));
		conf = configuration.get(TEST_INDEX_2);
		assertEquals(TEST_INDEX_2, conf.getChannelPin());
		assertEquals(ADC_CHANNEL_TYPE_NAME, conf.getPinType());
		assertEquals(TEST_DATASTREAM_2, conf.getDatastreamId());
		assertFalse(conf.isGetter());
		assertTrue(conf.isEvent());
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, AdcChannelDatastreamConfiguration> getCurrentConfiguration() {
		return (Map<Integer, AdcChannelDatastreamConfiguration>)
				Whitebox.getInternalState(testHandler, "currentConfiguration");
	}

	@Test
	public void loadConfigurationWithNoDatastreamIdIllegalArgumentExceptionIsCaught() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(Integer.toString(TEST_INDEX_1),
				PIN_TYPE_PROPERTY_NAME + ":" + ADC_CHANNEL_TYPE_NAME + "," + GETTER_PROPERTY_NAME + ": true");

		testHandler.loadConfiguration(props);

		Map<Integer, AdcChannelDatastreamConfiguration> configuration = getCurrentConfiguration();
		assertTrue(configuration.isEmpty());
	}

	@Test
	public void loadConfigurationWithInvalidPin() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put("invalidPin",
				PIN_TYPE_PROPERTY_NAME + ":" + ADC_CHANNEL_TYPE_NAME + "," + DATASTREAM_ID_PROPERTY_NAME + ":" +
						TEST_DATASTREAM_1 + "," + GETTER_PROPERTY_NAME + ": true");

		testHandler.loadConfiguration(props);

		Map<Integer, AdcChannelDatastreamConfiguration> configuration =
				getCurrentConfiguration();
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

		Map<Integer, AdcChannelDatastreamConfiguration> configuration =
				getCurrentConfiguration();
		assertEquals(2, configuration.size());
		assertTrue(configuration.containsKey(TEST_INDEX_1));
		AdcChannelDatastreamConfiguration conf = configuration.get(TEST_INDEX_1);
		assertEquals(TEST_INDEX_1, conf.getChannelPin());
		assertEquals(ADC_CHANNEL_TYPE_NAME, conf.getPinType());
		assertEquals(TEST_DATASTREAM_1, conf.getDatastreamId());
		assertTrue(conf.isGetter());
		assertFalse(conf.isEvent());
		assertTrue(configuration.containsKey(TEST_INDEX_2));
		conf = configuration.get(TEST_INDEX_2);
		assertEquals(TEST_INDEX_2, conf.getChannelPin());
		assertEquals(ADC_CHANNEL_TYPE_NAME, conf.getPinType());
		assertEquals(TEST_DATASTREAM_2, conf.getDatastreamId());
		assertTrue(conf.isGetter());
		assertFalse(conf.isEvent());
	}

	@Test
	public void applyConfiguration() {
		Map<Integer, AdcChannelDatastreamConfiguration> configuration = new HashMap<>();
		configuration.put(TEST_INDEX_1, new AdcChannelDatastreamConfiguration(TEST_INDEX_1, ADC_CHANNEL_TYPE_NAME,
				TEST_DATASTREAM_1, true, false));
		configuration.put(TEST_INDEX_2, new AdcChannelDatastreamConfiguration(TEST_INDEX_1, ADC_CHANNEL_TYPE_NAME,
				TEST_DATASTREAM_2, false, true));

		Whitebox.setInternalState(testHandler, "currentConfiguration", configuration);

		testHandler.applyConfiguration();

		verify(mockedRegistry).close();
		verify(mockedRegistry).addAdcDatastreamGetter(eq(TEST_INDEX_1), eq(TEST_DATASTREAM_1));
		verify(mockedRegistry).addAdcDatastreamEvent(eq(TEST_INDEX_2), eq(TEST_DATASTREAM_2));
	}

	@Test(expected = ConfigurationException.class)
	public void applyConfigurationInvalidType() {
		Map<Integer, AdcChannelDatastreamConfiguration> configuration = new HashMap<>();
		configuration.put(TEST_INDEX_1, new AdcChannelDatastreamConfiguration(TEST_INDEX_1, "invalidType",
				TEST_DATASTREAM_1, false, false));

		Whitebox.setInternalState(testHandler, "currentConfiguration", configuration);

		testHandler.applyConfiguration();

		fail("Configuration exception must be thrown");
	}
}
