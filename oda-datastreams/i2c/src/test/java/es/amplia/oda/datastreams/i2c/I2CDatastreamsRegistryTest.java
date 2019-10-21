package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(I2CDatastreamsRegistry.class)
public class I2CDatastreamsRegistryTest {
	private I2CDatastreamsRegistry testRegistry;

	private final String testName = "datastreamId";
	private final long min = 0;
	private final long max = 1;

	@Mock
	BundleContext mockedContext;
	@Mock
	I2CService mockedService;
	@Mock
	ServiceRegistration mockedRegistration;

	@Before
	public void setUp() {
		when(mockedContext.registerService(eq(DatastreamsGetter.class),any(),any())).thenReturn(mockedRegistration);
		when(mockedContext.registerService(eq(DatastreamsSetter.class),any(),any())).thenReturn(mockedRegistration);
		testRegistry = new I2CDatastreamsRegistry(mockedContext, mockedService);
	}

	@Test
	public void testAddDatastreamGetter() {
		testRegistry.addDatastreamGetter(testName, min, max);

		List<ServiceRegistration<?>> serviceRegistration =
				getServiceRegistrations();
		verify(mockedContext).registerService(eq(DatastreamsGetter.class), any(), any());
		assertEquals(1, serviceRegistration.size());
	}

	@Test
	public void testAddDatastreamSetter() {
		testRegistry.addDatastreamSetter(testName);

		List<ServiceRegistration<?>> serviceRegistration =
				getServiceRegistrations();
		verify(mockedContext).registerService(eq(DatastreamsSetter.class), any(), any());
		assertEquals(1, serviceRegistration.size());
	}

	@Test
	public void testClose() {
		testRegistry.addDatastreamGetter(testName, min, max);
		testRegistry.close();

		List<ServiceRegistration<?>> serviceRegistration =
				getServiceRegistrations();
		assertTrue(serviceRegistration.isEmpty());
	}

	@SuppressWarnings("unchecked")
	private List<ServiceRegistration<?>> getServiceRegistrations() {
		return (List<ServiceRegistration<?>>) Whitebox.getInternalState(testRegistry, "serviceRegistrations");
	}
}