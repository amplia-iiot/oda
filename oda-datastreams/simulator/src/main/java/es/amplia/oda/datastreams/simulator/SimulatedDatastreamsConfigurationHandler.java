package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class SimulatedDatastreamsConfigurationHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedDatastreamsConfigurationHandler.class);


    private final SimulatedDatastreamsManager manager;
    private final List<SimulatedDatastreamsConfiguration> lastConfiguration = new ArrayList<>();


    SimulatedDatastreamsConfigurationHandler(SimulatedDatastreamsManager datastreamsManager) {
        this.manager = datastreamsManager;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        Enumeration<String> e = props.keys();
        while(e.hasMoreElements()) {
            String key = e.nextElement();
            String value = (String) props.get(key);

            parseSimulatedDatastreamConfiguration(key, value);
        }

    }

    private void parseSimulatedDatastreamConfiguration(String key, String value) {
        try {
            String[] keyFields = key.split(";");
            String datastreamId = keyFields[0];

            String deviceId = "";
            if (keyFields.length == 2) {
                deviceId = keyFields[1];
            }

            String[] valueFields = value.split(",");
            double minValue = Double.parseDouble(valueFields[0]);
            double maxValue = Double.parseDouble(valueFields[1]);
            double maxDiff = Double.parseDouble(valueFields[2]);

            lastConfiguration.add(
                    new SimulatedDatastreamsConfiguration(datastreamId, deviceId, minValue, maxValue, maxDiff));
        } catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("Invalid configuration '{}={}'", key, value, ex);
        }
    }

    @Override
    public void loadDefaultConfiguration() {
        lastConfiguration.clear();
    }

    @Override
    public void applyConfiguration() {
        manager.loadConfiguration(lastConfiguration);
    }
}
