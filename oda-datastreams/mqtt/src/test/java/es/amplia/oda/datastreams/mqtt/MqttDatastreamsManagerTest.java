package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerWithKey;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MqttDatastreamsManagerTest {

    private static final String TEST_DEVICE_ID_1 = "testDevice1";
    private static final String TEST_DEVICE_ID_2 = "testDevice2";
    private static final String TEST_DATASTREAM_ID_1 = "testDatastream1";

    @Mock
    private ServiceRegistrationManagerWithKey<String, DatastreamsGetter> mockedGetterRegistrationManager;
    @Mock
    private ServiceRegistrationManagerWithKey<String, DatastreamsSetter> mockedSetterRegistrationManager;
    @Mock
    private MqttDatastreamsFactory mockedDatastreamsFactory;

    private MqttDatastreamsManager testManager;

    @Mock
    private MqttDatastreamsGetter mockedGetter;
    @Mock
    private MqttDatastreamsSetter mockedSetter;


    @Before
    public void setUp() {
        testManager = new MqttDatastreamsManager(mockedGetterRegistrationManager, mockedSetterRegistrationManager,
                mockedDatastreamsFactory);
    }

    @Test
    public void createDatastreamWithDatastreamIdThatNotExists() throws MqttException {
        when(mockedDatastreamsFactory.createDatastreamGetter(anyString())).thenReturn(mockedGetter);
        when(mockedDatastreamsFactory.createDatastreamSetter(anyString())).thenReturn(mockedSetter);

        testManager.createDatastream(TEST_DEVICE_ID_1, TEST_DATASTREAM_ID_1);

        verify(mockedDatastreamsFactory).createDatastreamGetter(eq(TEST_DATASTREAM_ID_1));
        verify(mockedGetter).addManagedDevice(eq(TEST_DEVICE_ID_1));
        verify(mockedGetterRegistrationManager).register(eq(TEST_DATASTREAM_ID_1), eq(mockedGetter));
        verify(mockedDatastreamsFactory).createDatastreamSetter(eq(TEST_DATASTREAM_ID_1));
        verify(mockedSetter).addManagedDevice(eq(TEST_DEVICE_ID_1));
        verify(mockedSetterRegistrationManager).register(eq(TEST_DATASTREAM_ID_1), eq(mockedSetter));
    }

    @Test
    public void createDatastreamWithDatastreamIdThatAlreadyExists() throws MqttException {
        Map<String, MqttDatastreamsGetter> datastreamsGetters = getMqttDatastreamsGetters();
        Map<String, MqttDatastreamsSetter> datastreamsSetters = getMqttDatastreamsSetters();

        datastreamsGetters.put(TEST_DATASTREAM_ID_1, mockedGetter);
        datastreamsSetters.put(TEST_DATASTREAM_ID_1, mockedSetter);

        testManager.createDatastream(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_1);

        verifyZeroInteractions(mockedDatastreamsFactory);
        verifyZeroInteractions(mockedGetterRegistrationManager);
        verify(mockedGetter).addManagedDevice(TEST_DEVICE_ID_2);
        verifyZeroInteractions(mockedGetterRegistrationManager);
        verify(mockedGetter).addManagedDevice(TEST_DEVICE_ID_2);
    }

    @SuppressWarnings("unchecked")
    private Map<String, MqttDatastreamsGetter> getMqttDatastreamsGetters() {
        return (Map<String, MqttDatastreamsGetter>)
                Whitebox.getInternalState(testManager, "mqttDatastreamsGetters");
    }

    @SuppressWarnings("unchecked")
    private Map<String, MqttDatastreamsSetter> getMqttDatastreamsSetters() {
        return (Map<String, MqttDatastreamsSetter>)
                Whitebox.getInternalState(testManager, "mqttDatastreamsSetters");
    }

    @Test
    public void removeDatastreamNoMoreDevicesManaged() {
        Map<String, MqttDatastreamsGetter> datastreamsGetters = getMqttDatastreamsGetters();
        Map<String, MqttDatastreamsSetter> datastreamsSetters = getMqttDatastreamsSetters();

        datastreamsGetters.put(TEST_DATASTREAM_ID_1, mockedGetter);
        datastreamsSetters.put(TEST_DATASTREAM_ID_1, mockedSetter);

        when(mockedGetter.getDevicesIdManaged()).thenReturn(Collections.emptyList());
        when(mockedSetter.getDevicesIdManaged()).thenReturn(Collections.emptyList());

        testManager.removeDatastream(TEST_DEVICE_ID_1, TEST_DATASTREAM_ID_1);

        verify(mockedGetter).removeManagedDevice(eq(TEST_DEVICE_ID_1));
        verify(mockedSetter).removeManagedDevice(eq(TEST_DEVICE_ID_1));
        assertFalse(datastreamsGetters.containsKey(TEST_DATASTREAM_ID_1));
        assertFalse(datastreamsSetters.containsKey(TEST_DATASTREAM_ID_1));
    }

    @Test
    public void removeDatastreamMoreDevicesManaged() {
        Map<String, MqttDatastreamsGetter> datastreamsGetters = getMqttDatastreamsGetters();
        Map<String, MqttDatastreamsSetter> datastreamsSetters = getMqttDatastreamsSetters();

        datastreamsGetters.put(TEST_DATASTREAM_ID_1, mockedGetter);
        datastreamsSetters.put(TEST_DATASTREAM_ID_1, mockedSetter);

        when(mockedGetter.getDevicesIdManaged()).thenReturn(Collections.singletonList(TEST_DEVICE_ID_2));
        when(mockedSetter.getDevicesIdManaged()).thenReturn(Collections.singletonList(TEST_DEVICE_ID_2));

        testManager.removeDatastream(TEST_DEVICE_ID_1, TEST_DATASTREAM_ID_1);

        verify(mockedGetter).removeManagedDevice(eq(TEST_DEVICE_ID_1));
        verify(mockedSetter).removeManagedDevice(eq(TEST_DEVICE_ID_1));
    }

    @Test
    public void testRemoveDevice() {
        Map<String, MqttDatastreamsGetter> datastreamsGetters = getMqttDatastreamsGetters();
        Map<String, MqttDatastreamsSetter> datastreamsSetters = getMqttDatastreamsSetters();

        datastreamsGetters.put(TEST_DATASTREAM_ID_1, mockedGetter);
        datastreamsSetters.put(TEST_DATASTREAM_ID_1, mockedSetter);

        when(mockedGetter.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(mockedGetter.getDevicesIdManaged()).thenReturn(Arrays.asList(TEST_DEVICE_ID_1, TEST_DEVICE_ID_2))
                .thenReturn(Collections.singletonList(TEST_DEVICE_ID_2));
        when(mockedSetter.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(mockedSetter.getDevicesIdManaged()).thenReturn(Arrays.asList(TEST_DEVICE_ID_1, TEST_DEVICE_ID_2))
                .thenReturn(Collections.singletonList(TEST_DEVICE_ID_2));

        testManager.removeDevice(TEST_DEVICE_ID_1);

        verify(mockedGetter).removeManagedDevice(eq(TEST_DEVICE_ID_1));
        verify(mockedSetter).removeManagedDevice(eq(TEST_DEVICE_ID_1));
    }

    @Test
    public void close() {
        Map<String, MqttDatastreamsGetter> datastreamsGetters = getMqttDatastreamsGetters();
        Map<String, MqttDatastreamsSetter> datastreamsSetters = getMqttDatastreamsSetters();

        datastreamsGetters.put(TEST_DATASTREAM_ID_1, mockedGetter);
        datastreamsSetters.put(TEST_DATASTREAM_ID_1, mockedSetter);

        testManager.close();

        verify(mockedGetterRegistrationManager).unregisterAll();
        verify(mockedSetterRegistrationManager).unregisterAll();
        verify(mockedGetter).close();
        assertTrue(datastreamsGetters.isEmpty());
        verify(mockedSetter).close();
        assertTrue(datastreamsSetters.isEmpty());
    }
}