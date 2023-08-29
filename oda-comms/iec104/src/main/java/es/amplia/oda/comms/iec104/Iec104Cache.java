package es.amplia.oda.comms.iec104;

import es.amplia.oda.comms.iec104.types.BitStringPointInformationSequence;
import es.amplia.oda.comms.iec104.types.BitStringPointInformationSingle;

import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SinglePointInformationSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SinglePointInformationSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Iec104Cache {

	private static final Logger LOGGER = LoggerFactory.getLogger(Iec104Cache.class);

	// map is <Type(ASDU), Map<Index(IECAddress), Value>>
	private final Map<String, Map<Integer, Iec104CacheValue>> cache = new HashMap<>();


	public <T> void add(String typeId, T value, int index) {
		LOGGER.info("Adding data to cache with SCADA type {} with index {} and value {}", typeId, index, value);

		// get from cache the Map associated to the type(ASDU) indicated
		Map<Integer, Iec104CacheValue> contain = cache.get(typeId);

		// create new value with current time
		Iec104CacheValue newValueInCache = new Iec104CacheValue(value, System.currentTimeMillis(), false);

		// if cache doesn't contain a map for the ASDU indicated, create a new Map and add it to cache
		if (contain == null) {
			contain = new HashMap<>();
			contain.put(index, newValueInCache);
			// add new map to cache
			cache.put(typeId, contain);
		}
		// if cache already contains a map for the ASDU indicated, add new value to map
		else {
			contain.put(index, newValueInCache);
		}
	}

	public <T> Object getAsdu(String type, T value, int index, long timestamp, int commonAddress) {
		CauseOfTransmission cot = new CauseOfTransmission(StandardCause.SPONTANEOUS);
		ASDUAddress address = ASDUAddress.valueOf(commonAddress);
		ASDUHeader header = new ASDUHeader(cot, address);
		LOGGER.debug("ASDU type {}, value {}, index {}, timestamp {}, commonAddress {}", type, value, index, timestamp, commonAddress);
		switch (type) {
			case "M_SP_NA_1":
				return getSinglePointInformation(value, header, index, timestamp);
			case "M_BO_NA_1":
				return getBitStringPointInformation(value, header, index, timestamp);
			case "M_ME_NB_1":
				return getMeasuredValueScaled(value, header, index, timestamp);
			default:
				LOGGER.warn("{} cannot be translated", type);
				return null;
		}
	}

	private <T> Object getSinglePointInformation(T value, ASDUHeader header, int index, long timestamp){
		if(value instanceof ArrayList) {
			List<Value<Boolean>> result = new ArrayList<>();
			for (Object val: ((ArrayList) value).toArray()) {
				result.add(new Value(val, timestamp, QualityInformation.OK));
			}
			return SinglePointInformationSequence.create(InformationObjectAddress.valueOf(index), header, result);

		}
		return SinglePointInformationSingle.create(header, InformationObjectAddress.valueOf(index),
				new Value<>(Boolean.parseBoolean(value.toString()), timestamp, QualityInformation.OK));
	}

	private <T> Object getBitStringPointInformation(T value, ASDUHeader header, int index, long timestamp){
		if(value instanceof ArrayList) {
			List<Value<Long>> result = new ArrayList<>();
			for (Object val: ((ArrayList) value).toArray()) {
				result.add(new Value(transformValueToBitString(val), timestamp, QualityInformation.OK));
			}
			return BitStringPointInformationSequence.create(header, InformationObjectAddress.valueOf(index), result);
		}

		return BitStringPointInformationSingle.create(header, InformationObjectAddress.valueOf(index),
				new Value(transformValueToBitString(value),
						timestamp, QualityInformation.OK));
	}

	private byte[] transformValueToBitString(Object value) {
		if(value == null) {
			return new byte[] {0x00, 0x00, 0x00, 0x00};
		} else {
			long dataLong = Long.parseLong(value.toString());
			return new byte[] {(byte) (dataLong >> 24), (byte) (dataLong >> 16), (byte) (dataLong >> 8), (byte) (dataLong)};
		}
	}

	private <T> Object getMeasuredValueScaled(T value, ASDUHeader header, int index, long timestamp){
		if(value instanceof ArrayList) {
			List<Value<Short>> result = new ArrayList<>();
			for (Object val: ((ArrayList) value).toArray()) {
				result.add(new Value(val, timestamp, QualityInformation.OK));
			}
			return MeasuredValueScaledSequence.create(InformationObjectAddress.valueOf(index), header, result);
		}
		return MeasuredValueScaledSingle.create(header, InformationObjectAddress.valueOf(index),
				new Value<>(Short.valueOf(value.toString()), timestamp, QualityInformation.OK));
	}

	public static boolean isSpontaneous(String type) {
		return type.equals(SinglePointInformationSingle.class.getAnnotation(ASDU.class).name());
	}

	public void clear() {
		cache.clear();
	}

	public List<Object> getASDUS(int commonAddress) {
		List<Object> ret = new ArrayList<>();
		for (Map.Entry<String, Map<Integer, Iec104CacheValue>> entry: cache.entrySet()) {
			String key = entry.getKey();
			Map<Integer, Iec104CacheValue> info = entry.getValue();

			try {
				for (Map.Entry<Integer, Iec104CacheValue> value: info.entrySet()) {
					Object asdu = getAsdu(key, value.getValue().getValue(), value.getKey(), value.getValue().getValueTime(), commonAddress);
					if (asdu != null) {
						ret.add(asdu);
					}
				}
			} catch (NullPointerException e) {
				LOGGER.error("Null value stored on cache");
			}
		}
		return ret;
	}

	public Iec104CacheValue getValue(String type, int index) {
		Map<Integer, Iec104CacheValue> ret = cache.get(type);
		if (ret != null)
			return ret.get(index);
		return null;
	}

	public void markValueAsProcessed(String type, int index) {
		Map<Integer, Iec104CacheValue> ret = cache.get(type);
		if (ret != null) {
			ret.get(index).setProcessed(true);
		}
	}

	private int[] getLowestAndHighestIndex(Map<Integer, Object> info) {
		int lowest = Integer.MAX_VALUE;
		int highest = 0;
		for (Integer index : info.keySet()) {
			if (index < lowest) {
				lowest = index;
			}
			if (index > highest) {
				highest = index;
			}
		}
		return new int[]{lowest, highest};
	}
}
