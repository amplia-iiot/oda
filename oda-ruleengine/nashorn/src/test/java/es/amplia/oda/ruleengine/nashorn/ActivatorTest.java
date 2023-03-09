package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfigurationHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
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
		whenNew(NashornScriptTranslator.class).withAnyArguments().thenReturn(mockedTranslator);
		whenNew(RuleEngineNashorn.class).withAnyArguments().thenReturn(mockedRuleEngine);
		whenNew(RuleEngineConfigurationHandler.class).withAnyArguments().thenReturn(mockedRuleEngineHandler);
		whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		when(mockedContext.registerService(eq(ManagedService.class), any(), any())).thenReturn(mockedServiceRegistration);
		when(mockedContext.getBundle()).thenReturn(mockedBundle);
		when(mockedBundle.getSymbolicName()).thenReturn("symbolicName");

		testActivator.start(mockedContext);

		verifyNew(NashornScriptTranslator.class).withNoArguments();
		verifyNew(RuleEngineNashorn.class).withArguments(eq(mockedTranslator));
		verifyNew(RuleEngineConfigurationHandler.class).withArguments(eq(mockedRuleEngine), eq(mockedTranslator));
		verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedRuleEngineHandler));
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
