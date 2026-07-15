package es.amplia.oda.connector.dnp3;

import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableInfoProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.connector.dnp3.configuration.DNP3ConnectorConfigurationHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ScadaTableInfoProxy mockedTableInfo;
    @Mock
    private ScadaDispatcherProxy mockedDispatcher;
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
        List<List<?>> tableInfoArgs = new ArrayList<>();
        List<List<?>> dispatcherArgs = new ArrayList<>();
        List<List<?>> registrationManagerArgs = new ArrayList<>();
        List<List<?>> connectorArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();
        List<List<?>> serviceListenerArgs = new ArrayList<>();

        try (MockedConstruction<ScadaTableInfoProxy> tableInfoCons =
                     mockConstruction(ScadaTableInfoProxy.class,
                             (mock, mctx) -> tableInfoArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ScadaDispatcherProxy> dispatcherCons =
                     mockConstruction(ScadaDispatcherProxy.class,
                             (mock, mctx) -> dispatcherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
                     mockConstruction(ServiceRegistrationManagerOsgi.class,
                             (mock, mctx) -> registrationManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DNP3Connector> connectorCons =
                     mockConstruction(DNP3Connector.class,
                             (mock, mctx) -> connectorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DNP3ConnectorConfigurationHandler> configHandlerCons =
                     mockConstruction(DNP3ConnectorConfigurationHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceListenerBundle> serviceListenerCons =
                     mockConstruction(ServiceListenerBundle.class,
                             (mock, mctx) -> serviceListenerArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, tableInfoCons.constructed().size());
            assertEquals(mockedContext, tableInfoArgs.get(0).get(0));
            assertEquals(1, dispatcherCons.constructed().size());
            assertEquals(mockedContext, dispatcherArgs.get(0).get(0));
            assertEquals(1, registrationManagerCons.constructed().size());
            assertEquals(mockedContext, registrationManagerArgs.get(0).get(0));
            assertEquals(ScadaConnector.class, registrationManagerArgs.get(0).get(1));
            assertEquals(1, connectorCons.constructed().size());
            assertEquals(tableInfoCons.constructed().get(0), connectorArgs.get(0).get(0));
            assertEquals(dispatcherCons.constructed().get(0), connectorArgs.get(0).get(1));
            assertEquals(registrationManagerCons.constructed().get(0), connectorArgs.get(0).get(2));
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(connectorCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(1, configurableBundleCons.constructed().size());
            assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
            assertEquals(1, serviceListenerCons.constructed().size());
            assertEquals(mockedContext, serviceListenerArgs.get(0).get(0));
            assertEquals(ScadaTableInfo.class, serviceListenerArgs.get(0).get(1));
            assertTrue(serviceListenerArgs.get(0).get(2) instanceof Runnable);
        }
    }

    @Test
    public void testOnServiceChanged() {
        Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testOnServiceChangedExceptionCaught() {
        Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

        doThrow(new IllegalArgumentException("")).when(mockedConfigHandler).applyConfiguration();

        testActivator.onServiceChanged();

        assertTrue("Exceptions should be caught", true);
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
