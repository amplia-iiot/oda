package es.amplia.oda.operation.update;

import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.event.api.ResponseDispatcherProxy;
import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.update.configuration.UpdateConfigurationHandler;
import es.amplia.oda.operation.update.internal.*;

import es.amplia.oda.operation.update.operations.DeploymentElementOperationFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Activator.class, ConfigurableBundle.class})
public class ActivatorTest {

    private final Activator activator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceRegistration<OperationUpdate> mockedRegistration;
    @Mock
    private ServiceRegistration<EventHandler> mockedEventHandlerServiceRegistration;
    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProviderProxy;
    @Mock
    private FileManagerImpl mockedFileManagerImpl;
    @Mock
    private BackupManagerImpl mockedBackupManagerImpl;
    @Mock
    private DownloadManagerImpl mockedDownloadManagerImpl;
    @Mock
    private DeploymentElementOperationFactory mockedDeploymentElementOperationFactory;
    @Mock
    private InstallManagerImpl mockedInstallManagerImpl;
    @Mock
    private OperationUpdateImpl mockedOperationUpdateImpl;
    @Mock
    private UpdateConfigurationHandler mockedUpdateConfigurationHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private ResponseDispatcherProxy mockedDispatcher;


    @Test
    public void testStart() throws Exception {
        whenNew(DeviceInfoProviderProxy.class).withAnyArguments().thenReturn(mockedDeviceInfoProviderProxy);
        whenNew(FileManagerImpl.class).withAnyArguments().thenReturn(mockedFileManagerImpl);
        whenNew(BackupManagerImpl.class).withAnyArguments().thenReturn(mockedBackupManagerImpl);
        whenNew(DownloadManagerImpl.class).withAnyArguments().thenReturn(mockedDownloadManagerImpl);
        whenNew(DeploymentElementOperationFactory.class).withAnyArguments().thenReturn(mockedDeploymentElementOperationFactory);
        whenNew(InstallManagerImpl.class).withAnyArguments().thenReturn(mockedInstallManagerImpl);
        whenNew(OperationUpdateImpl.class).withAnyArguments().thenReturn(mockedOperationUpdateImpl);
        whenNew(UpdateConfigurationHandler.class).withAnyArguments().thenReturn(mockedUpdateConfigurationHandler);
        whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
        whenNew(ResponseDispatcherProxy.class).withAnyArguments().thenReturn(mockedDispatcher);

        activator.start(mockedContext);

        verify(mockedContext).registerService(eq(EventHandler.class), any(OperationUpdateEventHandler.class), any());
        verify(mockedContext).registerService(eq(OperationUpdate.class), any(OperationUpdate.class), any());
        verifyNew(DeviceInfoProviderProxy.class).withArguments(mockedContext);
        verifyNew(FileManagerImpl.class).withNoArguments();
        verifyNew(BackupManagerImpl.class).withArguments(any());
        verifyNew(DownloadManagerImpl.class).withArguments(any(), any());
        verifyNew(DeploymentElementOperationFactory.class).withArguments(eq(mockedFileManagerImpl), any());
        verifyNew(InstallManagerImpl.class).withArguments(mockedDeploymentElementOperationFactory);
        verifyNew(OperationUpdateImpl.class).withArguments(mockedBackupManagerImpl, mockedDownloadManagerImpl, mockedInstallManagerImpl, mockedDispatcher, mockedContext);
        verifyNew(UpdateConfigurationHandler.class).withArguments(mockedOperationUpdateImpl);
        verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedUpdateConfigurationHandler), any());
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