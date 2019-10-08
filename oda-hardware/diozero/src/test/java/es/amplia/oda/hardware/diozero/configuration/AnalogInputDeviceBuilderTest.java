package es.amplia.oda.hardware.diozero.configuration;

import es.amplia.oda.core.commons.diozero.DeviceType;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import static org.junit.Assert.assertEquals;

public class AnalogInputDeviceBuilderTest {
	private static final int channelIndex = 5;
	private static final String name = "testDevice";
	private static final String path = "";
	private static final boolean lowMode = false;
	private static final DeviceType deviceType = DeviceType.DEFAULT;

	private AnalogInputDeviceBuilder builder = AnalogInputDeviceBuilder.newBuilder();

	@Test
	public void testSetChannelIndex() {
		builder.setChannelIndex(channelIndex);

		assertEquals(channelIndex, Whitebox.getInternalState(builder, "channelIndex"));
	}

	@Test
	public void testSetName() {
		builder.setName(name);

		assertEquals(name, Whitebox.getInternalState(builder, "name"));
	}

	@Test
	public void testSetPath() {
		builder.setPath(path);

		assertEquals(path, Whitebox.getInternalState(builder, "path"));
	}

	@Test
	public void testSetLowMode() {
		builder.setLowMode(lowMode);

		assertEquals(lowMode, Whitebox.getInternalState(builder, "lowMode"));
	}

	@Test
	public void testSetDeviceType() {
		builder.setDeviceType(deviceType);

		assertEquals(deviceType, Whitebox.getInternalState(builder, "deviceType"));
	}
}
