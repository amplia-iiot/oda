package es.amplia.oda.datastreams.diozero.datastreams.adc;

import es.amplia.oda.core.commons.diozero.AdcChannel;
import es.amplia.oda.core.commons.diozero.AdcDeviceException;
import es.amplia.oda.core.commons.diozero.AdcEvent;
import es.amplia.oda.core.commons.diozero.AdcService;
import es.amplia.oda.event.api.EventDispatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
public class AdcDatastreamsEventTest {
	private final String datastreamId = "temperature";
	private final int pinIndex = 0;
	@Mock
	AdcService mockedService;
	@Mock
	EventDispatcher mockedDispatcher;
	private AdcDatastreamsEvent testEvent;
	@Mock
	AdcChannel mockedChannel;
	@Mock
	AdcEvent mockedEvent;

	@Before
	public void prepareForTest() {
		testEvent = new AdcDatastreamsEvent(datastreamId, pinIndex, mockedService, mockedDispatcher);
	}

	@Test
	public void testRegisterToEventSource() {
		when(mockedService.getChannelByIndex(pinIndex)).thenReturn(mockedChannel);
		doNothing().when(mockedChannel).addAdcPinListener(any());

		testEvent.registerToEventSource();
	}

	@Test
	public void testRegisterToEventSourceWithException() {
		when(mockedService.getChannelByIndex(pinIndex)).thenReturn(mockedChannel);
		doThrow(new AdcDeviceException("")).when(mockedChannel).addAdcPinListener(any());

		testEvent.registerToEventSource();
	}

	@Test
	public void testUnregisterFromEventSource() {
		doNothing().when(mockedChannel).removeAllAdcPinListener();

		testEvent.unregisterFromEventSource();
	}

	@Test
	public void testUnregisterFromEventSourceWithAdcException() {
		Whitebox.setInternalState(testEvent, "channel", mockedChannel);
		doThrow(new AdcDeviceException("")).when(mockedChannel).removeAllAdcPinListener();

		testEvent.unregisterFromEventSource();
	}

	@Test
	public void testUnregisterFromEventSourceWithException() {
		Whitebox.setInternalState(testEvent, "channel", mockedChannel);
		doThrow(new ArrayIndexOutOfBoundsException("")).when(mockedChannel).removeAllAdcPinListener();

		testEvent.unregisterFromEventSource();
	}

	@Test
	public void testPublish() {
		testEvent.publish("", datastreamId, Collections.emptyList(), System.currentTimeMillis(), mockedEvent);

		verify(mockedDispatcher).publish(any());
	}
}
