package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinderImpl;
import es.amplia.oda.core.commons.utils.ServiceLocator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DatastreamsSettersFinderImplTest {

    private static final String TEST_DATASTREAM_ID_1 = "testDatastream1";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final String TEST_DATASTREAM_ID_3 = "testDatastream3";
    private static final String OTHER_DATASTREAM_ID_1 = "otherDatastream1";
    private static final String OTHER_DATASTREAM_ID_2 = "otherDatastream2";
    private static final String OTHER_DATASTREAM_ID_3 = "otherDatastream3";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String OTHER_DEVICE_ID = "otherDevice";

    @Mock
    ServiceLocator<DatastreamsSetter> mockedLocator;
    @InjectMocks
    DatastreamsSettersFinderImpl testFinder;

    @Mock
    private DatastreamsSetter mockedSetter1;
    @Mock
    private DatastreamsSetter mockedSetter2;
    @Mock
    private DatastreamsSetter mockedSetter3;
    @Mock
    private DatastreamsSetter mockedSetter4;
    @Mock
    private DatastreamsSetter mockedSetter5;
    @Mock
    private DatastreamsSetter mockedSetter6;


    @Test
    public void testGetSettersSatisfying() {
        Map<String, DatastreamsSetter> expectedSetters = new HashMap<>();
        expectedSetters.put(TEST_DATASTREAM_ID_1, mockedSetter1);
        expectedSetters.put(TEST_DATASTREAM_ID_2, mockedSetter2);

        when(mockedLocator.findAll()).thenReturn(
                Arrays.asList(mockedSetter1, mockedSetter2, mockedSetter3, mockedSetter4, mockedSetter5, mockedSetter6));
        when(mockedSetter1.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(mockedSetter1.getDevicesIdManaged()).thenReturn(Arrays.asList(TEST_DEVICE_ID, OTHER_DEVICE_ID));
        when(mockedSetter2.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_2);
        when(mockedSetter2.getDevicesIdManaged()).thenReturn(java.util.Collections.singletonList(TEST_DEVICE_ID));
        when(mockedSetter3.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_3);
        when(mockedSetter3.getDevicesIdManaged()).thenReturn(java.util.Collections.singletonList(OTHER_DEVICE_ID));
        when(mockedSetter4.getDatastreamIdSatisfied()).thenReturn(OTHER_DATASTREAM_ID_1);
        when(mockedSetter4.getDevicesIdManaged()).thenReturn(Arrays.asList(TEST_DEVICE_ID, OTHER_DEVICE_ID));
        when(mockedSetter5.getDatastreamIdSatisfied()).thenReturn(OTHER_DATASTREAM_ID_2);
        when(mockedSetter5.getDevicesIdManaged()).thenReturn(java.util.Collections.singletonList(TEST_DEVICE_ID));
        when(mockedSetter6.getDatastreamIdSatisfied()).thenReturn(OTHER_DATASTREAM_ID_3);
        when(mockedSetter6.getDevicesIdManaged()).thenReturn(java.util.Collections.singletonList(OTHER_DEVICE_ID));

        DatastreamsSettersFinder.Return result =
                testFinder.getSettersSatisfying(TEST_DEVICE_ID,
                        new HashSet<>(Arrays.asList(TEST_DATASTREAM_ID_1, TEST_DATASTREAM_ID_2, TEST_DATASTREAM_ID_3)));

        assertNotNull(result);
        assertEquals(expectedSetters, result.getSetters());
        Assertions.assertEquals(Collections.singleton(TEST_DATASTREAM_ID_3), result.getNotFoundIds());

    }

    @Test
    public void testGetSettersSatisfyingNullDeviceId() {
        assertThrows(IllegalArgumentException.class, () -> testFinder.getSettersSatisfying(null, new HashSet<>()));
    }

    @Test
    public void testGetSettersSatisfyingThrowsRuntimeException() {
        when(mockedLocator.findAll()).thenThrow(new RuntimeException());

        DatastreamsSettersFinder.Return result =
                testFinder.getSettersSatisfying(TEST_DEVICE_ID,
                        new HashSet<>(Arrays.asList(TEST_DATASTREAM_ID_1, TEST_DATASTREAM_ID_2, TEST_DATASTREAM_ID_3)));

        assertEquals(new HashMap<String, DatastreamsSetter>(), result.getSetters());
        assertEquals(new HashSet<>(Arrays.asList(TEST_DATASTREAM_ID_1, TEST_DATASTREAM_ID_2, TEST_DATASTREAM_ID_3)),
                result.getNotFoundIds());

    }

    @Test
    public void testClose() {
        testFinder.close();

        verify(mockedLocator).close();
    }
}