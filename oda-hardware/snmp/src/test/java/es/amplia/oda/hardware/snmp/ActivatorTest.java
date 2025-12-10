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
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private SnmpClientManager mockedSnmpClientsManager;
    @Mock
    private ServiceRegistrationManagerOsgi<SnmpClient> mockedRegistrationManager;
    @Mock
    private SnmpTranslatorProxy mockedSnmpTranslatorProxy;
    @Mock
    private StateManagerProxy mockedStateManagerProxy;
    @Mock
    private SnmpClientFactory mockedSnmpClientFactory;
    @Mock
    private SnmpConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class).withAnyArguments().thenReturn(mockedRegistrationManager);
        PowerMockito.whenNew(SnmpClientManager.class).withAnyArguments().thenReturn(mockedSnmpClientsManager);
        PowerMockito.whenNew(SnmpTranslatorProxy.class).withAnyArguments().thenReturn(mockedSnmpTranslatorProxy);
        PowerMockito.whenNew(StateManagerProxy.class).withAnyArguments().thenReturn(mockedStateManagerProxy);
        PowerMockito.whenNew(SnmpClientFactory.class).withAnyArguments().thenReturn(mockedSnmpClientFactory);
        PowerMockito.whenNew(SnmpConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class).withArguments(eq(mockedContext), eq(SnmpClient.class));
        PowerMockito.verifyNew(SnmpClientManager.class).withArguments(eq(mockedRegistrationManager));
        PowerMockito.verifyNew(SnmpClientFactory.class).withNoArguments();
        PowerMockito.verifyNew(SnmpClientManager.class).withArguments(eq(mockedRegistrationManager));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
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