package es.amplia.oda.comms.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceRegistration<HttpClientFactory> mockedRegistration;

    @Test
    public void testStart() {
        testActivator.start(mockedContext);

        verify(mockedContext).registerService(eq(HttpClientFactory.class), isA(HttpClientFactory.class), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "httpClientFactoryServiceRegistration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
    }
}