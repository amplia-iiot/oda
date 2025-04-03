package es.amplia.oda.operation.rebootequipment;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinder;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinder.Return;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinderImpl;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.core.commons.utils.operation.response.Operation;
import es.amplia.oda.core.commons.utils.operation.response.OperationResponse;
import es.amplia.oda.core.commons.utils.operation.response.OperationResultCode;
import es.amplia.oda.core.commons.utils.operation.response.Response;
import es.amplia.oda.core.commons.utils.operation.response.StepResultCode;
import es.amplia.oda.event.api.EventDispatcher;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

class RebootEquipmentImpl implements EventHandler, CustomOperation {

    public static final String STARTED_BUNDLE_EVENT = "org/osgi/framework/BundleEvent/STARTED";
    
    private static final String REBOOT_EQUIPMENT_OPERATION_NAME = "REBOOT_EQUIPMENT";
    private static final String REBOOT_EQUIPMENT_FILE_NAME = "rebootequipment.info";
    private static final String FELIX_INSTALL_DIR = "felix.fileinstall.dir";
    private static final String PROPERTY_SEPARATOR = ";";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RebootEquipmentImpl.class);

    private final BundleContext context;
    private final ResponseDispatcher dispatcher;
    private final OpenGateConnector ogConnector;
    private final DatastreamsGettersFinder datastreamsGettersFinder;
    private final EventDispatcher eventDispatcher;

    private Set<String> datastreamCollected = new HashSet<>(Arrays.asList(
            "device.software",
            "device.cpu.usage",
            "device.ram.usage",
            "device.storage.disk.usage",
            "device.communicationModules[].subscription.mobile.signalStrength",
            "device.communicationModules[].mobile.imei",
            "device.communicationModules[].subscription.mobile.icc",
            "device.communicationModules[].subscription.mobile.imsi")
        );

    private Timer timer = null;
    //private final String symbolicName;
    private List<String> bundlesName = new ArrayList<>();
    private String opId = null;
    private String configPath = null;

    public RebootEquipmentImpl(BundleContext context, ResponseDispatcher respDispatcher, OpenGateConnector connector, DatastreamsGettersFinderImpl datastreamsGettersFinder, EventDispatcher evDispatcher) {
        this.context = context;
        this.dispatcher = respDispatcher;
        this.ogConnector = connector;
        this.datastreamsGettersFinder = datastreamsGettersFinder;
        this.eventDispatcher = evDispatcher;
        //this.symbolicName = context.getBundle().getSymbolicName();

        // Recuperamos el valor de la operación en curso si es que la hubiera
        String []arrayDir = context.getProperty(FELIX_INSTALL_DIR).split(",");
        for (int i = 0; i < arrayDir.length; i++) {
            String dir = arrayDir[i];
            if (dir.contains("conf")) configPath = dir + "/" + REBOOT_EQUIPMENT_FILE_NAME;
        }
        if (configPath == null) configPath = "./configuration/" + REBOOT_EQUIPMENT_FILE_NAME; // Dejamos la carpeta ./configuration como ruta por defecto
        restoreOperationStatus();
    }

    @Override
    public String getOperationSatisfied() {
        return REBOOT_EQUIPMENT_OPERATION_NAME;
    }

    @Override
    public void handleEvent(Event event) {
        if (opId != null) { // Quiere decir que tenemos una operación REBBOT_EQUIPMENT en curso y debemos atender a los eventos
            //String eventBundleName = (String) event.getProperty(EventConstants.BUNDLE_SYMBOLICNAME);
            if (event.getTopic().equals(STARTED_BUNDLE_EVENT)/* && !eventBundleName.equals(symbolicName)*/) {
                String eventBundleName = (String) event.getProperty(EventConstants.BUNDLE_SYMBOLICNAME);
                LOGGER.debug("Bundle " + eventBundleName + " STARTED");
                bundlesName.remove(eventBundleName);
                if (bundlesName.isEmpty()) {
                    /*synchronized(bundlesName) {
                        // Desbloqueamos el hilo
                        bundlesName.notify();
                    }*/
                    // Paramos el timer y enviamos la respuesta y los datos de recolección
                    if (timer != null) timer.cancel();
                    waitForConnected();
                    sendCollection();
                    sendResponse(OperationResultCode.SUCCESSFUL, "Operation finished successfull");
                } else {
                    // Paramos y arrancamos el timer por si consiguen arrancar todos los bundles
                    if (timer != null) timer.cancel();
                    timer = new Timer();
                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            StringBuilder bdlNotStarted = new StringBuilder();
                            bundlesName.forEach(bn -> bdlNotStarted.append(bn + " |"));
                            String operDescription = "Bundles not started: " + bdlNotStarted.toString();
                            waitForConnected();
                            sendResponse(OperationResultCode.ERROR_PROCESSING, operDescription);
                        }
                        
                    }, 20000);
                }
            }
        }
    }
    
    @Override
    public CompletableFuture<Result> execute(String deviceId, String operationId, Map<String, Object> params) {
        LOGGER.debug("Reboot Equipment - Restarting all bundles in ODA");
        //OperationResultCode operStatus = OperationResultCode.SUCCESSFUL;
        //String operDescription = "Operation finished successfull";
        StringBuilder bdlNotStopped = new StringBuilder();
        List<es.amplia.oda.core.commons.utils.operation.response.Step> steps = new ArrayList<>();
        //List<Bundle> bundles = new ArrayList<>();

        opId = operationId;

        Bundle[] bundlesArray = this.context.getBundles();

        saveOperationStatus(opId, bundlesArray);

        // Enviamos el paso de que hemos recibido la operación
        steps.add(new es.amplia.oda.core.commons.utils.operation.response.Step(REBOOT_EQUIPMENT_OPERATION_NAME, StepResultCode.SUCCESSFUL, "Bundles to be rebooted: " + bundlesArray.length, null, null));
        sendResponse(null, operationId, steps);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        for (int i = 0; i < bundlesArray.length; i++) {
            Bundle bdl = bundlesArray[i];
            String bdSymbolicName = bdl.getSymbolicName();
            //if (bdSymbolicName.startsWith("es.amplia") && !bdSymbolicName.equals(symbolicName)) {
                try {
                    bdl.stop();
                    LOGGER.debug("Bundle " + bdSymbolicName + " STOPPED");
                    //bundlesName.add(bdSymbolicName);
                    //bundles.add(bdl);
                } catch (BundleException e) {
                    LOGGER.error("Error stopping bundle " + bdSymbolicName , e);
                    bdlNotStopped.append(bdSymbolicName + " | ");
                }
            //}
        }

        /*boolean notCompleted = true;
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
            StringBuilder bdlNotStarted = new StringBuilder();
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
        opId = null;*/

        return CompletableFuture.completedFuture(null);
    }

    /*private void startBundles(List<Bundle> bundles) {
        bundles.forEach(b-> {
            try {
                // Con esta comprobación sólo arrancamos los bundles que estén en la lista, que son los bundles que falten por arrancar
                if (bundlesName.contains(b.getSymbolicName())) b.start();
            } catch (BundleException e) {
                LOGGER.error("Error starting bundle " + b.getSymbolicName(), e);
            }
        });
    }*/

    private void waitForConnected() {
        boolean notConnected = true;
        int retries = 11;
        while (notConnected && retries > 0) {
            if (ogConnector.isConnected()) notConnected = false;
            else retries--;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //LOGGER.error("Error sleeping", e);
            }
        }
    }

    private void sendResponse (OperationResultCode operStatus, String operDescription) {
        List<es.amplia.oda.core.commons.utils.operation.response.Step> steps = new ArrayList<>();
        sendResponse(operStatus, operDescription, steps);
    }

    private void sendResponse (OperationResultCode operStatus, String operDescription, List<es.amplia.oda.core.commons.utils.operation.response.Step> steps) {
        OperationResponse resp = new OperationResponse("9.0", new Operation
                        (new Response(opId, null, null, REBOOT_EQUIPMENT_OPERATION_NAME, operStatus, operDescription, steps)));
        dispatcher.publishResponse(resp);
        if (operStatus != null) {
            // Al tener Status se cierra la operación por lo que eliminamos el fichero
            File f = new File(this.configPath);
            f.delete();
        }
    }

    private void saveOperationStatus (String opId, Bundle []bundles) {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < bundles.length; i++) {
            Bundle bdl = bundles[i];
            names.append(bdl.getSymbolicName() + PROPERTY_SEPARATOR);
        }
        names.deleteCharAt(names.length() - 1); // Borramos el último PROPERTY_SEPARATOR
        Properties prop = new Properties();
        prop.setProperty(opId, names.toString());
        try {
            LOGGER.debug("Saving file: " + this.configPath);
            FileWriter fw = new FileWriter(this.configPath);
            prop.store(fw, null);
            fw.flush();
            LOGGER.debug("File saved!!!!");
        } catch (Throwable e) {
            LOGGER.error("Error saving Operation Status", e);
        }
    }

    private void restoreOperationStatus() {
        File file = new File(this.configPath);
        if (file.exists()) {
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream(this.configPath));
                prop.forEach( (k, v) -> this.opId = (String)k);
                this.bundlesName.addAll(Arrays.asList(prop.getProperty(opId).split(PROPERTY_SEPARATOR)));

                Bundle[] bundles = context.getBundles();
                for (int i = 0; i < bundles.length; i++) {
                    Bundle bdl = bundles[i];
                    if (bdl.getState() == Bundle.ACTIVE) bundlesName.remove(bdl.getSymbolicName()); // El bundle ya se ha iniciado por lo que lo borramos de la lista
                }
            } catch (Throwable e) {
                LOGGER.error("Error restoring Operation Status", e);
            }
        }
    }

    private void sendCollection() {
        DevicePattern deviceIdPattern = DevicePattern.NullDevicePattern;
        LOGGER.debug("Sending collection for ({},{})", deviceIdPattern, this.datastreamCollected);
        Return getters = this.datastreamsGettersFinder.getGettersSatisfying(deviceIdPattern, this.datastreamCollected);
        List<es.amplia.oda.core.commons.utils.Event> events = new ArrayList<>();

        for (DatastreamsGetter getter : getters.getGetters()) {
            for (String deviceId : getter.getDevicesIdManaged()) {
                if (deviceIdPattern.match(deviceId)) {
                    CompletableFuture<CollectedValue> futureValue = getter.get(deviceId);
                    if (futureValue != null) {
                        futureValue.thenAccept(
                            data -> events.add(new es.amplia.oda.core.commons.utils.Event(getter.getDatastreamIdSatisfied(), deviceId, null, data.getFeed(), data.getAt(), data.getValue()))
                            );
                        LOGGER.debug("Adding collection for datastreamsGetter of {}", getter.getDatastreamIdSatisfied());
                    }
                }
            }
        }

        eventDispatcher.publishImmediately(events);
    }

}
