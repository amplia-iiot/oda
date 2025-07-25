package es.amplia.oda.comms.iec104;

import es.amplia.oda.comms.iec104.types.BitStringPointInformationSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SinglePointInformationSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SinglePointInformationSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.CauseOfTransmission;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.QualityInformation;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.Value;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Iec104CacheTest.class)
public class Iec104CacheTest {
	private final Iec104Cache testCache = new Iec104Cache(null);
	private static Map<String, Map<Integer, Iec104CacheValue>> cache;

	@Mock
	private Map<String, Map<Integer, Iec104CacheValue>> mockedCache;
	@Mock
	private CauseOfTransmission mockedCOT;
	@Mock
	private ASDUHeader mockedHeader;


	@Before
	public void prepareForTest() {
		cache = new HashMap<>();
		Map<Integer, Iec104CacheValue> value = new HashMap<>();
		Map<Integer, Iec104CacheValue> valueBytes = new HashMap<>();
		long longVal = 1984;
		value.put(1, new Iec104CacheValue(true, System.currentTimeMillis(), false));
		value.put(2, new Iec104CacheValue(false, System.currentTimeMillis(), false));
		valueBytes.put(14, new Iec104CacheValue(longVal, System.currentTimeMillis(), false));
		cache.put("M_SP_NA_1", value);
		cache.put("M_BO_NA_1", valueBytes);
	}


	@Test
	public void testAddCreate() {
		String typeId = "ASDU";
		Value<String> value = new Value<>("Testing value", System.currentTimeMillis(), null);
		int index = 1;
		Iec104CacheValue iec104Value = new Iec104CacheValue(value.getValue(), value.getTimestamp(), false);
		Map<Integer, Iec104CacheValue> cacheValue = new HashMap<>();
		cacheValue.put(index, iec104Value);

		Whitebox.setInternalState(testCache, "cache", mockedCache);
		Mockito.when(mockedCache.get(typeId)).thenReturn(null);

		testCache.add(typeId, value, index);

		verify(mockedCache, times(1)).put(any(), any());
	}

	@Test
	public void testAddValue() {
		String typeId = "ASDU";
		Value<String> value = new Value<>("Testing value", System.currentTimeMillis(), null);
		int index = 1;
		Iec104CacheValue iec104Value = new Iec104CacheValue(value.getValue(), value.getTimestamp(), false);
		Map<Integer, Iec104CacheValue> cacheValue = new HashMap<>();
		cacheValue.put(index, iec104Value);

		testCache.add(typeId, value, index);

		Map<String, Map<Integer, Iec104CacheValue>> internalCache = (Map<String, Map<Integer, Iec104CacheValue>>) Whitebox.getInternalState(testCache, "cache");
		Iec104CacheValue internalIEC104Value = internalCache.get(typeId).get(index);
		Assert.assertEquals(internalIEC104Value.getValue(), cacheValue.get(index).getValue());
		Assert.assertEquals(internalIEC104Value.isProcessed(), cacheValue.get(index).isProcessed());
	}

	@Test
	public void testAddUpdate() {
		Whitebox.setInternalState(testCache, "cache", mockedCache);
		String typeId = "ASDU";
		Value<String> value = new Value<>("Testing value", System.currentTimeMillis(), null);
		int index = 1;
		Iec104CacheValue iec104Value = new Iec104CacheValue(value.getValue(), value.getTimestamp(), false);
		Map<Integer, Iec104CacheValue> cacheValue = new HashMap<>();
		cacheValue.put(index, iec104Value);
		Mockito.when(mockedCache.get(eq(typeId))).thenReturn(cacheValue);

		testCache.add(typeId, value, index);

		verify(mockedCache, times(0)).put(eq(typeId), eq(cacheValue));
	}

	@Test
	public void getAsduBooleanSingle() throws Exception {
		PowerMockito.whenNew(CauseOfTransmission.class).withAnyArguments().thenReturn(mockedCOT);
		PowerMockito.whenNew(ASDUHeader.class).withAnyArguments().thenReturn(mockedHeader);
		String type = "M_SP_NA_1";
		int index = 42, commonAddress = 1;
		long timestamp = System.currentTimeMillis();

		Object result = testCache.getAsdu(type, true, index, timestamp, commonAddress);

		assertTrue(result instanceof SinglePointInformationSingle);
	}

	@Test
	public void getAsduBooleanSequence() throws Exception {
		PowerMockito.whenNew(CauseOfTransmission.class).withAnyArguments().thenReturn(mockedCOT);
		PowerMockito.whenNew(ASDUHeader.class).withAnyArguments().thenReturn(mockedHeader);
		String type = "M_SP_NA_1";
		int index = 42, commonAddress = 1;
		long timestamp = System.currentTimeMillis();
		Value<Boolean> val1 = new Value<>(true, timestamp, QualityInformation.OK);
		Value<Boolean> val2 = new Value<>(false, timestamp, QualityInformation.OK);
		List<Value<Boolean>> list = new ArrayList<>();
		list.add(val1); list.add(val2);

		Object result = testCache.getAsdu(type, list, index, timestamp, commonAddress);

		assertTrue(result instanceof SinglePointInformationSequence);
	}

	@Test
	public void getAsduShortSingle() throws Exception {
		PowerMockito.whenNew(CauseOfTransmission.class).withAnyArguments().thenReturn(mockedCOT);
		PowerMockito.whenNew(ASDUHeader.class).withAnyArguments().thenReturn(mockedHeader);
		String type = "M_ME_NB_1";
		short value = 1789;
		int index = 42, commonAddress = 1;
		long timestamp = System.currentTimeMillis();

		Object result = testCache.getAsdu(type, value, index, timestamp, commonAddress);

		assertTrue(result instanceof MeasuredValueScaledSingle);
	}

	@Test
	public void getAsduShortSequence() throws Exception {
		PowerMockito.whenNew(CauseOfTransmission.class).withAnyArguments().thenReturn(mockedCOT);
		PowerMockito.whenNew(ASDUHeader.class).withAnyArguments().thenReturn(mockedHeader);
		String type = "M_ME_NB_1";
		int index = 42, commonAddress = 1;
		long timestamp = System.currentTimeMillis();
		Value<Short> val1 = new Value<>((short) 71, timestamp, QualityInformation.OK);
		Value<Short> val2 = new Value<>((short) 59, timestamp, QualityInformation.OK);
		List<Value<Short>> list = new ArrayList<>();
		list.add(val1); list.add(val2);

		Object result = testCache.getAsdu(type, list, index, timestamp, commonAddress);

		assertTrue(result instanceof MeasuredValueScaledSequence);
	}

	@Test
	public void getAsduDefault() throws Exception {
		PowerMockito.whenNew(CauseOfTransmission.class).withAnyArguments().thenReturn(mockedCOT);
		PowerMockito.whenNew(ASDUHeader.class).withAnyArguments().thenReturn(mockedHeader);
		String type = "UNKNOWN";
		byte value = 0x00;
		int index = 42, commonAddress = 1;
		long timestamp = System.currentTimeMillis();

		Object result = testCache.getAsdu(type, value, index, timestamp, commonAddress);

		assertNull(result);
	}

	@Test
	public void isSpontaneousFalse() {
		boolean result = Iec104Cache.isSpontaneous("Spontaneous Combustion");

		assertFalse(result);
	}

	@Test
	public void isSpontaneousTrue() {
		boolean result = Iec104Cache.isSpontaneous("M_SP_NA_1");

		assertTrue(result);
	}

	@Test
	public void clear() {
		Whitebox.setInternalState(testCache, "cache", cache);

		testCache.clear();

		assertEquals(0, cache.size());
	}

	@Test
	public void getASDUS() {
		Whitebox.setInternalState(testCache, "cache", cache);

		List<Object> result = testCache.getASDUS(1);

		assertEquals(3, result.size());
		assertTrue(result.get(0) instanceof SinglePointInformationSingle);
		assertTrue(result.get(1) instanceof SinglePointInformationSingle);
		assertTrue(result.get(2) instanceof BitStringPointInformationSingle);
	}
}
