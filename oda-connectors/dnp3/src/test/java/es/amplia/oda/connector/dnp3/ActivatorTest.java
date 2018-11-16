package es.amplia.oda.connector.dnp3;

import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableInfoProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.connector.dnp3.configuration.DNP3ConnectorConfigurationHandler;

import com.automatak.dnp3.DNP3Exception;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ScadaTableInfoProxy mockedTableInfo;
    @Mock
    private ScadaDispatcherProxy mockedDispatcher;
    @Mock
    private ServiceRegistrationManagerOsgi<ScadaConnector> mockedScadaConnectorRegistrationManager;
    @Mock
    private DNP3Connector mockedConnector;
    @Mock
    private DNP3ConnectorConfigurationHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private ServiceListenerBundle<ScadaTableInfo> mockedServiceListenerBundle;


    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ScadaTableInfoProxy.class).withAnyArguments().thenReturn(mockedTableInfo);
        PowerMockito.whenNew(ScadaDispatcherProxy.class).withAnyArguments().thenReturn(mockedDispatcher);
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class).withAnyArguments()
                .thenReturn(mockedScadaConnectorRegistrationManager);
        PowerMockito.whenNew(DNP3Connector.class).withAnyArguments().thenReturn(mockedConnector);
        PowerMockito.whenNew(DNP3ConnectorConfigurationHandler.class).withAnyArguments()
                .thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedServiceListenerBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ScadaTableInfoProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ScadaDispatcherProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(ScadaConnector.class));
        PowerMockito.verifyNew(DNP3Connector.class)
                .withArguments(eq(mockedTableInfo), eq(mockedDispatcher), eq(mockedScadaConnectorRegistrationManager));
        PowerMockito.verifyNew(DNP3ConnectorConfigurationHandler.class).withArguments(eq(mockedConnector));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(ScadaTableInfo.class), any(Runnable.class));
    }

    @Test
    public void testOnServiceChanged() throws DNP3Exception {
        Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testOnServiceChangedExceptionCaught() throws DNP3Exception {
        Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

        doThrow(new DNP3Exception("")).when(mockedConfigHandler).applyConfiguration();

        testActivator.onServiceChanged();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "connector", mockedConnector);
        Whitebox.setInternalState(testActivator, "tableInfo", mockedTableInfo);
        Whitebox.setInternalState(testActivator, "dispatcher", mockedDispatcher);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "serviceListenerBundle", mockedServiceListenerBundle);

        testActivator.stop(mockedContext);

        verify(mockedServiceListenerBundle).close();
        verify(mockedConfigurableBundle).close();
        verify(mockedConnector).close();
        verify(mockedTableInfo).close();
        verify(mockedDispatcher).close();
    }
}