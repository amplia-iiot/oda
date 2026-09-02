package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfigurationHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

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
	RuleEngineConfigurationHandler mockedRuleEngineHandler;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	BundleContext mockedContext;
	@Mock
	ServiceRegistration<ManagedService> mockedServiceRegistration;
	@Mock
	Bundle mockedBundle;

	@Test
	public void testStart() throws Exception {
		List<List<?>> ruleEngineArgs = new ArrayList<>();
		List<List<?>> handlerArgs = new ArrayList<>();
		List<List<?>> configurableBundleArgs = new ArrayList<>();
		try (MockedConstruction<NashornScriptTranslator> translatorConstruction =
					 mockConstruction(NashornScriptTranslator.class);
			 MockedConstruction<RuleEngineNashorn> ruleEngineConstruction =
					 mockConstruction(RuleEngineNashorn.class,
							 (mock, mctx) -> ruleEngineArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<RuleEngineConfigurationHandler> handlerConstruction =
					 mockConstruction(RuleEngineConfigurationHandler.class,
							 (mock, mctx) -> handlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configurableBundleConstruction =
					 mockConstruction(ConfigurableBundleImpl.class,
							 (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {
			when(mockedContext.registerService(eq(ManagedService.class), any(), any())).thenReturn(mockedServiceRegistration);
			when(mockedContext.getBundle()).thenReturn(mockedBundle);
			when(mockedBundle.getSymbolicName()).thenReturn("symbolicName");

			testActivator.start(mockedContext);

			assertEquals(1, translatorConstruction.constructed().size());
			assertEquals(1, ruleEngineConstruction.constructed().size());
			assertEquals(translatorConstruction.constructed().get(0), ruleEngineArgs.get(0).get(0));
			assertEquals(1, handlerConstruction.constructed().size());
			assertEquals(ruleEngineConstruction.constructed().get(0), handlerArgs.get(0).get(0));
			assertEquals(translatorConstruction.constructed().get(0), handlerArgs.get(0).get(1));
			assertEquals(1, configurableBundleConstruction.constructed().size());
			assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
			assertEquals(handlerConstruction.constructed().get(0), configurableBundleArgs.get(0).get(1));
		}
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "ruleEngineServiceRegistration", mockedServiceRegistration);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(testActivator, "ruleEngine", mockedRuleEngine);
		Whitebox.setInternalState(testActivator, "scriptTranslator", mockedTranslator);

		testActivator.stop(mockedContext);

		verify(mockedServiceRegistration).unregister();
		verify(mockedConfigurableBundle).close();
		verify(mockedRuleEngine).stop();
		verify(mockedTranslator).close();
	}
}
