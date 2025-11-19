package es.amplia.oda.operation.rebootequipment;

import es.amplia.oda.core.commons.osgi.proxies.OpenGateConnectorProxy;
import es.amplia.oda.core.commons.osgi.proxies.ResponseDispatcherProxy;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinderImpl;
import es.amplia.oda.event.api.EventDispatcherProxy;
import es.amplia.oda.operation.api.CustomOperation;
import es.amplia.oda.operation.api.OperationRefreshInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private RebootEquipmentImpl mockedRebootEquipment;
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
        PowerMockito.whenNew(RebootEquipmentImpl.class).withAnyArguments().thenReturn(mockedRebootEquipment);

        testActivator.start(mockedContext);

        verify(mockedContext).registerService(eq(CustomOperation.class), eq(mockedRebootEquipment), any());
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