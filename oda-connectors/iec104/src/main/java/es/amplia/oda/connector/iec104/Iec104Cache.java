package es.amplia.oda.connector.iec104;

import es.amplia.oda.connector.iec104.types.BitStringPointInformationSequence;
import es.amplia.oda.connector.iec104.types.BitStringPointInformationSingle;

import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SinglePointInformationSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SinglePointInformationSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class Iec104Cache {

	private static final Logger LOGGER = LoggerFactory.getLogger(Iec104Cache.class);


	private final Map<String, Map<Integer, Object>> cache = new HashMap<>();


	<T> void add(String typeId, T value, int index) {
		LOGGER.info("Adding data with SCADA type {} with index {} and value {}", typeId, index, value);
		Map<Integer, Object> contain = cache.get(typeId);

		if(contain == null) {
			contain = new HashMap<>();
			contain.put(index, value);
			cache.put(typeId, contain);
		} else {
			contain.put(index, value);
		}
	}

	<T> Object getAsdu(String type, T value, int index, long timestamp, int commonAddress) {
		CauseOfTransmission cot = new CauseOfTransmission(StandardCause.SPONTANEOUS);
		ASDUAddress address = ASDUAddress.valueOf(commonAddress);
		ASDUHeader header = new ASDUHeader(cot, address);
		switch (type) {
			case "M_SP_NA_1":
				return getSinglePointInformation(value, header, index, timestamp);
			case "M_BO_NA_1":
				return getBitStringPointInformation(value, header, index, timestamp);
			case "M_ME_NB_1":
				return getMeasuredValueScaled(value, header, index, timestamp);
			default:
				LOGGER.error("{} cannot be translated", type);
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
			List<Value<byte[]>> result = new ArrayList<>();
			for (Object val: ((ArrayList) value).toArray()) {
				result.add(new Value(transformValueToBitString(val), timestamp, QualityInformation.OK));
			}
			return BitStringPointInformationSequence.create(header, InformationObjectAddress.valueOf(index), result);
		}

		return BitStringPointInformationSingle.create(header, InformationObjectAddress.valueOf(index),
				new Value<>(transformValueToBitString(value),
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

	static boolean isSpontaneous(String type) {
		return type.equals(SinglePointInformationSingle.class.getAnnotation(ASDU.class).name());
	}

	void clear() {
		cache.clear();
	}

	List<Object> getASDUS(int commonAddress) {
		List<Object> ret = new ArrayList<>();
		for (Map.Entry<String, Map<Integer, Object>> entry: cache.entrySet()) {
			String key = entry.getKey();
			Map<Integer, Object> info = entry.getValue();

			List<Object> values = new ArrayList<>();
			try {
				int[] array = getLowestAndHighestIndex(info);
				int lowestIndex = array[0];
				int highestIndex = array[1];
				for (int i = lowestIndex; i <= highestIndex; i++) {
					values.add(info.get(i));
				}
				Object asdu = getAsdu(key, values, lowestIndex, System.currentTimeMillis(), commonAddress);
				if (asdu != null) {
					ret.add(asdu);
				}
			} catch (NullPointerException e) {
				LOGGER.error("Null value stored on cache");
			}
		}
		return ret;
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
