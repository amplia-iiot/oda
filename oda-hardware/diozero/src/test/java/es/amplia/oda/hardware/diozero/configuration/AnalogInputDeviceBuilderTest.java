package es.amplia.oda.hardware.diozero.configuration;

import com.diozero.api.AnalogInputDevice;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.DeviceType;

import es.amplia.oda.hardware.diozero.analog.devices.fx30.Fx30AnalogInputDeviceFactory;
import es.amplia.oda.hardware.diozero.analog.devices.owasys.OwasysAnalogInputDeviceFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static es.amplia.oda.hardware.diozero.configuration.AnalogInputDeviceBuilder.DEFAULT_LOW_MODE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mockConstruction;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AnalogInputDeviceBuilderTest {

	private static final int TEST_CHANNEL_INDEX = 1;
	private static final String TEST_NAME = "testDevice";
	private static final String TEST_PATH = "path";
	private static final boolean TEST_LOW_MODE = true;
	private static final float TEST_SCALE = 1;
	private static final DeviceType TEST_DEVICE_TYPE = DeviceType.DEFAULT;


	private final AnalogInputDeviceBuilder builder = AnalogInputDeviceBuilder.newBuilder();


	@Test
	public void testSetChannelIndex() {
		builder.setChannelIndex(TEST_CHANNEL_INDEX);

		assertEquals(TEST_CHANNEL_INDEX, (int) Whitebox.getInternalState(builder, "channelIndex"));
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

		List<List<?>> deviceArgs = new ArrayList<>();
		try (MockedConstruction<AnalogInputDevice> deviceCons = mockConstruction(AnalogInputDevice.class,
				(mock, mctx) -> deviceArgs.add(new ArrayList<>(mctx.arguments())))) {

			AnalogInputDevice device = builder.build();

			assertEquals(1, deviceCons.constructed().size());
			assertEquals(deviceCons.constructed().get(0), device);
			assertEquals(Arrays.asList(TEST_CHANNEL_INDEX, TEST_SCALE), deviceArgs.get(0));
		}
	}

	@Test
	public void testBuildFx30() throws Exception {
		builder.setChannelIndex(TEST_CHANNEL_INDEX);
		builder.setName(TEST_NAME);
		builder.setPath(TEST_PATH);
		builder.setLowMode(TEST_LOW_MODE);
		builder.setDeviceType(DeviceType.FX30);

		List<List<?>> factoryArgs = new ArrayList<>();
		List<List<?>> deviceArgs = new ArrayList<>();
		try (MockedConstruction<Fx30AnalogInputDeviceFactory> factoryCons =
					 mockConstruction(Fx30AnalogInputDeviceFactory.class,
							 (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<AnalogInputDevice> deviceCons = mockConstruction(AnalogInputDevice.class,
					 (mock, mctx) -> deviceArgs.add(new ArrayList<>(mctx.arguments())))) {

			AnalogInputDevice device = builder.build();

			assertEquals(1, factoryCons.constructed().size());
			assertEquals(Arrays.asList(TEST_NAME, TEST_PATH, TEST_LOW_MODE, DeviceType.FX30), factoryArgs.get(0));
			assertEquals(1, deviceCons.constructed().size());
			assertEquals(deviceCons.constructed().get(0), device);
			assertEquals(Arrays.asList(factoryCons.constructed().get(0), TEST_CHANNEL_INDEX, TEST_SCALE),
					deviceArgs.get(0));
		}
	}

	@Test
	public void testBuildFx30Default() throws Exception {
		builder.setChannelIndex(TEST_CHANNEL_INDEX);
		builder.setPath(TEST_PATH);
		builder.setDeviceType(DeviceType.FX30);

		List<List<?>> factoryArgs = new ArrayList<>();
		List<List<?>> deviceArgs = new ArrayList<>();
		try (MockedConstruction<Fx30AnalogInputDeviceFactory> factoryCons =
					 mockConstruction(Fx30AnalogInputDeviceFactory.class,
							 (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<AnalogInputDevice> deviceCons = mockConstruction(AnalogInputDevice.class,
					 (mock, mctx) -> deviceArgs.add(new ArrayList<>(mctx.arguments())))) {

			AnalogInputDevice device = builder.build();

			assertEquals(1, factoryCons.constructed().size());
			assertEquals(Arrays.asList(null, TEST_PATH, DEFAULT_LOW_MODE, DeviceType.FX30), factoryArgs.get(0));
			assertEquals(1, deviceCons.constructed().size());
			assertEquals(deviceCons.constructed().get(0), device);
			assertEquals(Arrays.asList(factoryCons.constructed().get(0), TEST_CHANNEL_INDEX, TEST_SCALE),
					deviceArgs.get(0));
		}
	}

	@Test
	public void testBuildOwa() throws Exception {
		builder.setChannelIndex(TEST_CHANNEL_INDEX);
		builder.setName(TEST_NAME);
		builder.setPath(TEST_PATH);
		builder.setLowMode(TEST_LOW_MODE);
		builder.setDeviceType(DeviceType.OWASYS);

		List<List<?>> factoryArgs = new ArrayList<>();
		List<List<?>> deviceArgs = new ArrayList<>();
		try (MockedConstruction<OwasysAnalogInputDeviceFactory> factoryCons =
					 mockConstruction(OwasysAnalogInputDeviceFactory.class,
							 (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<AnalogInputDevice> deviceCons = mockConstruction(AnalogInputDevice.class,
					 (mock, mctx) -> deviceArgs.add(new ArrayList<>(mctx.arguments())))) {

			AnalogInputDevice device = builder.build();

			assertEquals(1, factoryCons.constructed().size());
			assertEquals(Arrays.asList(TEST_NAME, TEST_PATH, TEST_LOW_MODE, DeviceType.OWASYS), factoryArgs.get(0));
			assertEquals(1, deviceCons.constructed().size());
			assertEquals(deviceCons.constructed().get(0), device);
			assertEquals(Arrays.asList(factoryCons.constructed().get(0), TEST_CHANNEL_INDEX, TEST_SCALE),
					deviceArgs.get(0));
		}
	}

	@Test
	public void testBuildOwasysDefault() throws Exception {
		builder.setChannelIndex(TEST_CHANNEL_INDEX);
		builder.setPath(TEST_PATH);
		builder.setDeviceType(DeviceType.OWASYS);

		List<List<?>> factoryArgs = new ArrayList<>();
		List<List<?>> deviceArgs = new ArrayList<>();
		try (MockedConstruction<OwasysAnalogInputDeviceFactory> factoryCons =
					 mockConstruction(OwasysAnalogInputDeviceFactory.class,
							 (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<AnalogInputDevice> deviceCons = mockConstruction(AnalogInputDevice.class,
					 (mock, mctx) -> deviceArgs.add(new ArrayList<>(mctx.arguments())))) {

			AnalogInputDevice device = builder.build();

			assertEquals(1, factoryCons.constructed().size());
			assertEquals(Arrays.asList(null, TEST_PATH, DEFAULT_LOW_MODE, DeviceType.OWASYS), factoryArgs.get(0));
			assertEquals(1, deviceCons.constructed().size());
			assertEquals(deviceCons.constructed().get(0), device);
			assertEquals(Arrays.asList(factoryCons.constructed().get(0), TEST_CHANNEL_INDEX, TEST_SCALE),
					deviceArgs.get(0));
		}
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
