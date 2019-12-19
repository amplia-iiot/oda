package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatastreamsGettersFinderImplTest {

    private static final String TEST_DATASTREAM_ID_1 = "testDatastream1";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final String TEST_DATASTREAM_ID_3 = "testDatastream3";
    private static final String OTHER_DATASTREAM_ID_1 = "otherDatastream1";
    private static final String OTHER_DATASTREAM_ID_2 = "otherDatastream2";
    private static final String OTHER_DATASTREAM_ID_3 = "otherDatastream3";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String OTHER_DEVICE_ID = "otherDevice";

    @Mock
    private ServiceLocator<DatastreamsGetter> mockedLocator;
    @InjectMocks
    private DatastreamsGettersFinderImpl testFinder;

    @Mock
    private DatastreamsGetter mockedGetter1;
    @Mock
    private DatastreamsGetter mockedGetter2;
    @Mock
    private DatastreamsGetter mockedGetter3;
    @Mock
    private DatastreamsGetter mockedGetter4;
    @Mock
    private DatastreamsGetter mockedGetter5;
    @Mock
    private DatastreamsGetter mockedGetter6;

    @Test
    public void testGetGettersSatisfying() {
        when(mockedLocator.findAll()).thenReturn(
                Arrays.asList(mockedGetter1, mockedGetter2, mockedGetter3, mockedGetter4, mockedGetter5, mockedGetter6));
        when(mockedGetter1.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(mockedGetter1.getDevicesIdManaged()).thenReturn(Arrays.asList(TEST_DEVICE_ID, OTHER_DEVICE_ID));
        when(mockedGetter2.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_2);
        when(mockedGetter2.getDevicesIdManaged()).thenReturn(Collections.singletonList(TEST_DEVICE_ID));
        when(mockedGetter3.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_3);
        when(mockedGetter3.getDevicesIdManaged()).thenReturn(Collections.singletonList(OTHER_DEVICE_ID));
        when(mockedGetter4.getDatastreamIdSatisfied()).thenReturn(OTHER_DATASTREAM_ID_1);
        when(mockedGetter4.getDevicesIdManaged()).thenReturn(Arrays.asList(TEST_DEVICE_ID, OTHER_DEVICE_ID));
        when(mockedGetter5.getDatastreamIdSatisfied()).thenReturn(OTHER_DATASTREAM_ID_2);
        when(mockedGetter5.getDevicesIdManaged()).thenReturn(Collections.singletonList(TEST_DEVICE_ID));
        when(mockedGetter6.getDatastreamIdSatisfied()).thenReturn(OTHER_DATASTREAM_ID_3);
        when(mockedGetter6.getDevicesIdManaged()).thenReturn(Collections.singletonList(OTHER_DEVICE_ID));

        DatastreamsGettersFinder.Return result =
                testFinder.getGettersSatisfying(new DevicePattern(TEST_DEVICE_ID),
                        new HashSet<>(Arrays.asList(TEST_DATASTREAM_ID_1, TEST_DATASTREAM_ID_2, TEST_DATASTREAM_ID_3)));

        assertNotNull(result);
        assertEquals(Arrays.asList(mockedGetter1, mockedGetter2), result.getGetters());
        assertEquals(Collections.singleton(TEST_DATASTREAM_ID_3), result.getNotFoundIds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetGettersSatisfyingNullDevicePattern() {
        testFinder.getGettersSatisfying(null, new HashSet<>());
    }

    @Test
    public void testGetGettersSatisfyingThrowsRuntimeException() {
        when(mockedLocator.findAll()).thenThrow(new RuntimeException());

        DatastreamsGettersFinder.Return result =
                testFinder.getGettersSatisfying(new DevicePattern(TEST_DEVICE_ID),
                        new HashSet<>(Arrays.asList(TEST_DATASTREAM_ID_1, TEST_DATASTREAM_ID_2, TEST_DATASTREAM_ID_3)));

        assertEquals(Collections.emptyList(), result.getGetters());
        assertEquals(new HashSet<>(Arrays.asList(TEST_DATASTREAM_ID_1, TEST_DATASTREAM_ID_2, TEST_DATASTREAM_ID_3)),
                result.getNotFoundIds());

    }

    @Test
    public void testGetGettersOfDevice() {
        when(mockedLocator.findAll()).thenReturn(Arrays.asList(mockedGetter1, mockedGetter2, mockedGetter3));
        when(mockedGetter1.getDevicesIdManaged()).thenReturn(Arrays.asList(TEST_DEVICE_ID, OTHER_DEVICE_ID));
        when(mockedGetter2.getDevicesIdManaged()).thenReturn(Collections.singletonList(TEST_DEVICE_ID));
        when(mockedGetter3.getDevicesIdManaged()).thenReturn(Collections.singletonList(OTHER_DEVICE_ID));

        List<DatastreamsGetter> result = testFinder.getGettersOfDevice(TEST_DEVICE_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Arrays.asList(mockedGetter1, mockedGetter2), result);
    }

    @Test
    public void testClose() {
        testFinder.close();

        verify(mockedLocator).close();
    }
}