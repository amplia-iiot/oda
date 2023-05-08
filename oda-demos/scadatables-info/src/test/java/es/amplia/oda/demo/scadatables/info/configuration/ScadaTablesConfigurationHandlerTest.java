package es.amplia.oda.demo.scadatables.info.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import es.amplia.oda.demo.scadatables.info.configuration.ScadaTableEntryConfiguration;
import es.amplia.oda.demo.scadatables.info.configuration.ScadaTablesConfigurationHandler;
import es.amplia.oda.demo.scadatables.info.internal.ScadaTableInfoService;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ScadaTablesConfigurationHandler.class)
public class ScadaTablesConfigurationHandlerTest {
	@Mock
	ScadaTableInfoService mockedScadaTableInfoService;
	@Mock
	ScadaTableEntryConfiguration mockedEntry;

	ScadaTablesConfigurationHandler testScadaTablesConfigurationHandler;

	@Before
	public void setUp() {
		testScadaTablesConfigurationHandler = new ScadaTablesConfigurationHandler(mockedScadaTableInfoService);
	}

	@Test
	public void testLoadConfiguration() throws Exception {
		whenNew(ScadaTableEntryConfiguration.class).withAnyArguments().thenReturn(mockedEntry);
		doNothing().when(mockedEntry).setScript(any());
		Dictionary<String, String> dic = new Hashtable<String, String>() {{
			put("BinaryInput,10001","datastream:batteryAlarm");
			put("BinaryInput,10015","datastream:doorAlarm");
		}};

		testScadaTablesConfigurationHandler.loadConfiguration(dic);

		Map<ScadaTableEntryConfiguration, Integer> tableConfig = Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableConfig");
		assertEquals(2, tableConfig.size());
	}
}
