package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.operation.api.engine.OperationEngine;
import es.amplia.oda.operation.nashorn.OperationEngineNashorn;
import es.amplia.oda.operation.nashorn.configuration.OperationEngineConfigurationHandler;
import es.amplia.oda.ruleengine.api.RuleEngine;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfigurationHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

	Activator testActivator = new Activator();

	@Mock
	NashornScriptTranslator mockedTranslator;
	@Mock
	RuleEngineNashorn mockedRuleEngine;
	@Mock
	OperationEngineNashorn mockedOperationEngine;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	BundleContext mockedContext;
	@Mock
	ServiceRegistration<RuleEngine> mockedRuleRegistration;
	@Mock
	ServiceRegistration<OperationEngine> mockedOperationRegistration;

	@Test
	public void testStart() {
		try (MockedConstruction<NashornScriptTranslator> translatorConstruction =
					 mockConstruction(NashornScriptTranslator.class);
			 MockedConstruction<RuleEngineNashorn> ruleEngineConstruction =
					 mockConstruction(RuleEngineNashorn.class);
			 MockedConstruction<OperationEngineNashorn> operationEngineConstruction =
					 mockConstruction(OperationEngineNashorn.class);
			 MockedConstruction<RuleEngineConfigurationHandler> ruleHandlerConstruction =
					 mockConstruction(RuleEngineConfigurationHandler.class);
			 MockedConstruction<OperationEngineConfigurationHandler> operationHandlerConstruction =
					 mockConstruction(OperationEngineConfigurationHandler.class);
			 MockedConstruction<ConfigurableBundleImpl> configurableBundleConstruction =
					 mockConstruction(ConfigurableBundleImpl.class)) {
			when(mockedContext.registerService(eq(RuleEngine.class), any(), any())).thenReturn(mockedRuleRegistration);
			when(mockedContext.registerService(eq(OperationEngine.class), any(), any())).thenReturn(mockedOperationRegistration);

			testActivator.start(mockedContext);

			// one translator per engine (rules preloads utils.js, operations does not)
			assertEquals(2, translatorConstruction.constructed().size());
			assertEquals(1, ruleEngineConstruction.constructed().size());
			assertEquals(1, operationEngineConstruction.constructed().size());
			assertEquals(1, ruleHandlerConstruction.constructed().size());
			assertEquals(1, operationHandlerConstruction.constructed().size());
			// one ConfigurableBundle per engine, each with its own explicit PID
			assertEquals(2, configurableBundleConstruction.constructed().size());
			verify(mockedContext).registerService(eq(RuleEngine.class), any(), any());
			verify(mockedContext).registerService(eq(OperationEngine.class), any(), any());
		}
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "ruleEngineServiceRegistration", mockedRuleRegistration);
		Whitebox.setInternalState(testActivator, "operationEngineServiceRegistration", mockedOperationRegistration);
		Whitebox.setInternalState(testActivator, "ruleConfigurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(testActivator, "operationConfigurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(testActivator, "ruleEngine", mockedRuleEngine);
		Whitebox.setInternalState(testActivator, "operationEngine", mockedOperationEngine);
		Whitebox.setInternalState(testActivator, "ruleScriptTranslator", mockedTranslator);
		Whitebox.setInternalState(testActivator, "operationScriptTranslator", mockedTranslator);

		testActivator.stop(mockedContext);

		verify(mockedRuleRegistration).unregister();
		verify(mockedOperationRegistration).unregister();
		verify(mockedRuleEngine).stop();
		verify(mockedOperationEngine).stop();
	}
}
