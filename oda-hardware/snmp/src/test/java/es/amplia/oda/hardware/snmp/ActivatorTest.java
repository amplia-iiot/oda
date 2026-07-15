package es.amplia.oda.hardware.snmp;

import es.amplia.oda.core.commons.osgi.proxies.SnmpTranslatorProxy;
import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.hardware.snmp.configuration.SnmpConfigurationUpdateHandler;
import es.amplia.oda.hardware.snmp.internal.SnmpClientFactory;
import es.amplia.oda.hardware.snmp.internal.SnmpClientManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;

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
    private SnmpClientManager mockedSnmpClientsManager;
    @Mock
    private SnmpTranslatorProxy mockedSnmpTranslatorProxy;
    @Mock
    private StateManagerProxy mockedStateManagerProxy;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;

    @Test
    public void testStart() throws Exception {
        List<List<?>> registrationManagerArgs = new ArrayList<>();
        List<List<?>> snmpClientManagerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();

        try (MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
                     mockConstruction(ServiceRegistrationManagerOsgi.class,
                             (mock, mctx) -> registrationManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SnmpClientManager> snmpClientManagerCons = mockConstruction(SnmpClientManager.class,
                     (mock, mctx) -> snmpClientManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SnmpClientFactory> snmpClientFactoryCons = mockConstruction(SnmpClientFactory.class);
             MockedConstruction<SnmpTranslatorProxy> snmpTranslatorProxyCons = mockConstruction(SnmpTranslatorProxy.class);
             MockedConstruction<StateManagerProxy> stateManagerProxyCons = mockConstruction(StateManagerProxy.class);
             MockedConstruction<SnmpConfigurationUpdateHandler> configHandlerCons =
                     mockConstruction(SnmpConfigurationUpdateHandler.class);
             MockedConstruction<ConfigurableBundleImpl> configBundleCons = mockConstruction(ConfigurableBundleImpl.class,
                     (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, registrationManagerCons.constructed().size());
            assertEquals(mockedContext, registrationManagerArgs.get(0).get(0));
            assertEquals(SnmpClient.class, registrationManagerArgs.get(0).get(1));
            assertEquals(1, snmpClientManagerCons.constructed().size());
            assertEquals(registrationManagerCons.constructed().get(0), snmpClientManagerArgs.get(0).get(0));
            assertEquals(1, snmpClientFactoryCons.constructed().size());
            assertEquals(1, snmpTranslatorProxyCons.constructed().size());
            assertEquals(1, stateManagerProxyCons.constructed().size());
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "snmpManager", mockedSnmpClientsManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "snmpTranslatorProxy", mockedSnmpTranslatorProxy);
        Whitebox.setInternalState(testActivator, "stateManagerProxy", mockedStateManagerProxy);

        testActivator.stop(mockedContext);

        verify(mockedSnmpClientsManager).close();
        verify(mockedConfigurableBundle).close();
        verify(mockedSnmpTranslatorProxy).close();
        verify(mockedStateManagerProxy).close();
    }
}
