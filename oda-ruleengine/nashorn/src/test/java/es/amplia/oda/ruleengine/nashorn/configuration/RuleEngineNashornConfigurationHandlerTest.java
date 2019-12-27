package es.amplia.oda.ruleengine.nashorn.configuration;

import es.amplia.oda.ruleengine.nashorn.RuleEngineNashorn;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RuleEngineNashornConfigurationHandlerTest {

	RuleEngineConfigurationHandler testHandler;

	@Mock
	RuleEngineNashorn mockedEngine;

	@Before
	public void setUp() {
		testHandler = new RuleEngineConfigurationHandler(mockedEngine);
	}

	@Test
	public void testLoadConfiguration() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put("path", "this/is/a/path");

		testHandler.loadConfiguration(props);

		assertNotNull(Whitebox.getInternalState(testHandler, "config"));
	}

	@Test
	public void testApplyConfiguration() {
		Whitebox.setInternalState(testHandler, "ruleEngine", mockedEngine);

		testHandler.applyConfiguration();

		verify(mockedEngine).loadConfiguration(null);
	}
}
