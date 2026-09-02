package es.amplia.oda.service.jsonserializer;

import es.amplia.oda.core.commons.interfaces.Serializer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
