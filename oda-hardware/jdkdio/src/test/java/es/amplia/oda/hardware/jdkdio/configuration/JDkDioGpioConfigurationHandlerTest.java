package es.amplia.oda.hardware.jdkdio.configuration;

import es.amplia.oda.core.commons.utils.Collections;

import es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JdkDioConfigurationHandler.class, Collections.class, JdkDioGpioPinBuilder.class})
public class JDkDioGpioConfigurationHandlerTest {

    private static final String GPIO_PINS_CONFIGURATION_FIELD_NAME = "gpioPinsConfiguration";

    @Mock
    private JdkDioGpioService mockedService;
    @InjectMocks
    private JdkDioConfigurationHandler testConfigHandler;

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> dictionary = new Hashtable<>();
        Dictionary<String, String> dictionaryParam = Mockito.any();
        Map<String, String> map = new Hashtable<>();

        PowerMockito.mockStatic(Collections.class);
        PowerMockito.when(Collections.dictionaryToMap(dictionaryParam)).thenReturn(map);

        testConfigHandler.loadConfiguration(dictionary);

        PowerMockito.verifyStatic(Collections.class);
        Collections.dictionaryToMap(eq(dictionary));
        assertEquals(map, Whitebox.getInternalState(testConfigHandler, GPIO_PINS_CONFIGURATION_FIELD_NAME));
    }

    @Test
    public void testLoadDefaultConfiguration() throws Exception {
        String testPath = "testPath";
        Properties mockedProperties = mock(Properties.class);
        FileInputStream mockedFis = mock(FileInputStream.class);
        Map<String, String> map = new Hashtable<>();

        PowerMockito.mockStatic(Collections.class);

        System.setProperty(JdkDioConfigurationHandler.JDK_DIO_REGISTRY_PROPERTY, testPath);

        PowerMockito.whenNew(Properties.class).withNoArguments().thenReturn(mockedProperties);
        PowerMockito.whenNew(FileInputStream.class).withArguments(anyString()).thenReturn(mockedFis);
        PowerMockito.when(Collections.propertiesToMap(any(Properties.class))).thenReturn(map);

        testConfigHandler.loadDefaultConfiguration();

        PowerMockito.verifyNew(Properties.class).withNoArguments();
        PowerMockito.verifyNew(FileInputStream.class).withArguments(testPath);
        verify(mockedProperties).load(eq(mockedFis));
        PowerMockito.verifyStatic(Collections.class);
        Collections.propertiesToMap(mockedProperties);
        assertEquals(map, Whitebox.getInternalState(testConfigHandler, GPIO_PINS_CONFIGURATION_FIELD_NAME));
    }

    @Test
    public void testLoadDefaultConfigurationNoDefaultFile() throws Exception {
        Map<String, String> map = new Hashtable<>();
        PowerMockito.mockStatic(Collections.class);

        PowerMockito.when(Collections.propertiesToMap(any(Properties.class))).thenReturn(map);

        testConfigHandler.loadDefaultConfiguration();

        assertNull(Whitebox.getInternalState(testConfigHandler, GPIO_PINS_CONFIGURATION_FIELD_NAME));
    }

    @Test
    public void testLoadDefaultConfigurationFileNotFound() throws Exception {
        String testPath = "testPath";
        Properties mockedProperties = mock(Properties.class);

        PowerMockito.mockStatic(Collections.class);

        System.setProperty(JdkDioConfigurationHandler.JDK_DIO_REGISTRY_PROPERTY, testPath);

        PowerMockito.whenNew(Properties.class).withNoArguments().thenReturn(mockedProperties);
        PowerMockito.whenNew(FileInputStream.class).withArguments(anyString()).thenThrow(new FileNotFoundException());

        testConfigHandler.loadDefaultConfiguration();

        PowerMockito.verifyNew(Properties.class).withNoArguments();
        PowerMockito.verifyNew(FileInputStream.class).withArguments(testPath);
        assertNull(Whitebox.getInternalState(testConfigHandler, GPIO_PINS_CONFIGURATION_FIELD_NAME));
    }

    @Test
    public void testApplyConfiguration() {
        /*Map<String, String> map = new Hashtable<>();
        map.put("1", "");
        map.put("2", "deviceType:gpio.GPIOPin");
        map.put("3", "deviceType:gpio.GPIOPin, name:GPIO1");
        map.put("4", "deviceType:gpio.GPIOPin, name:GPIO2, direction:OUTPUT");
        map.put("5", "deviceType:gpio.GPIOPin, name:GPIO3, direction:ERROR");
        map.put("6", "deviceType:gpio.GPIOPin, name:GPIO4, direction:OUTPUT, mode:PUSH_PULL, trigger:NONE,"
                + "activeLow:true, initialValue:true");
        map.put("7", "deviceType:gpio.GPIOPin, name:GPIO5, nonexistent:field, mode=badFormat");
        JdkDioGpioPin mockedPin = mock(JdkDioGpioPin.class);

        JdkDioGpioPinBuilder mockedBuilder = mock(JdkDioGpioPinBuilder.class);
        PowerMockito.mockStatic(JdkDioGpioPinBuilder.class);

        PowerMockito.when(JdkDioGpioPinBuilder.newBuilder()).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedPin);

        Whitebox.setInternalState(testConfigHandler, GPIO_PINS_CONFIGURATION_FIELD_NAME, map);

        testConfigHandler.applyConfiguration();

        verify(mockedService).release();
        verify(mockedService, atLeastOnce()).addConfiguredPin(eq(mockedPin));*/
    }

    @Test
    public void testApplyConfigurationNullConfiguration() {
        /*testConfigHandler.applyConfiguration();

        verify(mockedService).release();*/
    }

}