package es.amplia.oda.comms.iec104.master;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.types.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.*;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.DoublePoint;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.InformationStructure;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iec104ResponseHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ResponseHandler.class);

	private final Iec104Cache cache;

	private final String deviceId;

	public Iec104ResponseHandler(Iec104Cache cache, String deviceId) {
		this.cache = cache;
		this.deviceId = deviceId;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		ctx.write(DataTransmissionMessage.REQUEST_START, ctx.newPromise());
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
		LOGGER.trace("New message received: {}", msg);

		if (!msg.getClass().isAnnotationPresent(ASDU.class)) {
			LOGGER.warn("Unknown message received: {}", msg);
			return;
		}

		// get ASDU information
		String type = msg.getClass().getAnnotation(ASDU.class).name();
		InformationStructure msgInfoStruct = msg.getClass().getAnnotation(ASDU.class).informationStructure();
		String translatedASDU = translateASDULogging(type);
		LOGGER.info("ASDU received type: {} {}, informationStructure: {} for deviceId {}", type, translatedASDU, msgInfoStruct, this.deviceId);

		if (!(msgInfoStruct.equals(InformationStructure.SINGLE) || msgInfoStruct.equals(InformationStructure.SEQUENCE))) {
			LOGGER.error("Unknown ASDU informationStructure {}", msgInfoStruct);
		}

		try {
			switch (type) {
				case "C_IC_NA_1":
					// interrogation command, don't do anything, avoid to log it as an unknown message
					break;
				case "M_DP_TB_1":
					parseDoublePointInformationTime(type, msgInfoStruct, msg);
					break;
				case "M_DP_NA_1":
					parseDoublePointInformation(type, msgInfoStruct, msg);
					break;
				case "M_ME_NC_1":
					parseMeasuredValueFloatingPoint(type, msgInfoStruct, msg);
					break;
				case "M_ME_TF_1":
					parseMeasuredValueFloatingPointTime(type, msgInfoStruct, msg);
					break;
				case "M_ME_NB_1":
					parseMeasuredValueScaled(type, msgInfoStruct, msg);
					break;
				case "M_ME_TE_1":
					parseMeasuredValueScaledTime(type, msgInfoStruct, msg);
					break;
				case "M_SP_NA_1":
					parseSinglePointInformation(type, msgInfoStruct, msg);
					break;
				case "M_SP_TB_1":
					parseSinglePointInformationTime(type, msgInfoStruct, msg);
					break;
				case "M_BO_NA_1":
					parseBitstring(type, msgInfoStruct, msg);
					break;
				case "M_BO_TB_1":
					parseBitstringTime(type, msgInfoStruct, msg);
					break;
				case "M_ST_NA_1":
					parseStepPosition(type, msgInfoStruct, msg);
					break;
				case "M_ST_TB_1":
					parseStepPositionTime(type, msgInfoStruct, msg);
					break;
				case "M_ME_NA_1":
					parseMeasureValueNormalized(type, msgInfoStruct, msg);
					break;
				case "M_ME_TD_1":
					parseMeasureValueNormalizedTime(type, msgInfoStruct, msg);
					break;
				case "M_ME_ND_1":
					parseMeasureValueNormalizedNoQuality(type, msgInfoStruct, msg);
					break;
				default:
					LOGGER.warn("Unknown ASDU: type {}, informationStructure {}", type, msgInfoStruct);
			}
		} catch (Exception e) {
			LOGGER.error("Exception processing ASDU {}", msg, e);
		}
	}

	private String translateASDULogging(String type)
	{
		switch (type) {
			case "C_IC_NA_1":
				return "(Interrogation Command)";
			case "M_DP_TB_1":
				return "(DoublePointInformation with seven octets time)";
			case "M_DP_NA_1":
				return "(DoublePointInformation)";
			case "M_ME_NC_1":
				return "(MeasuredValueFloatingPoint)";
			case "M_ME_TF_1":
				return "(MeasuredValueFloatingPoint with seven octets time)";
			case "M_ME_NB_1":
				return "(MeasuredValueScaled)";
			case "M_ME_TE_1":
				return "(MeasuredValueScaled with seven octets time)";
			case "M_SP_NA_1":
				return "(SinglePointInformation)";
			case "M_SP_TB_1":
				return "(SinglePointInformation with seven octets time)";
			case "M_BO_NA_1":
				return "(Bitstring)";
			case "M_BO_TB_1":
				return "(Bitstring with seven octets time)";
			case "M_ST_NA_1":
				return "(StepPosition)";
			case "M_ST_TB_1":
				return "(StepPositionTime with seven octets time)";
			case "M_ME_NA_1":
				return "(MeasuredValueNormalized)";
			case "M_ME_TD_1":
				return "(MeasuredValueNormalized with seven octets time)";
			case "M_ME_ND_1":
				return "(MeasuredValueNormalizedNoQuality)";
			default:
				return "()";
		}
	}

	private void parseDoublePointInformationTime(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("DoublePointInformation single with seven octets time received.");
			DoublePointInformationTimeSingle dataDPIT = (DoublePointInformationTimeSingle) msg;
			dataDPIT.getEntries().forEach(e -> cache.add(type, e.getValue().getValue().toString(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("DoublePointInformation sequence with seven octets time received.");
			DoublePointInformationTimeSequence dataDPITSeq = (DoublePointInformationTimeSequence) msg;
			int addressMVNNQSeq = dataDPITSeq.getStartAddress().getAddress();
			for (Value<DoublePoint> v : dataDPITSeq.getValues())
				cache.add(type, v.getValue().toString(), addressMVNNQSeq++);
		}
	}

	private void parseDoublePointInformation(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("DoublePointInformation single received.");
			DoublePointInformationSingle dataDPIS = (DoublePointInformationSingle) msg;
			dataDPIS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue().toString(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("DoublePointInformation sequence received.");
			DoublePointInformationSequence dataDPISeq = (DoublePointInformationSequence) msg;
			int addressDPISeq = dataDPISeq.getStartAddress().getAddress();
			for (Value<DoublePoint> v : dataDPISeq.getValues())
				cache.add(type, v.getValue().toString(), addressDPISeq++);
		}
	}

	private void parseMeasuredValueFloatingPoint(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("MeasuredValueFloatingPoint single received.");
			MeasuredValueShortFloatingPointSingle dataMVSFPS = (MeasuredValueShortFloatingPointSingle) msg;
			dataMVSFPS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("MeasuredValueFloatingPoint sequence received.");
			MeasuredValueShortFloatingPointSequence dataMVSFPSeq = (MeasuredValueShortFloatingPointSequence) msg;
			int addressMVSFPSeq = dataMVSFPSeq.getStartAddress().getAddress();
			for (Value<Float> v : dataMVSFPSeq.getValues())
				cache.add(type, v.getValue(), addressMVSFPSeq++);
		}
	}

	private void parseMeasuredValueFloatingPointTime(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("MeasuredValueFloatingPoint single with seven octets time received.");
			MeasuredValueShortFloatingPointTimeSingle dataMVSFPTS = (MeasuredValueShortFloatingPointTimeSingle) msg;
			dataMVSFPTS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("MeasuredValueFloatingPoint sequence with seven octets time received.");
			MeasuredValueShortFloatingPointTimeSequence dataMVSFPTSeq = (MeasuredValueShortFloatingPointTimeSequence) msg;
			int addressMVSFPTSeq = dataMVSFPTSeq.getStartAddress().getAddress();
			for (Value<Short> v : dataMVSFPTSeq.getValues())
				cache.add(type, v.getValue(), addressMVSFPTSeq++);
		}
	}

	private void parseMeasuredValueScaled(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("MeasuredValueScaled single received.");
			MeasuredValueScaledSingle dataMVSS = (MeasuredValueScaledSingle) msg;
			dataMVSS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("MeasuredValueScaled sequence received.");
			MeasuredValueScaledSequence dataMVSSeq = (MeasuredValueScaledSequence) msg;
			int addressMVSSeq = dataMVSSeq.getStartAddress().getAddress();
			for (Value<Short> v : dataMVSSeq.getValues())
				cache.add(type, v.getValue(), addressMVSSeq++);
		}
	}

	private void parseMeasuredValueScaledTime(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("MeasuredValueScaled single with seven octets time received.");
			MeasuredValueScaledTimeSingle dataMVSTS = (MeasuredValueScaledTimeSingle) msg;
			dataMVSTS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("MeasuredValueScaled sequence with seven octets time received.");
			MeasuredValueScaledSequence dataMVSTSeq = (MeasuredValueScaledSequence) msg;
			int addressMVSTSeq = dataMVSTSeq.getStartAddress().getAddress();
			for (Value<Short> v : dataMVSTSeq.getValues())
				cache.add(type, v.getValue(), addressMVSTSeq++);
		}
	}

	private void parseSinglePointInformation(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("SinglePointInformation single received.");
			SinglePointInformationSingle dataSPIS = (SinglePointInformationSingle) msg;
			dataSPIS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("SinglePointInformation sequence received.");
			SinglePointInformationSequence dataSPISeq = (SinglePointInformationSequence) msg;
			int addressSPISeq = dataSPISeq.getStartAddress().getAddress();
			for (Value<Boolean> v : dataSPISeq.getValues())
				cache.add(type, v.getValue(), addressSPISeq++);
		}
	}

	private void parseSinglePointInformationTime(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("SinglePointInformation single with seven octets time received.");
			SinglePointInformationTimeSingle dataSPITS = (SinglePointInformationTimeSingle) msg;
			dataSPITS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("SinglePointInformation sequence with seven octets time received.");
			SinglePointInformationTimeSequence dataSPITSeq = (SinglePointInformationTimeSequence) msg;
			int addressSPITSeq = dataSPITSeq.getStartAddress().getAddress();
			for (Value<Boolean> v : dataSPITSeq.getValues())
				cache.add(type, v.getValue(), addressSPITSeq++);
		}
	}

	private void parseBitstring(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("Bitstring single received.");
			BitStringPointInformationSingle dataBSS = (BitStringPointInformationSingle) msg;
			dataBSS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("Bitstring sequence received.");
			BitStringPointInformationSequence dataBSSeq = (BitStringPointInformationSequence) msg;
			int addressBSSeq = dataBSSeq.getStartAddress().getAddress();
			for (Value<Long> v: dataBSSeq.getValues())
				cache.add(type, v.getValue(), addressBSSeq++);
		}
	}

	private void parseBitstringTime(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("Bitstring single with seven octets time received.");
			BitStringPointInformationTimeSingle dataBSTS = (BitStringPointInformationTimeSingle) msg;
			dataBSTS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("Bitstring sequence with seven octets time received.");
			BitStringPointInformationTimeSequence dataBSTSeq = (BitStringPointInformationTimeSequence) msg;
			int addressBSTSeq = dataBSTSeq.getStartAddress().getAddress();
			for (Value<Long> v: dataBSTSeq.getValues())
				cache.add(type, v.getValue(), addressBSTSeq++);
		}
	}

	private void parseStepPosition(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("Step position single received.");
			StepPositionSingle dataSPS = (StepPositionSingle) msg;
			dataSPS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("Step position sequence received.");
			StepPositionSequence dataSPSeq = (StepPositionSequence) msg;
			int addressSPSeq = dataSPSeq.getStartAddress().getAddress();
			for (Value<Byte> v: dataSPSeq.getValues())
				cache.add(type, v.getValue(), addressSPSeq++);
		}
	}

	private void parseStepPositionTime(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("Step position single with seven octets time received.");
			StepPositionTimeSingle dataSPTS = (StepPositionTimeSingle) msg;
			dataSPTS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("Step position sequence with seven octets time received.");
			StepPositionTimeSequence dataSPTSeq = (StepPositionTimeSequence) msg;
			int addressSPTSeq = dataSPTSeq.getStartAddress().getAddress();
			for (Value<Byte> v: dataSPTSeq.getValues())
				cache.add(type, v.getValue(), addressSPTSeq++);
		}
	}

	private void parseMeasureValueNormalized(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("MeasuredValueNormalized single received.");
			MeasuredValueNormalizedSingle dataMVNS = (MeasuredValueNormalizedSingle) msg;
			dataMVNS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("MeasuredValueNormalized sequence received.");
			MeasuredValueNormalizedSequence dataMVNSeq = (MeasuredValueNormalizedSequence) msg;
			int addressMVNSeq = dataMVNSeq.getStartAddress().getAddress();
			for (Value<Short> v: dataMVNSeq.getValues())
				cache.add(type, v.getValue(), addressMVNSeq++);
		}
	}

	private void parseMeasureValueNormalizedTime(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("MeasuredValueNormalized single with seven octets time received.");
			MeasuredValueNormalizedTimeSingle dataMVNTS = (MeasuredValueNormalizedTimeSingle) msg;
			dataMVNTS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("MeasuredValueNormalized sequence with seven octets time received.");
			MeasuredValueNormalizedTimeSequence dataMVNTSeq = (MeasuredValueNormalizedTimeSequence) msg;
			int addressMVNTSeq = dataMVNTSeq.getStartAddress().getAddress();
			for (Value<Short> v: dataMVNTSeq.getValues())
				cache.add(type, v.getValue(), addressMVNTSeq++);
		}
	}

	private void parseMeasureValueNormalizedNoQuality(String type, InformationStructure msgInfoStruct, final Object msg) {

		if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
			LOGGER.debug("MeasuredValueNormalized NoQuality single received.");
			MeasuredValueNormalizedNoQualitySingle dataMVNNQS = (MeasuredValueNormalizedNoQualitySingle) msg;
			dataMVNNQS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
		} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
			LOGGER.debug("MeasuredValueNormalized NoQuality sequence received.");
			MeasuredValueNormalizedNoQualitySequence dataMVNNQSeq = (MeasuredValueNormalizedNoQualitySequence) msg;
			int addressMVNNQSeq = dataMVNNQSeq.getStartAddress().getAddress();
			for (Value<Short> v: dataMVNNQSeq.getValues())
				cache.add(type, v.getValue(), addressMVNNQSeq++);
		}
	}

}
