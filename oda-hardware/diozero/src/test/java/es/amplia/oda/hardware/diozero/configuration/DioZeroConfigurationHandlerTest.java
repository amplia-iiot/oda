package es.amplia.oda.hardware.diozero.configuration;

import com.diozero.api.AnalogInputDevice;
import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.DeviceType;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static es.amplia.oda.hardware.diozero.configuration.DioZeroConfigurationHandler.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DioZeroConfigurationHandler.class, AnalogInputDeviceBuilder.class})
public class DioZeroConfigurationHandlerTest {

    private static final int TEST_ADC_CHANNEL_1_INDEX = 0;
    private static final String TEST_ADC_CHANNEL_1_NAME = "test1";
    private static final boolean TEST_ADC_CHANNEL_1_LOW_MODE = true;
    private static final String TEST_ADC_CHANNEL_1_PATH = "path1";
    private static final String TEST_ADC_CHANNEL_1_DEVICE_TYPE = "FX30";
    private static final int TEST_ADC_CHANNEL_2_INDEX = 1;
    private static final String TEST_ADC_CHANNEL_2_PATH = "path2";


    @Mock
    private DioZeroAdcService mockedService;
    @InjectMocks
    private DioZeroConfigurationHandler testHandler;

    @Mock
    AnalogInputDeviceBuilder mockedBuilder;
    @Mock
    AnalogInputDevice mockedAnalogInputDevice;

    @Test
    public void testLoadDefaultConfiguration() {
        List<AnalogInputDevice> configuredChannels = new ArrayList<>();
        configuredChannels.add(mockedAnalogInputDevice);

        Whitebox.setInternalState(testHandler, "configuredChannels", configuredChannels);

        testHandler.loadDefaultConfiguration();

        assertTrue(configuredChannels.isEmpty());
    }

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(Integer.toString(TEST_ADC_CHANNEL_1_INDEX), DEVICE_TYPE_PROPERTY_NAME + ":" + ADC_CHANNEL_DEVICE_TYPE + "," +
                NAME_PROPERTY_NAME + ":" + TEST_ADC_CHANNEL_1_NAME + "," + LOW_MODE_PROPERTY_NAME  + ":" +
                TEST_ADC_CHANNEL_1_LOW_MODE + "," + PATH_PROPERTY_NAME + ":" + TEST_ADC_CHANNEL_1_PATH + "," +
                DEVICE_PROPERTY_NAME + ":" + TEST_ADC_CHANNEL_1_DEVICE_TYPE);
        props.put(Integer.toString(TEST_ADC_CHANNEL_2_INDEX), DEVICE_TYPE_PROPERTY_NAME + ":" + ADC_CHANNEL_DEVICE_TYPE + "," +
                PATH_PROPERTY_NAME + ":" + TEST_ADC_CHANNEL_2_PATH);

        PowerMockito.mockStatic(AnalogInputDeviceBuilder.class);
        PowerMockito.when(AnalogInputDeviceBuilder.newBuilder()).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedAnalogInputDevice);

        testHandler.loadConfiguration(props);

        PowerMockito.verifyStatic(AnalogInputDeviceBuilder.class, times(2));
        //noinspection ResultOfMethodCallIgnored
        AnalogInputDeviceBuilder.newBuilder();
        verify(mockedBuilder).setChannelIndex(eq(TEST_ADC_CHANNEL_1_INDEX));
        verify(mockedBuilder).setName(eq(TEST_ADC_CHANNEL_1_NAME));
        verify(mockedBuilder).setLowMode(eq(TEST_ADC_CHANNEL_1_LOW_MODE));
        verify(mockedBuilder).setPath(eq(TEST_ADC_CHANNEL_1_PATH));
        verify(mockedBuilder).setDeviceType(eq(DeviceType.FX30));
        verify(mockedBuilder).setChannelIndex(eq(TEST_ADC_CHANNEL_2_INDEX));
        verify(mockedBuilder).setPath(eq(TEST_ADC_CHANNEL_2_PATH));
        List<AdcChannel> configuredChannels = Whitebox.getInternalState(testHandler, "configuredChannels");
        assertNotNull(configuredChannels);
        assertEquals(2, configuredChannels.size());
    }

    @Test
    public void testLoadConfigurationInvalidDevices() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(Integer.toString(TEST_ADC_CHANNEL_1_INDEX), DEVICE_TYPE_PROPERTY_NAME + ": other");
        props.put("invalid", DEVICE_TYPE_PROPERTY_NAME + ":" + ADC_CHANNEL_DEVICE_TYPE + "," +
                PATH_PROPERTY_NAME + ":" + TEST_ADC_CHANNEL_2_PATH);

        testHandler.loadConfiguration(props);

        List<AdcChannel> configuredChannels = Whitebox.getInternalState(testHandler, "configuredChannels");
        assertNotNull(configuredChannels);
        assertTrue(configuredChannels.isEmpty());
    }

    @Test
    public void testApplyConfiguration() {
        AdcChannel mockedChannel1 = mock(AdcChannel.class);
        AdcChannel mockedChannel2 = mock(AdcChannel.class);
        List<AdcChannel> configuredChannels = Arrays.asList(mockedChannel1, mockedChannel2);

        Whitebox.setInternalState(testHandler, "configuredChannels", configuredChannels);

        testHandler.applyConfiguration();

        verify(mockedService).loadConfiguration(eq(configuredChannels));
    }
}