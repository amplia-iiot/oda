package es.amplia.oda.datastreams.gpio.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.gpio.GpioDirection;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.gpio.GpioTrigger;
import es.amplia.oda.datastreams.gpio.GpioDatastreamsManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatastreamsGpioConfigurationHandlerTest {

    private static final String CURRENT_CONFIGURATION_FIELD_NAME = "currentConfiguration";


    @Mock
    private GpioDatastreamsManager mockedManager;
    @Mock
    private ServiceTracker<GpioService, GpioService> mockedGpioServiceTracker;
    @InjectMocks
    private DatastreamsGpioConfigurationHandler testConfigHandler;

    @Spy
    private Map<Integer, GpioPinDatastreamConfiguration> spiedCurrentConfiguration = new HashMap<>();
    @Mock
    private GpioService mockedGpioService;


    @Test
    public void testConstructor() {
        assertNotNull(testConfigHandler);
    }

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("1","datastreamId:testId1, getter: true");
        properties.put("2","datastreamId:testId2, getter: true, setter:true, event:true");
        properties.put("3","datastreamId:testId3, getter: true");
        properties.put("4","datastreamId:testId4, getter: true, setter:true");

        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, spiedCurrentConfiguration);

        testConfigHandler.loadConfiguration(properties);

        verify(spiedCurrentConfiguration).clear();
        verify(spiedCurrentConfiguration, times(4))
                .put(anyInt(), any(GpioPinDatastreamConfiguration.class));
        assertTrue(spiedCurrentConfiguration.containsKey(1));
        assertEquals("testId1", spiedCurrentConfiguration.get(1).getDatastreamId());
        assertEquals(1, spiedCurrentConfiguration.get(1).getPinIndex());
        assertTrue(spiedCurrentConfiguration.get(1).isGetter());
        assertFalse(spiedCurrentConfiguration.get(1).isSetter());
        assertFalse(spiedCurrentConfiguration.get(1).isEvent());
        assertTrue(spiedCurrentConfiguration.containsKey(2));
        assertEquals("testId2", spiedCurrentConfiguration.get(2).getDatastreamId());
        assertEquals(2, spiedCurrentConfiguration.get(2).getPinIndex());
        assertTrue(spiedCurrentConfiguration.get(2).isGetter());
        assertTrue(spiedCurrentConfiguration.get(2).isSetter());
        assertTrue(spiedCurrentConfiguration.get(2).isEvent());
        assertTrue(spiedCurrentConfiguration.containsKey(3));
        assertEquals("testId3", spiedCurrentConfiguration.get(3).getDatastreamId());
        assertEquals(3, spiedCurrentConfiguration.get(3).getPinIndex());
        assertTrue(spiedCurrentConfiguration.get(3).isGetter());
        assertFalse(spiedCurrentConfiguration.get(3).isSetter());
        assertFalse(spiedCurrentConfiguration.get(3).isEvent());
        assertTrue(spiedCurrentConfiguration.containsKey(4));
        assertEquals("testId4", spiedCurrentConfiguration.get(4).getDatastreamId());
        assertEquals(4, spiedCurrentConfiguration.get(4).getPinIndex());
        assertTrue(spiedCurrentConfiguration.get(4).isGetter());
        assertTrue(spiedCurrentConfiguration.get(4).isSetter());
        assertFalse(spiedCurrentConfiguration.get(4).isEvent());
    }

    @Test
    public void testLoadConfigurationNumberFormatExceptionCaught() {
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("wrongKey","datastreamId:testId1, getter: true");

        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, spiedCurrentConfiguration);

        testConfigHandler.loadConfiguration(properties);

        verify(spiedCurrentConfiguration).clear();

    }

    @Test (expected = ConfigurationException.class)
    public void testLoadConfigurationNoDatastreamIdRequiredProperty() {
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("1","getter: true");
        properties.put("2","datastreamId:testId2, getter: true, setter:true, event:true");

        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, spiedCurrentConfiguration);

        testConfigHandler.loadConfiguration(properties);

        fail("Configuration exception must be thrown");
    }

    @Test
    public void testLoadDefaultConfiguration() {
        Map<Integer, GpioPin> availablePins = new HashMap<>();
        GpioPin mockedPin1 =  mock(GpioPin.class);
        GpioPin mockedPin2 =  mock(GpioPin.class);
        GpioPin mockedPin3 =  mock(GpioPin.class);
        GpioPin mockedPin4 =  mock(GpioPin.class);
        availablePins.put(1, mockedPin1);
        availablePins.put(2, mockedPin2);
        availablePins.put(3, mockedPin3);
        availablePins.put(4, mockedPin4);

        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, spiedCurrentConfiguration);

        when(mockedGpioServiceTracker.getService()).thenReturn(mockedGpioService);
        when(mockedGpioService.getAvailablePins()).thenReturn(availablePins);
        when(mockedPin1.getName()).thenReturn("GPIO1");
        when(mockedPin1.getDirection()).thenReturn(GpioDirection.OUTPUT);
        when(mockedPin2.getName()).thenReturn("GPIO2");
        when(mockedPin2.getDirection()).thenReturn(GpioDirection.INPUT);
        when(mockedPin2.getTrigger()).thenReturn(GpioTrigger.RISING_EDGE);
        when(mockedPin3.getName()).thenReturn("GPIO3");
        when(mockedPin3.getDirection()).thenReturn(GpioDirection.OUTPUT);
        when(mockedPin4.getName()).thenReturn("GPIO4");
        when(mockedPin4.getDirection()).thenReturn(GpioDirection.INPUT);
        when(mockedPin4.getTrigger()).thenReturn(GpioTrigger.NONE);

        testConfigHandler.loadDefaultConfiguration();

        verify(spiedCurrentConfiguration).clear();
        verify(spiedCurrentConfiguration, times(4))
                .put(anyInt(), any(GpioPinDatastreamConfiguration.class));
        assertTrue(spiedCurrentConfiguration.containsKey(1));
        assertEquals("GPIO1", spiedCurrentConfiguration.get(1).getDatastreamId());
        assertEquals(1, spiedCurrentConfiguration.get(1).getPinIndex());
        assertTrue(spiedCurrentConfiguration.get(1).isGetter());
        assertTrue(spiedCurrentConfiguration.get(1).isSetter());
        assertFalse(spiedCurrentConfiguration.get(1).isEvent());
        assertTrue(spiedCurrentConfiguration.containsKey(2));
        assertEquals("GPIO2", spiedCurrentConfiguration.get(2).getDatastreamId());
        assertEquals(2, spiedCurrentConfiguration.get(2).getPinIndex());
        assertTrue(spiedCurrentConfiguration.get(2).isGetter());
        assertFalse(spiedCurrentConfiguration.get(2).isSetter());
        assertTrue(spiedCurrentConfiguration.get(2).isEvent());
        assertTrue(spiedCurrentConfiguration.containsKey(3));
        assertEquals("GPIO3", spiedCurrentConfiguration.get(3).getDatastreamId());
        assertEquals(3, spiedCurrentConfiguration.get(3).getPinIndex());
        assertTrue(spiedCurrentConfiguration.get(3).isGetter());
        assertTrue(spiedCurrentConfiguration.get(3).isSetter());
        assertFalse(spiedCurrentConfiguration.get(3).isEvent());
        assertTrue(spiedCurrentConfiguration.containsKey(4));
        assertEquals("GPIO4", spiedCurrentConfiguration.get(4).getDatastreamId());
        assertEquals(4, spiedCurrentConfiguration.get(4).getPinIndex());
        assertTrue(spiedCurrentConfiguration.get(4).isGetter());
        assertFalse(spiedCurrentConfiguration.get(4).isSetter());
        assertFalse(spiedCurrentConfiguration.get(4).isEvent());
    }

    @Test
    public void testApplyConfiguration() {
        GpioPinDatastreamConfiguration conf1 =
                GpioPinDatastreamConfiguration.builder().datastreamId("").getter(true).build();
        GpioPinDatastreamConfiguration conf2 =
                GpioPinDatastreamConfiguration.builder().datastreamId("").getter(true).setter(true).event(true).build();
        GpioPinDatastreamConfiguration conf3 =
                GpioPinDatastreamConfiguration.builder().datastreamId("").getter(true).build();
        GpioPinDatastreamConfiguration conf4 =
                GpioPinDatastreamConfiguration.builder().datastreamId("").getter(true).setter(true).build();

        spiedCurrentConfiguration.put(1, conf1);
        spiedCurrentConfiguration.put(2, conf2);
        spiedCurrentConfiguration.put(3, conf3);
        spiedCurrentConfiguration.put(4, conf4);
        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, spiedCurrentConfiguration);

        testConfigHandler.applyConfiguration();

        verify(mockedManager).close();
        verify(mockedManager, times(4)).addDatastreamGetter(anyInt(), anyString());
        verify(mockedManager, times(2)).addDatastreamSetter(anyInt(), anyString());
        verify(mockedManager, times(1)).addDatastreamEvent(anyInt(), anyString());
    }

    @Test
    public void testApplyConfigurationEmpty() {
        testConfigHandler.applyConfiguration();

        verify(mockedManager).close();
    }
}