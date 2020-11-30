package es.amplia.oda.operation.synchronizeclock;

import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.operation.api.OperationSynchronizeClock;
import es.amplia.oda.operation.synchronizeclock.configuration.SynchronizeConfigurationHandler;
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
    private OperationSynchronizeClockImpl mockedSynchronizeClock;
    @Mock
    private ServiceRegistration<OperationSynchronizeClock> mockedRegistration;
    @Mock
    private SynchronizeConfigurationHandler mockedHandler;
    @Mock
    private ConfigurableBundleImpl mockedBundle;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(StateManagerProxy.class).withAnyArguments().thenReturn(mockedStateManager);
        PowerMockito.whenNew(OperationSynchronizeClockImpl.class).withAnyArguments().thenReturn(mockedSynchronizeClock);
        PowerMockito.whenNew(SynchronizeConfigurationHandler.class).withAnyArguments().thenReturn(mockedHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(StateManagerProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OperationSynchronizeClockImpl.class).withArguments(eq(mockedStateManager));
        PowerMockito.verifyNew(SynchronizeConfigurationHandler.class).withArguments(eq(mockedSynchronizeClock));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedHandler), any());
        verify(mockedContext).registerService(eq(OperationSynchronizeClock.class), eq(mockedSynchronizeClock), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedBundle);
        Whitebox.setInternalState(testActivator, "stateManager", mockedStateManager);
        Whitebox.setInternalState(testActivator, "synchronizeClockRegistration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedBundle).close();
        verify(mockedRegistration).unregister();
        verify(mockedStateManager).close();
    }
}