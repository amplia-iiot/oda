package es.amplia.oda.operation.refreshinfo;

import es.amplia.oda.operation.api.OperationRefreshInfo;
import es.amplia.oda.statemanager.api.StateManagerProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
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
    private StateManagerProxy mockedStateManager;
    @Mock
    private OperationRefreshInfoImpl mockedRefreshInfo;
    @Mock
    private ServiceRegistration<OperationRefreshInfo> mockedRegistration;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(StateManagerProxy.class).withAnyArguments().thenReturn(mockedStateManager);
        PowerMockito.whenNew(OperationRefreshInfoImpl.class).withAnyArguments().thenReturn(mockedRefreshInfo);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(StateManagerProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OperationRefreshInfoImpl.class).withArguments(eq(mockedStateManager));
        verify(mockedContext).registerService(eq(OperationRefreshInfo.class), eq(mockedRefreshInfo), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "stateManager", mockedStateManager);
        Whitebox.setInternalState(testActivator, "registration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedStateManager).close();
    }
}