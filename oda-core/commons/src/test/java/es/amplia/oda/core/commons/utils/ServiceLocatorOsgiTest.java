package es.amplia.oda.core.commons.utils;

import lombok.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    private MockedConstruction<ServiceTracker> serviceTrackerConstruction;
    private final List<List<?>> serviceTrackerArgs = new ArrayList<>();
    private ServiceTracker<TestService, TestService> mockedServiceTracker;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        serviceTrackerConstruction = mockConstruction(ServiceTracker.class,
                (mock, mctx) -> serviceTrackerArgs.add(new ArrayList<>(mctx.arguments())));

        testServiceLocator = new ServiceLocatorOsgi<>(mockedContext, TestService.class);
        mockedServiceTracker = serviceTrackerConstruction.constructed().get(0);
    }

    @AfterEach
    public void tearDown() {
        serviceTrackerConstruction.close();
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals(1, serviceTrackerConstruction.constructed().size());
        assertEquals(mockedContext, serviceTrackerArgs.get(0).get(0));
        assertEquals(TestService.class, serviceTrackerArgs.get(0).get(1));
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
