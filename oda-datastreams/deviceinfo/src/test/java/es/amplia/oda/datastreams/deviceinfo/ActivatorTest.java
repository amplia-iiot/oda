package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandProcessorImpl;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfigurationHandler;

import es.amplia.oda.datastreams.deviceinfo.configuration.ScriptsLoader;
import es.amplia.oda.datastreams.deviceinfo.datastreams.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private CommandProcessorImpl mockedCommandProcessor;
    @Mock
    private DeviceInfoDatastreamsGetter mockedDeviceDatastreamsGetter;
    @Mock
    private DeviceInfoConfigurationHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private DatastreamsGetter datastreamsGetterForDeviceId;
    @Mock
    private DatastreamsGetter datastreamsGetterForSerialNumber;
    @Mock
    private DatastreamGetterTemplate mockedSoftwareGetter;
    @Mock
    private DatastreamGetterTemplate mockedClockGetter;
    @Mock
    private DatastreamGetterTemplate mockedUptimeGetter;
    @Mock
    private DatastreamGetterTemplate mockedTemperatureValueGetter;
    @Mock
    private DatastreamGetterTemplate mockedTemperatureStatusGetter;
    @Mock
    private DatastreamGetterTemplate mockedCpuStatusGetter;
    @Mock
    private DatastreamGetterTemplate mockedCpuUsageGetter;
    @Mock
    private DatastreamGetterTemplate mockedCpuTotalGetter;
    @Mock
    private DatastreamGetterTemplate mockedRamUsageGetter;
    @Mock
    private DatastreamGetterTemplate mockedRamTotalGetter;
    @Mock
    private DatastreamGetterTemplate mockedDiskUsageGetter;
    @Mock
    private DatastreamGetterTemplate mockedDiskTotalGetter;
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
    @Mock
    private ScriptsLoader mockedScriptsLoader;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(CommandProcessorImpl.class).withAnyArguments().thenReturn(mockedCommandProcessor);
        PowerMockito.whenNew(DeviceInfoDatastreamsGetter.class).withAnyArguments()
                .thenReturn(mockedDeviceDatastreamsGetter);
        PowerMockito.whenNew(ScriptsLoader.class).withAnyArguments().thenReturn(mockedScriptsLoader);
        when(mockedDeviceDatastreamsGetter.getDatastreamsGetterForDeviceId()).thenReturn(datastreamsGetterForDeviceId);
        when(mockedDeviceDatastreamsGetter.getDatastreamsGetterForSerialNumber())
                .thenReturn(datastreamsGetterForSerialNumber);
        PowerMockito.whenNew(DeviceInfoConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);
        when(mockedContext.registerService(eq(DeviceInfoProvider.class), any(), any()))
                .thenReturn(mockedDeviceInfoProviderRegistration);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedSoftwareGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedClockGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedUptimeGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedTemperatureValueGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedTemperatureStatusGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedCpuStatusGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedCpuUsageGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedCpuTotalGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedRamUsageGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedRamTotalGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedDiskUsageGetter);
        PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedDiskTotalGetter);
        when(mockedContext.getBundles()).thenReturn(new Bundle[0]);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(DeviceInfoConfigurationHandler.class).withArguments(eq(mockedDeviceDatastreamsGetter), eq(mockedScriptsLoader));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler),
                eq(Collections.singletonList(mockedDeviceInfoProviderRegistration)));
        verify(mockedContext).registerService(eq(DeviceInfoProvider.class), eq(mockedDeviceDatastreamsGetter), eq(null));
        PowerMockito.verifyNew(DatastreamGetterTemplate.class, times(14)).withArguments(anyString(), any());
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