package es.amplia.oda.service.jsonserializer;

import lombok.Value;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class JsonSerializerTest {
    
    private final JsonSerializer serializer = new JsonSerializer();

    @Value
    private static class TestObject {
        private int field1;
        private double field2;
        private String field3;
    }

    @Test
    public void testSerialize() throws Exception {
        TestObject test = new TestObject(1, 2.95, "Hello World!");
        byte[] expected = "{\"field1\":1,\"field2\":2.95,\"field3\":\"Hello World!\"}".getBytes();

        byte[] actual = serializer.serialize(test);

        assertArrayEquals(expected, actual);
    }


    @Test
    public void testDeserialize() throws Exception {
        byte[] testInput = "{\"field1\":1,\"field2\":2.95,\"field3\":\"Hello World!\"}".getBytes();
        TestObject expected = new TestObject(1, 2.95, "Hello World!");

        TestObject actual = serializer.deserialize(testInput, TestObject.class);

        assertEquals(expected, actual);
    }
}
