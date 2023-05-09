package es.amplia.oda.demo.scadatables.info;

import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.demo.scadatables.info.Activator;
import es.amplia.oda.demo.scadatables.info.configuration.ScadaTablesConfigurationHandler;
import es.amplia.oda.demo.scadatables.info.internal.ScadaTableInfoService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.script.ScriptEngineManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {
	private final Activator testActivator = new Activator();

	@Mock
	private ScadaTableInfoService mockedScadaTableInfoService;
	@Mock
	private ScriptEngineManager mockedScriptEngineManager;
	@Mock
	private ScadaTablesConfigurationHandler mockedScadaTablesConfigurationHandler;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundleImpl;
	@Mock
	private ServiceRegistration<ScadaTableInfo> mockedServiceRegistrationScadaTableInfo;
	@Mock
	private ServiceRegistration<ScadaTableTranslator> mockedServiceRegistrationScadaTableTranslator;

	@Mock
	private BundleContext mockedContext;

	@Test
	public void testStart() throws Exception {
		PowerMockito.whenNew(ScadaTableInfoService.class).withAnyArguments()
				.thenReturn(mockedScadaTableInfoService);
		PowerMockito.whenNew(ScriptEngineManager.class).withAnyArguments()
				.thenReturn(mockedScriptEngineManager);
		PowerMockito.whenNew(ScadaTablesConfigurationHandler.class).withAnyArguments()
				.thenReturn(mockedScadaTablesConfigurationHandler);
		PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments()
				.thenReturn(mockedConfigurableBundleImpl);

		testActivator.start(mockedContext);

		PowerMockito.verifyNew(ScadaTableInfoService.class).withNoArguments();
		PowerMockito.verifyNew(ScadaTablesConfigurationHandler.class).withArguments(eq(mockedScadaTableInfoService));
		PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedScadaTablesConfigurationHandler), any());
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "scadaTableInfoServiceRegistration", mockedServiceRegistrationScadaTableInfo);
		Whitebox.setInternalState(testActivator, "scadaTranslatorServiceRegistration", mockedServiceRegistrationScadaTableTranslator);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundleImpl);

		testActivator.stop(mockedContext);

		verify(mockedServiceRegistrationScadaTableInfo).unregister();
		verify(mockedServiceRegistrationScadaTableTranslator).unregister();
		verify(mockedConfigurableBundleImpl).close();
	}
}
