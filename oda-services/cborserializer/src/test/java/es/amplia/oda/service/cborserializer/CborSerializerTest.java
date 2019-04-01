package es.amplia.oda.service.cborserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doThrow;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CborSerializer.class)
public class CborSerializerTest {

	private final CborSerializer serializer = new CborSerializer();

	@Mock
	ObjectMapper mockedMapper;
	private ObjectMapper realMapper = new ObjectMapper(new CBORFactory());
	@Mock
	JsonProcessingException mockedException;

	@Test
	public void testSerialize() throws Exception {
		Whitebox.setInternalState(serializer, "MAPPER", realMapper);
		int in = 42;
		byte[] expectedOut = {0x18, 0x2A};
		byte[] realOut;

		realOut = serializer.serialize(in);

		assertArrayEquals(expectedOut, realOut);
	}

	@Test
	public void testDeserialize() throws Exception {
		Whitebox.setInternalState(serializer, "MAPPER", realMapper);
		byte[] in = {0x18, 0x2A};
		Integer expectedOut = 42;
		Integer realOut;

		realOut = serializer.deserialize(in, Integer.class);

		assertEquals(expectedOut, realOut);
	}

	@Test (expected = IOException.class)
	public void testSerializeIOException() throws Exception {
		Whitebox.setInternalState(serializer, "MAPPER", mockedMapper);
		String dump = "I gonna be a exception";

		doThrow(mockedException).when(mockedMapper).writeValueAsBytes(any());

		serializer.serialize(dump);
	}

	@Test (expected = IOException.class)
	public void testDeserializeIOException() throws Exception {
		Whitebox.setInternalState(serializer, "MAPPER", mockedMapper);
		byte[] dump = {0x64, 0x4E, 0x61, 0x64, 0x61};

		doThrow(mockedException).when(mockedMapper).readValue(dump, String.class);

		serializer.deserialize(dump, String.class);
	}
}
