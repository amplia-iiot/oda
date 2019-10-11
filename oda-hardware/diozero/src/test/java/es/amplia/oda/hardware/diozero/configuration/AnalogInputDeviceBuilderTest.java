package es.amplia.oda.hardware.diozero.configuration;

import com.diozero.api.AnalogInputDevice;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.DeviceType;

import es.amplia.oda.hardware.diozero.analog.Fx30AnalogInputDeviceFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static es.amplia.oda.hardware.diozero.configuration.AnalogInputDeviceBuilder.DEFAULT_LOW_MODE;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AnalogInputDeviceBuilder.class, Fx30AnalogInputDeviceFactory.class})
public class AnalogInputDeviceBuilderTest {

	private static final int TEST_CHANNEL_INDEX = 1;
	private static final String TEST_NAME = "testDevice";
	private static final String TEST_PATH = "path";
	private static final boolean TEST_LOW_MODE = true;
	private static final DeviceType TEST_DEVICE_TYPE = DeviceType.DEFAULT;


	private final AnalogInputDeviceBuilder builder = AnalogInputDeviceBuilder.newBuilder();

	@Mock
	private Fx30AnalogInputDeviceFactory mockedFactory;
	@Mock
	private AnalogInputDevice mockedAnalogInputDevice;


	@Test
	public void testSetChannelIndex() {
		builder.setChannelIndex(TEST_CHANNEL_INDEX);

		assertEquals(TEST_CHANNEL_INDEX, Whitebox.getInternalState(builder, "channelIndex"));
	}

	@Test
	public void testSetName() {
		builder.setName(TEST_NAME);

		assertEquals(TEST_NAME, Whitebox.getInternalState(builder, "name"));
	}

	@Test
	public void testSetPath() {
		builder.setPath(TEST_PATH);

		assertEquals(TEST_PATH, Whitebox.getInternalState(builder, "path"));
	}

	@Test
	public void testSetLowMode() {
		builder.setLowMode(TEST_LOW_MODE);

		assertEquals(TEST_LOW_MODE, Whitebox.getInternalState(builder, "lowMode"));
	}

	@Test
	public void testSetDeviceType() {
		builder.setDeviceType(TEST_DEVICE_TYPE);

		assertEquals(TEST_DEVICE_TYPE, Whitebox.getInternalState(builder, "deviceType"));
	}

	@Test
	public void testBuild() throws Exception {
		builder.setChannelIndex(TEST_CHANNEL_INDEX);
		builder.setPath(TEST_PATH);

		PowerMockito.whenNew(AnalogInputDevice.class).withAnyArguments().thenReturn(mockedAnalogInputDevice);

		AnalogInputDevice device = builder.build();

		assertEquals(mockedAnalogInputDevice, device);
		PowerMockito.verifyNew(AnalogInputDevice.class).withArguments(eq(TEST_CHANNEL_INDEX));
	}

	@Test
	public void testBuildFx30() throws Exception {
		builder.setChannelIndex(TEST_CHANNEL_INDEX);
		builder.setName(TEST_NAME);
		builder.setPath(TEST_PATH);
		builder.setLowMode(TEST_LOW_MODE);
		builder.setDeviceType(DeviceType.FX30);

		PowerMockito.whenNew(Fx30AnalogInputDeviceFactory.class).withAnyArguments().thenReturn(mockedFactory);
		PowerMockito.whenNew(AnalogInputDevice.class).withAnyArguments().thenReturn(mockedAnalogInputDevice);

		AnalogInputDevice device = builder.build();

		assertEquals(mockedAnalogInputDevice, device);
		PowerMockito.verifyNew(Fx30AnalogInputDeviceFactory.class)
				.withArguments(eq(TEST_NAME), eq(TEST_PATH), eq(TEST_LOW_MODE), eq(DeviceType.FX30));
		PowerMockito.verifyNew(AnalogInputDevice.class).withArguments(eq(mockedFactory), eq(TEST_CHANNEL_INDEX));
	}

	@Test
	public void testBuildFx30Default() throws Exception {
		builder.setChannelIndex(TEST_CHANNEL_INDEX);
		builder.setPath(TEST_PATH);
		builder.setDeviceType(DeviceType.FX30);

		PowerMockito.whenNew(Fx30AnalogInputDeviceFactory.class).withAnyArguments().thenReturn(mockedFactory);
		PowerMockito.whenNew(AnalogInputDevice.class).withAnyArguments().thenReturn(mockedAnalogInputDevice);

		AnalogInputDevice device = builder.build();

		assertEquals(mockedAnalogInputDevice, device);
		PowerMockito.verifyNew(Fx30AnalogInputDeviceFactory.class)
				.withArguments(eq(null), eq(TEST_PATH), eq(DEFAULT_LOW_MODE), eq(DeviceType.FX30));
		PowerMockito.verifyNew(AnalogInputDevice.class).withArguments(eq(mockedFactory), eq(TEST_CHANNEL_INDEX));
	}

	@Test(expected = AdcDeviceException.class)
	public void testBuildMissingRequiredChannelIndex() {
		builder.setPath(TEST_PATH);

		builder.build();

		fail("ADC Device Exception should be thrown");
	}

	@Test(expected = AdcDeviceException.class)
	public void testBuildMissingRequiredPath() {
		builder.setChannelIndex(1);

		builder.build();

		fail("ADC Device Exception should be thrown");
	}

	@Test(expected = AdcDeviceException.class)
	public void testBuildIncompatibleParams() {
		builder.setChannelIndex(3);
		builder.setPath("path");
		builder.setDeviceType(DeviceType.FX30);

		builder.build();

		fail("ADC Device Exception should be thrown");
	}
}
