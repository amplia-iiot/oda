package es.amplia.oda.datastreams.lora.datastructures;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RxpkTest {

	private static final long TEST_TMST_PROPERTY_VALUE = 192837465L;
	private static final String TEST_TIME_PROPERTY_VALUE = "1996-12-13T13:30:00.000000Z";
	private static final int TEST_CHAN_PROPERTY_VALUE = 2;
	private static final int TEST_RFCH_PROPERTY_VALUE = 0;
	private static final double TEST_FREQ_PROPERTY_VALUE = 866.349812;
	private static final int TEST_STAT_PROPERTY_VALUE = 1;
	private static final String TEST_MODU_PROPERTY_VALUE = "LORA";
	private static final String TEST_DATR_PROPERTY_VALUE = "SF7BW125";
	private static final String TEST_CODR_PROPERTY_VALUE = "4/6";
	private static final double TEST_LSNR_PROPERTY_VALUE = 5.1;
	private static final int TEST_RSSI_PROPERTY_VALUE = -35;
	private static final int TEST_SIZE_PROPERTY_VALUE = 32;
	private static final String TEST_DATA_PROPERTY_VALUE = "-DS4CGaDCdG+48eJNM3Vai-zDpsR71Pn9CPA9uCON84";

	private Rxpk testRxpk;

	@Before
	public void setUp() {
		testRxpk = new Rxpk();
	}

	@Test
	public void testConstructor() {
		assertEquals(0L, (long) Whitebox.getInternalState(testRxpk, "tmst"));
		assertNull(Whitebox.getInternalState(testRxpk, "time"));
		assertEquals(0, (int) Whitebox.getInternalState(testRxpk, "chan"));
		assertEquals(0, (int) Whitebox.getInternalState(testRxpk, "rfch"));
		assertEquals(0.0, Whitebox.getInternalState(testRxpk, "freq"), 0);
		assertEquals(0, (int) Whitebox.getInternalState(testRxpk, "stat"));
		assertNull(Whitebox.getInternalState(testRxpk, "modu"));
		assertNull(Whitebox.getInternalState(testRxpk, "datr"));
		assertNull(Whitebox.getInternalState(testRxpk, "codr"));
		assertEquals(0.0, Whitebox.getInternalState(testRxpk, "lsnr"), 0);
		assertEquals(0, (int) Whitebox.getInternalState(testRxpk, "rssi"));
		assertEquals(0, (int) Whitebox.getInternalState(testRxpk, "size"));
		assertNull(Whitebox.getInternalState(testRxpk, "data"));
	}

	@Test
	public void testGetTmst() {
		Whitebox.setInternalState(testRxpk, "tmst", TEST_TMST_PROPERTY_VALUE);

		assertEquals(TEST_TMST_PROPERTY_VALUE, testRxpk.getTmst());
	}

	@Test
	public void testSetTmst() {
		testRxpk.setTmst(TEST_TMST_PROPERTY_VALUE);

		assertEquals(TEST_TMST_PROPERTY_VALUE, (long) Whitebox.getInternalState(testRxpk, "tmst"));
	}

	@Test
	public void testGetTime() {
		Whitebox.setInternalState(testRxpk, "time", TEST_TIME_PROPERTY_VALUE);

		assertEquals(TEST_TIME_PROPERTY_VALUE, testRxpk.getTime());
	}

	@Test
	public void testSetTime() {
		testRxpk.setTime(TEST_TIME_PROPERTY_VALUE);

		assertEquals(TEST_TIME_PROPERTY_VALUE, Whitebox.getInternalState(testRxpk, "time"));
	}

	@Test
	public void testGetChan() {
		Whitebox.setInternalState(testRxpk, "chan", TEST_CHAN_PROPERTY_VALUE);

		assertEquals(TEST_CHAN_PROPERTY_VALUE, testRxpk.getChan());
	}

	@Test
	public void testSetChan() {
		testRxpk.setChan(TEST_CHAN_PROPERTY_VALUE);

		assertEquals(TEST_CHAN_PROPERTY_VALUE, (int) Whitebox.getInternalState(testRxpk, "chan"));
	}

	@Test
	public void testGetRfch() {
		Whitebox.setInternalState(testRxpk, "rfch", TEST_RFCH_PROPERTY_VALUE);

		assertEquals(TEST_RFCH_PROPERTY_VALUE, testRxpk.getRfch());
	}

	@Test
	public void testSetRfch() {
		testRxpk.setRfch(TEST_RFCH_PROPERTY_VALUE);

		assertEquals(TEST_RFCH_PROPERTY_VALUE, (int) Whitebox.getInternalState(testRxpk, "rfch"));
	}

	@Test
	public void testGetFreq() {
		Whitebox.setInternalState(testRxpk, "freq", TEST_FREQ_PROPERTY_VALUE);

		assertEquals(TEST_FREQ_PROPERTY_VALUE, testRxpk.getFreq(), 0);
	}

	@Test
	public void testSetFreq() {
		testRxpk.setFreq(TEST_FREQ_PROPERTY_VALUE);

		assertEquals(TEST_FREQ_PROPERTY_VALUE, Whitebox.getInternalState(testRxpk, "freq"), 0);
	}

	@Test
	public void testGetStat() {
		Whitebox.setInternalState(testRxpk, "stat", TEST_STAT_PROPERTY_VALUE);

		assertEquals(TEST_STAT_PROPERTY_VALUE, testRxpk.getStat());
	}

	@Test
	public void testSetStat() {
		testRxpk.setStat(TEST_STAT_PROPERTY_VALUE);

		assertEquals(TEST_STAT_PROPERTY_VALUE, (int) Whitebox.getInternalState(testRxpk, "stat"));
	}

	@Test
	public void testGetModu() {
		Whitebox.setInternalState(testRxpk, "modu", TEST_MODU_PROPERTY_VALUE);

		assertEquals(TEST_MODU_PROPERTY_VALUE, testRxpk.getModu());
	}

	@Test
	public void testSetModu() {
		testRxpk.setModu(TEST_MODU_PROPERTY_VALUE);

		assertEquals(TEST_MODU_PROPERTY_VALUE, Whitebox.getInternalState(testRxpk, "modu"));
	}

	@Test
	public void testGetDatr() {
		Whitebox.setInternalState(testRxpk, "datr", TEST_DATR_PROPERTY_VALUE);

		assertEquals(TEST_DATR_PROPERTY_VALUE, testRxpk.getDatr());
	}

	@Test
	public void testSetDatr() {
		testRxpk.setDatr(TEST_DATR_PROPERTY_VALUE);

		assertEquals(TEST_DATR_PROPERTY_VALUE, Whitebox.getInternalState(testRxpk, "datr"));
	}

	@Test
	public void testGetCodr() {
		Whitebox.setInternalState(testRxpk, "codr", TEST_CODR_PROPERTY_VALUE);

		assertEquals(TEST_CODR_PROPERTY_VALUE, testRxpk.getCodr());
	}

	@Test
	public void testSetCodr() {
		testRxpk.setCodr(TEST_CODR_PROPERTY_VALUE);

		assertEquals(TEST_CODR_PROPERTY_VALUE, Whitebox.getInternalState(testRxpk, "codr"));
	}

	@Test
	public void testGetLsnr() {
		Whitebox.setInternalState(testRxpk, "lsnr", TEST_LSNR_PROPERTY_VALUE);

		assertEquals(TEST_LSNR_PROPERTY_VALUE, testRxpk.getLsnr(), 0);
	}

	@Test
	public void testSetLsnr() {
		testRxpk.setLsnr(TEST_LSNR_PROPERTY_VALUE);

		assertEquals(TEST_LSNR_PROPERTY_VALUE, Whitebox.getInternalState(testRxpk, "lsnr"), 0);
	}

	@Test
	public void testGetRssi() {
		Whitebox.setInternalState(testRxpk, "rssi", TEST_RSSI_PROPERTY_VALUE);

		assertEquals(TEST_RSSI_PROPERTY_VALUE, testRxpk.getRssi());
	}

	@Test
	public void testSetRssi() {
		testRxpk.setRssi(TEST_RSSI_PROPERTY_VALUE);

		assertEquals(TEST_RSSI_PROPERTY_VALUE, (int) Whitebox.getInternalState(testRxpk, "rssi"));
	}

	@Test
	public void testGetSize() {
		Whitebox.setInternalState(testRxpk, "size", TEST_SIZE_PROPERTY_VALUE);

		assertEquals(TEST_SIZE_PROPERTY_VALUE, testRxpk.getSize());
	}

	@Test
	public void testSetSize() {
		testRxpk.setSize(TEST_SIZE_PROPERTY_VALUE);

		assertEquals(TEST_SIZE_PROPERTY_VALUE, (int) Whitebox.getInternalState(testRxpk, "size"));
	}

	@Test
	public void testGetData() {
		Whitebox.setInternalState(testRxpk, "data", TEST_DATA_PROPERTY_VALUE);

		assertEquals(TEST_DATA_PROPERTY_VALUE, testRxpk.getData());
	}

	@Test
	public void testSetData() {
		testRxpk.setData(TEST_DATA_PROPERTY_VALUE);

		assertEquals(TEST_DATA_PROPERTY_VALUE, Whitebox.getInternalState(testRxpk, "data"));
	}
}
