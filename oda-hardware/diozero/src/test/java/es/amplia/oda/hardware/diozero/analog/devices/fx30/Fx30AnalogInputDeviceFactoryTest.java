package es.amplia.oda.hardware.diozero.analog.devices.fx30;

import com.diozero.api.PinInfo;
import es.amplia.oda.core.commons.adc.DeviceType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Fx30AnalogInputDeviceFactory.class)
public class Fx30AnalogInputDeviceFactoryTest {

	private static final int channelIndex = 1;
	private static final String name = "testDevice";
	private static final DeviceType deviceType = DeviceType.DEFAULT;
	private static final boolean activeLow = true;
	private static final String path = "";

	private Fx30AnalogInputDeviceFactory factory;

	@Mock
	PinInfo mockedInfo;
	@Mock
	Fx30AnalogInputDevice mockedDevice;

	@Before
	public void prepareForTest() {
		factory = new Fx30AnalogInputDeviceFactory(name, path, activeLow, deviceType);
	}

	@Test
	public void testCreateAnalogInputDevice() throws Exception {
		when(mockedInfo.getDeviceNumber()).thenReturn(channelIndex);
		whenNew(Fx30AnalogInputDevice.class).withAnyArguments().thenReturn(mockedDevice);

		factory.createAnalogInputDevice(name, mockedInfo);

		verifyNew(Fx30AnalogInputDevice.class).withArguments(
				eq(factory),
				eq(name),
				eq(DioZeroAdcPinMapper.mapChannelIndexToDevicePin(channelIndex)),
				eq(channelIndex),
				eq(path),
				eq(1.8f));
	}

	@Test
	public void testGetVRef() {
		assertEquals(Float.valueOf(activeLow ? 5f: 10f), Float.valueOf(factory.getVRef()));
	}

	@Test
	public void testGetName() {
		assertEquals(name, factory.getName());
	}

	@Test
	public void testGetBoardPinInfo() {
		assertNotNull(factory.getBoardPinInfo());
	}
}
