package es.amplia.oda.dispatcher.opengate;

import lombok.NonNull;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConfigurationParser {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationParser.class);

	private static final String REDUCE_BANDWIDTH_PROPERTY_NAME = "reduceBandwidthMode";

	// Hide public default constructor
	private ConfigurationParser() {}

	static boolean getReduceBandwidthMode(Dictionary<String, ?> properties) {
		return Optional.ofNullable(properties.get(REDUCE_BANDWIDTH_PROPERTY_NAME))
				.map(value -> Boolean.parseBoolean((String) value))
				.orElse(false);
	}

	@Value
	public static class Key {
		@NonNull
		private long secondsFirstDispatch;
		@NonNull
        private long secondsBetweenDispatches;
	}
	
	static void parse(Dictionary<String, ?> properties, Map<Key, Set<String>> recolections) {
		Enumeration<String> e = properties.keys();
        while(e.hasMoreElements()) {
            String key = e.nextElement();
            String value = (String) properties.get(key);
            try {
				String[] valueFields = value.split("\\s*(;\\s*)");
				long secondsFirstDispatch;
				long secondsBetweenDispatches;
				if (valueFields.length == 1) {
					secondsFirstDispatch = Long.parseLong(valueFields[0]);
					secondsBetweenDispatches = secondsFirstDispatch;
				} else {
					secondsFirstDispatch = Long.parseLong(valueFields[0]);
					secondsBetweenDispatches = Long.parseLong(valueFields[1]);
				}
            	add(recolections, new Key(secondsFirstDispatch, secondsBetweenDispatches), key);
            } catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            	logger.info("Rejecting configuration '{}={}' because is invalid: {}", key, value, ex);
            }
        }
	}

	private static void add(Map<Key, Set<String>> map, Key key, String id) {
		Set<String> set = map.computeIfAbsent(key, k -> new HashSet<>());
		set.add(id);
	}
}
