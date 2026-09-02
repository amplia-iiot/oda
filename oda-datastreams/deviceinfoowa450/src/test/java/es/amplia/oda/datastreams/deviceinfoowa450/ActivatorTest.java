package es.amplia.oda.datastreams.deviceinfoowa450;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandProcessorImpl;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.datastreams.deviceinfoowa450.configuration.DeviceInfoConfigurationHandler;
import es.amplia.oda.datastreams.deviceinfoowa450.configuration.ScriptsLoader;
import es.amplia.oda.datastreams.deviceinfoowa450.datastreams.DatastreamGetterTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private DatastreamsGetter datastreamsGetterForDeviceId;
    @Mock
    private DatastreamsGetter datastreamsGetterForSerialNumber;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedDatastreamsGetterRegistrationForSerialNumber;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedDatastreamsGetterRegistrationForDeviceId;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForSoftware;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForClock;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForUptime;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForTemperatureValue;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForTemperatureStatus;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForCpuStatus;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForCpuUsage;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForCpuTotal;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForRamUsage;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForRamTotal;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForDiskUsage;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedRegistrationForDiskTotal;
    @Mock
    private ServiceRegistration<DeviceInfoProvider> mockedDeviceInfoProviderRegistration;

    @Test
    public void testStart() throws Exception {
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();
        try (MockedConstruction<CommandProcessorImpl> commandProcessorCons =
                     mockConstruction(CommandProcessorImpl.class);
             MockedConstruction<DeviceInfoOwa450DatastreamsGetter> getterCons =
                     mockConstruction(DeviceInfoOwa450DatastreamsGetter.class,
                             (mock, mctx) -> {
                                 when(mock.getDatastreamsGetterForDeviceId()).thenReturn(datastreamsGetterForDeviceId);
                                 when(mock.getDatastreamsGetterForSerialNumber())
                                         .thenReturn(datastreamsGetterForSerialNumber);
                             });
             MockedConstruction<ScriptsLoader> scriptsLoaderCons = mockConstruction(ScriptsLoader.class);
             MockedConstruction<DeviceInfoConfigurationHandler> configHandlerCons =
                     mockConstruction(DeviceInfoConfigurationHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DatastreamGetterTemplate> getterTemplateCons =
                     mockConstruction(DatastreamGetterTemplate.class)) {
            when(mockedContext.registerService(eq(DeviceInfoProvider.class), any(), any()))
                    .thenReturn(mockedDeviceInfoProviderRegistration);
            when(mockedContext.getBundles()).thenReturn(new Bundle[0]);

            testActivator.start(mockedContext);

            assertEquals(1, commandProcessorCons.constructed().size());
            assertEquals(1, getterCons.constructed().size());
            assertEquals(1, scriptsLoaderCons.constructed().size());
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(getterCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(scriptsLoaderCons.constructed().get(0), configHandlerArgs.get(0).get(1));
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
            assertEquals(Collections.singletonList(mockedDeviceInfoProviderRegistration),
                    configBundleArgs.get(0).get(2));
            verify(mockedContext).registerService(eq(DeviceInfoProvider.class),
                    eq(getterCons.constructed().get(0)), eq(null));
            assertEquals(14, getterTemplateCons.constructed().size());
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "deviceIdProviderRegistration", mockedDeviceInfoProviderRegistration);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForDeviceId",
                mockedDatastreamsGetterRegistrationForDeviceId);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForSerialNumber",
                mockedDatastreamsGetterRegistrationForSerialNumber);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForSoftware",
                mockedRegistrationForSoftware);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForClock",
                mockedRegistrationForClock);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForUptime",
                mockedRegistrationForUptime);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForTemperatureValue",
                mockedRegistrationForTemperatureValue);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForTemperatureStatus",
                mockedRegistrationForTemperatureStatus);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForCpuStatus",
                mockedRegistrationForCpuStatus);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForCpuUsage",
                mockedRegistrationForCpuUsage);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForCpuTotal",
                mockedRegistrationForCpuTotal);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForRamUsage",
                mockedRegistrationForRamUsage);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForRamTotal",
                mockedRegistrationForRamTotal);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForDiskUsage",
                mockedRegistrationForDiskUsage);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForDiskTotal",
                mockedRegistrationForDiskTotal);


        testActivator.stop(mockedContext);

        verify(mockedDatastreamsGetterRegistrationForSerialNumber).unregister();
        verify(mockedDatastreamsGetterRegistrationForDeviceId).unregister();
        verify(mockedDeviceInfoProviderRegistration).unregister();
        verify(mockedConfigBundle).close();
        verify(mockedRegistrationForSoftware).unregister();
        verify(mockedRegistrationForClock).unregister();
        verify(mockedRegistrationForUptime).unregister();
        verify(mockedRegistrationForTemperatureValue).unregister();
        verify(mockedRegistrationForTemperatureStatus).unregister();
        verify(mockedRegistrationForCpuStatus).unregister();
        verify(mockedRegistrationForCpuUsage).unregister();
        verify(mockedRegistrationForCpuTotal).unregister();
        verify(mockedRegistrationForRamUsage).unregister();
        verify(mockedRegistrationForRamTotal).unregister();
        verify(mockedRegistrationForDiskUsage).unregister();
        verify(mockedRegistrationForDiskTotal).unregister();
    }
}