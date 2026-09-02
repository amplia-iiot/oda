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
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
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
    private ServiceRegistration<ScadaDispatcher> mockedOperationDispatcherReg;
    @Mock
    private ServiceRegistration<EventDispatcher> mockedEventDispatcherReg;

    @Test
    public void testStart() throws Exception {
        List<List<?>> translatorArgs = new ArrayList<>();
        List<List<?>> getOperationArgs = new ArrayList<>();
        List<List<?>> setOperationArgs = new ArrayList<>();
        List<List<?>> connectorArgs = new ArrayList<>();
        List<List<?>> operationDispatcherArgs = new ArrayList<>();
        List<List<?>> eventDispatcherArgs = new ArrayList<>();
        try (MockedConstruction<ScadaTableTranslatorProxy> translatorCons =
                     mockConstruction(ScadaTableTranslatorProxy.class,
                             (mock, mctx) -> translatorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<OperationGetDeviceParametersProxy> getOperationCons =
                     mockConstruction(OperationGetDeviceParametersProxy.class,
                             (mock, mctx) -> getOperationArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<OperationSetDeviceParametersProxy> setOperationCons =
                     mockConstruction(OperationSetDeviceParametersProxy.class,
                             (mock, mctx) -> setOperationArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ScadaConnectorProxy> connectorCons = mockConstruction(ScadaConnectorProxy.class,
                     (mock, mctx) -> connectorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ScadaOperationDispatcher> operationDispatcherCons =
                     mockConstruction(ScadaOperationDispatcher.class,
                             (mock, mctx) -> operationDispatcherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ScadaEventDispatcher> eventDispatcherCons =
                     mockConstruction(ScadaEventDispatcher.class,
                             (mock, mctx) -> eventDispatcherArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, translatorCons.constructed().size());
            assertEquals(mockedContext, translatorArgs.get(0).get(0));
            assertEquals(1, getOperationCons.constructed().size());
            assertEquals(mockedContext, getOperationArgs.get(0).get(0));
            assertEquals(1, setOperationCons.constructed().size());
            assertEquals(mockedContext, setOperationArgs.get(0).get(0));
            assertEquals(1, connectorCons.constructed().size());
            assertEquals(mockedContext, connectorArgs.get(0).get(0));
            assertEquals(1, operationDispatcherCons.constructed().size());
            assertEquals(translatorCons.constructed().get(0), operationDispatcherArgs.get(0).get(0));
            assertEquals(getOperationCons.constructed().get(0), operationDispatcherArgs.get(0).get(1));
            assertEquals(setOperationCons.constructed().get(0), operationDispatcherArgs.get(0).get(2));
            assertEquals(1, eventDispatcherCons.constructed().size());
            assertEquals(translatorCons.constructed().get(0), eventDispatcherArgs.get(0).get(0));
            assertEquals(connectorCons.constructed().get(0), eventDispatcherArgs.get(0).get(1));
            verify(mockedContext).registerService(eq(ScadaDispatcher.class),
                    eq(operationDispatcherCons.constructed().get(0)), any());
            verify(mockedContext).registerService(eq(EventDispatcher.class),
                    eq(eventDispatcherCons.constructed().get(0)), any());
        }
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
