package es.amplia.oda.operation.update;

import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.update.internal.OperationUpdateEventHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ActivatorTest {

    private final Activator activator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceRegistration<OperationUpdate> mockedRegistration;
    @Mock
    private ServiceRegistration<EventHandler> eventHandlerServiceRegistration;

    @Test
    public void testStart() {
        activator.start(mockedContext);

        verify(mockedContext).registerService(eq(EventHandler.class), any(OperationUpdateEventHandler.class), any());
        verify(mockedContext).registerService(eq(OperationUpdate.class), any(OperationUpdate.class), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(activator, "operationUpdateRegistration", mockedRegistration);
        Whitebox.setInternalState(activator, "eventHandlerServiceRegistration", eventHandlerServiceRegistration);

        activator.stop(mockedContext);

        verify(mockedRegistration).unregister();
    }
}