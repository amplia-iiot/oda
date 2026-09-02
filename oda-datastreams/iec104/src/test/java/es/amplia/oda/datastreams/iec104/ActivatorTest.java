package es.amplia.oda.datastreams.iec104;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.iec104.configuration.Iec104DatastreamsConfigurationUpdateHandler;
import es.amplia.oda.datastreams.iec104.internal.Iec104DatastreamsFactoryImpl;
import es.amplia.oda.event.api.EventDispatcherProxy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private Iec104ConnectionsFactory mockedConnectionsFactory;
    @Mock
    private Iec104DatastreamsManager mockedIec104DatastreamsManager;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private ServiceListenerBundle<ScadaTableInfo> mockedServiceListenerBundle;
    @Mock
    private EventPublisherProxy mockedEventPublisher;
    @Mock
    private EventDispatcherProxy mockedEventDispatcher;

    @Test
    public void testStart() throws Exception {
        List<List<?>> factoryArgs = new ArrayList<>();
        List<List<?>> registrationManagerArgs = new ArrayList<>();
        List<List<?>> managerArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();

        try (MockedConstruction<Iec104ConnectionsFactory> connectionsFactoryCons =
                     mockConstruction(Iec104ConnectionsFactory.class);
             MockedConstruction<Iec104DatastreamsFactoryImpl> factoryCons =
                     mockConstruction(Iec104DatastreamsFactoryImpl.class,
                             (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
                     mockConstruction(ServiceRegistrationManagerOsgi.class,
                             (mock, mctx) -> registrationManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<Iec104DatastreamsManager> iec104DatastreamsManagerCons =
                     mockConstruction(Iec104DatastreamsManager.class,
                             (mock, mctx) -> managerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<Iec104DatastreamsConfigurationUpdateHandler> configHandlerCons =
                     mockConstruction(Iec104DatastreamsConfigurationUpdateHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<EventPublisherProxy> eventPublisherCons =
                     mockConstruction(EventPublisherProxy.class);
             MockedConstruction<EventDispatcherProxy> eventDispatcherCons =
                     mockConstruction(EventDispatcherProxy.class)) {

            testActivator.start(mockedContext);

            assertEquals(1, factoryCons.constructed().size());
            assertEquals(connectionsFactoryCons.constructed().get(0), factoryArgs.get(0).get(1));
            assertEquals(2, registrationManagerCons.constructed().size());
            assertEquals(mockedContext, registrationManagerArgs.get(0).get(0));
            assertEquals(DatastreamsGetter.class, registrationManagerArgs.get(0).get(1));
            assertEquals(mockedContext, registrationManagerArgs.get(1).get(0));
            assertEquals(DatastreamsSetter.class, registrationManagerArgs.get(1).get(1));
            assertEquals(1, iec104DatastreamsManagerCons.constructed().size());
            assertEquals(factoryCons.constructed().get(0), managerArgs.get(0).get(0));
            assertEquals(registrationManagerCons.constructed().get(0), managerArgs.get(0).get(1));
            assertEquals(registrationManagerCons.constructed().get(1), managerArgs.get(0).get(2));
            assertEquals(connectionsFactoryCons.constructed().get(0), managerArgs.get(0).get(3));
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(iec104DatastreamsManagerCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(1, configurableBundleCons.constructed().size());
            assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
            assertEquals(1, eventPublisherCons.constructed().size());
            assertEquals(1, eventDispatcherCons.constructed().size());
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "connectionsFactory", mockedConnectionsFactory);
        Whitebox.setInternalState(testActivator, "iec104DatastreamsManager", mockedIec104DatastreamsManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "serviceListenerBundle", mockedServiceListenerBundle);
        Whitebox.setInternalState(testActivator, "eventPublisher", mockedEventPublisher);
        Whitebox.setInternalState(testActivator, "eventDispatcher", mockedEventDispatcher);

        testActivator.stop(mockedContext);

        verify(mockedServiceListenerBundle).close();
        verify(mockedConfigurableBundle).close();
        verify(mockedIec104DatastreamsManager).close();
        verify(mockedConnectionsFactory).disconnect();
        verify(mockedEventPublisher).close();
        verify(mockedEventDispatcher).close();
    }
}
