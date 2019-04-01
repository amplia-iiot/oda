package es.amplia.oda.service.jsonserializer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.util.Hashtable;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

	@InjectMocks
	private final Activator activator = new Activator();

	@Mock
	private Logger logger;
	@Mock
	BundleContext mockedContext;
	@Mock
	ServiceRegistration<?> mockedRegistration;
	@Mock
	Hashtable<String, String> mockedDictionary;

	@Before
	public void prepareForTests() {
		Whitebox.setInternalState(activator, "logger", logger);
	}

	@Test
	public void testStart() throws Exception {
		whenNew(Hashtable.class).withAnyArguments().thenReturn(mockedDictionary);

		when(mockedContext.registerService(anyString(), any(), any())).thenReturn(null);

		activator.start(mockedContext);

		verify(logger).info("Starting Service JSON Serializer");
		verify(logger).info("JSON Serializer Activator started");
		verify(mockedContext).registerService(anyString(), any(), any());
	}

	@Test
	public void testStop() throws Exception {
		Whitebox.setInternalState(activator, "registration", mockedRegistration);

		activator.stop(mockedContext);

		verify(logger).info("Stopping Service JSON Serializer");
		verify(logger).info("JSON Serializer Activator stopped");
		verify(mockedRegistration).unregister();
	}
}