package es.amplia.oda.operation.update.internal;

import es.amplia.oda.operation.update.OperationConfirmationProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static es.amplia.oda.operation.api.OperationUpdate.*;


public class OperationUpdateEventHandler implements EventHandler, OperationConfirmationProcessor {

    static final long OPERATION_TIMEOUT = 75;

    static final String INSTALL_BUNDLE_EVENT = "org/osgi/framework/BundleEvent/INSTALLED";
    static final String UNINSTALL_BUNDLE_EVENT = "org/osgi/framework/BundleEvent/UNINSTALLED";

    static final String UPDATE_CONFIGURATION_EVENT = "org/osgi/service/cm/ConfigurationEvent/CM_UPDATED";
    static final String DELETE_CONFIGURATION_EVENT = "org/osgi/service/cm/ConfigurationEvent/CM_DELETED";

    static final String INSTALL_RULE_EVENT = "org/osgi/service/cm/ConfigurationEvent/CM_DELETED";
    static final String UPDATE_RULE_EVENT = "org/osgi/service/cm/ConfigurationEvent/CM_DELETED";
    static final String DELETE_RULE_EVENT = "org/osgi/service/cm/ConfigurationEvent/CM_DELETED";

    private static final Logger logger = LoggerFactory.getLogger(OperationUpdateEventHandler.class);

    private final BundleContext bundleContext;

    private String waitingForEvent;
    private String waitingForBundleName;
    private CountDownLatch activeLatch;

    public OperationUpdateEventHandler(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(waitingForEvent)) {
            String eventBundleName = (String) event.getProperty(EventConstants.BUNDLE_SYMBOLICNAME);
            if (waitingForBundleName.equals(eventBundleName)) {
                activeLatch.countDown();
            }
        }
    }

    public boolean waitForConfirmation(DeploymentElement deploymentElement) {
        return waitForConfirmation(deploymentElement, false);
    }

    private boolean waitForConfirmation(DeploymentElement deploymentElement, boolean rollback) {
        String name = deploymentElement.getName();
        String version = deploymentElement.getVersion();
        DeploymentElementType type = deploymentElement.getType();
        DeploymentElementOperationType operation = deploymentElement.getOperation();

        if (isAlreadyProcessed(name, version, type, operation)) {
            return true;
        }

        waitingForBundleName = deploymentElement.getName();
        waitingForEvent = getEventToWait(deploymentElement, rollback);
        if (waitingForEvent != null) {
            activeLatch = new CountDownLatch(1);
            try {
                return activeLatch.await(OPERATION_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Thread interrupted while waiting for update operation confirmation");
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    private boolean isAlreadyProcessed(String name, String version, DeploymentElementType type, DeploymentElementOperationType operation) {
        boolean installed = isAlreadyInstalledBundle(name, version);

        return isOrphanConfiguration(type, installed) || isSoftwareOperationAlreadyProcess(type, operation, installed) || isARule(type);
    }

    private boolean isOrphanConfiguration(DeploymentElementType type, boolean installed) {
        return type == DeploymentElementType.CONFIGURATION && !installed;
    }

    private boolean isSoftwareOperationAlreadyProcess(DeploymentElementType type,
                                                      DeploymentElementOperationType operation,
                                                      boolean installed) {
        boolean requireInstallation = operation == DeploymentElementOperationType.INSTALL ||
                operation == DeploymentElementOperationType.UPGRADE;

        return type == DeploymentElementType.SOFTWARE &&
                ((requireInstallation && installed) || (!requireInstallation && !installed));
    }

    private boolean isARule(DeploymentElementType type) {
        return type.equals(DeploymentElementType.RULE);
    }


    private boolean isAlreadyInstalledBundle(String name, String version) {
        return Arrays.stream(bundleContext.getBundles())
                .anyMatch(bundle -> (bundle.getSymbolicName().equals(name))
                                        && bundle.getVersion().equals(Version.parseVersion(version))
                                        && bundle.getState() == Bundle.ACTIVE);
    }


    private String getEventToWait(DeploymentElement deploymentElement, boolean rollback) {
        DeploymentElementType type = deploymentElement.getType();
        DeploymentElementOperationType operation =
                rollback ? getRollbackOperation(deploymentElement.getOperation()): deploymentElement.getOperation();
        if (type.equals(DeploymentElementType.SOFTWARE)) {
            switch (operation) {
                case INSTALL:
                case UPGRADE:
                    return INSTALL_BUNDLE_EVENT;
                case UNINSTALL:
                    return UNINSTALL_BUNDLE_EVENT;
                default:
                    throw new IllegalArgumentException("Unknown operation");
            }
        } else if (type.equals(DeploymentElementType.CONFIGURATION)) {
            switch (operation) {
                case INSTALL:
                case UPGRADE:
                    return UPDATE_CONFIGURATION_EVENT;
                case UNINSTALL:
                    return DELETE_CONFIGURATION_EVENT;
                default:
                    throw new IllegalArgumentException("Unknown operation");
            }
        }
        return null;
    }

    private DeploymentElementOperationType getRollbackOperation(DeploymentElementOperationType operation) {
        switch (operation) {
            case INSTALL:
                return DeploymentElementOperationType.UNINSTALL;
            case UPGRADE:
                return operation;
            case UNINSTALL:
                return DeploymentElementOperationType.INSTALL;
            default:
                throw new IllegalArgumentException("Unknown deployment element operation type " + operation);
        }
    }

    public boolean waitForRollbackConfirmation(DeploymentElement deploymentElement) {
        return waitForConfirmation(deploymentElement, true);
    }
}
