package es.amplia.oda.service.jsonserializer;

import es.amplia.oda.core.commons.interfaces.Serializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ActivatorTest {

	private final Activator activator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private ServiceRegistration<Serializer> mockedRegistration;

	@Test
	public void testStart() {
		activator.start(mockedContext);

		verify(mockedContext).registerService(eq(Serializer.class), any(JsonSerializer.class), any());
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(activator, "registration", mockedRegistration);

		activator.stop(mockedContext);

		verify(mockedRegistration).unregister();
	}
}