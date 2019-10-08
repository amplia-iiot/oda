package es.amplia.oda.datastreams.diozero.configuration;

import es.amplia.oda.core.commons.diozero.AdcChannel;
import es.amplia.oda.core.commons.diozero.AdcService;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.datastreams.diozero.DatastreamsRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class DatastreamsAdcConfigurationHandlerTest {

	private static final String device = "11";
	private static final String environment = "22";
	private static final String hour = "33";
	private static final String light = "44";
	private static final String humidity = "55";

	@Mock
	DatastreamsRegistry mockedRegistry;
	@Mock
	AdcService mockedService;
	@InjectMocks
	DatastreamsAdcConfigurationHandler testHandler;

	@Mock
	Dictionary<String, String> props;
	@Mock
	AdcChannel EXT_ADC0;
	@Mock
	AdcChannel EXT_ADC1;


	@Before
	public void prepareForTest() {
		String devData = "datastreamId:devTemperature,getter:true,pinType:adc.ADCChannel";
		String envData = "datastreamId:envTemperature,event:true,pinType:adc.ADCChannel";
		String hourData = "datastreamId:hour,getter:true,pinType:gpio.GPIOPin";
		String lightData = "datastreamId:lightLevel,setter:true,pinType:gpio.GPIOPin";
		String humidityData = "datastreamId:humidity,event:true,pinType:gpio.GPIOPin";
		Enumeration<String> keys = Collections.enumeration(Arrays.asList(device,environment,hour,light,humidity));
		when(props.keys()).thenReturn(keys);
		when(props.get(eq(device))).thenReturn(devData);
		when(props.get(eq(environment))).thenReturn(envData);
		when(props.get(eq(hour))).thenReturn(hourData);
		when(props.get(eq(light))).thenReturn(lightData);
		when(props.get(eq(humidity))).thenReturn(humidityData);
	}

	@Test
	public void loadConfiguration() {
		testHandler.loadConfiguration(props);

		Map<Integer, AdcChannelDatastreamConfiguration> configuration =
				(Map<Integer, AdcChannelDatastreamConfiguration>) Whitebox.getInternalState(testHandler, "currentConfiguration");
		assertEquals(5, configuration.size());
		assertTrue(configuration.containsKey(11) && configuration.containsKey(22) && configuration.containsKey(33)
			&& configuration.containsKey(44) && configuration.containsKey(55));
		assertNotNull(configuration.get(11));
		assertNotNull(configuration.get(22));
		assertNotNull(configuration.get(33));
		assertNotNull(configuration.get(44));
		assertNotNull(configuration.get(55));
	}

	@Test(expected = ConfigurationException.class)
	public void loadConfigurationWithNoDatastreamId() {
		when(props.get(device)).thenReturn("");

		testHandler.loadConfiguration(props);
	}

	@Test
	public void loadConfigurationWithAlphabeticPin() {
		String hour = "DD"; String hourData = "datastreamId:another,getter:true,pinType:gpio.GPIOPin";
		when(props.keys()).thenReturn(Collections.enumeration(Collections.singletonList(hour)));

		testHandler.loadConfiguration(props);

		Map<Integer, AdcChannelDatastreamConfiguration> configuration =
				(Map<Integer, AdcChannelDatastreamConfiguration>) Whitebox.getInternalState(testHandler, "currentConfiguration");
		assertEquals(0, configuration.size());
	}

	@Test
	public void loadDefaultConfiguration() {
		when(EXT_ADC0.getName()).thenReturn("devDatastream");
		when(EXT_ADC1.getName()).thenReturn("envDatastream");
		Map<Integer,AdcChannel> availableChannels = new HashMap<>();
		availableChannels.put(Integer.valueOf(device), EXT_ADC0);
		availableChannels.put(Integer.valueOf(environment), EXT_ADC1);
		when(mockedService.getAvailableChannels()).thenReturn(availableChannels);

		testHandler.loadDefaultConfiguration();

		Map<Integer, AdcChannelDatastreamConfiguration> configuration =
				(Map<Integer, AdcChannelDatastreamConfiguration>) Whitebox.getInternalState(testHandler, "currentConfiguration");
		assertEquals(2, configuration.size());
		assertTrue(configuration.containsKey(11) && configuration.containsKey(22));
		assertNotNull(configuration.get(11));
		assertNotNull(configuration.get(22));
	}

	@Test
	public void applyConfiguration() throws Exception {
		Map<Integer, AdcChannelDatastreamConfiguration> configuration = new HashMap<>();
		configuration.put(Integer.valueOf(device), new AdcChannelDatastreamConfiguration(Integer.valueOf(device),
				"adc.ADCChannel", "devTemperature", true, false, false));
		configuration.put(Integer.valueOf(environment), new AdcChannelDatastreamConfiguration(Integer.valueOf(environment),
				"adc.ADCChannel", "envTemperature", false, false, true));
		configuration.put(Integer.valueOf(hour), new AdcChannelDatastreamConfiguration(Integer.valueOf(hour),
				"gpio.GPIOPin", "hour", true, false, false));
		configuration.put(Integer.valueOf(light), new AdcChannelDatastreamConfiguration(Integer.valueOf(light),
				"gpio.GPIOPin", "lightLevel", false, true, false));
		configuration.put(Integer.valueOf(humidity), new AdcChannelDatastreamConfiguration(Integer.valueOf(humidity),
				"gpio.GPIOPin", "humidity", false, false, true));
		Whitebox.setInternalState(testHandler, "currentConfiguration", configuration);
		doNothing().when(mockedRegistry).close();
		doNothing().when(mockedRegistry).addAdcDatastreamGetter(eq(Integer.valueOf(device)), any());
		doNothing().when(mockedRegistry).addAdcDatastreamEvent(eq(Integer.valueOf(environment)), any());
		doNothing().when(mockedRegistry).addGpioDatastreamEvent(eq(Integer.valueOf(hour)), any());
		doNothing().when(mockedRegistry).addGpioDatastreamGetter(eq(Integer.valueOf(light)), any());
		doNothing().when(mockedRegistry).addGpioDatastreamSetter(eq(Integer.valueOf(humidity)), any());

		testHandler.applyConfiguration();

		verify(mockedRegistry).addAdcDatastreamGetter(eq(Integer.valueOf(device)), any());
		verify(mockedRegistry).addAdcDatastreamEvent(eq(Integer.valueOf(environment)), any());
		verify(mockedRegistry).addGpioDatastreamGetter(eq(Integer.valueOf(hour)), any());
		verify(mockedRegistry).addGpioDatastreamSetter(eq(Integer.valueOf(light)), any());
		verify(mockedRegistry).addGpioDatastreamEvent(eq(Integer.valueOf(humidity)), any());
	}

	@Test(expected = ConfigurationException.class)
	public void applyConfigurationAdcSetter() throws Exception {
		Map<Integer, AdcChannelDatastreamConfiguration> configuration = new HashMap<>();
		configuration.put(Integer.valueOf(device), new AdcChannelDatastreamConfiguration(Integer.valueOf(device),
				"adc.ADCChannel", "devTemperature", false, true, false));
		Whitebox.setInternalState(testHandler, "currentConfiguration", configuration);

		testHandler.applyConfiguration();
	}

	@Test(expected = ConfigurationException.class)
	public void applyConfigurationInvalidType() throws Exception {
		Map<Integer, AdcChannelDatastreamConfiguration> configuration = new HashMap<>();
		configuration.put(Integer.valueOf(device), new AdcChannelDatastreamConfiguration(Integer.valueOf(device),
				"not.ValidType", "devTemperature", false, true, false));
		Whitebox.setInternalState(testHandler, "currentConfiguration", configuration);

		testHandler.applyConfiguration();
	}
}
