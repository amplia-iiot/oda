package es.amplia.oda.operation.update;

import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.ResponseDispatcherProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.update.configuration.UpdateConfigurationHandler;
import es.amplia.oda.operation.update.internal.*;

import es.amplia.oda.operation.update.operations.DeploymentElementOperationFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator activator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceRegistration<OperationUpdate> mockedRegistration;
    @Mock
    private ServiceRegistration<EventHandler> mockedEventHandlerServiceRegistration;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private ResponseDispatcherProxy mockedDispatcher;


    @Test
    public void testStart() throws Exception {
        List<List<?>> deviceInfoProviderArgs = new ArrayList<>();
        List<List<?>> deploymentElementOperationFactoryArgs = new ArrayList<>();
        List<List<?>> installManagerArgs = new ArrayList<>();
        List<List<?>> operationUpdateArgs = new ArrayList<>();
        List<List<?>> updateConfigurationHandlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();
        try (MockedConstruction<DeviceInfoProviderProxy> deviceInfoProviderCons =
                     mockConstruction(DeviceInfoProviderProxy.class,
                             (mock, mctx) -> deviceInfoProviderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<FileManagerImpl> fileManagerCons = mockConstruction(FileManagerImpl.class);
             MockedConstruction<BackupManagerImpl> backupManagerCons = mockConstruction(BackupManagerImpl.class);
             MockedConstruction<DownloadManagerImpl> downloadManagerCons = mockConstruction(DownloadManagerImpl.class);
             MockedConstruction<DeploymentElementOperationFactory> deploymentElementOperationFactoryCons =
                     mockConstruction(DeploymentElementOperationFactory.class,
                             (mock, mctx) -> deploymentElementOperationFactoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<InstallManagerImpl> installManagerCons = mockConstruction(InstallManagerImpl.class,
                     (mock, mctx) -> installManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<OperationUpdateImpl> operationUpdateCons = mockConstruction(OperationUpdateImpl.class,
                     (mock, mctx) -> operationUpdateArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<UpdateConfigurationHandler> updateConfigurationHandlerCons =
                     mockConstruction(UpdateConfigurationHandler.class,
                             (mock, mctx) -> updateConfigurationHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ResponseDispatcherProxy> responseDispatcherCons =
                     mockConstruction(ResponseDispatcherProxy.class)) {

            activator.start(mockedContext);

            verify(mockedContext).registerService(eq(EventHandler.class), any(OperationUpdateEventHandler.class), any());
            verify(mockedContext).registerService(eq(OperationUpdate.class), any(OperationUpdate.class), any());
            assertEquals(1, deviceInfoProviderCons.constructed().size());
            assertEquals(mockedContext, deviceInfoProviderArgs.get(0).get(0));
            assertEquals(1, fileManagerCons.constructed().size());
            assertEquals(1, backupManagerCons.constructed().size());
            assertEquals(1, downloadManagerCons.constructed().size());
            assertEquals(1, deploymentElementOperationFactoryCons.constructed().size());
            assertEquals(fileManagerCons.constructed().get(0), deploymentElementOperationFactoryArgs.get(0).get(0));
            assertEquals(1, installManagerCons.constructed().size());
            assertEquals(deploymentElementOperationFactoryCons.constructed().get(0), installManagerArgs.get(0).get(0));
            assertEquals(1, operationUpdateCons.constructed().size());
            assertEquals(backupManagerCons.constructed().get(0), operationUpdateArgs.get(0).get(0));
            assertEquals(downloadManagerCons.constructed().get(0), operationUpdateArgs.get(0).get(1));
            assertEquals(installManagerCons.constructed().get(0), operationUpdateArgs.get(0).get(2));
            assertEquals(responseDispatcherCons.constructed().get(0), operationUpdateArgs.get(0).get(3));
            assertEquals(mockedContext, operationUpdateArgs.get(0).get(4));
            assertEquals(1, updateConfigurationHandlerCons.constructed().size());
            assertEquals(operationUpdateCons.constructed().get(0), updateConfigurationHandlerArgs.get(0).get(0));
            assertEquals(1, configurableBundleCons.constructed().size());
            assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
            assertEquals(updateConfigurationHandlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(activator, "operationUpdateRegistration", mockedRegistration);
        Whitebox.setInternalState(activator, "eventHandlerServiceRegistration", mockedEventHandlerServiceRegistration);
        Whitebox.setInternalState(activator, "configurableUpdate", mockedConfigurableBundle);
        Whitebox.setInternalState(activator, "responseDispatcher", mockedDispatcher);

        activator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedEventHandlerServiceRegistration).unregister();
        verify(mockedConfigurableBundle).close();
        verify(mockedDispatcher).close();
    }
}
