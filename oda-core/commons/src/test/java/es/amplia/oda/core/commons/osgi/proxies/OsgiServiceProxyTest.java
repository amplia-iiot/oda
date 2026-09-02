package es.amplia.oda.core.commons.osgi.proxies;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OsgiServiceProxyTest {

    private interface TestService {}

    private MockedConstruction<ServiceTracker> serviceTrackerConstruction;
    private final List<List<?>> serviceTrackerArgs = new ArrayList<>();
    private ServiceTracker<TestService,TestService> mockedServiceTracker;

    private OsgiServiceProxy<TestService> testProxy;

    @Mock
    private BundleContext mockedContext;
    @Mock
    private Filter mockedFilter;
    @Mock
    private TestService mockedTestService;
    @Mock
    private Function<TestService, Integer> mockedMethodToCall;
    @Mock
    private Consumer<TestService> mockedMethodToConsume;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        serviceTrackerConstruction = mockConstruction(ServiceTracker.class,
                (mock, mctx) -> serviceTrackerArgs.add(new ArrayList<>(mctx.arguments())));

        testProxy = new OsgiServiceProxy<>(TestService.class, mockedContext);
        mockedServiceTracker = serviceTrackerConstruction.constructed().get(0);
    }

    @After
    public void tearDown() {
        serviceTrackerConstruction.close();
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(testProxy);
        assertEquals(1, serviceTrackerConstruction.constructed().size());
        assertEquals(mockedContext, serviceTrackerArgs.get(0).get(0));
        assertEquals(TestService.class, serviceTrackerArgs.get(0).get(1));
        assertEquals(null, serviceTrackerArgs.get(0).get(2));
        verify(mockedServiceTracker).open();
    }

    @Test
    public void testConstructorWithFilter() throws Exception {
        Map<String, String> properties = new HashMap<>();

        reset(mockedContext, mockedServiceTracker);
        when(mockedContext.createFilter(anyString())).thenReturn(mockedFilter);

        testProxy = new OsgiServiceProxy<>(TestService.class, properties, mockedContext);

        assertNotNull(testProxy);
        verify(mockedContext).createFilter(contains(TestService.class.getName()));
        assertEquals(2, serviceTrackerConstruction.constructed().size());
        assertEquals(mockedContext, serviceTrackerArgs.get(1).get(0));
        assertEquals(mockedFilter, serviceTrackerArgs.get(1).get(1));
        verify(serviceTrackerConstruction.constructed().get(1)).open();
    }

    @Test
    public void testConstructorCreateFilterException() throws Exception {
        Map<String, String> properties = new HashMap<>();

        reset(mockedContext, mockedServiceTracker);
        when(mockedContext.createFilter(anyString())).thenThrow(new InvalidSyntaxException("", ""));

        OsgiServiceProxy createFilterFailsProxy = new OsgiServiceProxy<>(TestService.class, properties, mockedContext);

        assertNotNull(createFilterFailsProxy);
        verify(mockedContext).createFilter(contains(TestService.class.getName()));
        assertEquals(2, serviceTrackerConstruction.constructed().size());
        assertEquals(mockedContext, serviceTrackerArgs.get(1).get(0));
        assertEquals(TestService.class, serviceTrackerArgs.get(1).get(1));
        assertEquals(null, serviceTrackerArgs.get(1).get(2));
        verify(serviceTrackerConstruction.constructed().get(1)).open();
    }

    @Test
    public void testCallFirst() {
        when(mockedServiceTracker.getService()).thenReturn(mockedTestService);

        testProxy.callFirst(mockedMethodToCall);

        verify(mockedServiceTracker).getService();
        verify(mockedMethodToCall).apply(eq(mockedTestService));
    }

    @Test
    public void testCallFirstNoService() {
        when(mockedServiceTracker.getService()).thenReturn(null);

        testProxy.callFirst(mockedMethodToCall);

        verify(mockedServiceTracker).getService();
    }

    @Test
    public void testConsumeFirst() {
        when(mockedServiceTracker.getService()).thenReturn(mockedTestService);

        testProxy.consumeFirst(mockedMethodToConsume);

        verify(mockedServiceTracker).getService();
        verify(mockedMethodToConsume).accept(eq(mockedTestService));
    }

    @Test
    public void testConsumeFirstNoService() {
        when(mockedServiceTracker.getService()).thenReturn(null);

        testProxy.consumeFirst(mockedMethodToConsume);

        verify(mockedServiceTracker).getService();
    }

    @Test
    public void testCallAll() {
        TestService[] mockedTestServices = new TestService[] {mockedTestService, mockedTestService, mockedTestService};

        when(mockedServiceTracker.getServices()).thenReturn(mockedTestServices);

        testProxy.callAll(mockedMethodToCall);

        verify(mockedServiceTracker).getServices();
        verify(mockedMethodToCall, times(mockedTestServices.length)).apply(eq(mockedTestService));
    }

    @Test
    public void testCallAllNoServices() {
        when(mockedServiceTracker.getServices()).thenReturn(null);

        testProxy.callAll(mockedMethodToCall);

        verify(mockedServiceTracker).getServices();
    }

    @Test
    public void testConsumeAll() {
        TestService[] mockedTestServices = new TestService[] {mockedTestService, mockedTestService, mockedTestService};

        when(mockedServiceTracker.getServices()).thenReturn(mockedTestServices);

        testProxy.consumeAll(mockedMethodToConsume);

        verify(mockedServiceTracker).getServices();
        verify(mockedMethodToConsume, times(mockedTestServices.length)).accept(eq(mockedTestService));
    }

    @Test
    public void testConsumeAllNoServices() {
        when(mockedServiceTracker.getServices()).thenReturn(null);

        testProxy.consumeAll(mockedMethodToConsume);

        verify(mockedServiceTracker).getServices();
    }

    @Test
    public void testClose() {
        testProxy.close();

        verify(mockedServiceTracker).close();
    }
}
