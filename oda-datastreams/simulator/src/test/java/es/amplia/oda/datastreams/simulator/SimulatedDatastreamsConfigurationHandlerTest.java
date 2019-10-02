package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.utils.MapBasedDictionary;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SimulatedDatastreamsConfigurationHandlerTest {

    @Mock
    private SimulatedDatastreamsManager mockedDatastreamsManager;
    private SimulatedDatastreamsConfigurationHandler testHandler;

    @Spy
    ArrayList<SimulatedDatastreamsConfiguration> spiedConfigurations;
    @Captor
    ArgumentCaptor<SimulatedDatastreamsConfiguration> configurationCaptor;

    @Before
    public void setUp() {
        testHandler = new SimulatedDatastreamsConfigurationHandler(mockedDatastreamsManager);

        Whitebox.setInternalState(testHandler, "lastConfiguration", spiedConfigurations);
    }

    @Test
    public void testLoadConfiguration() {
        String testDatastream1 = "testDatastream1";
        String testDevice1 = "testDevice1";
        double testMinValue1 = 10.0;
        double testMaxValue1 = 20.0;
        double testMaxDiff1 = 50.0;
        String testDatastream2 = "testDatastream2";
        double testMinValue2 = 100.0;
        double testMaxValue2 = 200.0;
        double testMaxDiff2 = 20.0;
        SimulatedDatastreamsConfiguration expectedConfiguration1 = new SimulatedDatastreamsConfiguration(testDatastream1,
                testDevice1, testMinValue1, testMaxValue1, testMaxDiff1);
        SimulatedDatastreamsConfiguration expectedConfiguration2 = new SimulatedDatastreamsConfiguration(testDatastream2,
                "", testMinValue2, testMaxValue2, testMaxDiff2);
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put(testDatastream1 + ";" + testDevice1, String.join(",", Arrays.asList(
                Double.toString(testMinValue1), Double.toString(testMaxValue1), Double.toString(testMaxDiff1))));
        props.put(testDatastream2, String.join(",", Arrays.asList(
                Double.toString(testMinValue2), Double.toString(testMaxValue2), Double.toString(testMaxDiff2))));

        testHandler.loadConfiguration(props);

        verify(spiedConfigurations,times(2)).add(configurationCaptor.capture());
        List<SimulatedDatastreamsConfiguration> configurations = configurationCaptor.getAllValues();
        verifyConfiguration(expectedConfiguration1, configurations);
        verifyConfiguration(expectedConfiguration2, configurations);
    }

    @Test
    public void testLoadConfigurationInvalidConfigurationMissingPropertyCaught() {
        String testDatastream1 = "testDatastream1";
        String testDevice1 = "testDevice1";
        double testMinValue1 = 10.0;
        double testMaxValue1 = 20.0;
        Dictionary<String, String> props = new MapBasedDictionary<>(String.class);
        props.put(testDatastream1 + ";" + testDevice1, String.join(",", Arrays.asList(
                Double.toString(testMinValue1), Double.toString(testMaxValue1))));

        testHandler.loadConfiguration(props);

        assertTrue(spiedConfigurations.isEmpty());
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

        assertTrue(spiedConfigurations.isEmpty());
    }

    private void verifyConfiguration(SimulatedDatastreamsConfiguration expected,
                                     List<SimulatedDatastreamsConfiguration> configurations) {
        if (configurations.stream().noneMatch(expected::equals)) {
            fail("Expected configuration " + expected + " not found");
        }
    }

    @Test
    public void testLoadDefaultConfiguration() {
        testHandler.loadDefaultConfiguration();

        verify(spiedConfigurations).clear();
    }

    @Test
    public void testApplyConfiguration() {
        testHandler.applyConfiguration();

        verify(mockedDatastreamsManager).loadConfiguration(eq(spiedConfigurations));
    }
}