package es.amplia.oda.operation.update;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.MapBasedDictionary;
import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.update.configuration.UpdateConfigurationHandler;
import es.amplia.oda.operation.update.internal.*;
import es.amplia.oda.operation.update.operations.DeploymentElementOperationFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Dictionary;

public class Activator implements BundleActivator {
    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    private ConfigurableBundle configurableUpdate;
    private ServiceRegistration<EventHandler> eventHandlerServiceRegistration;
    private ServiceRegistration<OperationUpdate> operationUpdateRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        logger.info("Starting Operation Update Activator");

        DeviceInfoProvider deviceInfoProvider = new DeviceInfoProviderProxy(bundleContext);
        FileManager fileManager = new FileManagerImpl();
        BackupManager backupManager = new BackupManagerImpl(fileManager);
        DownloadManager downloadManager = new DownloadManagerImpl(deviceInfoProvider, fileManager);
        OperationUpdateEventHandler operationUpdateEventHandler = new OperationUpdateEventHandler(bundleContext);
        String[] updateTopics =
                new String[] { "org/osgi/framework/BundleEvent/*", "org/osgi/service/cm/ConfigurationEvent/*" };
        Dictionary<String, Object> props = new MapBasedDictionary<>(String.class);
        props.put(EventConstants.EVENT_TOPIC, updateTopics);
        eventHandlerServiceRegistration =
                bundleContext.registerService(EventHandler.class, operationUpdateEventHandler, props);
        DeploymentElementOperationFactory deploymentElementOperationFactory =
                new DeploymentElementOperationFactory(fileManager, operationUpdateEventHandler);
        InstallManager installManager = new InstallManagerImpl(deploymentElementOperationFactory);
        OperationUpdateImpl operationUpdate =
                new OperationUpdateImpl(backupManager, downloadManager, installManager);
        UpdateConfigurationHandler configHandler = new UpdateConfigurationHandler(operationUpdate);
        operationUpdateRegistration = bundleContext.registerService(OperationUpdate.class, operationUpdate, null);
        configurableUpdate = new ConfigurableBundleImpl(bundleContext, configHandler,
                Collections.singletonList(operationUpdateRegistration));

        logger.info("Operation Update Activator started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        logger.info("Stopping Operation Update Activator");

        operationUpdateRegistration.unregister();
        eventHandlerServiceRegistration.unregister();
        configurableUpdate.close();

        logger.info("Operation Update Activator stopped");
    }
}
