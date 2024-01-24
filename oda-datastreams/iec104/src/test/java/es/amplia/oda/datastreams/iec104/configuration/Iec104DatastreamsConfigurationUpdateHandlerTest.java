package es.amplia.oda.datastreams.iec104.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import es.amplia.oda.datastreams.iec104.Iec104DatastreamsManager;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Iec104DatastreamsConfigurationUpdateHandlerTest {

    @Mock
    private Iec104DatastreamsManager mockedIec104DatastreamsManager;
    @InjectMocks
    private Iec104DatastreamsConfigurationUpdateHandler testConfigHandler;

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put("pollingTime", "10000");
        props.put("initialPollingTime", "1000");
        props.put("device1", "127.0.0.1;2404;1");
        props.put("device2", "127.0.0.1;2404;2");
        props.put("device3", "127.0.0.2;2404;1");
        props.put("device4", "127.0.0.3;2404;1");
        props.put("device5", "127.0.0.4;2404;1");

        List<Iec104DatastreamsConfiguration> spiedConfiguration = spy(new ArrayList<>());

        Whitebox.setInternalState(testConfigHandler, "currentIec104DatastreamsConfigurations", spiedConfiguration);

        testConfigHandler.loadConfiguration(props);

        verify(spiedConfiguration).clear();
        verify(spiedConfiguration, times(5)).add(any(Iec104DatastreamsConfiguration.class));
    }

    @Test
    public void testLoadDefaultConfiguration() {
        List<Iec104DatastreamsConfiguration> spiedConfiguration = spy(new ArrayList<>());
        Whitebox.setInternalState(testConfigHandler, "currentIec104DatastreamsConfigurations", spiedConfiguration);

        testConfigHandler.loadDefaultConfiguration();

        verify(spiedConfiguration).clear();
    }

    @Test
    public void testApplyConfiguration() {
        List<Iec104DatastreamsConfiguration> currentConfiguration = new ArrayList<>();
        Whitebox.setInternalState(testConfigHandler, "currentIec104DatastreamsConfigurations", currentConfiguration);
        Whitebox.setInternalState(testConfigHandler, "iec104Polling", 10000);
        Whitebox.setInternalState(testConfigHandler, "iec104PollingInitialDelay", 1000);


        testConfigHandler.applyConfiguration();

        verify(mockedIec104DatastreamsManager).loadConfiguration(currentConfiguration, 1000, 10000, 0, 0);
    }

    @Test
    public void testWrongConfig(){
        Dictionary<String, String> props = new Hashtable<>();
        props.put("pollingTime", "10000");
        props.put("initialPollingTime", "1000");
        props.put("device1", "127.0.0.1;2404;1;1");

        List<Iec104DatastreamsConfiguration> spiedConfiguration = spy(new ArrayList<>());

        Whitebox.setInternalState(testConfigHandler, "currentIec104DatastreamsConfigurations", spiedConfiguration);

        testConfigHandler.loadConfiguration(props);

        verify(spiedConfiguration).clear();
        verify(spiedConfiguration, times(0)).add(any(Iec104DatastreamsConfiguration.class));
    }
}