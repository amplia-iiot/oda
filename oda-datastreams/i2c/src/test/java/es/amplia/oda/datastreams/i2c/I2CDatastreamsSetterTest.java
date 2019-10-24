package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.datastreams.i2c.datastreams.I2CDatastreamsSetter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class I2CDatastreamsSetterTest {

	private static final String TEST_DATASTREAMS_ID = "testDevice";


	@Mock
	private I2CService mockedService;
	private I2CDatastreamsSetter testSetter;

	@Mock
	private I2CDevice mockedDevice;


	@Before
	public void testSetUp() {
		testSetter = new I2CDatastreamsSetter(TEST_DATASTREAMS_ID, mockedService);
	}

	@Test
	public void testGetDatastreamIdSatisfied() {
		assertEquals(TEST_DATASTREAMS_ID, testSetter.getDatastreamIdSatisfied());
	}

	@Test
	public void testGetDatastreamType() {
		assertEquals(float.class, testSetter.getDatastreamType());
	}

	@Test
	public void testGetDevicesIdManaged() {
		assertEquals(Collections.singletonList(""), testSetter.getDevicesIdManaged());
	}

	@Test
	public void testSet() throws ExecutionException, InterruptedException {
		float dumbData = 131000000L;
		when(mockedService.getI2CFromName(eq(TEST_DATASTREAMS_ID))).thenReturn(mockedDevice);

		testSetter.set("dumbData", dumbData).get();

		verify(mockedService).getI2CFromName(eq(TEST_DATASTREAMS_ID));
		verify(mockedDevice).write(eq(dumbData));
	}

	@Test(expected = ExecutionException.class)
	public void testSetAnException() throws ExecutionException, InterruptedException {
		ByteBuffer dumbData = ByteBuffer.wrap(new byte[]{new Long(131000000L).byteValue()});
		doThrow(I2CDeviceException.class).when(mockedService).getI2CFromName(eq(TEST_DATASTREAMS_ID));

		testSetter.set("dumbData", dumbData).get();
	}
}