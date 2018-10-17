package es.amplia.oda.dispatcher.opengate;

import lombok.NonNull;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConfigurationParser {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationParser.class);

	private static final String REDUCE_BANDWIDTH_PROPERTY_NAME = "reduceBandwidthMode";

	public static boolean getReduceBandwidthMode(Dictionary<String, ?> properties) {
		return Optional.ofNullable(properties.get(REDUCE_BANDWIDTH_PROPERTY_NAME))
				.map(value -> Boolean.parseBoolean((String) value))
				.orElse(false);
	}

	@Value
	public static class Key {
		@NonNull
        Long seconds; // Only one filed for Key, is prepared for more fields in the future
	}
	
	public static void parse(Dictionary<String, ?> properties, Map<Key, Set<String>> recolections) {
		Enumeration<String> e = properties.keys();
        while(e.hasMoreElements()) {
            String key = e.nextElement();
            String line = (String) properties.get(key);
            try {
            	Long secondsForCollection = Long.parseLong(line);
            	add(recolections, new Key(secondsForCollection), key);
            } catch(NumberFormatException ex) {
            	logger.info("Rejecting configuration '{}={}' because values are not numbers", key, line);
            	continue;
            }
        }
	}

	private static void add(Map<Key, Set<String>> map, Key key, String id) {
		Set<String> set = map.get(key);
		if(set==null) {
			set = new HashSet<>();
			map.put(key, set);
		}
		set.add(id);
	}
}
