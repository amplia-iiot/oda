package es.amplia.oda.datastreams.simulator.configuration;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import es.amplia.oda.datastreams.simulator.SimulatedDatastreamsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SimulatedDatastreamsConfigurationHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedDatastreamsConfigurationHandler.class);


    private final SimulatedDatastreamsManager manager;
    private final List<SimulatedDatastreamsGetterConfiguration> gettersConfigured = new ArrayList<>();
    private final List<SetDatastreamSetterConfiguration> settersConfigured = new ArrayList<>();


    public SimulatedDatastreamsConfigurationHandler(SimulatedDatastreamsManager datastreamsManager) {
        this.manager = datastreamsManager;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {

        loadDefaultConfiguration();

        Enumeration<String> e = props.keys();
        while(e.hasMoreElements()) {
            String key = e.nextElement();
            String value = (String) props.get(key);

            parseSimulatedDatastreamConfiguration(key, value);
        }

    }

    private void parseSimulatedDatastreamConfiguration(String key, String value) {
        try {
            String[] keyFields = key.split("\\s*;\\s*");
            String datastreamId = keyFields[0].trim();

            String deviceId = "";
            if (keyFields.length >= 2) {
                deviceId = keyFields[1];
            }

            String feed = null;
            if (keyFields.length == 3) {
                feed = keyFields[2];
            }

            String[] valueFields = value.split("\\s*,\\s*");
            if (valueFields.length < 2) {
                settersConfigured.add(
                        new SetDatastreamSetterConfiguration(datastreamId, deviceId));
            } else if (valueFields.length == 2) {
                Object datastreamValue = getValue(valueFields[0].trim(), valueFields[1]);
                gettersConfigured.add(
                        new ConstantDatastreamGetterConfiguration(datastreamId, deviceId, feed, datastreamValue));
            } else if (valueFields.length == 3) {
                double minValue = Double.parseDouble(valueFields[0].trim());
                double maxValue = Double.parseDouble(valueFields[1]);
                double maxDiff = Double.parseDouble(valueFields[2]);

                gettersConfigured.add(
                        new RandomDatastreamGetterConfiguration(datastreamId, deviceId, feed, minValue, maxValue, maxDiff));
            } else {
                LOGGER.error("Invalid configuration '{}={}'", key, value);
            }

        } catch(IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("Invalid configuration '{}={}'", key, value, ex);
        }
    }

    private Object getValue(String type, String value) {
        switch (type.toLowerCase()) {
            case "string":
                return value;
            case "int":
            case "integer":
                return Integer.valueOf(value);
            case "float":
                return Float.valueOf(value);
            case "double":
            case "number":
                return Double.valueOf(value);
            default:
                throw new IllegalArgumentException("Invalid type " + type);
        }
    }

    @Override
    public void loadDefaultConfiguration() {
        gettersConfigured.clear();
        settersConfigured.clear();
    }

    @Override
    public void applyConfiguration() {
        manager.loadConfiguration(gettersConfigured, settersConfigured);
    }
}
