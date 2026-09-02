package es.amplia.oda.hardware.jdkdio.configuration;

import es.amplia.oda.core.commons.utils.Collections;

import es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class JDkDioGpioConfigurationHandlerTest {

    private static final String GPIO_PINS_CONFIGURATION_FIELD_NAME = "gpioPinsConfiguration";

    @Mock
    private JdkDioGpioService mockedService;
    @InjectMocks
    private JdkDioConfigurationHandler testConfigHandler;

    @After
    public void tearDown() {
        System.clearProperty(JdkDioConfigurationHandler.JDK_DIO_REGISTRY_PROPERTY);
    }

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> dictionary = new Hashtable<>();
        Map<String, String> map = new Hashtable<>();

        try (MockedStatic<Collections> mockedCollections = mockStatic(Collections.class)) {
            mockedCollections.when(() -> Collections.dictionaryToMap(any(Dictionary.class))).thenReturn(map);

            testConfigHandler.loadConfiguration(dictionary);

            mockedCollections.verify(() -> Collections.dictionaryToMap(eq(dictionary)));
            assertEquals(map, Whitebox.getInternalState(testConfigHandler, GPIO_PINS_CONFIGURATION_FIELD_NAME));
        }
    }

    @Test
    public void testLoadDefaultConfiguration() throws Exception {
        File testFile = File.createTempFile("jdkDioTestRegistry", ".properties");
        testFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write("testKey=testValue\n".getBytes());
        }
        Map<String, String> map = new Hashtable<>();

        try (MockedStatic<Collections> mockedCollections = mockStatic(Collections.class)) {
            System.setProperty(JdkDioConfigurationHandler.JDK_DIO_REGISTRY_PROPERTY, testFile.getAbsolutePath());

            mockedCollections.when(() -> Collections.propertiesToMap(any(Properties.class))).thenReturn(map);

            testConfigHandler.loadDefaultConfiguration();

            mockedCollections.verify(() -> Collections.propertiesToMap(any(Properties.class)));
            assertEquals(map, Whitebox.getInternalState(testConfigHandler, GPIO_PINS_CONFIGURATION_FIELD_NAME));
        }
    }

    @Test
    public void testLoadDefaultConfigurationNoDefaultFile() throws Exception {
        System.clearProperty(JdkDioConfigurationHandler.JDK_DIO_REGISTRY_PROPERTY);

        testConfigHandler.loadDefaultConfiguration();

        assertNull(Whitebox.getInternalState(testConfigHandler, GPIO_PINS_CONFIGURATION_FIELD_NAME));
    }

    @Test
    public void testLoadDefaultConfigurationFileNotFound() throws Exception {
        String testPath = "testPath";

        System.setProperty(JdkDioConfigurationHandler.JDK_DIO_REGISTRY_PROPERTY, testPath);

        testConfigHandler.loadDefaultConfiguration();

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
