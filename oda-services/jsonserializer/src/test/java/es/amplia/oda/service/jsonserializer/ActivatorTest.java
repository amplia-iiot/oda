package es.amplia.oda.service.jsonserializer;

import es.amplia.oda.core.commons.interfaces.Serializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
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
		verify(mockedContext).registerService(eq(Serializer.class), any(CborSerializer.class), any());
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(activator, "jsonRegistration", mockedRegistration);
		Whitebox.setInternalState(activator, "cborRegistration", mockedRegistration);

		activator.stop(mockedContext);

		verify(mockedRegistration, times(2)).unregister();
	}
}
