package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.event.api.EventDispatcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GpioDatastreamsRegistry.class, GpioDatastreamsFactory.class })
public class GpioDatastreamsRegistryTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_PIN_INDEX = 1;

    @Mock
    private BundleContext mockedContext;
    @InjectMocks
    private GpioDatastreamsRegistry testRegistry;

    @Mock
    private GpioDatastreamsGetter mockedDatastreamsGetter;
    @Mock
    private GpioDatastreamsSetter mockedDatastreamsSetter;
    @Mock
    private GpioDatastreamsEvent mockedDatastreamsEvent;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedDatastreamGetterRegistration;
    @Mock
    private ServiceRegistration<DatastreamsSetter> mockedDatastreamSetterRegistration;
    @Spy
    private Map<String, GpioDatastreamsEvent> spiedDatastreamsEvents = new HashMap<>();
    @Spy
    private List<ServiceRegistration<?>> spiedDatastreamsRegistrations = new ArrayList<>();


    @Before
    public void setUp() {
        Whitebox.setInternalState(testRegistry, "datastreamsServiceRegistrations", spiedDatastreamsRegistrations);
        Whitebox.setInternalState(testRegistry, "datastreamsEvents", spiedDatastreamsEvents);
    }

    @Test
    public void testAddDatastreamGetter() {
        PowerMockito.mockStatic(GpioDatastreamsFactory.class);
        PowerMockito.when(GpioDatastreamsFactory.createGpioDatastreamsGetter(anyString(), anyInt(),
                any(GpioService.class), any(Executor.class))).thenReturn(mockedDatastreamsGetter);
        when(mockedContext.registerService(eq(DatastreamsGetter.class), any(DatastreamsGetter.class), any()))
                .thenReturn(mockedDatastreamGetterRegistration);

        testRegistry.addDatastreamGetter(TEST_PIN_INDEX, TEST_DATASTREAM_ID);

        verify(mockedContext).registerService(eq(DatastreamsGetter.class), eq(mockedDatastreamsGetter), any());
        verify(spiedDatastreamsRegistrations).add(eq(mockedDatastreamGetterRegistration));
    }

    @Test
    public void testAddDatastreamSetter() {
        PowerMockito.mockStatic(GpioDatastreamsFactory.class);
        PowerMockito.when(GpioDatastreamsFactory.createGpioDatastreamsSetter(anyString(), anyInt(),
                any(GpioService.class), any(Executor.class))).thenReturn(mockedDatastreamsSetter);
        when(mockedContext.registerService(eq(DatastreamsSetter.class), any(DatastreamsSetter.class), any()))
                .thenReturn(mockedDatastreamSetterRegistration);

        testRegistry.addDatastreamSetter(TEST_PIN_INDEX, TEST_DATASTREAM_ID);

        verify(mockedContext).registerService(eq(DatastreamsSetter.class), eq(mockedDatastreamsSetter), any());
        verify(spiedDatastreamsRegistrations).add(eq(mockedDatastreamSetterRegistration));
    }

    @Test
    public void testAddDatastreamEvent() {
        PowerMockito.mockStatic(GpioDatastreamsFactory.class);
        PowerMockito.when(GpioDatastreamsFactory.createGpioDatastreamsEvent(anyString(), anyInt(),
                any(GpioService.class), any(EventDispatcher.class))).thenReturn(mockedDatastreamsEvent);

        testRegistry.addDatastreamEvent(TEST_PIN_INDEX, TEST_DATASTREAM_ID);

        verify(mockedDatastreamsEvent).registerToEventSource();
        verify(spiedDatastreamsEvents).put(eq(TEST_DATASTREAM_ID), eq(mockedDatastreamsEvent));
    }


    @Test
    public void testAddDatastreamEventAlreadyRegisteredDatastreamId() {
        GpioDatastreamsEvent oldMockedDatastreamsEvent = mock(GpioDatastreamsEvent.class);
        spiedDatastreamsEvents.put(TEST_DATASTREAM_ID, oldMockedDatastreamsEvent);

        PowerMockito.mockStatic(GpioDatastreamsFactory.class);
        PowerMockito.when(GpioDatastreamsFactory.createGpioDatastreamsEvent(anyString(), anyInt(),
                any(GpioService.class), any(EventDispatcher.class))).thenReturn(mockedDatastreamsEvent);

        testRegistry.addDatastreamEvent(TEST_PIN_INDEX, TEST_DATASTREAM_ID);

        verify(oldMockedDatastreamsEvent).unregisterFromEventSource();
        verify(mockedDatastreamsEvent).registerToEventSource();
        verify(spiedDatastreamsEvents).put(eq(TEST_DATASTREAM_ID), eq(mockedDatastreamsEvent));
        assertNotEquals(oldMockedDatastreamsEvent, spiedDatastreamsEvents.get(TEST_DATASTREAM_ID));
        assertEquals(mockedDatastreamsEvent, spiedDatastreamsEvents.get(TEST_DATASTREAM_ID));
    }

    @Test
    public void testClear() {
        ServiceRegistration<?> mockedRegistration1 = mock(ServiceRegistration.class);
        ServiceRegistration<?> mockedRegistration2 = mock(ServiceRegistration.class);
        spiedDatastreamsRegistrations.add(mockedRegistration1);
        spiedDatastreamsRegistrations.add(mockedRegistration2);
        GpioDatastreamsEvent mockedDatastreamsEvent1 = mock(GpioDatastreamsEvent.class);
        GpioDatastreamsEvent mockedDatastreamsEvent2 = mock(GpioDatastreamsEvent.class);
        spiedDatastreamsEvents.put("datastream1", mockedDatastreamsEvent1);
        spiedDatastreamsEvents.put("datastream2", mockedDatastreamsEvent2);

        testRegistry.close();

        verify(mockedRegistration1).unregister();
        verify(mockedRegistration2).unregister();
        verify(spiedDatastreamsRegistrations).clear();
        verify(mockedDatastreamsEvent1).unregisterFromEventSource();
        verify(mockedDatastreamsEvent2).unregisterFromEventSource();
        verify(spiedDatastreamsEvents).clear();
    }
}