package es.amplia.oda.datastreams.iec104;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.iec104.configuration.Iec104DatastreamsConfigurationUpdateHandler;
import es.amplia.oda.datastreams.iec104.internal.Iec104DatastreamsFactoryImpl;

import es.amplia.oda.event.api.EventDispatcherProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private Iec104ConnectionsFactory mockedConnectionsFactory;
    @Mock
    private Iec104DatastreamsFactoryImpl mockedFactory;
    @Mock
    private ServiceRegistrationManagerOsgi mockedRegistrationManager;
    @Mock
    private Iec104DatastreamsManager mockedIec104DatastreamsManager;
    @Mock
    private Iec104DatastreamsConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private ServiceListenerBundle<ScadaTableInfo> mockedServiceListenerBundle;
    @Mock
    private EventDispatcherProxy mockedEventDispatcher;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(Iec104ConnectionsFactory.class).withAnyArguments().thenReturn(mockedConnectionsFactory);
        PowerMockito.whenNew(Iec104DatastreamsFactoryImpl.class).withAnyArguments().thenReturn(mockedFactory);
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class).withAnyArguments()
                .thenReturn(mockedRegistrationManager);
        PowerMockito.whenNew(Iec104DatastreamsManager.class).withAnyArguments()
                .thenReturn(mockedIec104DatastreamsManager);
        PowerMockito.whenNew(Iec104DatastreamsConfigurationUpdateHandler.class).withAnyArguments()
                .thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
        PowerMockito.whenNew(EventDispatcherProxy.class).withAnyArguments().thenReturn(mockedEventDispatcher);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(Iec104DatastreamsFactoryImpl.class).withArguments(any(ScadaTableTranslator.class), eq(mockedConnectionsFactory));
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
        PowerMockito.verifyNew(Iec104DatastreamsManager.class)
                .withArguments(eq(mockedFactory), eq(mockedRegistrationManager), eq(mockedRegistrationManager), eq(mockedConnectionsFactory), any(ScadaTableTranslator.class));
        PowerMockito.verifyNew(Iec104DatastreamsConfigurationUpdateHandler.class)
                .withArguments(eq(mockedIec104DatastreamsManager));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "connectionsFactory", mockedConnectionsFactory);
        Whitebox.setInternalState(testActivator, "iec104DatastreamsManager", mockedIec104DatastreamsManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "serviceListenerBundle", mockedServiceListenerBundle);
        Whitebox.setInternalState(testActivator, "eventDispatcher", mockedEventDispatcher);


        testActivator.stop(mockedContext);

        verify(mockedServiceListenerBundle).close();
        verify(mockedConfigurableBundle).close();
        verify(mockedIec104DatastreamsManager).close();
        verify(mockedConnectionsFactory).disconnect();
    }
}