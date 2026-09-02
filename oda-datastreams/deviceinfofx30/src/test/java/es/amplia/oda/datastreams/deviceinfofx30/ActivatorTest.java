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
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

	private final Activator testActivator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private ScriptsLoaderImpl mockedScriptsLoader;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundle;

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
		List<List<?>> scriptsLoaderArgs = new ArrayList<>();
		List<List<?>> deviceInfoArgs = new ArrayList<>();
		List<List<?>> configHandlerArgs = new ArrayList<>();
		List<List<?>> configBundleArgs = new ArrayList<>();
		try (MockedConstruction<CommandProcessorImpl> commandProcessorCons =
					 mockConstruction(CommandProcessorImpl.class);
			 MockedConstruction<ScriptsLoaderImpl> scriptsLoaderCons =
					 mockConstruction(ScriptsLoaderImpl.class,
							 (mock, mctx) -> scriptsLoaderArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<DeviceInfoFX30> deviceInfoCons =
					 mockConstruction(DeviceInfoFX30.class,
							 (mock, mctx) -> deviceInfoArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<DeviceInfoFX30ConfigurationHandler> configHandlerCons =
					 mockConstruction(DeviceInfoFX30ConfigurationHandler.class,
							 (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configBundleCons =
					 mockConstruction(ConfigurableBundleImpl.class,
							 (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<DatastreamGetterTemplate> getterTemplateCons =
					 mockConstruction(DatastreamGetterTemplate.class)) {

			testActivator.start(mockedContext);

			assertEquals(1, commandProcessorCons.constructed().size());
			assertEquals(1, scriptsLoaderCons.constructed().size());
			assertEquals(commandProcessorCons.constructed().get(0), scriptsLoaderArgs.get(0).get(0));
			assertEquals(1, deviceInfoCons.constructed().size());
			assertEquals(commandProcessorCons.constructed().get(0), deviceInfoArgs.get(0).get(0));
			assertEquals(null, deviceInfoArgs.get(0).get(1));
			assertEquals(1, configHandlerCons.constructed().size());
			assertEquals(scriptsLoaderCons.constructed().get(0), configHandlerArgs.get(0).get(0));
			assertEquals(deviceInfoCons.constructed().get(0), configHandlerArgs.get(0).get(1));
			assertEquals(1, configBundleCons.constructed().size());
			assertEquals(mockedContext, configBundleArgs.get(0).get(0));
			assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
			assertEquals(21, getterTemplateCons.constructed().size());
		}
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
