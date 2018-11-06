package es.amplia.oda.connector.thingstream;

import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atserver.api.ATManagerProxy;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DispatcherProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.connector.thingstream.configuration.ConfigurationUpdateHandlerImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertTrue;
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
    private ATManagerProxy mockedATManagerProxy;
    @Mock
    private DispatcherProxy mockedDispatcherProxy;
    @Mock
    private ThingstreamConnector mockedConnector;
    @Mock
    private ConfigurationUpdateHandlerImpl mockedConfigHandler;
    @Mock
    private ConfigurableBundle mockedConfigBundle;
    @Mock
    private ServiceListenerBundle<ATManager> mockedAtManagerServiceListener;
    @Mock
    private ServiceRegistration<OpenGateConnector> mockedRegistration;


    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ATManagerProxy.class).withAnyArguments().thenReturn(mockedATManagerProxy);
        PowerMockito.whenNew(DispatcherProxy.class).withAnyArguments().thenReturn(mockedDispatcherProxy);
        PowerMockito.whenNew(ThingstreamConnector.class).withAnyArguments().thenReturn(mockedConnector);
        PowerMockito.whenNew(ConfigurationUpdateHandlerImpl.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundle.class).withAnyArguments().thenReturn(mockedConfigBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedAtManagerServiceListener);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ATManagerProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(DispatcherProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ThingstreamConnector.class)
                .withArguments(eq(mockedATManagerProxy), eq(mockedDispatcherProxy));
        PowerMockito.verifyNew(ConfigurationUpdateHandlerImpl.class).withArguments(eq(mockedConnector));
        PowerMockito.verifyNew(ConfigurableBundle.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        PowerMockito.verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(ATManager.class), any());
        verify(mockedContext).registerService(eq(OpenGateConnector.class), eq(mockedConnector), any());
    }

    @Test
    public void testOnServiceChanged() {
        testActivator.onServiceChanged(mockedConfigHandler);

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testOnServiceChangedExceptionCaught() {
        doThrow(new RuntimeException("")).when(mockedConfigHandler).applyConfiguration();

        testActivator.onServiceChanged(mockedConfigHandler);

        assertTrue("Exception is caught", true);
        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "atManager", mockedATManagerProxy);
        Whitebox.setInternalState(testActivator, "dispatcher", mockedDispatcherProxy);
        Whitebox.setInternalState(testActivator, "connector", mockedConnector);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "atManagerServiceListener", mockedAtManagerServiceListener);
        Whitebox.setInternalState(testActivator, "openGateConnectorRegistration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedAtManagerServiceListener).close();
        verify(mockedConfigBundle).close();
        verify(mockedConnector).close();
        verify(mockedDispatcherProxy).close();
        verify(mockedATManagerProxy).close();
    }
}