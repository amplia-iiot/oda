package es.amplia.oda.core.commons.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceListener;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ServiceListenerBundleTest {

    private interface TestService {}

    @Mock
    private BundleContext mockedContext;
    @Mock
    private Runnable mockedAction;

    private ServiceListenerBundle<TestService> testServiceListener;

    @Mock
    private ServiceListener mockedListener;

    @Before
    public void setUp() {
        testServiceListener = new ServiceListenerBundle<>(mockedContext, TestService.class, mockedAction);
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(testServiceListener);

        verify(mockedContext)
                .addServiceListener(any(ServiceListener.class),
                        eq(String.format("(%s=%s)", Constants.OBJECTCLASS, TestService.class.getName())));
    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testServiceListener, "serviceListener", mockedListener);

        testServiceListener.close();

        verify(mockedContext).removeServiceListener(eq(mockedListener));
    }
}