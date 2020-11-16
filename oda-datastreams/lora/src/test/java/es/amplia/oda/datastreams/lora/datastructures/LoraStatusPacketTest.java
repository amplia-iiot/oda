package es.amplia.oda.datastreams.lora.datastructures;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoraStatusPacket.class)
public class LoraStatusPacketTest {

	@Mock
	Stat mockedStat;

	LoraStatusPacket testStatusPacket;

	@Before
	public void setUp() {
		testStatusPacket = new LoraStatusPacket();
	}

	@Test
	public void testConstructor() {
		assertNull(Whitebox.getInternalState(testStatusPacket, "stat"));
	}

	@Test
	public void testGetStat() {
		Whitebox.setInternalState(testStatusPacket, "stat", mockedStat);

		assertEquals(mockedStat, testStatusPacket.getStat());
	}

	@Test
	public void testSetStat() {
		testStatusPacket.setStat(mockedStat);

		assertEquals(mockedStat, Whitebox.getInternalState(testStatusPacket, "stat"));
	}

	@Test
	public void testToShortString() {
		Whitebox.setInternalState(testStatusPacket, "stat", mockedStat);
		when(mockedStat.toShortString()).thenReturn("Test is ok");
		assertEquals(mockedStat.toShortString(), testStatusPacket.toShortString());
	}

	@Test
	public void testToString() {
		Whitebox.setInternalState(testStatusPacket, "stat", mockedStat);
		when(mockedStat.toString()).thenReturn("stat string value");
		assertEquals("LoraStayAlive{stat=stat string value}", testStatusPacket.toString());
	}
}
