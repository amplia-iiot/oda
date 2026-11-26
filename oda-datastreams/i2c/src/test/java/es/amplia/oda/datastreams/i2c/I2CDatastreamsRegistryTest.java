package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class I2CDatastreamsRegistryTest {

	private static final String TEST_DATASTREAM_ID = "datastreamId";
	private static final String TEST_DEFAULT_DEVICE_NAME = "defaultDeviceName";
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


	@BeforeEach
	public void setUp() {
		testRegistry = new I2CDatastreamsRegistry(mockedFactory, mockedGetterRegistrationManager,
				mockedSetterRegistrationManager);
	}

	@Test
	public void testAddDatastreamGetter() {
		when(mockedFactory.createDatastreamsGetter(anyString(), anyString(), anyLong(), anyLong())).thenReturn(mockedGetter);

		testRegistry.addDatastreamGetter(TEST_DATASTREAM_ID, TEST_DEFAULT_DEVICE_NAME, TEST_MIN, TEST_MAX);

		verify(mockedFactory).createDatastreamsGetter(eq(TEST_DATASTREAM_ID), eq(TEST_DEFAULT_DEVICE_NAME), eq(TEST_MIN), eq(TEST_MAX));
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