package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({I2CDatastreamsGetter.class, I2CService.class})
public class I2CDatastreamsGetterTest {
	private I2CDatastreamsGetter testGetter;

	private final String datastreamId = "testId";
	private final long min = 0;
	private final long max = 1;
	private final Executor executor = Executors.newSingleThreadExecutor();

	@Mock
	I2CService mockedService;
	@Mock
	I2CDevice mockedDevice;

	@Before
	public void setUp() {
		testGetter = new I2CDatastreamsGetter(datastreamId, mockedService, executor, min, max);
	}

	@Test
	public void testGetDatastreamIdSatisfied() {
		assertEquals(datastreamId, testGetter.getDatastreamIdSatisfied());
	}

	@Test
	public void testGetDevicesIdManaged() {
		assertEquals(Collections.singletonList(""), testGetter.getDevicesIdManaged());
	}

	@Test
	public void testGet() throws ExecutionException, InterruptedException {
		double data = 131000000.0;
		long before = System.currentTimeMillis();
		when(mockedService.getI2CFromName(eq(datastreamId))).thenReturn(mockedDevice);
		when(mockedDevice.readScaledData()).thenReturn(data);

		DatastreamsGetter.CollectedValue result = testGetter.get("dumbData").get();


		long after = System.currentTimeMillis();
		assertEquals(Double.valueOf(data), Double.valueOf((double) result.getValue()));
		assertTrue((before<=result.getAt())&&(result.getAt()<=after));
	}

	@Test(expected = ExecutionException.class)
	public void testGetAnException() throws ExecutionException, InterruptedException {
		when(mockedService.getI2CFromName(eq(datastreamId))).thenThrow(new I2CDeviceException(""));

		testGetter.get("dumbData").get();
	}
}