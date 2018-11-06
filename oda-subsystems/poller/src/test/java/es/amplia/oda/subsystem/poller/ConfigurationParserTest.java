package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.DevicePattern;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static es.amplia.oda.core.commons.utils.DevicePattern.NullDevicePattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigurationParserTest {
    @SafeVarargs
    private static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }
    
    private Map<ConfigurationParser.Key, Set<String>> recollections;
    private Dictionary<String, String> properties;

    @Before
    public void setUp() {
        recollections = new HashMap<>();
        properties = new Hashtable<>();
    }

    @Test
    public void parsingEmptyDictionaryLeavesResultEmpty() {
        
        ConfigurationParser.parse(properties, recollections);
        
        assertTrue(recollections.isEmpty());
    }
    
    @Test
    public void onePropertyIsMappedToOneEntryWithOneValueInEachMap() {
        properties.put("id1", "3");
        
        ConfigurationParser.parse(properties, recollections);
        
        assertEquals(1, recollections.size());
        
        Set<String> expected = asSet("id1");
        assertEquals(expected, recollections.get(new ConfigurationParser.Key(3L,3L,NullDevicePattern)));
    }
    
    @Test
    public void twoPropertiesWithSameValuesAreMappedToOneEntryWithTwoValuesInEachMap() {
        properties.put("id1", "3");
        properties.put("id2", "3");
        
        ConfigurationParser.parse(properties, recollections);
        
        assertEquals(1, recollections.size());
        
        Set<String> expected = asSet("id1","id2");
        
        assertEquals(expected, recollections.get(new ConfigurationParser.Key(3L,3L,NullDevicePattern)));
    }

    @Test
    public void propertiesWithDifferentValuesAreMappedToDifferentEntries() {
        properties.put("id1", "3");
        properties.put("id2", "4");
        
        ConfigurationParser.parse(properties, recollections);

        assertEquals(2, recollections.size());

        assertEquals(asSet("id1"), recollections.get(new ConfigurationParser.Key(3L,3L,NullDevicePattern)));
        assertEquals(asSet("id2"), recollections.get(new ConfigurationParser.Key(4L,4L,NullDevicePattern)));
    }

    @Test
    public void propertiesWithDifferentFirstPollValueAreMappedToDifferentEntries() {
        properties.put("id1", "3;3");
        properties.put("id2", "4;3");

        ConfigurationParser.parse(properties, recollections);

        assertEquals(2, recollections.size());

        assertEquals(asSet("id1"), recollections.get(new ConfigurationParser.Key(3L,3L,NullDevicePattern)));
        assertEquals(asSet("id2"), recollections.get(new ConfigurationParser.Key(4L,3L,NullDevicePattern)));
    }

    @Test
    public void propertiesWithDifferentSecondsBetweenPollsValueAreMappedToDifferentEntries() {
        properties.put("id1", "3;3");
        properties.put("id2", "3;4");

        ConfigurationParser.parse(properties, recollections);

        assertEquals(2, recollections.size());

        assertEquals(asSet("id1"), recollections.get(new ConfigurationParser.Key(3L,3L,NullDevicePattern)));
        assertEquals(asSet("id2"), recollections.get(new ConfigurationParser.Key(3L,4L,NullDevicePattern)));
    }

    @Test
    public void differentDeviceIdMappersAreMappedToDifferentEntries() {
        properties.put("id1;dev*", "3");
        properties.put("id1;sect*", "3");
        ConfigurationParser.parse(properties, recollections);

        assertEquals(2, recollections.size());
        
        assertEquals(asSet("id1"), recollections.get(new ConfigurationParser.Key(3L, 3L, new DevicePattern("dev*"))));
        assertEquals(asSet("id1"), recollections.get(new ConfigurationParser.Key(3L, 3L, new DevicePattern("sect*"))));
    }

    @Test
    public void sameDeviceIdMappersAreMappedToSameEntries() {
        properties.put("id1;dev*", "3");
        properties.put("id2;dev*", "3");
        ConfigurationParser.parse(properties, recollections);
        
        assertEquals(1, recollections.size());
        
        assertEquals(asSet("id1","id2"), recollections.get(new ConfigurationParser.Key(3L, 3L,new DevicePattern("dev*"))));
    }
}
