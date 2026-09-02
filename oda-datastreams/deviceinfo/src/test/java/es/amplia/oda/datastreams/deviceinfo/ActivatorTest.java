package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandProcessorImpl;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfigurationHandler;

import es.amplia.oda.datastreams.deviceinfo.configuration.ScriptsLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private DeviceInfoDatastreamsGetter mockedDeviceDatastreamsGetter;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private ServiceRegistration<DeviceInfoProvider> mockedDeviceInfoProviderRegistration;

    @Test
    public void testStart() throws Exception {
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();
        try (MockedConstruction<CommandProcessorImpl> commandProcessorCons =
                     mockConstruction(CommandProcessorImpl.class);
             MockedConstruction<DeviceInfoDatastreamsGetter> getterCons =
                     mockConstruction(DeviceInfoDatastreamsGetter.class);
             MockedConstruction<ScriptsLoader> scriptsLoaderCons = mockConstruction(ScriptsLoader.class);
             MockedConstruction<DeviceInfoConfigurationHandler> configHandlerCons =
                     mockConstruction(DeviceInfoConfigurationHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())))) {
            when(mockedContext.registerService(eq(DeviceInfoProvider.class), any(), any()))
                    .thenReturn(mockedDeviceInfoProviderRegistration);
            when(mockedContext.getBundles()).thenReturn(new Bundle[0]);

            testActivator.start(mockedContext);

            assertEquals(1, commandProcessorCons.constructed().size());
            assertEquals(1, getterCons.constructed().size());
            assertEquals(1, scriptsLoaderCons.constructed().size());
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(getterCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(scriptsLoaderCons.constructed().get(0), configHandlerArgs.get(0).get(1));
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
            assertEquals(Collections.singletonList(mockedDeviceInfoProviderRegistration),
                    configBundleArgs.get(0).get(2));
            verify(mockedContext).registerService(eq(DeviceInfoProvider.class),
                    eq(getterCons.constructed().get(0)), eq(null));
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "deviceIdProviderRegistration", mockedDeviceInfoProviderRegistration);
        Whitebox.setInternalState(testActivator, "deviceInfoDatastreamsGetter", mockedDeviceDatastreamsGetter);

        testActivator.stop(mockedContext);

        verify(mockedDeviceDatastreamsGetter).unregister();
        verify(mockedDeviceInfoProviderRegistration).unregister();
        verify(mockedConfigBundle).close();
    }
}
