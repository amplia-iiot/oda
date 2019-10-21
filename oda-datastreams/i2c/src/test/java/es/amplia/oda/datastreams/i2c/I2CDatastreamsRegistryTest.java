package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(I2CDatastreamsRegistry.class)
public class I2CDatastreamsRegistryTest {

	private static final String TEST_DATASTREAM_ID = "datastreamId";
	private static final long TEST_MIN = 1;
	private static final long TEST_MAX = 50;


	@Mock
	private I2CDatastreamsFactory mockedFactory;
	@Mock
	private ServiceRegistrationManager<DatastreamsGetter> mockedGetterRegistrationManager;
	@Mock
	private ServiceRegistrationManager<DatastreamsSetter> mockedSetterRegistrationManager;
	private I2CDatastreamsRegistry testRegistry;

	@Mock
	private DatastreamsGetter mockedGetter;
	@Mock
	private DatastreamsSetter mockedSetter;


	@Before
	public void setUp() {
		testRegistry = new I2CDatastreamsRegistry(mockedFactory, mockedGetterRegistrationManager,
				mockedSetterRegistrationManager);
	}

	@Test
	public void testAddDatastreamGetter() {
		when(mockedFactory.createDatastreamsGetter(anyString(), anyLong(), anyLong())).thenReturn(mockedGetter);

		testRegistry.addDatastreamGetter(TEST_DATASTREAM_ID, TEST_MIN, TEST_MAX);

		verify(mockedFactory).createDatastreamsGetter(eq(TEST_DATASTREAM_ID), eq(TEST_MIN), eq(TEST_MAX));
		verify(mockedGetterRegistrationManager).register(eq(mockedGetter));
	}

	@Test
	public void testAddDatastreamSetter() {
		when(mockedFactory.createDatastreamsSetter(anyString())).thenReturn(mockedSetter);

		testRegistry.addDatastreamSetter(TEST_DATASTREAM_ID);

		verify(mockedFactory).createDatastreamsSetter(eq(TEST_DATASTREAM_ID));
		verify(mockedSetterRegistrationManager).register(eq(mockedSetter));
	}

	@Test
	public void testClose() {
		testRegistry.close();

		verify(mockedGetterRegistrationManager).unregister();
		verify(mockedSetterRegistrationManager).unregister();
	}
}