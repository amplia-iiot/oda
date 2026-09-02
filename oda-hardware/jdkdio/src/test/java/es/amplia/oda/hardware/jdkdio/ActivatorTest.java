package es.amplia.oda.hardware.jdkdio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.jdkdio.configuration.JdkDioConfigurationHandler;
import es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    @Mock
    private BundleContext mockedContext;
    @InjectMocks
    private Activator testActivator;

    @Mock
    private JdkDioGpioService mockedGpioService;
    @Mock
    private JdkDioConfigurationHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private ServiceRegistration<GpioService> mockedRegistration;

    @Test
    public void start() throws Exception {
        when(mockedContext.registerService(eq(GpioService.class), any(), any())).thenReturn(mockedRegistration);

        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();

        try (MockedConstruction<JdkDioGpioService> gpioServiceCons = mockConstruction(JdkDioGpioService.class);
             MockedConstruction<JdkDioConfigurationHandler> configHandlerCons =
                     mockConstruction(JdkDioConfigurationHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons = mockConstruction(ConfigurableBundleImpl.class,
                     (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, gpioServiceCons.constructed().size());
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(gpioServiceCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            verify(mockedContext).registerService(eq(GpioService.class), eq(gpioServiceCons.constructed().get(0)), any());
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
            assertEquals(Collections.singletonList(mockedRegistration), configBundleArgs.get(0).get(2));
        }
    }

    @Test
    public void stop() {
        Whitebox.setInternalState(testActivator, "gpioService", mockedGpioService);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "gpioServiceRegistration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedConfigBundle).close();
        verify(mockedGpioService).release();
    }
}
