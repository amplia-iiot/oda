package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GpioDatastreamsManagerTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_PIN_INDEX = 1;

    @Mock
    private GpioDatastreamsFactory mockedFactory;
    @Mock
    private ServiceRegistrationManager<DatastreamsGetter> mockedGetterRegistrationManager;
    @Mock
    private ServiceRegistrationManager<DatastreamsSetter> mockedSetterRegistrationManager;

    private GpioDatastreamsManager testManager;

    @Mock
    private GpioDatastreamsGetter mockedDatastreamsGetter;
    @Mock
    private GpioDatastreamsSetter mockedDatastreamsSetter;
    @Mock
    private GpioDatastreamsEvent mockedDatastreamsEvent;
    @Spy
    private Map<String, GpioDatastreamsEvent> spiedDatastreamsEvents = new HashMap<>();


    @Before
    public void setUp() {
        testManager = new GpioDatastreamsManager(mockedFactory, mockedGetterRegistrationManager,
                mockedSetterRegistrationManager);

        Whitebox.setInternalState(testManager, "datastreamsEvents", spiedDatastreamsEvents);
    }

    @Test
    public void testAddDatastreamGetter() {
        when(mockedFactory.createGpioDatastreamsGetter(anyString(), anyInt())).thenReturn(mockedDatastreamsGetter);

        testManager.addDatastreamGetter(TEST_PIN_INDEX, TEST_DATASTREAM_ID);

        verify(mockedGetterRegistrationManager).register(eq(mockedDatastreamsGetter));
    }

    @Test
    public void testAddDatastreamSetter() {
        when(mockedFactory.createGpioDatastreamsSetter(anyString(), anyInt())).thenReturn(mockedDatastreamsSetter);

        testManager.addDatastreamSetter(TEST_PIN_INDEX, TEST_DATASTREAM_ID);

        verify(mockedSetterRegistrationManager).register(eq(mockedDatastreamsSetter));
    }

    @Test
    public void testAddDatastreamEvent() {
        when(mockedFactory.createGpioDatastreamsEvent(anyString(), anyInt())).thenReturn(mockedDatastreamsEvent);

        testManager.addDatastreamEvent(TEST_PIN_INDEX, TEST_DATASTREAM_ID);

        verify(spiedDatastreamsEvents).put(eq(TEST_DATASTREAM_ID), eq(mockedDatastreamsEvent));
    }


    @Test
    public void testAddDatastreamEventAlreadyRegisteredDatastreamId() {
        GpioDatastreamsEvent oldMockedDatastreamsEvent = mock(GpioDatastreamsEvent.class);
        spiedDatastreamsEvents.put(TEST_DATASTREAM_ID, oldMockedDatastreamsEvent);

        when(mockedFactory.createGpioDatastreamsEvent(anyString(), anyInt())).thenReturn(mockedDatastreamsEvent);

        testManager.addDatastreamEvent(TEST_PIN_INDEX, TEST_DATASTREAM_ID);

        verify(oldMockedDatastreamsEvent).unregisterFromEventSource();
        verify(spiedDatastreamsEvents).put(eq(TEST_DATASTREAM_ID), eq(mockedDatastreamsEvent));
        assertNotEquals(oldMockedDatastreamsEvent, spiedDatastreamsEvents.get(TEST_DATASTREAM_ID));
        assertEquals(mockedDatastreamsEvent, spiedDatastreamsEvents.get(TEST_DATASTREAM_ID));
    }

    @Test
    public void testClear() {
        GpioDatastreamsEvent mockedDatastreamsEvent1 = mock(GpioDatastreamsEvent.class);
        GpioDatastreamsEvent mockedDatastreamsEvent2 = mock(GpioDatastreamsEvent.class);
        spiedDatastreamsEvents.put("datastream1", mockedDatastreamsEvent1);
        spiedDatastreamsEvents.put("datastream2", mockedDatastreamsEvent2);

        testManager.close();

        verify(mockedGetterRegistrationManager).unregister();
        verify(mockedSetterRegistrationManager).unregister();
        verify(mockedDatastreamsEvent1).unregisterFromEventSource();
        verify(mockedDatastreamsEvent2).unregisterFromEventSource();
        verify(spiedDatastreamsEvents).clear();
    }
}