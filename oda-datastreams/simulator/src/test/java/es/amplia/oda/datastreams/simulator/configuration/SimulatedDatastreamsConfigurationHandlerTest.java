package es.amplia.oda.datastreams.simulator.configuration;

import es.amplia.oda.core.commons.utils.MapBasedDictionary;

import es.amplia.oda.datastreams.simulator.SimulatedDatastreamsManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SimulatedDatastreamsConfigurationHandlerTest {

    @Mock
    private SimulatedDatastreamsManager mockedDatastreamsManager;
    private SimulatedDatastreamsConfigurationHandler testHandler;

    @Spy
    private ArrayList<SimulatedDatastreamsGetterConfiguration> spiedGetterConfigurations;
    @Spy
    private ArrayList<SetDatastreamSetterConfiguration> spiedSetterConfigurations;
    @Captor
    private ArgumentCaptor<SimulatedDatastreamsGetterConfiguration> configurationGetterCaptor;
    @Captor
    private ArgumentCaptor<SetDatastreamSetterConfiguration> configurationSetterCaptor;

    @Before
    public void setUp() {
        testHandler = new SimulatedDatastreamsConfigurationHandler(mockedDatastreamsManager);

        Whitebox.setInternalState(testHandler, "gettersConfigured", spiedGetterConfigurations);
    }

    @Test
    public void testLoadConfiguration() {
        String testDatastream1 = "testDatastream1";
        String testDevice1 = "testDevice1";
        String testFeed1 = "testFeed1";
        String testValue1 = "Hello World!";
        String testDatastream2 = "testDatastream2";
        double testMinValue2 = 100.0;
        double testMaxValue2 = 200.0;
        double testMaxDiff2 = 20.0;
        ConstantDatastreamGetterConfiguration expectedConfiguration1 = new ConstantDatastreamGetterConfiguration(testDatastream1,
                testDevice1, testFeed1, testValue1);
        RandomDatastreamGetterConfiguration expectedConfiguration2 = new RandomDatastreamGetterConfiguration(testDatastream2,
                "", null, testMinValue2, testMaxValue2, testMaxDiff2);
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put(testDatastream1 + " ; " + testDevice1 + " ; " + testFeed1, "String, " + testValue1);
        props.put(testDatastream2, String.join(",", Arrays.asList(
                Double.toString(testMinValue2), Double.toString(testMaxValue2), Double.toString(testMaxDiff2))));

        testHandler.loadConfiguration(props);

        verify(spiedGetterConfigurations,times(2)).add(configurationGetterCaptor.capture());
        List<SimulatedDatastreamsGetterConfiguration> configurations = configurationGetterCaptor.getAllValues();
        verifyConfiguration(expectedConfiguration1, configurations);
        verifyConfiguration(expectedConfiguration2, configurations);
    }

    @Test
    public void testLoadConstantConfigurationOfTypeInt() {
        String testDatastream = "testDatastream";
        int testValue = 321;
        ConstantDatastreamGetterConfiguration expectedConfiguration = new ConstantDatastreamGetterConfiguration(testDatastream,
                "", null, testValue);
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put(testDatastream, "  int, " + testValue);

        testHandler.loadConfiguration(props);

        verify(spiedGetterConfigurations).add(configurationGetterCaptor.capture());
        SimulatedDatastreamsGetterConfiguration configuration = configurationGetterCaptor.getValue();
        assertEquals(expectedConfiguration, configuration);
    }

    @Test
    public void testLoadConstantConfigurationOfTypeInteger() {
        String testDatastream = "testDatastream";
        int testValue = 321;
        ConstantDatastreamGetterConfiguration expectedConfiguration = new ConstantDatastreamGetterConfiguration(testDatastream,
                "", null, testValue);
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put(testDatastream, " Integer , " + testValue);

        testHandler.loadConfiguration(props);

        verify(spiedGetterConfigurations).add(configurationGetterCaptor.capture());
        SimulatedDatastreamsGetterConfiguration configuration = configurationGetterCaptor.getValue();
        assertEquals(expectedConfiguration, configuration);
    }

    @Test
    public void testLoadConstantConfigurationOfTypeFloat() {
        String testDatastream = "testDatastream";
        float testValue = 32.10f;
        ConstantDatastreamGetterConfiguration expectedConfiguration = new ConstantDatastreamGetterConfiguration(testDatastream,
                "", null, testValue);
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put(testDatastream, "float," + testValue);

        testHandler.loadConfiguration(props);

        verify(spiedGetterConfigurations).add(configurationGetterCaptor.capture());
        SimulatedDatastreamsGetterConfiguration configuration = configurationGetterCaptor.getValue();
        assertEquals(expectedConfiguration, configuration);
    }

    @Test
    public void testLoadConstantConfigurationOfTypeDouble() {
        String testDatastream = "testDatastream";
        double testValue = 54.321;
        ConstantDatastreamGetterConfiguration expectedConfiguration = new ConstantDatastreamGetterConfiguration(testDatastream,
                "", null, testValue);
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put(testDatastream, "Double, " + testValue);

        testHandler.loadConfiguration(props);

        verify(spiedGetterConfigurations).add(configurationGetterCaptor.capture());
        SimulatedDatastreamsGetterConfiguration configuration = configurationGetterCaptor.getValue();
        assertEquals(expectedConfiguration, configuration);
    }

    @Test
    public void testLoadConstantConfigurationOfTypeNumber() {
        String testDatastream = "testDatastream";
        double testValue = 54.321;
        ConstantDatastreamGetterConfiguration expectedConfiguration = new ConstantDatastreamGetterConfiguration(testDatastream,
                "", null,testValue);
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put(testDatastream, "number, " + testValue);

        testHandler.loadConfiguration(props);

        verify(spiedGetterConfigurations).add(configurationGetterCaptor.capture());
        SimulatedDatastreamsGetterConfiguration configuration = configurationGetterCaptor.getValue();
        assertEquals(expectedConfiguration, configuration);
    }

    @Test
    public void testLoadConstantConfigurationOfInvalidType() {
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put("test", "invalid, hello");

        testHandler.loadConfiguration(props);

        verify(spiedGetterConfigurations, never()).add(any(SimulatedDatastreamsGetterConfiguration.class));
    }

    @Test
    public void testLoadConfigurationInvalidConfigurationMissingPropertyCaught() {
        String testDatastream1 = "testDatastream1";
        String testDevice1 = "testDevice1";
        double testValue = 10.0;
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put(testDatastream1 + ";" + testDevice1, String.join(",", Collections.singletonList(
                Double.toString(testValue))));

        testHandler.loadConfiguration(props);

        assertTrue(spiedGetterConfigurations.isEmpty());
    }

    @Test
    public void testLoadConfigurationInvalidConfigurationInvalidNumberCaught() {
        String testDatastream1 = "testDatastream1";
        String testDevice1 = "testDevice1";
        double testMinValue1 = 10.0;
        double testMaxValue1 = 20.0;
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put(testDatastream1 + ";" + testDevice1, String.join(",", Arrays.asList(
                Double.toString(testMinValue1), Double.toString(testMaxValue1), "invalid")));

        testHandler.loadConfiguration(props);

        assertTrue(spiedGetterConfigurations.isEmpty());
    }

    private void verifyConfiguration(SimulatedDatastreamsGetterConfiguration expected,
                                     List<SimulatedDatastreamsGetterConfiguration> configurations) {
        if (configurations.stream().noneMatch(expected::equals)) {
            fail("Expected configuration " + expected + " not found");
        }
    }

    @Test
    public void testLoadDefaultConfiguration() {
        testHandler.loadDefaultConfiguration();

        verify(spiedGetterConfigurations).clear();
    }

    @Test
    public void testApplyConfiguration() {
        testHandler.applyConfiguration();

        verify(mockedDatastreamsManager).loadConfiguration(eq(spiedGetterConfigurations), any());
    }
}