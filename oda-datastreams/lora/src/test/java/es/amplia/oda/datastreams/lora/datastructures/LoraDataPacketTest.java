package es.amplia.oda.datastreams.lora.datastructures;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoraDataPacket.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class LoraDataPacketTest {
	private List<Rxpk> rxpk;

	private LoraDataPacket testDataPacket;

	@Before
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
