package es.amplia.oda.operation.setclock;

import es.amplia.oda.core.commons.utils.DatastreamsSettersFinderImpl;
import es.amplia.oda.core.commons.utils.DatastreamsSettersLocatorOsgi;
import es.amplia.oda.operation.api.OperationSetClock;

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
    private DatastreamsSettersLocatorOsgi mockedLocator;
    @Mock
    private DatastreamsSettersFinderImpl mockedFinder;
    @Mock
    private OperationSetClockImpl mockedSetClock;
    @Mock
    private ServiceRegistration<OperationSetClock> mockedRegistration;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(DatastreamsSettersLocatorOsgi.class).withAnyArguments().thenReturn(mockedLocator);
        PowerMockito.whenNew(DatastreamsSettersFinderImpl.class).withAnyArguments().thenReturn(mockedFinder);
        PowerMockito.whenNew(OperationSetClockImpl.class).withAnyArguments().thenReturn(mockedSetClock);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(DatastreamsSettersLocatorOsgi.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(DatastreamsSettersFinderImpl.class).withArguments(eq(mockedLocator));
        PowerMockito.verifyNew(OperationSetClockImpl.class).withArguments(eq(mockedFinder));
        verify(mockedContext).registerService(eq(OperationSetClock.class), eq(mockedSetClock), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "registration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
    }
}