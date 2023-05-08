package es.amplia.oda.connector.iec104;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.connector.iec104.configuration.Iec104ConnectorConfigurationUpdateHandler;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {
	private final Activator testActivator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private Iec104Cache mockedCache;
	@Mock
	private ScadaDispatcherProxy mockedDispatcher;
	@Mock
	private Iec104Connector mockedConnector;
	@Mock
	private ServiceRegistration<ScadaTableInfo> mockedRegistration;
	@Mock
	private ServiceListenerBundle<?> mockedListener;
	@Mock
	private ConfigurableBundleImpl mockedConfigurable;
	@Mock
	private Iec104ConnectorConfigurationUpdateHandler mockedConfigurationHandler;

	@Test
	public void testStart() throws Exception {
		PowerMockito.whenNew(ScadaDispatcherProxy.class).withAnyArguments().thenReturn(mockedDispatcher);
		PowerMockito.whenNew(Iec104Cache.class).withAnyArguments().thenReturn(mockedCache);
		PowerMockito.whenNew(Iec104Connector.class).withAnyArguments().thenReturn(mockedConnector);
		PowerMockito.whenNew(Iec104ConnectorConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedConfigurationHandler);
		PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurable);
		PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedListener);

		testActivator.start(mockedContext);

		PowerMockito.verifyNew(ScadaDispatcherProxy.class).withArguments(eq(mockedContext));
		PowerMockito.verifyNew(Iec104Cache.class).withNoArguments();
		PowerMockito.verifyNew(Iec104Connector.class).withArguments(eq(mockedCache), eq(mockedDispatcher));
		PowerMockito.verifyNew(Iec104ConnectorConfigurationUpdateHandler.class).withArguments(mockedConnector);
		PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigurationHandler));
		PowerMockito.verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(ScadaTableInfo.class), any(Runnable.class));
	}

	@Test
	public void testOnServiceChange() {
		Whitebox.setInternalState(testActivator, "configHandler", mockedConfigurationHandler);

		testActivator.onServiceChange();

		Mockito.verify(mockedConfigurationHandler).applyConfiguration();
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "scadaConnectorServiceRegistration", mockedRegistration);
		Whitebox.setInternalState(testActivator, "serviceListenerBundle", mockedListener);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurable);
		Whitebox.setInternalState(testActivator, "connector", mockedConnector);
		Whitebox.setInternalState(testActivator, "dispatcher", mockedDispatcher);

		testActivator.stop(mockedContext);

		Mockito.verify(mockedRegistration).unregister();
		Mockito.verify(mockedListener).close();
		Mockito.verify(mockedConfigurable).close();
		Mockito.verify(mockedConnector).close();
		Mockito.verify(mockedDispatcher).close();
	}
}
