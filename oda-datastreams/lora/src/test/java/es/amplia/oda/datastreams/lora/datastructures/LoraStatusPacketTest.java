package es.amplia.oda.datastreams.lora.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoraStatusPacketTest {

	@Mock
	Stat mockedStat;

	LoraStatusPacket testStatusPacket;

	@BeforeEach
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
