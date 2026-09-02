package es.amplia.oda.operation.rebootequipment;

import es.amplia.oda.core.commons.osgi.proxies.OpenGateConnectorProxy;
import es.amplia.oda.core.commons.osgi.proxies.ResponseDispatcherProxy;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinderImpl;
import es.amplia.oda.event.api.EventDispatcherProxy;
import es.amplia.oda.operation.api.CustomOperation;
import es.amplia.oda.operation.api.OperationRefreshInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceRegistration<OperationRefreshInfo> mockedRegistration;
    @Mock
    private ServiceRegistration<EventHandler> mockedEventHandlerServiceRegistration;
    @Mock
    private ResponseDispatcherProxy mockedResponseDispatcher;
    @Mock
    private OpenGateConnectorProxy mockedOgConnector;
    @Mock
    private DatastreamsGettersFinderImpl mockedDatastreamsGettersFinder;
    @Mock
    private EventDispatcherProxy mockedEventDispatcher;

    @Test
    public void testStart() throws Exception {
        try (MockedConstruction<RebootEquipmentImpl> rebootEquipmentCons =
                     mockConstruction(RebootEquipmentImpl.class)) {

            testActivator.start(mockedContext);

            assertEquals(1, rebootEquipmentCons.constructed().size());
            verify(mockedContext).registerService(eq(CustomOperation.class),
                    eq(rebootEquipmentCons.constructed().get(0)), any());
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "registration", mockedRegistration);
        Whitebox.setInternalState(testActivator, "eventHandlerServiceRegistration", mockedEventHandlerServiceRegistration);
        Whitebox.setInternalState(testActivator, "responseDispatcher", mockedResponseDispatcher);
        Whitebox.setInternalState(testActivator, "ogConnector", mockedOgConnector);
        Whitebox.setInternalState(testActivator, "datastreamsGettersFinder", mockedDatastreamsGettersFinder);
        Whitebox.setInternalState(testActivator, "eventDispatcher", mockedEventDispatcher);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedEventHandlerServiceRegistration).unregister();
        verify(mockedResponseDispatcher).close();
        verify(mockedOgConnector).close();
        verify(mockedDatastreamsGettersFinder).close();
        verify(mockedEventDispatcher).close();
    }
}
