package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.Scheduler;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

class DispatcherConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherConfigurationUpdateHandler.class);

    static final String REDUCED_OUTPUT_PROPERTY_NAME = "reducedOutput";
    static final String EVENT_CONTENT_TYPE_PROPERTY_NAME = "eventContentType";


    private final EventDispatcherFactory eventDispatcherFactory;
    private final Scheduler scheduler;
    private final ServiceRegistrationManager<EventDispatcher> eventDispatcherRegistrationManager;

    private final Map<DispatcherConfiguration, Set<String>> currentConfiguration = new HashMap<>();
    private boolean reducedOutput = false;
    private ContentType eventContentType = ContentType.JSON;


    DispatcherConfigurationUpdateHandler(EventDispatcherFactory eventDispatcherFactory, Scheduler scheduler,
                                         ServiceRegistrationManager<EventDispatcher> eventDispatcherRegistrationManager) {
        this.eventDispatcherFactory = eventDispatcherFactory;
        this.scheduler = scheduler;
        this.eventDispatcherRegistrationManager = eventDispatcherRegistrationManager;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("OpenGate Dispatcher updated with {} properties", props.size());
        currentConfiguration.clear();

        reducedOutput = Optional.ofNullable((String) props.remove(REDUCED_OUTPUT_PROPERTY_NAME))
                .map(Boolean::parseBoolean)
                .orElse(false);
        String contentTypeAsString = (String) props.remove(EVENT_CONTENT_TYPE_PROPERTY_NAME);
        try {
            eventContentType = Optional.ofNullable(contentTypeAsString)
                    .map(ContentType::getContentType)
                    .orElse(ContentType.JSON);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Unknown event content type {}. Configuring event content type as {}", contentTypeAsString,
                    ContentType.JSON, e);
        }


        Enumeration<String> e = props.keys();
        while(e.hasMoreElements()) {
            String key = e.nextElement();
            String value = (String) props.get(key);

            parseDispatchConfiguration(key, value);
        }
    }

    private void parseDispatchConfiguration(String key, String value) {
        try {
            String[] valueFields = value.split(";");
            long secondsFirstDispatch;
            long secondsBetweenDispatches;
            if (valueFields.length == 1) {
                secondsFirstDispatch = Long.parseLong(valueFields[0]);
                secondsBetweenDispatches = secondsFirstDispatch;
            } else {
                secondsFirstDispatch = Long.parseLong(valueFields[0]);
                secondsBetweenDispatches = Long.parseLong(valueFields[1]);
            }
            add(new DispatcherConfiguration(secondsFirstDispatch, secondsBetweenDispatches), key);
        } catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("Rejecting configuration '{}={}' because is invalid: {}", key, value, ex);
        }
    }

    private void add(DispatcherConfiguration dispatcherConfiguration, String datastream) {
        currentConfiguration.merge(dispatcherConfiguration, Collections.singleton(datastream), (d1, d2) -> {
            Set<String> jointSet = new HashSet<>(d1);
            jointSet.addAll(d2);
            return jointSet;
        });
    }

    @Override
    public void loadDefaultConfiguration(){
        currentConfiguration.clear();
    }

    @Override
    public void applyConfiguration() {
        LOGGER.info("Applying new OpenGate Dispatcher configuration");

        LOGGER.info("Clear scheduled tasks and last configuration");
        scheduler.clear();
        eventDispatcherRegistrationManager.unregister();

        EventCollector eventCollector = eventDispatcherFactory.createEventCollector(reducedOutput, eventContentType);
        eventCollector.loadDatastreamIdsToCollect(getDatastreamIds(currentConfiguration));

        currentConfiguration.forEach((conf, datastreams) -> {
            LOGGER.debug("Scheduling dispatch of datastreams '{}' with initial delay {} and every {} seconds",
                    datastreams, conf.getInitialDelay(), conf.getPeriod());
            scheduler.schedule(() -> eventCollector.publishCollectedEvents(datastreams), conf.getInitialDelay(),
                    conf.getPeriod(), TimeUnit.SECONDS);
        });

        eventDispatcherRegistrationManager.register(eventCollector);
    }

    private Collection<String> getDatastreamIds(Map<DispatcherConfiguration, Set<String>> configuration) {
        Collection<String> datastreamIds = new HashSet<>();
        configuration.forEach((conf, datastreams) -> datastreamIds.addAll(datastreams));
        return datastreamIds;
    }
}
