package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.DevicePattern;

import lombok.NonNull;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class ConfigurationParser {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationParser.class);

	// Hide public default constructor
	private ConfigurationParser() {}

	@Value
	public static class Key {
		Long seconds;
		@NonNull
        DevicePattern deviceIdPattern;
	}
	
	static void parse(Dictionary<String, ?> properties, Map<Key, Set<String>> recolections) {
		Enumeration<String> e = properties.keys();
        while(e.hasMoreElements()) {
            String key = e.nextElement();
            String line = (String) properties.get(key);
            String[] keyFields = key.split("\\s*(;\\s*)");
            String idInKey;
            DevicePattern deviceIdInField;
            if(keyFields.length == 1) {
            	idInKey = keyFields[0];
            	deviceIdInField=DevicePattern.NullDevicePattern;
            } else {
            	idInKey = keyFields[0];
            	deviceIdInField=new DevicePattern(keyFields[1]);
            }
            try {
            	Long secondsForCollection = Long.parseLong(line);
            	add(recolections, new Key(secondsForCollection, deviceIdInField), idInKey);
            } catch(NumberFormatException ex) {
            	logger.info("Rejecting configuration '{}={}' because values are not numbers", key, line);
            }
        }
	}

	private static void add(Map<Key, Set<String>> map, Key key, String id) {
        Set<String> set = map.computeIfAbsent(key, k -> new HashSet<>());
        set.add(id);
	}
}
