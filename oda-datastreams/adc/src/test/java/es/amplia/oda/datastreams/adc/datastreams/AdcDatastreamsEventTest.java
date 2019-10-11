package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.*;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdcDatastreamsEventTest {

	private final static String TEST_DATASTREAM = "testDatastream";
	private final static int TEST_INDEX = 1;
	private final static float TEST_VALUE = 99.99f;


	@Mock
	private AdcService mockedService;
	@Mock
	private EventDispatcher mockedDispatcher;
	private AdcDatastreamsEvent testEvent;

	@Mock
	private AdcChannel mockedChannel;
	@Mock
	private AdcEvent mockedEvent;
	@Captor
	private ArgumentCaptor<AdcChannelListener> listenerCaptor;
	@Captor
	private ArgumentCaptor<Event> eventCaptor;


	@Before
	public void prepareForTest() {
		testEvent = new AdcDatastreamsEvent(TEST_DATASTREAM, TEST_INDEX, mockedService, mockedDispatcher);
	}

	@Test
	public void testRegisterToEventSource() {
		when(mockedService.getChannelByIndex(TEST_INDEX)).thenReturn(mockedChannel);

		testEvent.registerToEventSource();

		verify(mockedService).getChannelByIndex(eq(TEST_INDEX));
		verify(mockedChannel).addAdcPinListener(listenerCaptor.capture());
		AdcChannelListener capturedListener = listenerCaptor.getValue();

		when(mockedEvent.getScaledValue()).thenReturn(TEST_VALUE);

		capturedListener.channelValueChanged(mockedEvent);

		verify(mockedDispatcher).publish(eventCaptor.capture());
		Event capturedEvent = eventCaptor.getValue();
		assertEquals("", capturedEvent.getDeviceId());
		assertEquals(TEST_DATASTREAM, capturedEvent.getDatastreamId());
		assertArrayEquals(new String[]{}, capturedEvent.getPath());
		assertEquals(TEST_VALUE, capturedEvent.getValue());
	}

	@Test
	public void testRegisterToEventSourceAdcDeviceExceptionIsCaught() {
		when(mockedService.getChannelByIndex(TEST_INDEX)).thenReturn(mockedChannel);
		doThrow(new AdcDeviceException("")).when(mockedChannel).addAdcPinListener(any());

		testEvent.registerToEventSource();

		assertTrue("ADC Device Exception should be caught", true);
	}

	@Test
	public void testUnregisterFromEventSource() {
		Whitebox.setInternalState(testEvent, "channel", mockedChannel);

		testEvent.unregisterFromEventSource();

		verify(mockedChannel).removeAllAdcPinListener();
	}

	@Test
	public void testUnregisterFromEventSourceWithAdcExceptionIsCaught() {
		Whitebox.setInternalState(testEvent, "channel", mockedChannel);

		doThrow(new AdcDeviceException("")).when(mockedChannel).removeAllAdcPinListener();

		testEvent.unregisterFromEventSource();

		assertTrue("ADC Device Exception should be caught", true);
	}

	@Test
	public void testUnregisterFromEventSourceWithException() {
		Whitebox.setInternalState(testEvent, "channel", mockedChannel);

		doThrow(new ArrayIndexOutOfBoundsException("")).when(mockedChannel).removeAllAdcPinListener();

		testEvent.unregisterFromEventSource();

		assertTrue("ADC Device Exception should be caught", true);
	}
}
