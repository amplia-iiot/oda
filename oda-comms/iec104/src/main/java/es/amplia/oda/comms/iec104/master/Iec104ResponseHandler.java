package es.amplia.oda.comms.iec104.master;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.types.StepPositionSequence;
import es.amplia.oda.comms.iec104.types.BitStringPointInformationSequence;
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

	public Iec104ResponseHandler(Iec104Cache cache) {
		this.cache = cache;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		ctx.write(DataTransmissionMessage.REQUEST_START, ctx.newPromise());
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
		LOGGER.trace("New message received: {}", msg);
		if (msg.getClass().isAnnotationPresent(ASDU.class)) {
			String type = msg.getClass().getAnnotation(ASDU.class).name();
			InformationStructure msgInfoStruct = msg.getClass().getAnnotation(ASDU.class).informationStructure();
			LOGGER.info("ASDU received type: {}, informationStructure: {}", type, msgInfoStruct);
			try {
				switch (type) {
					case "M_DP_TB_1":
						LOGGER.info("DoublePointInformationTimeSingle received.");
						DoublePointInformationTimeSingle dataDPIT = (DoublePointInformationTimeSingle) msg;
						dataDPIT.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
						break;
					case "M_DP_NA_1":
						LOGGER.info("DoublePointInformationSequence received.");
						DoublePointInformationSequence dataDPIS = (DoublePointInformationSequence) msg;
						int addressDPIS = dataDPIS.getStartAddress().getAddress();
						for (Value<DoublePoint> v: dataDPIS.getValues())
							cache.add(type, v.getValue().toString(), addressDPIS++);
						break;
					case "M_ME_NC_1":
						if(msgInfoStruct.equals(InformationStructure.SINGLE)) {
							LOGGER.info("MeasuredValueShortFloatingPointSingle received.");
							MeasuredValueShortFloatingPointSingle dataMVSFPS = (MeasuredValueShortFloatingPointSingle) msg;
							dataMVSFPS.getEntries().forEach(e -> cache.add(type, e.getValue().getValue(), e.getAddress().getAddress()));
						} else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
							LOGGER.info("MeasuredValueShortFloatingPointSequence received.");
							MeasuredValueShortFloatingPointSequence dataMVSFPS = (MeasuredValueShortFloatingPointSequence) msg;
							int addressMVSFPS = dataMVSFPS.getStartAddress().getAddress();
							for (Value<Float> v: dataMVSFPS.getValues())
								cache.add(type, v.getValue(), addressMVSFPS++);
						}
						else{
							LOGGER.warn("Unknown message: {}. No data will be collected", msg);
						}
						break;
					case "M_ME_NB_1":
						LOGGER.info("MeasuredValueScaledSequence received.");
						MeasuredValueScaledSequence dataMVSS = (MeasuredValueScaledSequence) msg;
						int addressMVSS = dataMVSS.getStartAddress().getAddress();
						for (Value<Short> v: dataMVSS.getValues())
							cache.add(type, v.getValue(), addressMVSS++);
						break;
					case "M_SP_NA_1":
						LOGGER.info("SinglePointInformationSequence received.");
						SinglePointInformationSequence dataSPIS = (SinglePointInformationSequence) msg;
						int addressSPIS = dataSPIS.getStartAddress().getAddress();
						for (Value<Boolean> v: dataSPIS.getValues())
							cache.add(type, v.getValue(), addressSPIS++);
						break;
					case "M_BO_NA_1":
						LOGGER.info("Bitstring sequence received.");
						BitStringPointInformationSequence dataBSS = (BitStringPointInformationSequence) msg;
						int addressBSS = dataBSS.getStartAddress().getAddress();
						for (Value<byte[]> v: dataBSS.getValues())
							cache.add(type, v.getValue(), addressBSS++);
						break;
					case "M_ST_NA_1":
						LOGGER.info("Step position sequence received.");
						StepPositionSequence dataSPS = (StepPositionSequence) msg;
						int addressSPS = dataSPS.getStartAddress().getAddress();
						for (Value<Byte> v: dataSPS.getValues())
							cache.add(type, v.getValue(), addressSPS++);
						break;
					default:
						LOGGER.warn("Unknown message: {}. No data will be collected", msg);
				}
			} catch (Exception e) {
				LOGGER.error("Exception processing ASDU {}", msg, e);
			}
		}
	}

}
