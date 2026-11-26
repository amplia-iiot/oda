package es.amplia.oda.core.commons.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ServiceRegistrationManagerOsgiTest {

    private interface TestService {}

    private ServiceRegistrationManagerOsgi<TestService> testServiceServiceRegistrationManager;

    @Mock
    private BundleContext mockedContext;
    @Mock
    private TestService mockedTestService;
    @Mock
    private ServiceRegistration<TestService> mockedRegistration1;
    @Mock
    private ServiceRegistration<TestService> mockedRegistration2;

    @BeforeEach
    public void setUp() {
        testServiceServiceRegistrationManager = new ServiceRegistrationManagerOsgi<>(mockedContext, TestService.class);
    }

    @Test
    public void testRegister() {
        List<ServiceRegistration<TestService>> spiedRegistrationList = spy(new ArrayList<>());
        Whitebox.setInternalState(testServiceServiceRegistrationManager, "registrations", spiedRegistrationList);

        when(mockedContext.registerService(eq(TestService.class), any(TestService.class), any()))
                .thenReturn(mockedRegistration1);

        testServiceServiceRegistrationManager.register(mockedTestService);

        verify(mockedContext).registerService(eq(TestService.class), eq(mockedTestService), any());
        verify(spiedRegistrationList).add(mockedRegistration1);
    }

    @Test
    public void testUnregister() {
        List<ServiceRegistration<TestService>> registrationList = new ArrayList<>();
        registrationList.add(mockedRegistration1);
        registrationList.add(mockedRegistration2);
        List<ServiceRegistration<TestService>> spiedRegistrations = spy(registrationList);

        Whitebox.setInternalState(testServiceServiceRegistrationManager, "registrations", spiedRegistrations);

        testServiceServiceRegistrationManager.unregister();

        verify(mockedRegistration1).unregister();
        verify(mockedRegistration2).unregister();
        verify(spiedRegistrations).clear();
    }
}