package es.amplia.oda.datastreams.deviceinfofx30;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandProcessorImpl;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.datastreams.deviceinfofx30.configuration.DeviceInfoFX30ConfigurationHandler;
import es.amplia.oda.datastreams.deviceinfofx30.configuration.ScriptsLoader;
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
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

	private final Activator testActivator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private CommandProcessorImpl mockedCommandProcessor;
	@Mock
	private DeviceInfoFX30 mockedDeviceInfo;
	@Mock
	private DeviceInfoFX30ConfigurationHandler mockedConfigHandler;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	private ScriptsLoader mockedScriptsLoader;

	@Mock
	private SerialNumberDatastreamGetter mockedSerialNumberGetter;
	@Mock
	private DeviceIdDatastreamGetter mockedDeviceIdGetter;
	@Mock
	private MakerDatastreamGetter mockedMakerGetter;
	@Mock
	private ModelDatastreamGetter mockedModelGetter;
	@Mock
	private ImeiDatastreamGetter mockedImeiGetter;
	@Mock
	private ImsiDatastreamGetter mockedImsiGetter;
	@Mock
	private IccDatastreamGetter mockedIccGetter;
	@Mock
	private RssiDatastreamGetter mockedRssiGetter;
	@Mock
	private SoftwareDatastreamGetter mockedSoftwareGetter;
	@Mock
	private ApnDatastreamGetter mockedApnGetter;
	@Mock
	private ClockDatastreamGetter mockedClockGetter;
	@Mock
	private UptimeDatastreamGetter mockedUptimeGetter;
	@Mock
	private TemperatureValueDatastreamGetter mockedTemperatureValueGetter;
	@Mock
	private TemperatureStatusDatastreamGetter mockedTemperatureStatusGetter;
	@Mock
	private CpuStatusDatastreamGetter mockedCpuStatusGetter;
	@Mock
	private CpuUsageDatastreamGetter mockedCpuUsageGetter;
	@Mock
	private CpuTotalDatastreamGetter mockedCpuTotalGetter;
	@Mock
	private RamUsageDatastreamGetter mockedRamUsageGetter;
	@Mock
	private RamTotalDatastreamGetter mockedRamTotalGetter;
	@Mock
	private DiskUsageDatastreamGetter mockedDiskUsageGetter;
	@Mock
	private DiskTotalDatastreamGetter mockedDiskTotalGetter;

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
		PowerMockito.whenNew(DeviceInfoFX30.class).withAnyArguments().thenReturn(mockedDeviceInfo);
		PowerMockito.whenNew(DeviceInfoFX30ConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
		PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		PowerMockito.whenNew(ScriptsLoader.class).withAnyArguments().thenReturn(mockedScriptsLoader);
		PowerMockito.whenNew(SerialNumberDatastreamGetter.class).withAnyArguments().thenReturn(mockedSerialNumberGetter);
		PowerMockito.whenNew(DeviceIdDatastreamGetter.class).withAnyArguments().thenReturn(mockedDeviceIdGetter);
		PowerMockito.whenNew(MakerDatastreamGetter.class).withAnyArguments().thenReturn(mockedMakerGetter);
		PowerMockito.whenNew(ModelDatastreamGetter.class).withAnyArguments().thenReturn(mockedModelGetter);
		PowerMockito.whenNew(ImeiDatastreamGetter.class).withAnyArguments().thenReturn(mockedImeiGetter);
		PowerMockito.whenNew(ImsiDatastreamGetter.class).withAnyArguments().thenReturn(mockedImsiGetter);
		PowerMockito.whenNew(IccDatastreamGetter.class).withAnyArguments().thenReturn(mockedIccGetter);
		PowerMockito.whenNew(RssiDatastreamGetter.class).withAnyArguments().thenReturn(mockedRssiGetter);
		PowerMockito.whenNew(SoftwareDatastreamGetter.class).withAnyArguments().thenReturn(mockedSoftwareGetter);
		PowerMockito.whenNew(ApnDatastreamGetter.class).withAnyArguments().thenReturn(mockedApnGetter);
		PowerMockito.whenNew(ClockDatastreamGetter.class).withAnyArguments().thenReturn(mockedClockGetter);
		PowerMockito.whenNew(UptimeDatastreamGetter.class).withAnyArguments().thenReturn(mockedUptimeGetter);
		PowerMockito.whenNew(TemperatureValueDatastreamGetter.class).withAnyArguments().thenReturn(mockedTemperatureValueGetter);
		PowerMockito.whenNew(TemperatureStatusDatastreamGetter.class).withAnyArguments().thenReturn(mockedTemperatureStatusGetter);
		PowerMockito.whenNew(CpuStatusDatastreamGetter.class).withAnyArguments().thenReturn(mockedCpuStatusGetter);
		PowerMockito.whenNew(CpuUsageDatastreamGetter.class).withAnyArguments().thenReturn(mockedCpuUsageGetter);
		PowerMockito.whenNew(CpuTotalDatastreamGetter.class).withAnyArguments().thenReturn(mockedCpuTotalGetter);
		PowerMockito.whenNew(RamUsageDatastreamGetter.class).withAnyArguments().thenReturn(mockedRamUsageGetter);
		PowerMockito.whenNew(RamTotalDatastreamGetter.class).withAnyArguments().thenReturn(mockedRamTotalGetter);
		PowerMockito.whenNew(DiskUsageDatastreamGetter.class).withAnyArguments().thenReturn(mockedDiskUsageGetter);
		PowerMockito.whenNew(DiskTotalDatastreamGetter.class).withAnyArguments().thenReturn(mockedDiskTotalGetter);

		testActivator.start(mockedContext);

		PowerMockito.verifyNew(CommandProcessorImpl.class).withNoArguments();
		PowerMockito.verifyNew(DeviceInfoFX30.class).withArguments(eq(mockedCommandProcessor), isNull());
		PowerMockito.verifyNew(DeviceInfoFX30ConfigurationHandler.class).withArguments(eq(mockedDeviceInfo), eq(mockedScriptsLoader));
		PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler), any());
		PowerMockito.verifyNew(ScriptsLoader.class).withArguments(mockedCommandProcessor);
		PowerMockito.verifyNew(SerialNumberDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(DeviceIdDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(MakerDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(ModelDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(ImeiDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(ImsiDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(IccDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(RssiDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(SoftwareDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(ApnDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(ClockDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(UptimeDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(TemperatureValueDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(TemperatureStatusDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(CpuStatusDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(CpuUsageDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(CpuTotalDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(RamUsageDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(RamTotalDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(DiskUsageDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
		PowerMockito.verifyNew(DiskTotalDatastreamGetter.class).withArguments(eq(mockedDeviceInfo));
	}

	@Test
	public void testStop() {
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

		verify(mockedConfigurableBundle).close();
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
	}
}
