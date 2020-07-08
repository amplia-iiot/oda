package es.amplia.oda.datastreams.deviceinfofx30;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandProcessorImpl;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ScriptsLoaderImpl;
import es.amplia.oda.datastreams.deviceinfofx30.configuration.DeviceInfoFX30ConfigurationHandler;
import es.amplia.oda.datastreams.deviceinfofx30.datastreams.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

	private final Activator testActivator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private ScriptsLoaderImpl mockedScriptsLoader;
	@Mock
	private CommandProcessorImpl mockedCommandProcessor;
	@Mock
	private DeviceInfoFX30 mockedDeviceInfo;
	@Mock
	private DeviceInfoFX30ConfigurationHandler mockedConfigHandler;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundle;

	@Mock
	private DatastreamGetterTemplate mockedSerialNumberGetter;
	@Mock
	private DatastreamGetterTemplate mockedDeviceIdGetter;
	@Mock
	private DatastreamGetterTemplate mockedMakerGetter;
	@Mock
	private DatastreamGetterTemplate mockedModelGetter;
	@Mock
	private DatastreamGetterTemplate mockedImeiGetter;
	@Mock
	private DatastreamGetterTemplate mockedImsiGetter;
	@Mock
	private DatastreamGetterTemplate mockedIccGetter;
	@Mock
	private DatastreamGetterTemplate mockedRssiGetter;
	@Mock
	private DatastreamGetterTemplate mockedSoftwareGetter;
	@Mock
	private DatastreamGetterTemplate mockedApnGetter;
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
	private ServiceRegistration<DeviceInfoProvider> mockedRegistrationDeviceInfo;
	@Mock
	private ServiceRegistration<DatastreamsGetter> mockedRegistrationForSerialNumber;
	@Mock
	private ServiceRegistration<DatastreamsGetter> mockedRegistrationForDeviceId;
	@Mock
	private ServiceRegistration<DatastreamsGetter> mockedRegistrationForMaker;
	@Mock
	private ServiceRegistration<DatastreamsGetter> mockedRegistrationForModel;
	@Mock
	private ServiceRegistration<DatastreamsGetter> mockedRegistrationForImei;
	@Mock
	private ServiceRegistration<DatastreamsGetter> mockedRegistrationForImsi;
	@Mock
	private ServiceRegistration<DatastreamsGetter> mockedRegistrationForIcc;
	@Mock
	private ServiceRegistration<DatastreamsGetter> mockedRegistrationForRssi;
	@Mock
	private ServiceRegistration<DatastreamsGetter> mockedRegistrationForSoftware;
	@Mock
	private ServiceRegistration<DatastreamsGetter> mockedRegistrationForApn;
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

	@Test
	public void testStart() throws Exception {
		PowerMockito.whenNew(CommandProcessorImpl.class).withAnyArguments().thenReturn(mockedCommandProcessor);
		PowerMockito.whenNew(ScriptsLoaderImpl.class).withAnyArguments().thenReturn(mockedScriptsLoader);
		PowerMockito.whenNew(DeviceInfoFX30.class).withAnyArguments().thenReturn(mockedDeviceInfo);
		PowerMockito.whenNew(DeviceInfoFX30ConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
		PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedSerialNumberGetter);
		PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedDeviceIdGetter);
		PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedMakerGetter);
		PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedModelGetter);
		PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedImeiGetter);
		PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedImsiGetter);
		PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedIccGetter);
		PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedRssiGetter);
		PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedSoftwareGetter);
		PowerMockito.whenNew(DatastreamGetterTemplate.class).withAnyArguments().thenReturn(mockedApnGetter);
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

		testActivator.start(mockedContext);

		PowerMockito.verifyNew(CommandProcessorImpl.class).withNoArguments();
		PowerMockito.verifyNew(ScriptsLoaderImpl.class).withArguments(mockedCommandProcessor);
		PowerMockito.verifyNew(DeviceInfoFX30.class).withArguments(eq(mockedCommandProcessor), isNull());
		PowerMockito.verifyNew(DeviceInfoFX30ConfigurationHandler.class)
				.withArguments(eq(mockedScriptsLoader), eq(mockedDeviceInfo));
		PowerMockito.verifyNew(ConfigurableBundleImpl.class)
				.withArguments(eq(mockedContext), eq(mockedConfigHandler), any());
		PowerMockito.verifyNew(DatastreamGetterTemplate.class, times(21)).withArguments(anyString(), any());
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "scriptsLoader", mockedScriptsLoader);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(testActivator, "deviceIdProviderRegistration", mockedRegistrationDeviceInfo);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForSerialNumber", mockedRegistrationForSerialNumber);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForDeviceId", mockedRegistrationForDeviceId);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForMaker", mockedRegistrationForMaker);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForModel", mockedRegistrationForModel);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForImei", mockedRegistrationForImei);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForImsi", mockedRegistrationForImsi);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForIcc", mockedRegistrationForIcc);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForRssi", mockedRegistrationForRssi);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForSoftware", mockedRegistrationForSoftware);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForApn", mockedRegistrationForApn);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForClock", mockedRegistrationForClock);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForUptime", mockedRegistrationForUptime);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForTemperatureValue", mockedRegistrationForTemperatureValue);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForTemperatureStatus", mockedRegistrationForTemperatureStatus);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForCpuStatus", mockedRegistrationForCpuStatus);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForCpuUsage", mockedRegistrationForCpuUsage);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForCpuTotal", mockedRegistrationForCpuTotal);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForRamUsage", mockedRegistrationForRamUsage);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForRamTotal", mockedRegistrationForRamTotal);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForDiskUsage", mockedRegistrationForDiskUsage);
		Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForDiskTotal", mockedRegistrationForDiskTotal);

		testActivator.stop(mockedContext);

		verify(mockedRegistrationDeviceInfo).unregister();
		verify(mockedRegistrationForSerialNumber).unregister();
		verify(mockedRegistrationForDeviceId).unregister();
		verify(mockedRegistrationForMaker).unregister();
		verify(mockedRegistrationForModel).unregister();
		verify(mockedRegistrationForImei).unregister();
		verify(mockedRegistrationForImsi).unregister();
		verify(mockedRegistrationForIcc).unregister();
		verify(mockedRegistrationForRssi).unregister();
		verify(mockedRegistrationForSoftware).unregister();
		verify(mockedRegistrationForApn).unregister();
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
		verify(mockedConfigurableBundle).close();
		verify(mockedScriptsLoader).close();
	}
}
