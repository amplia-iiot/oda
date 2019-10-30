package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import es.amplia.oda.datastreams.i2c.datastreams.I2CDatastreamsGetter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
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


	@Before
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

	@Test(expected = ExecutionException.class)
	public void testGetAnException() throws ExecutionException, InterruptedException {
		when(mockedService.getI2CFromName(eq(TEST_DATASTREAM_ID))).thenThrow(new I2CDeviceException(""));

		testGetter.get("dumbData").get();
	}
}