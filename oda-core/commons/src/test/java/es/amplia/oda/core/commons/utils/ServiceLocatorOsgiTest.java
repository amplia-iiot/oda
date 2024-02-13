package es.amplia.oda.core.commons.utils;

import lombok.Value;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceLocatorOsgi.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ServiceLocatorOsgiTest {

    @Value
    private static class TestService {
        int id;
        String name;
        double value;
    }

    @Mock
    private BundleContext mockedContext;

    private ServiceLocatorOsgi<TestService> testServiceLocator;

    @Mock
    private ServiceTracker<TestService, TestService> mockedServiceTracker;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(ServiceTracker.class)
                .withParameterTypes(BundleContext.class, Class.class, ServiceTrackerCustomizer.class)
                .withArguments(any(BundleContext.class), eq(TestService.class), any())
                .thenReturn(mockedServiceTracker);

        testServiceLocator = new ServiceLocatorOsgi<>(mockedContext, TestService.class);
    }

    @Test
    public void testConstructor() throws Exception {
        PowerMockito.verifyNew(ServiceTracker.class).withArguments(eq(mockedContext), eq(TestService.class), any());
        verify(mockedServiceTracker).open();
    }

    @Test
    public void testFindAll() {
        TestService[] services =
                new TestService[] {new TestService(1, "test", 50.0), new TestService(2, "other", 99.99)};
        when(mockedServiceTracker.getServices(any(TestService[].class))).thenReturn(services);

        List<TestService> result = testServiceLocator.findAll();

        assertArrayEquals(services, result.toArray());
        verify(mockedServiceTracker).getServices(any(TestService[].class));
    }

    @Test
    public void testClose() {
        testServiceLocator.close();

        verify(mockedServiceTracker).close();
    }
}
