package es.amplia.oda.datastreams.lora.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoraDataPacketTest {
	private List<Rxpk> rxpk;

	private LoraDataPacket testDataPacket;

	@BeforeEach
	public void setUp() {
		rxpk = new ArrayList<>();
		rxpk.add(new Rxpk());
		testDataPacket = new LoraDataPacket();
	}

	@Test
	public void testConstructor() {
		assertNull(testDataPacket.getRxpk());
	}

	@Test
	public void testGetRxpk() {
		Whitebox.setInternalState(testDataPacket, "rxpk", rxpk);

		assertEquals(rxpk, testDataPacket.getRxpk());
	}

	@Test
	public void testSetRxpk() {
		testDataPacket.setRxpk(rxpk);

		assertEquals(rxpk, Whitebox.getInternalState(testDataPacket, "rxpk"));
	}

	@Test
	public void testToShortString() {
		Whitebox.setInternalState(testDataPacket, "rxpk", rxpk);

		String result = testDataPacket.toShortString();

		assertEquals((new Rxpk()).toShortString(), result);
	}

	@Test
	public void testToString() {
		Whitebox.setInternalState(testDataPacket, "rxpk", rxpk);

		String result = testDataPacket.toString();

		assertEquals("LoraPackets{[" + (new Rxpk()).toString() + "]}", result);
	}
}
