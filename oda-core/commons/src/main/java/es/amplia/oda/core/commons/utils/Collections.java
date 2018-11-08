package es.amplia.oda.core.commons.utils;

import java.util.*;

/**
 * Utility class to handle collections.
 */
public final class Collections {

    /**
     * Private constructor to avoid the class instantiation.
     */
    private Collections() {}

    /**
     * Get the map corresponding to the given dictionary object.
     * @param dictionary Dictionary to convert.
     * @param <K> Dictionary key type.
     * @param <V> Dictionary value type.
     * @return Corresponding map.
     */
    public static <K, V> Map<K, V> dictionaryToMap(Dictionary<K, V> dictionary) {
        Map<K, V> map = new HashMap<>();
        Enumeration<K> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            K key = keys.nextElement();
            map.put(key, dictionary.get(key));
        }
        return map;
    }

    /**
     * Get the map corresponding to the given properties object.
     * @param properties Properties to convert.
     * @return Corresponding map.
     */
    public static Map<String, String> propertiesToMap(Properties properties) {
        Map<String, String> map = new HashMap<>();
        for (String propertyName: properties.stringPropertyNames()) {
            map.put(propertyName, properties.getProperty(propertyName));
        }
        return map;
    }
}
