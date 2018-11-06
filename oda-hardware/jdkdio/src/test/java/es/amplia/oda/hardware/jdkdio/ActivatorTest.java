package es.amplia.oda.hardware.jdkdio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.hardware.jdkdio.configuration.JDkDioConfigurationHandler;
import es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    @Mock
    private BundleContext mockedContext;
    @InjectMocks
    private Activator testActivator;

    @Mock
    private JdkDioGpioService mockedGpioService;
    @Mock
    private JDkDioConfigurationHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundle mockedConfigBundle;
    @Mock
    private ServiceRegistration<GpioService> mockedRegistration;

    @Test
    public void start() throws Exception {
        PowerMockito.whenNew(JdkDioGpioService.class).withAnyArguments().thenReturn(mockedGpioService);
        PowerMockito.whenNew(JDkDioConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundle.class).withAnyArguments().thenReturn(mockedConfigBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(JdkDioGpioService.class).withNoArguments();
        PowerMockito.verifyNew(JDkDioConfigurationHandler.class).withArguments(eq(mockedGpioService));
        PowerMockito.verifyNew(ConfigurableBundle.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        verify(mockedContext).registerService(eq(GpioService.class), eq(mockedGpioService), any());
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