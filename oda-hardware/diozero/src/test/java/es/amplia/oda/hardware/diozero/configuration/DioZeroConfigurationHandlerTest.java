package es.amplia.oda.hardware.diozero.configuration;

import com.diozero.internal.provider.AnalogInputDeviceInterface;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcChannel;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcService;
import es.amplia.oda.hardware.diozero.analog.Fx30AnalogInputDeviceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AnalogInputDeviceBuilder.class)
public class DioZeroConfigurationHandlerTest {

	private DioZeroConfigurationHandler handler;
	private Map<String, String> map = new HashMap<>();

	@Mock
	DioZeroAdcService mockedService;
	@Mock
	Fx30AnalogInputDeviceFactory mockedFactory;
	@Mock
	AnalogInputDeviceInterface mockedDevice;

	@Before
	public void setUp() {
		handler = new DioZeroConfigurationHandler(mockedService);
		map.put("1", "deviceType:adc.ADCChannel,name:testDevice,pinNumber:5,lowMode:false,path:something,device:FX30");

	}

	@Test
	public void testApplyConfiguration() throws Exception {
		/*Whitebox.setInternalState(handler, "gpioPinsConfiguration", map);
		doNothing().when(mockedService).addConfiguredPin(any());
		whenNew(Fx30AnalogInputDeviceFactory.class).withAnyArguments().thenReturn(mockedFactory);
		when(mockedFactory.provisionAnalogInputDevice(any())).thenReturn(mockedDevice);

		handler.applyConfiguration();

		verify(mockedService).addConfiguredPin(any(DioZeroAdcChannel.class));*/
	}
}
