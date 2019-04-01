package es.amplia.oda.service.jsonserializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JsonSerializer.class)
public class JsonSerializerTest {

	@InjectMocks
	private final JsonSerializer serializer = new JsonSerializer();
	@Mock
	ObjectMapper mockedMapper;

	@Value
	public static class TestObject {
		@JsonProperty("field1")private int field1;
		@JsonProperty("field2")private double field2;
		@JsonProperty("field3")private String field3;

		@JsonCreator
		public TestObject(@JsonProperty("field1")int field1, @JsonProperty("field2")double field2, @JsonProperty("field3")String field3) {
			this.field1 = field1;
			this.field2 = field2;
			this.field3 = field3;
		}
	}

	@Test
	public void testSerialize() throws Exception {
		TestObject test = new TestObject(1, 2.95, "Hello World!");
		byte[] expectedOut = "{\"field1\":1,\"field2\":2.95,\"field3\":\"Hello World!\"}".getBytes();
		byte[] realOut;

		realOut = serializer.serialize(test);

		assertArrayEquals(expectedOut, realOut);
	}


	@Test
	public void testDeserialize() throws Exception {
		byte[] in = "{\"field1\":1,\"field2\":2.95,\"field3\":\"Hello World!\"}".getBytes();
		TestObject expectedOut = new TestObject(1, 2.95, "Hello World!");
		TestObject realOut;

		realOut = serializer.deserialize(in, TestObject.class);

		assertEquals(expectedOut, realOut);
	}
}
