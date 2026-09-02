package es.amplia.oda.hardware.diozero.analog.devices.fx30;

import com.diozero.api.PinInfo;
import es.amplia.oda.core.commons.adc.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class Fx30AnalogInputDeviceFactoryTest {

	private static final int channelIndex = 1;
	private static final String name = "testDevice";
	private static final DeviceType deviceType = DeviceType.DEFAULT;
	private static final boolean activeLow = true;
	private static final String path = "";

	private Fx30AnalogInputDeviceFactory factory;

	@Mock
	PinInfo mockedInfo;

	@BeforeEach
	public void prepareForTest() {
		factory = new Fx30AnalogInputDeviceFactory(name, path, activeLow, deviceType);
	}

	@Test
	public void testCreateAnalogInputDevice() throws Exception {
		when(mockedInfo.getDeviceNumber()).thenReturn(channelIndex);

		List<List<?>> deviceArgs = new ArrayList<>();
		try (MockedConstruction<Fx30AnalogInputDevice> deviceCons = mockConstruction(Fx30AnalogInputDevice.class,
				(mock, mctx) -> deviceArgs.add(new ArrayList<>(mctx.arguments())))) {

			factory.createAnalogInputDevice(name, mockedInfo);

			assertEquals(1, deviceCons.constructed().size());
			assertEquals(Arrays.asList(factory, name,
					DioZeroAdcPinMapper.mapChannelIndexToDevicePin(channelIndex), channelIndex, path, 1.8f),
					deviceArgs.get(0));
		}
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
