package es.amplia.oda.core.commons.osgi.proxies;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OsgiServiceProxy.class)
public class OsgiServiceProxyTest {

    private interface TestService {}

    @Mock
    private ServiceTracker<TestService,TestService> mockedServiceTracker;

    private OsgiServiceProxy<TestService> testProxy;

    @Mock
    private BundleContext mockedContext;
    @Mock
    private TestService mockedTestService;
    @Mock
    private Function<TestService, Integer> mockedMethodToCall;
    @Mock
    private Consumer<TestService> mockedMethodToConsume;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(ServiceTracker.class).withAnyArguments().thenReturn(mockedServiceTracker);

        testProxy = new OsgiServiceProxy<>(TestService.class, mockedContext);
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(testProxy);
        PowerMockito.verifyNew(ServiceTracker.class).withArguments(eq(mockedContext), eq(TestService.class), any());
        verify(mockedServiceTracker).open();
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
        Whitebox.setInternalState(testProxy, "serviceTracker", mockedServiceTracker);

        testProxy.close();

        verify(mockedServiceTracker).close();
    }
}