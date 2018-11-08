package es.amplia.oda.core.commons.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRegistrationManagerOsgiTest {

    private interface TestService {}

    private ServiceRegistrationManagerOsgi<TestService> testServiceServiceRegistrationManager;

    @Mock
    private BundleContext mockedContext;
    @Mock
    private TestService mockedTestService;
    @Mock
    private ServiceRegistration<TestService> mockedRegistration;

    @Before
    public void setUp() {
        testServiceServiceRegistrationManager = new ServiceRegistrationManagerOsgi<>(mockedContext, TestService.class);
    }

    @Test
    public void testRegister() {
        testServiceServiceRegistrationManager.register(mockedTestService);

        verify(mockedContext).registerService(eq(TestService.class), eq(mockedTestService), any());
    }

    @Test
    public void testUnregister() {
        Whitebox.setInternalState(testServiceServiceRegistrationManager, "registration", mockedRegistration);

        testServiceServiceRegistrationManager.unregister();

        verify(mockedRegistration).unregister();
    }
}