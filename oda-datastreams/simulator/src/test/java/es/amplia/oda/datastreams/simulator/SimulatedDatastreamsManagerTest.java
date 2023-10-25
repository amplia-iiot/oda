package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.simulator.configuration.ConstantDatastreamGetterConfiguration;
import es.amplia.oda.datastreams.simulator.configuration.RandomDatastreamGetterConfiguration;
import es.amplia.oda.datastreams.simulator.configuration.SetDatastreamSetterConfiguration;
import es.amplia.oda.datastreams.simulator.configuration.SimulatedDatastreamsGetterConfiguration;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsGetterFactory;

import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsSetterFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SimulatedDatastreamsManagerTest {

    private static final String TEST_DATASTREAM_ID_1 = "testDatastream";
    private static final String TEST_DEVICE_ID_1 = "testDevice";
    private static final String TEST_FEED_1 = "testFeed";
    private static final String TEST_VALUE = "Hello!";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream";
    private static final double TEST_MIN_VALUE = 10.0;
    private static final double TEST_MAX_VALUE = 50.0;
    private static final double TEST_MAX_DIFF = 25.0;
    private static final String TEST_DEVICE_ID_2 = "testDevice";
    private static final String TEST_DATASTREAM_ID_3 = "settingDatastream";
    private static final String TEST_DEVICE_ID_3 = "theDevice";
    private static final ConstantDatastreamGetterConfiguration TEST_CONSTANT_DATASTREAM_CONFIGURATION =
            new ConstantDatastreamGetterConfiguration(TEST_DATASTREAM_ID_1, TEST_DEVICE_ID_1, TEST_FEED_1, TEST_VALUE);
    private static final RandomDatastreamGetterConfiguration TEST_RANDOM_DATASTREAM_CONFIGURATION =
            new RandomDatastreamGetterConfiguration(TEST_DATASTREAM_ID_2, TEST_DEVICE_ID_2, null, TEST_MIN_VALUE, TEST_MAX_VALUE,
                    TEST_MAX_DIFF);
    private static final SetDatastreamSetterConfiguration TEST_CONSTANT_DATASTREAM_CONFIGURATION_SETTER =
            new SetDatastreamSetterConfiguration(TEST_DATASTREAM_ID_3, TEST_DEVICE_ID_3);
    private static final List<SimulatedDatastreamsGetterConfiguration> TEST_CONFIGURATION =
            Arrays.asList(TEST_CONSTANT_DATASTREAM_CONFIGURATION, TEST_RANDOM_DATASTREAM_CONFIGURATION);
    private static final List<SetDatastreamSetterConfiguration> TEST_CONFIGURATION_SETTER =
            Collections.singletonList(TEST_CONSTANT_DATASTREAM_CONFIGURATION_SETTER);


    @Mock
    private SimulatedDatastreamsGetterFactory mockedGetterFactory;
    @Mock
    private SimulatedDatastreamsSetterFactory mockedSetterFactory;
    @Mock
    private ServiceRegistrationManager<DatastreamsGetter> mockedRegistrationGetterManager;
    @Mock
    private ServiceRegistrationManager<DatastreamsSetter> mockedRegistrationSetterManager;

    private SimulatedDatastreamsManager testDatastreamsManager;

    @Mock
    private DatastreamsGetter mockedGetter;
    @Mock
    private DatastreamsSetter mockedSetter;

    @Before
    public void setUp() {
        testDatastreamsManager = new SimulatedDatastreamsManager(mockedGetterFactory, mockedSetterFactory,
                mockedRegistrationGetterManager, mockedRegistrationSetterManager);
    }

    @Test
    public void testLoadConfiguration() {
        when(mockedGetterFactory.createConstantDatastreamsGetter(anyString(), anyString(), anyString(), any()))
                .thenReturn(mockedGetter);
        when(mockedGetterFactory.createRandomDatastreamsGetter(anyString(), anyString(), anyString(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(mockedGetter);
        when(mockedSetterFactory.createSetDatastreamsSetter(anyString(), anyString()))
                .thenReturn(mockedSetter);

        testDatastreamsManager.loadConfiguration(TEST_CONFIGURATION, TEST_CONFIGURATION_SETTER);

        verify(mockedGetterFactory).createConstantDatastreamsGetter(eq(TEST_DATASTREAM_ID_1), eq(TEST_DEVICE_ID_1),
                eq(TEST_FEED_1), eq(TEST_VALUE));
        verify(mockedGetterFactory).createRandomDatastreamsGetter(eq(TEST_DATASTREAM_ID_2), eq(TEST_DEVICE_ID_2),
                eq(null), eq(TEST_MIN_VALUE), eq(TEST_MAX_VALUE), eq(TEST_MAX_DIFF));
        verify(mockedSetterFactory).createSetDatastreamsSetter(eq(TEST_DATASTREAM_ID_3), eq(TEST_DEVICE_ID_3));
        verify(mockedRegistrationGetterManager, times(2)).register(eq(mockedGetter));
        verify(mockedRegistrationSetterManager, times(1)).register(eq(mockedSetter));
    }

    @Test
    public void testClose() {
        testDatastreamsManager.close();

        verify(mockedRegistrationGetterManager).unregister();
        verify(mockedRegistrationSetterManager).unregister();
    }
}