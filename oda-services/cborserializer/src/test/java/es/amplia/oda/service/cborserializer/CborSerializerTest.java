package es.amplia.oda.service.cborserializer;

import org.junit.Test;

import static org.junit.Assert.*;

public class CborSerializerTest {

    private final CborSerializer serializer = new CborSerializer();

    @Test
    public void testSerialize() throws Exception {
        int in = 42;
        byte[] expected = {0x18, 0x2A};

        byte[] actual = serializer.serialize(in);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testDeserialize() throws Exception {
        byte[] in = {0x18, 0x2A};
        Integer expected = 42;

        Integer actual = serializer.deserialize(in, Integer.class);

        assertEquals(expected, actual);
    }
}
