package es.amplia.oda.ruleengine.nashorn.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.ruleengine.nashorn.NashornScriptTranslator;
import es.amplia.oda.ruleengine.nashorn.RuleEngineNashorn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RuleEngineNashornConfigurationHandlerTest {

	RuleEngineConfigurationHandler testHandler;

	@Mock
	NashornScriptTranslator mockedScriptTranslator;
	@Mock
	RuleEngineNashorn mockedEngine;

	@BeforeEach
	public void setUp() {
		testHandler = new RuleEngineConfigurationHandler(mockedEngine, mockedScriptTranslator);
	}

	@Test
	public void testLoadConfiguration() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put("path", "this/is/a/path");
		props.put("utilsPath", "this/is/another/path");

		testHandler.loadConfiguration(props);

		assertNotNull(Whitebox.getInternalState(testHandler, "config"));
	}

	@Test
	public void testApplyConfiguration() {
		Whitebox.setInternalState(testHandler, "ruleEngine", mockedEngine);

		testHandler.applyConfiguration();

		verify(mockedEngine).loadConfiguration(null);
	}

	@Test
	public void testMissingPath() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put("path", "this/is/a/path");

		assertThrows(ConfigurationException.class, () -> testHandler.loadConfiguration(props));
	}
}
