package es.amplia.oda.operation.createrule;

import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.operation.api.OperationCreateRule;
import es.amplia.oda.operation.createrule.configuration.RuleCreatorConfigurationHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
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

	private final Activator activator = new Activator();

	@Mock
	private OperationCreateRuleImpl mockedOperationCreate;
	@Mock
	private BundleContext mockedContext;
	@Mock
	private ServiceRegistration<OperationCreateRule> mockedRegistration;
	@Mock
	private RuleCreatorConfigurationHandler mockedHandler;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundle;

	@Test
	public void testStart() throws Exception {
		whenNew(OperationCreateRuleImpl.class).withAnyArguments().thenReturn(mockedOperationCreate);
		whenNew(RuleCreatorConfigurationHandler.class).withAnyArguments().thenReturn(mockedHandler);
		whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		when(mockedContext.registerService(eq(OperationCreateRule.class), eq(mockedOperationCreate), eq(null))).thenReturn(mockedRegistration);

		activator.start(mockedContext);

		verifyNew(OperationCreateRuleImpl.class).withNoArguments();
		verifyNew(RuleCreatorConfigurationHandler.class).withArguments(mockedOperationCreate);
		verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedHandler), any());
		verify(mockedContext).registerService(OperationCreateRule.class, mockedOperationCreate, null);
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(activator, "registration", mockedRegistration);
		Whitebox.setInternalState(activator, "configurableBundle", mockedConfigurableBundle);

		activator.stop(mockedContext);

		verify(mockedRegistration).unregister();
		verify(mockedConfigurableBundle).close();
	}
}
