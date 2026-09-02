package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.modbus.configuration.ModbusDatastreamsConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbus.internal.ModbusDatastreamsFactoryImpl;
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
    private ModbusConnectionsFinder mockedConnectionsFinder;
    @Mock
    private ModbusDatastreamsManager mockedModbusDatastreamsManager;
    @Mock
    private ModbusDatastreamsConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private ServiceListenerBundle<ModbusMaster> mockedModbusMasterListener;

    @Test
    public void testStart() throws Exception {
        List<List<?>> factoryArgs = new ArrayList<>();
        List<List<?>> registrationManagerArgs = new ArrayList<>();
        List<List<?>> managerArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();
        List<List<?>> listenerArgs = new ArrayList<>();

        try (MockedConstruction<ModbusConnectionsFinder> connectionsFinderCons =
                     mockConstruction(ModbusConnectionsFinder.class);
             MockedConstruction<ModbusDatastreamsFactoryImpl> factoryCons =
                     mockConstruction(ModbusDatastreamsFactoryImpl.class,
                             (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
                     mockConstruction(ServiceRegistrationManagerOsgi.class,
                             (mock, mctx) -> registrationManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ModbusDatastreamsManager> modbusDatastreamsManagerCons =
                     mockConstruction(ModbusDatastreamsManager.class,
                             (mock, mctx) -> managerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ModbusDatastreamsConfigurationUpdateHandler> configHandlerCons =
                     mockConstruction(ModbusDatastreamsConfigurationUpdateHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceListenerBundle> listenerCons =
                     mockConstruction(ServiceListenerBundle.class,
                             (mock, mctx) -> listenerArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, factoryCons.constructed().size());
            assertEquals(connectionsFinderCons.constructed().get(0), factoryArgs.get(0).get(0));
            assertEquals(2, registrationManagerCons.constructed().size());
            assertEquals(mockedContext, registrationManagerArgs.get(0).get(0));
            assertEquals(DatastreamsGetter.class, registrationManagerArgs.get(0).get(1));
            assertEquals(mockedContext, registrationManagerArgs.get(1).get(0));
            assertEquals(DatastreamsSetter.class, registrationManagerArgs.get(1).get(1));
            assertEquals(1, modbusDatastreamsManagerCons.constructed().size());
            assertEquals(factoryCons.constructed().get(0), managerArgs.get(0).get(0));
            assertEquals(registrationManagerCons.constructed().get(0), managerArgs.get(0).get(1));
            assertEquals(registrationManagerCons.constructed().get(1), managerArgs.get(0).get(2));
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(modbusDatastreamsManagerCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(1, configurableBundleCons.constructed().size());
            assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
            assertEquals(1, listenerCons.constructed().size());
            assertEquals(mockedContext, listenerArgs.get(0).get(0));
            assertEquals(ModbusMaster.class, listenerArgs.get(0).get(1));

            verify(connectionsFinderCons.constructed().get(0)).connect();
        }
    }

    @Test
    public void testOnServiceChanged() {
        Whitebox.setInternalState(testActivator, "modbusConnectionsFinder", mockedConnectionsFinder);
        Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

        testActivator.onServiceChanged();

        verify(mockedConnectionsFinder).connect();
        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "modbusConnectionsFinder", mockedConnectionsFinder);
        Whitebox.setInternalState(testActivator, "modbusDatastreamsManager", mockedModbusDatastreamsManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "modbusMasterListenerBundle", mockedModbusMasterListener);

        testActivator.stop(mockedContext);

        verify(mockedModbusMasterListener).close();
        verify(mockedConfigurableBundle).close();
        verify(mockedModbusDatastreamsManager).close();
        verify(mockedConnectionsFinder).close();
    }
}
