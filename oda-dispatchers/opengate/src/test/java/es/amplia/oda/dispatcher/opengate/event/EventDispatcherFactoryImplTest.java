package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.interfaces.SerializerProvider;
import es.amplia.oda.core.commons.utils.Scheduler;
import es.amplia.oda.dispatcher.opengate.EventCollector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EventDispatcherFactoryImpl.class)
public class EventDispatcherFactoryImplTest {

    private static final ContentType TEST_CONTENT_TYPE = ContentType.CBOR;


    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private SerializerProvider mockedSerializerProvider;
    @Mock
    private OpenGateConnector mockedConnector;
    @InjectMocks
    private EventDispatcherFactoryImpl testFactory;

    @Mock
    private EventParserImpl mockedEventParser;
    @Mock
    private EventParserReducedOutputImpl mockedReducedEventParser;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private EventDispatcherImpl mockedEventDispatcher;
    @Mock
    private EventCollectorImpl mockedEventCollector;
    @Mock
    private Scheduler mockedScheduler;


    @Test
    public void testCreateEventCollectorNotReduced() throws Exception {
        PowerMockito.whenNew(EventParserImpl.class).withAnyArguments().thenReturn(mockedEventParser);
        PowerMockito.whenNew(EventDispatcherImpl.class).withAnyArguments().thenReturn(mockedEventDispatcher);
        PowerMockito.whenNew(EventCollectorImpl.class).withAnyArguments().thenReturn(mockedEventCollector);
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);

        EventCollector eventCollector = testFactory.createEventCollector(false, TEST_CONTENT_TYPE);

        assertEquals(mockedEventCollector, eventCollector);
        PowerMockito.verifyNew(EventParserImpl.class).withArguments(eq(mockedDeviceInfoProvider));
        verify(mockedSerializerProvider).getSerializer(eq(TEST_CONTENT_TYPE));
        PowerMockito.verifyNew(EventDispatcherImpl.class)
                .withArguments(eq(mockedEventParser), eq(mockedSerializer), eq(TEST_CONTENT_TYPE), eq(mockedConnector), eq(mockedScheduler));
        PowerMockito.verifyNew(EventCollectorImpl.class).withArguments(eq(mockedEventDispatcher));
    }

    @Test
    public void testCreateEventCollectorReduced() throws Exception {
        PowerMockito.whenNew(EventParserReducedOutputImpl.class).withAnyArguments().thenReturn(mockedReducedEventParser);
        PowerMockito.whenNew(EventDispatcherImpl.class).withAnyArguments().thenReturn(mockedEventDispatcher);
        PowerMockito.whenNew(EventCollectorImpl.class).withAnyArguments().thenReturn(mockedEventCollector);
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);

        EventCollector eventCollector = testFactory.createEventCollector(true, TEST_CONTENT_TYPE);

        assertEquals(mockedEventCollector, eventCollector);
        PowerMockito.verifyNew(EventParserReducedOutputImpl.class).withArguments(eq(mockedDeviceInfoProvider));
        verify(mockedSerializerProvider).getSerializer(eq(TEST_CONTENT_TYPE));
        PowerMockito.verifyNew(EventDispatcherImpl.class).withArguments(eq(mockedReducedEventParser),
                eq(mockedSerializer), eq(TEST_CONTENT_TYPE), eq(mockedConnector), eq(mockedScheduler));
        PowerMockito.verifyNew(EventCollectorImpl.class).withArguments(eq(mockedEventDispatcher));
    }
}