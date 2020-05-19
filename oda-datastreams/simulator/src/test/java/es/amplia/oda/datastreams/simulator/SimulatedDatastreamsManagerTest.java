package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.simulator.configuration.ConstantDatastreamConfiguration;
import es.amplia.oda.datastreams.simulator.configuration.RandomDatastreamConfiguration;
import es.amplia.oda.datastreams.simulator.configuration.SimulatedDatastreamsConfiguration;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsGetterFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SimulatedDatastreamsManagerTest {

    private static final String TEST_DATASTREAM_ID_1 = "testDatastream";
    private static final String TEST_DEVICE_ID_1 = "testDevice";
    private static final String TEST_VALUE = "Hello!";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream";
    private static final String TEST_DEVICE_ID_2 = "testDevice";
    private static final double TEST_MIN_VALUE = 10.0;
    private static final double TEST_MAX_VALUE = 50.0;
    private static final double TEST_MAX_DIFF = 25.0;
    private static final ConstantDatastreamConfiguration TEST_CONSTANT_DATASTREAM_CONFIGURATION =
            new ConstantDatastreamConfiguration(TEST_DATASTREAM_ID_1, TEST_DEVICE_ID_1, TEST_VALUE);
    private static final RandomDatastreamConfiguration TEST_RANDOM_DATASTREAM_CONFIGURATION =
            new RandomDatastreamConfiguration(TEST_DATASTREAM_ID_2, TEST_DEVICE_ID_2, TEST_MIN_VALUE, TEST_MAX_VALUE,
                    TEST_MAX_DIFF);
    private static final List<SimulatedDatastreamsConfiguration> TEST_CONFIGURATION =
            Arrays.asList(TEST_CONSTANT_DATASTREAM_CONFIGURATION, TEST_RANDOM_DATASTREAM_CONFIGURATION);


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
        when(mockedFactory.createConstantDatastreamsGetter(anyString(), anyString(), any()))
                .thenReturn(mockedGetter);
        when(mockedFactory.createRandomDatastreamsGetter(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(mockedGetter);

        testDatastreamsManager.loadConfiguration(TEST_CONFIGURATION);

        verify(mockedFactory).createConstantDatastreamsGetter(eq(TEST_DATASTREAM_ID_1), eq(TEST_DEVICE_ID_1),
                eq(TEST_VALUE));
        verify(mockedFactory).createRandomDatastreamsGetter(eq(TEST_DATASTREAM_ID_2), eq(TEST_DEVICE_ID_2),
                eq(TEST_MIN_VALUE), eq(TEST_MAX_VALUE), eq(TEST_MAX_DIFF));
        verify(mockedRegistrationManager, times(2)).register(eq(mockedGetter));
    }

    @Test
    public void testClose() {
        testDatastreamsManager.close();

        verify(mockedRegistrationManager).unregister();
    }
}