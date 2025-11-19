package es.amplia.oda.core.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.ResponseDispatcher;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.operation.response.Operation;
import es.amplia.oda.core.commons.utils.operation.response.OperationResponse;
import es.amplia.oda.core.commons.utils.operation.response.OperationResultCode;
import es.amplia.oda.core.commons.utils.operation.response.Response;
import es.amplia.oda.core.commons.utils.operation.response.Step;
import es.amplia.oda.core.commons.utils.operation.response.StepResultCode;

public class OsgiContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsgiContext.class);
    
    private final DatastreamsGettersFinder datastreamsGettersFinder;
    private final DatastreamsSettersFinder datastreamsSettersFinder;
    private final BundleContext bundleContext;
    private HashMap<String, ServiceLocatorOsgi<Object>> bundleContextMap;

    public OsgiContext(BundleContext bundleContext, DatastreamsGettersFinder gettersFinder, DatastreamsSettersFinder settersFinder) {
        this.datastreamsGettersFinder = gettersFinder;
        this.datastreamsSettersFinder = settersFinder;
        this.bundleContext = bundleContext;
        this.bundleContextMap = new HashMap<>();
    }

    public Object getBundle(String bundleName) {
        List<Object> aux = getBundles(bundleName);
        if ( (aux == null) || aux.isEmpty()) return null;
        else return aux.get(0);
    }

    public List<Object> getBundles(String bundleName) {
        ServiceLocatorOsgi<Object> ret = this.bundleContextMap.get(bundleName);
        if (ret == null) {
            try {
                ServiceLocatorOsgi serviceLocator = new ServiceLocatorOsgi<>(bundleContext, Class.forName(bundleName));
                this.bundleContextMap.put(bundleName, serviceLocator);
                return serviceLocator.findAll();
            } catch (Throwable e) {
                return null;
            }
        }
        return ret.findAll();
    }

    public DatastreamsGetter getGetter(String device, String datastreamId) {
        List<DatastreamsGetter> aux = getGetters(device, datastreamId);
        if ( (aux == null) || aux.isEmpty()) return null;
        else return aux.get(0);
    }

    public List<DatastreamsGetter> getGetters(String device, String datastreamId) {
        return datastreamsGettersFinder.getGettersOfDevice(device).stream().filter(getter -> getter.getDatastreamIdSatisfied().equals(datastreamId)).collect(Collectors.toList());
    }

    public DatastreamsSetter getSetter(String device, String datastreamId) {
        List<DatastreamsSetter> aux = getSetters(device, datastreamId);
        if ( (aux == null) || aux.isEmpty()) return null;
        else return aux.get(0);
    }

    public List<DatastreamsSetter> getSetters(String device, String datastreamId) {
        return new ArrayList<>(datastreamsSettersFinder.getSettersSatisfying(device, Collections.singleton(datastreamId)).getSetters().values());
    }

    private void send (OperationResponse resp) {
        Object responseDispatcher = getBundle("es.amplia.oda.core.commons.interfaces.ResponseDispatcher");
        if (responseDispatcher != null) {
            ResponseDispatcher dispatcher = (ResponseDispatcher) responseDispatcher;
            dispatcher.publishResponse(resp);
        } else {
            LOGGER.warn("Response could not be sent because there is not ResponseDispatcher instance");
        }
    }

    public void sendResponse(Map<String, Object> ret) {
        Response response = new Response((String)ret.get("id"),
                            (String)ret.get("deviceId"),
                            (String[])ret.get("path"),
                            (String)ret.get("name"),
                            OperationResultCode.valueOf((String)ret.get("resultCode")),
                            (String)ret.get("resultDescription"),
                            getSteps(ret.get("steps")));
        OperationResponse resp = new OperationResponse("9.0", new Operation(response));
        send(resp);
    }

    public void sendSteps(String deviceId, String[] path, String operationId, Map<String, Object> steps) {
        Response response = new Response(operationId,
                            deviceId,
                            path,
                            null,
                            null,
                            null,
                            getSteps(steps));
        OperationResponse resp = new OperationResponse("9.0", new Operation(response));
        send(resp);
    }

    private List<Step> getSteps(Object steps) {
        if (steps == null) return null;
        List<Object> stepsList = ((Map<String,Object>)steps).values().stream().collect(Collectors.toList());
        return stepsList.stream().map(this::getStep).collect(Collectors.toList());
    }

    private Step getStep(Object stepObject) {
        Map<String,Object> step = (Map<String, Object>) stepObject;
        return new Step((String)step.get("name"), StepResultCode.valueOf((String)step.get("result")), (String)step.get("description"), (Long)step.get("timestamp"), (List<Object>)step.get("response"));
    }

    private void collect(List<Event> events) {
        Object stateManager = getBundle("es.amplia.oda.core.commons.interfaces.StateManager");
        if (stateManager != null) {
            StateManager manager = (StateManager) stateManager;
            manager.onReceivedEvents(events);
        } else {
            LOGGER.warn("Collection could not be processed because there is not StateManager instance");
        }
    }

    private void publish(List<Event> events) {
        Object stateManager = getBundle("es.amplia.oda.core.commons.interfaces.StateManager");
        if (stateManager != null) {
            StateManager manager = (StateManager) stateManager;
            manager.publishValues(events);
        } else {
            LOGGER.warn("Collection could not be processed because there is not StateManager instance");
        }
    }

    public void publish(Map<String, Object> events) {
        if (events == null) return;
        publish(getEvents(events));
    }

    public void collect(Map<String, Object> events) {
        if (events == null) return;
        collect(getEvents(events));
    }

    private List<Event> getEvents(Map<String,Object> events) {
        List<Object> eventList = ((Map<String,Object>)events).values().stream().collect(Collectors.toList());
        return eventList.stream().map(this::getEvent).collect(Collectors.toList());
    }

    private Event getEvent(Object eventObject) {
        Map<String,Object> event = (Map<String, Object>) eventObject;
        return new Event((String)event.get("datastreamId"), (String)event.get("deviceId"), (String[])event.get("path"), (String)event.get("feed"), (Long)event.get("at"), event.get("value"));
    }

    /**
     * Function used from rules to log messages in INFO level
     * @param msg the message to log
     * @param objects the variables used in log message
     */
    public void logInfo(String msg, Object...objects) {
        LOGGER.info(msg, objects);
    }

    /**
     * Function used from rules to log messages in DEBUG level
     * @param msg the message to log
     * @param objects the variables used in log message
     */
    public void logDebug(String msg, Object...objects) {
        LOGGER.debug(msg, objects);
    }

    /**
     * Function used from rules to log messages in ERROR level
     * @param msg the message to log
     * @param objects the variables used in log message
     */
    public void logError(String msg, Object...objects) {
        LOGGER.error(msg, objects);
    }

    /**
     * Function used from rules to log messages in TRACE level
     * @param msg the message to log
     * @param objects the variables used in log message
     */
    public void logTrace(String msg, Object...objects) {
        LOGGER.trace(msg, objects);
    }

    /**
     * Function used from rules to log messages in WARN level
     * @param msg the message to log
     * @param objects the variables used in log message
     */
    public void logWarn(String msg, Object...objects) {
        LOGGER.warn(msg, objects);
    }

    public void close() {
        this.bundleContextMap.values().forEach(sl -> sl.close());
    }
}
