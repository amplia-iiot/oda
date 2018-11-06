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
		private long secondsFirstPoll;
		private long secondsBetweenPolls;
		@NonNull
        private DevicePattern deviceIdPattern;
	}
	
	static void parse(Dictionary<String, ?> properties, Map<Key, Set<String>> recolections) {
		Enumeration<String> e = properties.keys();
        while(e.hasMoreElements()) {
            String key = e.nextElement();
            String value = (String) properties.get(key);
            try {
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

				String[] valueFields = value.split("\\s*(;\\s*)");
				long firstPoll;
				long secondsBetweenPolls;
				if (valueFields.length == 1) {
					firstPoll = Long.parseLong(valueFields[0]);
					secondsBetweenPolls = firstPoll;
				} else {
					firstPoll = Long.parseLong(valueFields[0]);
					secondsBetweenPolls = Long.parseLong(valueFields[1]);
				}
            	add(recolections, new Key(firstPoll, secondsBetweenPolls, deviceIdInField), idInKey);
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
