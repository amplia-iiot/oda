package es.amplia.oda.core.commons.utils;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static org.junit.Assert.*;

public class MapBasedDictionaryTest {

    private static final String TEST_KEY_1 = "test1";
    private static final String TEST_VALUE_1 = "value1";
    private static final String TEST_KEY_2 = "test2";
    private static final String TEST_VALUE_2 = "value2";


    private final MapBasedDictionary<String, Object> testDictionary = new MapBasedDictionary<>(String.class);

    @Before
    public void setUp() {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put(TEST_KEY_1, TEST_VALUE_1);
        innerMap.put(TEST_KEY_2, TEST_VALUE_2);

        Whitebox.setInternalState(testDictionary, "innerMap", innerMap);
    }

    @Test
    public void testSize() {
        assertEquals(2, testDictionary.size());
    }

    @Test
    public void testIsEmpty() {
        assertFalse(testDictionary.isEmpty());
    }

    @Test
    public void testKeys() {
        verifyHasElement(testDictionary.keys(), TEST_KEY_1);
        verifyHasElement(testDictionary.keys(), TEST_KEY_2);
    }

    private <T> void verifyHasElement(Enumeration<T> enumeration, T element) {
        boolean found = false;

        while(enumeration.hasMoreElements() && !found) {
            found = element.equals(enumeration.nextElement());
        }

        if (!found) {
            fail("Element " + element + " not found in enumeration");
        }
    }

    @Test
    public void testElements() {
        verifyHasElement(testDictionary.elements(), TEST_VALUE_1);
        verifyHasElement(testDictionary.elements(), TEST_VALUE_2);
    }

    @Test
    public void testGet() {
        assertEquals(TEST_VALUE_1, testDictionary.get(TEST_KEY_1));
        assertEquals(TEST_VALUE_2, testDictionary.get(TEST_KEY_2));
    }

    @Test
    public void testPut() {
        String testKey = "newKey";
        String testValue = "newValue";

        testDictionary.put(testKey, testValue);

        Map<String, Object> innerMap = Whitebox.getInternalState(testDictionary, "innerMap");
        assertTrue(innerMap.containsKey(testKey));
        assertEquals(testValue, innerMap.get(testKey));
    }

    @Test
    public void testPutReplaceValue() {
        String testValue = "newValue";

        testDictionary.put(TEST_KEY_1, testValue);

        Map<String, Object> innerMap = Whitebox.getInternalState(testDictionary, "innerMap");
        assertTrue(innerMap.containsKey(TEST_KEY_1));
        assertEquals(testValue, innerMap.get(TEST_KEY_1));
    }

    @Test
    public void testRemove() {
        assertEquals(TEST_VALUE_1, testDictionary.remove(TEST_KEY_1));
        assertNull(testDictionary.remove(TEST_KEY_1));
    }

    @Test
    public void testMapBasedDictionaryOf() {
        Dictionary<String, Object> testDictionary = new Hashtable<>();
        testDictionary.put(TEST_KEY_1, TEST_VALUE_1);
        testDictionary.put(TEST_KEY_2, TEST_VALUE_2);

        MapBasedDictionary<String, Object> result = MapBasedDictionary.of(testDictionary, String.class, Object.class);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(TEST_VALUE_1, result.get(TEST_KEY_1));
        assertEquals(TEST_VALUE_2, result.get(TEST_KEY_2));
    }
}