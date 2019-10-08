package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.EventCollector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EventDispatcherFactoryImpl.class)
public class EventDispatcherFactoryImplTest {

    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private OpenGateConnector mockedConnector;
    @InjectMocks
    private EventDispatcherFactoryImpl testFactory;

    @Mock
    private EventParserImpl mockedEventParser;
    @Mock
    private EventParserReducedOutputImpl mockedReducedEventParser;
    @Mock
    private EventDispatcherImpl mockedEventDispatcher;
    @Mock
    private EventCollectorImpl mockedEventCollector;


    @Test
    public void testCreateEventCollectorNotReduced() throws Exception {
        PowerMockito.whenNew(EventParserImpl.class).withAnyArguments().thenReturn(mockedEventParser);
        PowerMockito.whenNew(EventDispatcherImpl.class).withAnyArguments().thenReturn(mockedEventDispatcher);
        PowerMockito.whenNew(EventCollectorImpl.class).withAnyArguments().thenReturn(mockedEventCollector);

        EventCollector eventCollector = testFactory.createEventCollector(false);

        assertEquals(mockedEventCollector, eventCollector);
        PowerMockito.verifyNew(EventParserImpl.class).withArguments(eq(mockedDeviceInfoProvider));
        PowerMockito.verifyNew(EventDispatcherImpl.class)
                .withArguments(eq(mockedEventParser), eq(mockedSerializer), eq(mockedConnector));
        PowerMockito.verifyNew(EventCollectorImpl.class).withArguments(eq(mockedEventDispatcher));
    }

    @Test
    public void testCreateEventCollectorReduced() throws Exception {
        PowerMockito.whenNew(EventParserReducedOutputImpl.class).withAnyArguments().thenReturn(mockedReducedEventParser);
        PowerMockito.whenNew(EventDispatcherImpl.class).withAnyArguments().thenReturn(mockedEventDispatcher);
        PowerMockito.whenNew(EventCollectorImpl.class).withAnyArguments().thenReturn(mockedEventCollector);

        EventCollector eventCollector = testFactory.createEventCollector(true);

        assertEquals(mockedEventCollector, eventCollector);
        PowerMockito.verifyNew(EventParserReducedOutputImpl.class).withArguments(eq(mockedDeviceInfoProvider));
        PowerMockito.verifyNew(EventDispatcherImpl.class)
                .withArguments(eq(mockedReducedEventParser), eq(mockedSerializer), eq(mockedConnector));
        PowerMockito.verifyNew(EventCollectorImpl.class).withArguments(eq(mockedEventDispatcher));
    }
}