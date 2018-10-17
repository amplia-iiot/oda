package es.amplia.oda.core.commons.utils;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CollectionsTest {

    private int NUMBER_ELEMENTS_TO_TEST = 10;

    @Test
    public void testDictionaryToMap() {
        Dictionary<String, Integer> dictionary = new Hashtable<>();

        for (int i = 0; i < NUMBER_ELEMENTS_TO_TEST; i++) {
            dictionary.put("Key" + i, i);
        }

        Map<String, Integer> resultMap = Collections.dictionaryToMap(dictionary);

        assertNotNull(resultMap);
        assertEquals(dictionary.size(), resultMap.size());
        Enumeration<String> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            assertTrue(resultMap.containsKey(key));
            assertEquals(dictionary.get(key), resultMap.get(key));
        }
    }

    @Test
    public void testEmptyDictionaryToMap() {
        Dictionary<String,String> emptyDictionary = new Hashtable<>();

        Map<String,String> resultMap = Collections.dictionaryToMap(emptyDictionary);

        assertNotNull(resultMap);
        assertTrue(resultMap.isEmpty());
    }

    @Test
    public void testPropertiesToMap() {
        Properties properties = new Properties();

        for (int i = 0; i < NUMBER_ELEMENTS_TO_TEST; i++) {
            properties.put("Key" + i, String.valueOf(i));
        }

        Map<String, String> resultMap = Collections.propertiesToMap(properties);

        assertNotNull(resultMap);
        assertEquals(properties.size(), resultMap.size());
        for (String propertyName: properties.stringPropertyNames()) {
            assertTrue(resultMap.containsKey(propertyName));
            assertEquals(properties.get(propertyName), resultMap.get(propertyName));
        }
    }

    @Test
    public void testEmptyPropertiesToMap() {
        Properties emptyProperties = new Properties();

        Map<String, String> resultMap = Collections.propertiesToMap(emptyProperties);

        assertNotNull(resultMap);
        assertTrue(resultMap.isEmpty());
    }

}