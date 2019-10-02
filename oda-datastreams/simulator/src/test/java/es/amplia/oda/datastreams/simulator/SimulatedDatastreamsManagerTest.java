package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsGetterFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SimulatedDatastreamsManagerTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final double TEST_MIN_VALUE = 10.0;
    private static final double TEST_MAX_VALUE = 50.0;
    private static final double TEST_MAX_DIFF = 25.0;
    private static final SimulatedDatastreamsConfiguration TEST_CONFIGURATION =
            new SimulatedDatastreamsConfiguration(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_MIN_VALUE, TEST_MAX_VALUE,
                    TEST_MAX_DIFF);


    @Mock
    private SimulatedDatastreamsGetterFactory mockedFactory;
    @Mock
    private ServiceRegistrationManager<DatastreamsGetter> mockedRegistrationManager;
    @InjectMocks
    private SimulatedDatastreamsManager testDatastreamsManager;

    @Mock
    private DatastreamsGetter mockedGetter;

    @Test
    public void testLoadConfiguration() {
        when(mockedFactory.createSimulatedDatastreamsGetter(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(mockedGetter);

        testDatastreamsManager.loadConfiguration(
                Arrays.asList(TEST_CONFIGURATION, TEST_CONFIGURATION, TEST_CONFIGURATION));

        verify(mockedFactory, times(3)).createSimulatedDatastreamsGetter(eq(TEST_DATASTREAM_ID), eq(TEST_DEVICE_ID),
                eq(TEST_MIN_VALUE), eq(TEST_MAX_VALUE), eq(TEST_MAX_DIFF));
        verify(mockedRegistrationManager, times(3)).register(eq(mockedGetter));
    }

    @Test
    public void testClose() {
        testDatastreamsManager.close();

        verify(mockedRegistrationManager).unregister();
    }
}