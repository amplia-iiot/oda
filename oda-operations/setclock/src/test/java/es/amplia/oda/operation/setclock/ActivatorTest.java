package es.amplia.oda.operation.setclock;

import es.amplia.oda.operation.api.OperationSetClock;
import es.amplia.oda.statemanager.api.StateManagerProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

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
    private StateManagerProxy mockedStateManager;
    @Mock
    private OperationSetClockImpl mockedSetClock;
    @Mock
    private ServiceRegistration<OperationSetClock> mockedRegistration;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(StateManagerProxy.class).withAnyArguments().thenReturn(mockedStateManager);
        PowerMockito.whenNew(OperationSetClockImpl.class).withAnyArguments().thenReturn(mockedSetClock);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(StateManagerProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OperationSetClockImpl.class).withArguments(eq(mockedStateManager));
        verify(mockedContext).registerService(eq(OperationSetClock.class), eq(mockedSetClock), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "setClockRegistration", mockedRegistration);

        Whitebox.setInternalState(testActivator, "stateManager", mockedStateManager);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
    }
}