package es.amplia.oda.hardware.jdkdio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.jdkdio.configuration.JdkDioConfigurationHandler;
import es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

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
    private JdkDioConfigurationHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private ServiceRegistration<GpioService> mockedRegistration;

    @Test
    public void start() throws Exception {
        /*PowerMockito.whenNew(JdkDioGpioService.class).withAnyArguments().thenReturn(mockedGpioService);
        PowerMockito.whenNew(JdkDioConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);
        when(mockedContext.registerService(eq(GpioService.class), any(), any())).thenReturn(mockedRegistration);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(JdkDioGpioService.class).withNoArguments();
        PowerMockito.verifyNew(JdkDioConfigurationHandler.class).withArguments(eq(mockedGpioService));
        verify(mockedContext).registerService(eq(GpioService.class), eq(mockedGpioService), any());
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler),
                        eq(Collections.singletonList(mockedRegistration)));*/
    }

    @Test
    public void stop() {
        /*Whitebox.setInternalState(testActivator, "gpioService", mockedGpioService);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "gpioServiceRegistration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedConfigBundle).close();
        verify(mockedGpioService).release();*/
    }
}