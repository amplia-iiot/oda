package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.interfaces.SerializerProvider;
import es.amplia.oda.core.commons.utils.Scheduler;
import es.amplia.oda.dispatcher.opengate.EventCollector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private Serializer mockedSerializer;
    @Mock
    private Scheduler mockedScheduler;


    @Test
    public void testCreateEventCollectorNotReduced() throws Exception {
        List<List<?>> eventParserArgs = new ArrayList<>();
        List<List<?>> eventDispatcherArgs = new ArrayList<>();
        List<List<?>> eventCollectorArgs = new ArrayList<>();
        try (MockedConstruction<EventParserImpl> eventParserCons = mockConstruction(EventParserImpl.class,
                     (mock, mctx) -> eventParserArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<EventDispatcherImpl> eventDispatcherCons =
                     mockConstruction(EventDispatcherImpl.class,
                             (mock, mctx) -> eventDispatcherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<EventCollectorImpl> eventCollectorCons =
                     mockConstruction(EventCollectorImpl.class,
                             (mock, mctx) -> eventCollectorArgs.add(new ArrayList<>(mctx.arguments())))) {
            when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);

            EventCollector eventCollector = testFactory.createEventCollector(false, TEST_CONTENT_TYPE);

            assertEquals(eventCollectorCons.constructed().get(0), eventCollector);
            assertEquals(1, eventParserCons.constructed().size());
            assertEquals(mockedDeviceInfoProvider, eventParserArgs.get(0).get(0));
            verify(mockedSerializerProvider).getSerializer(eq(TEST_CONTENT_TYPE));
            assertEquals(1, eventDispatcherCons.constructed().size());
            assertEquals(eventParserCons.constructed().get(0), eventDispatcherArgs.get(0).get(0));
            assertEquals(mockedSerializer, eventDispatcherArgs.get(0).get(1));
            assertEquals(TEST_CONTENT_TYPE, eventDispatcherArgs.get(0).get(2));
            assertEquals(mockedConnector, eventDispatcherArgs.get(0).get(3));
            assertEquals(mockedScheduler, eventDispatcherArgs.get(0).get(4));
            assertEquals(1, eventCollectorCons.constructed().size());
            assertEquals(eventDispatcherCons.constructed().get(0), eventCollectorArgs.get(0).get(0));
        }
    }

    @Test
    public void testCreateEventCollectorReduced() throws Exception {
        List<List<?>> eventParserArgs = new ArrayList<>();
        List<List<?>> eventDispatcherArgs = new ArrayList<>();
        List<List<?>> eventCollectorArgs = new ArrayList<>();
        try (MockedConstruction<EventParserReducedOutputImpl> eventParserCons =
                     mockConstruction(EventParserReducedOutputImpl.class,
                             (mock, mctx) -> eventParserArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<EventDispatcherImpl> eventDispatcherCons =
                     mockConstruction(EventDispatcherImpl.class,
                             (mock, mctx) -> eventDispatcherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<EventCollectorImpl> eventCollectorCons =
                     mockConstruction(EventCollectorImpl.class,
                             (mock, mctx) -> eventCollectorArgs.add(new ArrayList<>(mctx.arguments())))) {
            when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);

            EventCollector eventCollector = testFactory.createEventCollector(true, TEST_CONTENT_TYPE);

            assertEquals(eventCollectorCons.constructed().get(0), eventCollector);
            assertEquals(1, eventParserCons.constructed().size());
            assertEquals(mockedDeviceInfoProvider, eventParserArgs.get(0).get(0));
            verify(mockedSerializerProvider).getSerializer(eq(TEST_CONTENT_TYPE));
            assertEquals(1, eventDispatcherCons.constructed().size());
            assertEquals(eventParserCons.constructed().get(0), eventDispatcherArgs.get(0).get(0));
            assertEquals(mockedSerializer, eventDispatcherArgs.get(0).get(1));
            assertEquals(TEST_CONTENT_TYPE, eventDispatcherArgs.get(0).get(2));
            assertEquals(mockedConnector, eventDispatcherArgs.get(0).get(3));
            assertEquals(mockedScheduler, eventDispatcherArgs.get(0).get(4));
            assertEquals(1, eventCollectorCons.constructed().size());
            assertEquals(eventDispatcherCons.constructed().get(0), eventCollectorArgs.get(0).get(0));
        }
    }
}
