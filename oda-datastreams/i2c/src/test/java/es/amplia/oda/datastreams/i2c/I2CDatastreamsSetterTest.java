package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(I2CDatastreamsSetter.class)
public class I2CDatastreamsSetterTest {
	private I2CDatastreamsSetter testSetter;

	private final String datastreamId = "testId";
	private final Executor executor = Executors.newSingleThreadExecutor();

	@Mock
	I2CService mockedService;
	@Mock
	I2CDevice mockedDevice;

	@Before
	public void testSetUp() {
		testSetter = new I2CDatastreamsSetter(datastreamId, mockedService, executor);
	}

	@Test
	public void testGetDatastreamIdSatisfied() {
		assertEquals(datastreamId, testSetter.getDatastreamIdSatisfied());
	}

	@Test
	public void testGetDatastreamType() {
		assertEquals(ByteBuffer.class, testSetter.getDatastreamType());
	}

	@Test
	public void testGetDevicesIdManaged() {
		assertEquals(Collections.singletonList(""), testSetter.getDevicesIdManaged());
	}

	@Test
	public void testSet() {
		ByteBuffer dumbData = ByteBuffer.wrap(new byte[]{new Long(131000000L).byteValue()});
		when(mockedService.getI2CFromName(eq(datastreamId))).thenReturn(mockedDevice);

		testSetter.set("dumbData", dumbData);

		verify(mockedService).getI2CFromName(eq(datastreamId));
		verify(mockedDevice).write(eq(dumbData));
	}

	@Test
	public void testSetAnException() {
		ByteBuffer dumbData = ByteBuffer.wrap(new byte[]{new Long(131000000L).byteValue()});
		doThrow(I2CDeviceException.class).when(mockedService).getI2CFromName(eq(datastreamId));

		testSetter.set("dumbData", dumbData);
	}
}