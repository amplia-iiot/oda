package es.amplia.oda.connector.coap;

import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atserver.api.ATManagerProxy;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.connector.coap.configuration.ConfigurationUpdateHandlerImpl;

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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;
    @Mock
    private ATManagerProxy mockedATManager;
    @Mock
    private COAPConnector mockedConnector;
    @Mock
    private ConfigurationUpdateHandlerImpl mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceListenerBundle<DeviceInfoProvider> mockedDeviceInfoServiceListener;
    @Mock
    private ServiceListenerBundle<ATManager> mockedATManagerServiceListener;
    @Mock
    private ServiceRegistration<OpenGateConnector> mockedRegistration;

    @Test
    public void testStart() throws Exception {
        List<List<?>> deviceInfoProviderArgs = new ArrayList<>();
        List<List<?>> atManagerArgs = new ArrayList<>();
        List<List<?>> coapClientFactoryArgs = new ArrayList<>();
        List<List<?>> connectorArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();
        List<List<?>> serviceListenerArgs = new ArrayList<>();

        try (MockedConstruction<DeviceInfoProviderProxy> deviceInfoProviderCons =
                     mockConstruction(DeviceInfoProviderProxy.class,
                             (mock, mctx) -> deviceInfoProviderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ATManagerProxy> atManagerCons =
                     mockConstruction(ATManagerProxy.class,
                             (mock, mctx) -> atManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<COAPClientFactory> coapClientFactoryCons =
                     mockConstruction(COAPClientFactory.class,
                             (mock, mctx) -> coapClientFactoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<COAPConnector> connectorCons =
                     mockConstruction(COAPConnector.class,
                             (mock, mctx) -> connectorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurationUpdateHandlerImpl> configHandlerCons =
                     mockConstruction(ConfigurationUpdateHandlerImpl.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceListenerBundle> serviceListenerCons =
                     mockConstruction(ServiceListenerBundle.class,
                             (mock, mctx) -> serviceListenerArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, deviceInfoProviderCons.constructed().size());
            assertEquals(mockedContext, deviceInfoProviderArgs.get(0).get(0));
            assertEquals(1, atManagerCons.constructed().size());
            assertEquals(mockedContext, atManagerArgs.get(0).get(0));
            assertEquals(1, coapClientFactoryCons.constructed().size());
            assertEquals(deviceInfoProviderCons.constructed().get(0), coapClientFactoryArgs.get(0).get(0));
            assertEquals(atManagerCons.constructed().get(0), coapClientFactoryArgs.get(0).get(1));
            assertEquals(1, connectorCons.constructed().size());
            assertEquals(coapClientFactoryCons.constructed().get(0), connectorArgs.get(0).get(0));
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(connectorCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
            assertEquals(2, serviceListenerCons.constructed().size());
            assertEquals(mockedContext, serviceListenerArgs.get(0).get(0));
            assertEquals(DeviceInfoProvider.class, serviceListenerArgs.get(0).get(1));
            assertEquals(mockedContext, serviceListenerArgs.get(1).get(0));
            assertEquals(ATManager.class, serviceListenerArgs.get(1).get(1));
            verify(mockedContext).registerService(eq(OpenGateConnector.class),
                    eq(connectorCons.constructed().get(0)), any());
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

        doThrow(new RuntimeException("")).when(mockedConfigHandler).applyConfiguration();

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "deviceInfoProvider", mockedDeviceInfoProvider);
        Whitebox.setInternalState(testActivator, "atManager", mockedATManager);
        Whitebox.setInternalState(testActivator, "connector", mockedConnector);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "deviceInfoServiceListener", mockedDeviceInfoServiceListener);
        Whitebox.setInternalState(testActivator, "atManagerServiceListener", mockedATManagerServiceListener);
        Whitebox.setInternalState(testActivator, "registration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedDeviceInfoServiceListener).close();
        verify(mockedATManagerServiceListener).close();
        verify(mockedATManager).close();
        verify(mockedConfigBundle).close();
        verify(mockedConnector).close();
        verify(mockedDeviceInfoProvider).close();
    }
}
