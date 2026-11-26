package es.amplia.oda.datastreams.iec104.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import es.amplia.oda.datastreams.iec104.Iec104DatastreamsManager;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        char[] qualityBitsMask = {1, 1, 1, 1};
        Whitebox.setInternalState(testConfigHandler, "qualityBitsMask", qualityBitsMask);
        Whitebox.setInternalState(testConfigHandler, "qualityBitsNotify", false);

        testConfigHandler.applyConfiguration();

        verify(mockedIec104DatastreamsManager).loadConfiguration(currentConfiguration, 1000,
                10000, 0, 0, qualityBitsMask, false);
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