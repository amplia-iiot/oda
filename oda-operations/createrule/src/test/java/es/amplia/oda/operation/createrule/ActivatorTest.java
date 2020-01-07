package es.amplia.oda.operation.createrule;

import es.amplia.oda.operation.api.OperationCreateRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.verify;
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

	@Test
	public void testStart() throws Exception {
		whenNew(OperationCreateRuleImpl.class).withAnyArguments().thenReturn(mockedOperationCreate);

		activator.start(mockedContext);

		verifyNew(OperationCreateRuleImpl.class).withNoArguments();
		verify(mockedContext).registerService(OperationCreateRule.class, mockedOperationCreate, null);
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(activator, "registration", mockedRegistration);

		activator.stop(mockedContext);

		verify(mockedRegistration).unregister();
	}
}
