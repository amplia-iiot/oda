package es.amplia.oda.datastreams.gpio.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.gpio.GpioDirection;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.gpio.GpioTrigger;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.gpio.GpioDatastreamsManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class DatastreamsGpioConfigurationHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamsGpioConfigurationHandler.class);

    private static final String DATASTREAM_ID_PROPERTY_NAME = "datastreamId";
    private static final String GETTER_PROPERTY_NAME = "getter";
    private static final String SETTER_PROPERTY_NAME = "setter";
    private static final String EVENT_PROPERTY_NAME = "event";

    private final GpioDatastreamsManager gpioDatastreamsManager;
    private final GpioService gpioService;
    private final Map<Integer, GpioPinDatastreamConfiguration> currentConfiguration = new HashMap<>();


    public DatastreamsGpioConfigurationHandler(GpioDatastreamsManager gpioDatastreamsManager,
                                               GpioService gpioService) {
        this.gpioDatastreamsManager = gpioDatastreamsManager;
        this.gpioService = gpioService;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new configuration");
        currentConfiguration.clear();

        Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);
        for (Map.Entry<String, ?> entry: mappedProperties.entrySet()) {
            try {
                int pinIndex = Integer.parseInt((entry.getKey()));
                String[] tokens = getTokensFromProperty((String) entry.getValue());

                GpioPinDatastreamConfiguration.GpioPinDatastreamConfigurationBuilder builder =
                        GpioPinDatastreamConfiguration.builder();

                builder.pinIndex(pinIndex);
                builder.datastreamId(getValueByToken(DATASTREAM_ID_PROPERTY_NAME, tokens)
                        .orElseThrow(() -> new ConfigurationException("Invalid GPIO Datastream configuration "
                                + entry.getKey()
                                + ": Data stream identifier is required")));

                getValueByToken(GETTER_PROPERTY_NAME, tokens)
                        .ifPresent(getterValue -> builder.getter(Boolean.parseBoolean(getterValue)));
                getValueByToken(SETTER_PROPERTY_NAME, tokens)
                        .ifPresent(setterValue -> builder.setter(Boolean.parseBoolean(setterValue)));
                getValueByToken(EVENT_PROPERTY_NAME, tokens)
                        .ifPresent(eventValue -> builder.event(Boolean.parseBoolean(eventValue)));

                currentConfiguration.put(pinIndex, builder.build());
            } catch (NumberFormatException nfe) {
                LOGGER.warn("Invalid configuration {}: {}", entry.getKey(), entry.getValue());
            }
        }

        LOGGER.info("Configuration loaded");
    }

    @Override
    public void loadDefaultConfiguration() {
        LOGGER.info("Loading default configuration");
        currentConfiguration.clear();

        Map<Integer, GpioPin> availablePins = gpioService.getAvailablePins();
        for (Map.Entry<Integer, GpioPin> availablePinEntry : availablePins.entrySet()) {
            int pinIndex = availablePinEntry.getKey();
            GpioPin pin = availablePinEntry.getValue();
            String datastreamId = pin.getName();
            boolean setter = false;
            boolean eventSender = false;

            if (pin.getDirection() == GpioDirection.INPUT && pin.getTrigger() != GpioTrigger.NONE) {
                eventSender = true;
            } else if (pin.getDirection() == GpioDirection.OUTPUT) {
                setter = true;
            }

            GpioPinDatastreamConfiguration pinConfig =
                    GpioPinDatastreamConfiguration
                            .builder()
                            .pinIndex(pinIndex)
                            .datastreamId(datastreamId)
                            .getter(true)
                            .setter(setter)
                            .event(eventSender)
                            .build();

            currentConfiguration.put(pinIndex, pinConfig);
        }

        LOGGER.info("Default configuration loaded");
    }

    @Override
    public void applyConfiguration() {
        gpioDatastreamsManager.close();

        for (Map.Entry<Integer, GpioPinDatastreamConfiguration> entry: currentConfiguration.entrySet()) {
            int pinIndex = entry.getKey();
            GpioPinDatastreamConfiguration config = entry.getValue();

            if (config.isGetter())
                createDatastreamGetter(pinIndex, config.getDatastreamId());

            if (config.isSetter())
                createDatastreamSetter(pinIndex, config.getDatastreamId());

            if (config.isEvent())
                createDatastreamEvent(pinIndex, config.getDatastreamId());
        }
    }

    private void createDatastreamGetter(int pinIndex, String datastreamId) {
        gpioDatastreamsManager.addDatastreamGetter(pinIndex, datastreamId);
    }

    private void createDatastreamSetter(int pinIndex, String datastreamId) {
        gpioDatastreamsManager.addDatastreamSetter(pinIndex, datastreamId);
    }

    private void createDatastreamEvent(int pinIndex, String datastreamId) {
        gpioDatastreamsManager.addDatastreamEvent(pinIndex, datastreamId);
    }
}
