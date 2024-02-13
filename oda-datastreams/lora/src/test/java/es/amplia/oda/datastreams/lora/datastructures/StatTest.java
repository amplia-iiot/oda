package es.amplia.oda.datastreams.lora.datastructures;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Stat.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class StatTest {

	private static final String TEST_TIME_PROPERTY_VALUE = "2020-11-11 13:11:52 GMT";
	private static final int TEST_RXNB_PROPERTY_VALUE = 2;
	private static final int TEST_RXOK_PROPERTY_VALUE = 2;
	private static final int TEST_RXFW_PROPERTY_VALUE = 2;
	private static final double TEST_ACKR_PROPERTY_VALUE = 100.0;
	private static final int TEST_DWNB_PROPERTY_VALUE = 2;
	private static final int TEST_TXNB_PROPERTY_VALUE = 2;
	private static final String TEST_PFRM_PROPERTY_VALUE = "IMST.+.Rpi";
	private static final String TEST_MAIL_PROPERTY_VALUE = "example@mail.do";
	private static final String TEST_DESC_PROPERTY_VALUE = "This is the testing Gateway";

	private Stat testStat;

	@Before
	public void setUp() {
		testStat = new Stat();
	}

	@Test
	public void testConstructor() {
		assertNull(Whitebox.getInternalState(testStat, "time"));
		assertEquals(0, (int) Whitebox.getInternalState(testStat, "rxnb"));
		assertEquals(0, (int) Whitebox.getInternalState(testStat, "rxok"));
		assertEquals(0, (int) Whitebox.getInternalState(testStat, "rxfw"));
		assertEquals(0.0, Whitebox.getInternalState(testStat, "ackr"), 0);
		assertEquals(0, (int) Whitebox.getInternalState(testStat, "dwnb"));
		assertEquals(0, (int) Whitebox.getInternalState(testStat, "txnb"));
		assertNull(Whitebox.getInternalState(testStat, "pfrm"));
		assertNull(Whitebox.getInternalState(testStat, "mail"));
		assertNull(Whitebox.getInternalState(testStat, "desc"));
	}

	@Test
	public void testGetTime() {
		Whitebox.setInternalState(testStat, "time", TEST_TIME_PROPERTY_VALUE);

		assertEquals(TEST_TIME_PROPERTY_VALUE, testStat.getTime());
	}

	@Test
	public void testSetTime() {
		testStat.setTime(TEST_TIME_PROPERTY_VALUE);

		assertEquals(TEST_TIME_PROPERTY_VALUE, Whitebox.getInternalState(testStat, "time"));
	}

	@Test
	public void testGetRxnb() {
		Whitebox.setInternalState(testStat, "rxnb", TEST_RXNB_PROPERTY_VALUE);

		assertEquals(TEST_RXNB_PROPERTY_VALUE, testStat.getRxnb());
	}

	@Test
	public void testSetRxnb() {
		testStat.setRxnb(TEST_RXNB_PROPERTY_VALUE);

		assertEquals(TEST_RXNB_PROPERTY_VALUE, (int) Whitebox.getInternalState(testStat, "rxnb"));
	}

	@Test
	public void testGetRxok() {
		Whitebox.setInternalState(testStat, "rxok", TEST_RXOK_PROPERTY_VALUE);

		assertEquals(TEST_RXOK_PROPERTY_VALUE, testStat.getRxok());
	}

	@Test
	public void testSetRxok() {
		testStat.setRxok(TEST_RXOK_PROPERTY_VALUE);

		assertEquals(TEST_RXOK_PROPERTY_VALUE, (int) Whitebox.getInternalState(testStat, "rxok"));
	}

	@Test
	public void testGetRxfw() {
		Whitebox.setInternalState(testStat, "rxfw", TEST_RXFW_PROPERTY_VALUE);

		assertEquals(TEST_RXFW_PROPERTY_VALUE, testStat.getRxfw());
	}

	@Test
	public void testSetRxfw() {
		testStat.setRxfw(TEST_RXFW_PROPERTY_VALUE);

		assertEquals(TEST_RXFW_PROPERTY_VALUE, (int) Whitebox.getInternalState(testStat, "rxfw"));
	}

	@Test
	public void testGetAckr() {
		Whitebox.setInternalState(testStat, "ackr", TEST_ACKR_PROPERTY_VALUE);

		assertEquals(TEST_ACKR_PROPERTY_VALUE, testStat.getAckr(), 0);
	}

	@Test
	public void testSetAckr() {
		testStat.setAckr(TEST_ACKR_PROPERTY_VALUE);

		assertEquals(TEST_ACKR_PROPERTY_VALUE, Whitebox.getInternalState(testStat, "ackr"), 0);
	}

	@Test
	public void testGetDwnb() {
		Whitebox.setInternalState(testStat, "dwnb", TEST_DWNB_PROPERTY_VALUE);

		assertEquals(TEST_DWNB_PROPERTY_VALUE, testStat.getDwnb());
	}

	@Test
	public void testSetDwnb() {
		testStat.setDwnb(TEST_DWNB_PROPERTY_VALUE);

		assertEquals(TEST_DWNB_PROPERTY_VALUE, (int) Whitebox.getInternalState(testStat, "dwnb"));
	}

	@Test
	public void testGetTxnb() {
		Whitebox.setInternalState(testStat, "txnb", TEST_TXNB_PROPERTY_VALUE);

		assertEquals(TEST_TXNB_PROPERTY_VALUE, testStat.getTxnb());
	}

	@Test
	public void testSetTxnb() {
		testStat.setTxnb(TEST_TXNB_PROPERTY_VALUE);

		assertEquals(TEST_TXNB_PROPERTY_VALUE, (int) Whitebox.getInternalState(testStat, "txnb"));
	}

	@Test
	public void testGetPfrm() {
		Whitebox.setInternalState(testStat, "pfrm", TEST_PFRM_PROPERTY_VALUE);

		assertEquals(TEST_PFRM_PROPERTY_VALUE, testStat.getPfrm());
	}

	@Test
	public void testSetPfrm() {
		testStat.setPfrm(TEST_PFRM_PROPERTY_VALUE);

		assertEquals(TEST_PFRM_PROPERTY_VALUE, Whitebox.getInternalState(testStat, "pfrm"));
	}

	@Test
	public void testGetMail() {
		Whitebox.setInternalState(testStat, "mail", TEST_MAIL_PROPERTY_VALUE);

		assertEquals(TEST_MAIL_PROPERTY_VALUE, testStat.getMail());
	}

	@Test
	public void testSetMail() {
		testStat.setMail(TEST_MAIL_PROPERTY_VALUE);

		assertEquals(TEST_MAIL_PROPERTY_VALUE, Whitebox.getInternalState(testStat, "mail"));
	}

	@Test
	public void testGetDesc() {
		Whitebox.setInternalState(testStat, "desc", TEST_DESC_PROPERTY_VALUE);

		assertEquals(TEST_DESC_PROPERTY_VALUE, testStat.getDesc());
	}

	@Test
	public void testSetDesc() {
		testStat.setDesc(TEST_DESC_PROPERTY_VALUE);

		assertEquals(TEST_DESC_PROPERTY_VALUE, Whitebox.getInternalState(testStat, "desc"));
	}

	@Test
	public void testToShortString() {
		assertEquals("Sent a status message at: " + testStat.getTime() + " from device " + testStat.getDesc(),
				testStat.toShortString());
	}

	@Test
	public void testToString() {
		String statString = "Stat{" +
				"time='" + TEST_TIME_PROPERTY_VALUE + '\'' +
				", rxnb=" + TEST_RXNB_PROPERTY_VALUE +
				", rxok=" + TEST_RXOK_PROPERTY_VALUE +
				", rxfw=" + TEST_RXFW_PROPERTY_VALUE +
				", ackr=" + TEST_ACKR_PROPERTY_VALUE +
				", dwnb=" + TEST_DWNB_PROPERTY_VALUE +
				", txnb=" + TEST_TXNB_PROPERTY_VALUE +
				", pfrm='" + TEST_PFRM_PROPERTY_VALUE + '\'' +
				", mail='" + TEST_MAIL_PROPERTY_VALUE + '\'' +
				", desc='" + TEST_DESC_PROPERTY_VALUE + '\'' +
				'}';
		Whitebox.setInternalState(testStat, "time", TEST_TIME_PROPERTY_VALUE);
		Whitebox.setInternalState(testStat, "rxnb", TEST_RXNB_PROPERTY_VALUE);
		Whitebox.setInternalState(testStat, "rxok", TEST_RXOK_PROPERTY_VALUE);
		Whitebox.setInternalState(testStat, "rxfw", TEST_RXFW_PROPERTY_VALUE);
		Whitebox.setInternalState(testStat, "ackr", TEST_ACKR_PROPERTY_VALUE);
		Whitebox.setInternalState(testStat, "dwnb", TEST_DWNB_PROPERTY_VALUE);
		Whitebox.setInternalState(testStat, "txnb", TEST_TXNB_PROPERTY_VALUE);
		Whitebox.setInternalState(testStat, "pfrm", TEST_PFRM_PROPERTY_VALUE);
		Whitebox.setInternalState(testStat, "mail", TEST_MAIL_PROPERTY_VALUE);
		Whitebox.setInternalState(testStat, "desc", TEST_DESC_PROPERTY_VALUE);

		assertEquals(statString, testStat.toString());
	}
}
