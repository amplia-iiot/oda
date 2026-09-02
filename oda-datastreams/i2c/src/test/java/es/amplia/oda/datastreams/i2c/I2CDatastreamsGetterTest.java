package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import es.amplia.oda.datastreams.i2c.datastreams.I2CDatastreamsGetter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class I2CDatastreamsGetterTest {

	private static final String TEST_DATASTREAM_ID = "testId";

	private static final String TEST_DEFAULT_DEVICE_NAME = "defaultDeviceName";
	private static final long TEST_MIN = 0;
	private static final long TEST_MAX = 50;
	private static final double DELTA = 0.001;


	@Mock
	private I2CService mockedService;
	private I2CDatastreamsGetter testGetter;

	@Mock
	private I2CDevice mockedDevice;


	@BeforeEach
	public void setUp() {
		testGetter = new I2CDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DEFAULT_DEVICE_NAME, TEST_MIN, TEST_MAX, mockedService);
	}

	@Test
	public void testGetDatastreamIdSatisfied() {
		assertEquals(TEST_DATASTREAM_ID, testGetter.getDatastreamIdSatisfied());
	}

	@Test
	public void testGetDevicesIdManaged() {
		assertEquals(Collections.singletonList(""), testGetter.getDevicesIdManaged());
	}

	@Test
	public void testGet() throws ExecutionException, InterruptedException {
		double data = 0.50;
		long before = System.currentTimeMillis();

		when(mockedService.getI2CFromName(eq(TEST_DEFAULT_DEVICE_NAME))).thenReturn(mockedDevice);
		when(mockedDevice.readScaledData()).thenReturn(data);

		DatastreamsGetter.CollectedValue result = testGetter.get("dumbData").get();

		long after = System.currentTimeMillis();
		assertEquals(25, (double) result.getValue(), DELTA);
		assertTrue((before<=result.getAt())&&(result.getAt()<=after));
	}

	@Test
	public void testGetAnException() throws ExecutionException, InterruptedException {
		when(mockedService.getI2CFromName(eq(TEST_DATASTREAM_ID))).thenThrow(new I2CDeviceException(""));

		assertThrows(ExecutionException.class, () -> testGetter.get("dumbData").get());
	}
}