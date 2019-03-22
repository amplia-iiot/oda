package es.amplia.oda.core.commons.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRegistrationManagerWithKeyOsgiTest {

    private static final String TEST_KEY = "test";
    private static final String TEST_KEY_2 = "test2";

    private interface TestService {}

    private ServiceRegistrationManagerWithKeyOsgi<String, TestService> testRegistrationManager;

    @Mock
    private BundleContext mockedContext;
    @Mock
    private TestService mockedTestService;
    @Mock
    private ServiceRegistration<TestService> mockedRegistration1;
    @Mock
    private ServiceRegistration<TestService> mockedRegistration2;


    @Before
    public void setUp() {
        testRegistrationManager = new ServiceRegistrationManagerWithKeyOsgi<>(mockedContext, TestService.class);
    }

    @Test
    public void testRegister() {
        testRegistrationManager.register(TEST_KEY, mockedTestService);

        verify(mockedContext).registerService(eq(TestService.class), eq(mockedTestService), any());
        assertTrue(getRegistrations().containsKey(TEST_KEY));
    }

    @SuppressWarnings("unchecked")
    private Map<String, ServiceRegistration<TestService>> getRegistrations() {
        return (Map<String, ServiceRegistration<TestService>>)
                Whitebox.getInternalState(testRegistrationManager, "registrations");
    }

    @Test
    public void testUnregister() {
        Map<String, ServiceRegistration<TestService>> registrations = getRegistrations();

        registrations.put(TEST_KEY, mockedRegistration1);

        testRegistrationManager.unregister(TEST_KEY);

        verify(mockedRegistration1).unregister();
        assertTrue(registrations.isEmpty());
    }

    @Test
    public void testUnregisterWithKeyThatNotExists() {
        Map<String, ServiceRegistration<TestService>> registrations = getRegistrations();

        registrations.put(TEST_KEY, mockedRegistration1);

        testRegistrationManager.unregister(TEST_KEY_2);

        assertFalse(registrations.isEmpty());
    }

    @Test
    public void TestUnregisterAll() {
        Map<String, ServiceRegistration<TestService>> registrations = getRegistrations();

        registrations.put(TEST_KEY, mockedRegistration1);
        registrations.put(TEST_KEY_2, mockedRegistration2);

        testRegistrationManager.unregisterAll();

        verify(mockedRegistration1).unregister();
        verify(mockedRegistration2).unregister();
        assertTrue(registrations.isEmpty());
    }
}