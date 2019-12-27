package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.*;
import es.amplia.oda.core.commons.interfaces.EventPublisher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdcDatastreamsEventTest {

	private static final String TEST_DATASTREAM = "testDatastream";
	private static final int TEST_INDEX = 1;
	private static final float TEST_VALUE = 99.99f;
	private static final float TEST_MIN = 0.0f;
	private static final float TEST_MAX = 100.f;
	private static final String ADC_DEVICE_EXCEPTION_SHOULD_BE_CAUGHT = "ADC Device Exception should be caught";
	private static final String CHANNEL_FIELD_NAME = "channel";


	@Mock
	private AdcService mockedService;
	@Mock
	private EventPublisher mockedEventPublisher;
	private AdcDatastreamsEvent testEvent;

	@Mock
	private AdcChannel mockedChannel;
	@Mock
	private AdcEvent mockedEvent;
	@Captor
	private ArgumentCaptor<AdcChannelListener> listenerCaptor;


	@Before
	public void prepareForTest() {
		testEvent = new AdcDatastreamsEvent(TEST_DATASTREAM, TEST_INDEX, mockedService, mockedEventPublisher, TEST_MIN,
				TEST_MAX);
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

		verify(mockedEventPublisher).publishEvent(eq(""), eq(TEST_DATASTREAM), eq(new String[0]), anyLong(),
				eq((TEST_MAX - TEST_MIN) * TEST_VALUE - TEST_MIN));
	}

	@Test
	public void testRegisterToEventSourceAdcDeviceExceptionIsCaught() {
		when(mockedService.getChannelByIndex(TEST_INDEX)).thenReturn(mockedChannel);
		doThrow(new AdcDeviceException("")).when(mockedChannel).addAdcPinListener(any());

		testEvent.registerToEventSource();

		assertTrue(ADC_DEVICE_EXCEPTION_SHOULD_BE_CAUGHT, true);
	}

	@Test
	public void testUnregisterFromEventSource() {
		Whitebox.setInternalState(testEvent, CHANNEL_FIELD_NAME, mockedChannel);

		testEvent.unregisterFromEventSource();

		verify(mockedChannel).removeAllAdcPinListener();
	}

	@Test
	public void testUnregisterFromEventSourceWithAdcExceptionIsCaught() {
		Whitebox.setInternalState(testEvent, CHANNEL_FIELD_NAME, mockedChannel);

		doThrow(new AdcDeviceException("")).when(mockedChannel).removeAllAdcPinListener();

		testEvent.unregisterFromEventSource();

		assertTrue(ADC_DEVICE_EXCEPTION_SHOULD_BE_CAUGHT, true);
	}

	@Test
	public void testUnregisterFromEventSourceWithException() {
		Whitebox.setInternalState(testEvent, CHANNEL_FIELD_NAME, mockedChannel);

		doThrow(new ArrayIndexOutOfBoundsException("")).when(mockedChannel).removeAllAdcPinListener();

		testEvent.unregisterFromEventSource();

		assertTrue(ADC_DEVICE_EXCEPTION_SHOULD_BE_CAUGHT, true);
	}
}
