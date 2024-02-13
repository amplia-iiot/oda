package es.amplia.oda.dispatcher.scada;

import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;
import es.amplia.oda.core.commons.osgi.proxies.ScadaConnectorProxy;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableTranslatorProxy;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.operation.api.osgi.proxies.OperationGetDeviceParametersProxy;
import es.amplia.oda.operation.api.osgi.proxies.OperationSetDeviceParametersProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
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
    private ScadaTableTranslatorProxy mockedTranslator;
    @Mock
    private OperationGetDeviceParametersProxy mockedGetOperation;
    @Mock
    private OperationSetDeviceParametersProxy mockedSetOperation;
    @Mock
    private ScadaConnectorProxy mockedConnector;
    @Mock
    private ScadaOperationDispatcher mockedOperationDispatcher;
    @Mock
    private ScadaEventDispatcher mockedEventDispatcher;
    @Mock
    private ServiceRegistration<ScadaDispatcher> mockedOperationDispatcherReg;
    @Mock
    private ServiceRegistration<EventDispatcher> mockedEventDispatcherReg;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ScadaTableTranslatorProxy.class).withAnyArguments().thenReturn(mockedTranslator);
        PowerMockito.whenNew(OperationGetDeviceParametersProxy.class).withAnyArguments().thenReturn(mockedGetOperation);
        PowerMockito.whenNew(OperationSetDeviceParametersProxy.class).withAnyArguments().thenReturn(mockedSetOperation);
        PowerMockito.whenNew(ScadaConnectorProxy.class).withAnyArguments().thenReturn(mockedConnector);
        PowerMockito.whenNew(ScadaOperationDispatcher.class).withAnyArguments().thenReturn(mockedOperationDispatcher);
        PowerMockito.whenNew(ScadaEventDispatcher.class).withAnyArguments().thenReturn(mockedEventDispatcher);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ScadaTableTranslatorProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OperationGetDeviceParametersProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OperationSetDeviceParametersProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ScadaConnectorProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ScadaOperationDispatcher.class)
                .withArguments(eq(mockedTranslator), eq(mockedGetOperation), eq(mockedSetOperation));
        PowerMockito.verifyNew(ScadaEventDispatcher.class).withArguments(eq(mockedTranslator), eq(mockedConnector));
        verify(mockedContext).registerService(eq(ScadaDispatcher.class), eq(mockedOperationDispatcher), any());
        verify(mockedContext).registerService(eq(EventDispatcher.class), eq(mockedEventDispatcher), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "translator", mockedTranslator);
        Whitebox.setInternalState(testActivator, "getOperation", mockedGetOperation);
        Whitebox.setInternalState(testActivator, "setOperation", mockedSetOperation);
        Whitebox.setInternalState(testActivator, "connector", mockedConnector);
        Whitebox.setInternalState(testActivator, "operationDispatcherRegistration", mockedOperationDispatcherReg);
        Whitebox.setInternalState(testActivator, "eventDispatcherRegistration", mockedEventDispatcherReg);

        testActivator.stop(mockedContext);

        verify(mockedOperationDispatcherReg).unregister();
        verify(mockedEventDispatcherReg).unregister();
        verify(mockedTranslator).close();
        verify(mockedGetOperation).close();
        verify(mockedSetOperation).close();
        verify(mockedConnector).close();
    }
}