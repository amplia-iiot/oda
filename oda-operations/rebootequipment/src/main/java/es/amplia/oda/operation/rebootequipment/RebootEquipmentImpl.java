package es.amplia.oda.operation.rebootequipment;

import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.utils.operation.response.Operation;
import es.amplia.oda.core.commons.utils.operation.response.OperationResponse;
import es.amplia.oda.core.commons.utils.operation.response.OperationResultCode;
import es.amplia.oda.core.commons.utils.operation.response.Response;
import es.amplia.oda.event.api.ResponseDispatcher;
import es.amplia.oda.operation.api.CustomOperation;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class RebootEquipmentImpl implements EventHandler, CustomOperation {

    public static final String STARTED_BUNDLE_EVENT = "org/osgi/framework/BundleEvent/STARTED";
    
    private static final String REBOOT_EQUIPMENT_OPERATION_NAME = "REBOOT_EQUIPMENT";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RebootEquipmentImpl.class);

    private final BundleContext context;
    private final ResponseDispatcher dispatcher;
    private final OpenGateConnector ogConnector;
    private final String symbolicName;
    private List<String> bundlesName = new ArrayList<>();
    private String opId = null;

    public RebootEquipmentImpl(BundleContext context, ResponseDispatcher dispatcher, OpenGateConnector connector) {
        this.context = context;
        this.dispatcher = dispatcher;
        this.ogConnector = connector;
        this.symbolicName = context.getBundle().getSymbolicName();
    }

    @Override
    public String getOperationSatisfied() {
        return REBOOT_EQUIPMENT_OPERATION_NAME;
    }

    @Override
    public void handleEvent(Event event) {
        if (opId != null) { // Quiere decir que tenemos una operación REBBOT_EQUIPMENT en curso y debemos atender a los eventos
            String eventBundleName = (String) event.getProperty(EventConstants.BUNDLE_SYMBOLICNAME);
            if (event.getTopic().equals(STARTED_BUNDLE_EVENT) && !eventBundleName.equals(symbolicName)) {
                LOGGER.debug("Bundle " + eventBundleName + " STARTED");
                bundlesName.remove(eventBundleName);
                if (bundlesName.isEmpty()) {
                    synchronized(bundlesName) {
                        // Desbloqueamos el hilo
                        bundlesName.notify();
                    }
                }
            }
        }
    }
    
    @Override
    public CompletableFuture<Result> execute(String deviceId, String operationId, Map<String, Object> params) {
        LOGGER.debug("Reboot Equipment - Restarting all bundles in ODA");
        OperationResultCode operStatus = OperationResultCode.SUCCESSFUL;
        String operDescription = "Operation finished successfull";
        StringBuffer bdlNotStopped = new StringBuffer();
        List<es.amplia.oda.core.commons.utils.operation.response.Step> steps = new ArrayList<>();
        List<Bundle> bundles = new ArrayList<>();

        opId = operationId;

        Bundle[] bundlesArray = this.context.getBundles();

        for (int i = 0; i < bundlesArray.length; i++) {
            Bundle bdl = bundlesArray[i];
            String bdSymbolicName = bdl.getSymbolicName();
            if (bdSymbolicName.startsWith("es.amplia") && !bdSymbolicName.equals(symbolicName)) {
                try {
                    bdl.stop();
                    LOGGER.debug("Bundle " + bdSymbolicName + " STOPPED");
                    bundlesName.add(bdSymbolicName);
                    bundles.add(bdl);
                } catch (BundleException e) {
                    LOGGER.error("Error stopping bundle " + bdSymbolicName , e);
                    bdlNotStopped.append(bdSymbolicName + " | ");
                }
            }
        }

        boolean notCompleted = true;
        int retries = 3;
        while (notCompleted && retries > 0) {
            startBundles(bundles);
            try {
                synchronized(bundlesName) {
                    bundlesName.wait(20000);
                }
            } catch (InterruptedException e) {
            }
            if (bundlesName.isEmpty()) notCompleted = false;
            else retries--;
        }

        if (notCompleted) {
            operStatus = OperationResultCode.ERROR_PROCESSING;
            StringBuffer bdlNotStarted = new StringBuffer();
            bundlesName.forEach(bn -> bdlNotStarted.append(bn + " |"));
            operDescription = "Bundles not started: " + bdlNotStarted.toString();
        } else if (bdlNotStopped.length() != 0) {
            operDescription = "Bundles not stopped: " + bdlNotStopped.toString();
        }

        boolean notConnected = true;
        retries = 10;
        while (notConnected && retries > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //LOGGER.error("Error sleeping", e);
            }
            if (ogConnector.isConnected()) notConnected = false;
            else retries--;
        }

        OperationResponse resp = new OperationResponse("9.0", new Operation
                        (new Response(opId, deviceId, null, REBOOT_EQUIPMENT_OPERATION_NAME, operStatus, operDescription, steps)));
        dispatcher.publishResponse(resp);
        opId = null;

        return CompletableFuture.completedFuture(null);
    }

    private void startBundles(List<Bundle> bundles) {
        bundles.forEach(b-> {
            try {
                // Con esta comprobación sólo arrancamos los bundles que estén en la lista, que son los bundles que falten por arrancar
                if (bundlesName.contains(b.getSymbolicName())) b.start();
            } catch (BundleException e) {
                LOGGER.error("Error starting bundle " + b.getSymbolicName(), e);
            }
        });
    }

}
