package es.amplia.oda.comms.iec104.master;

import es.amplia.oda.comms.iec104.Iec104Cache;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.eclipse.neoscada.protocol.iec60870.asdu.message.DataTransmissionMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DoublePointInformationSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DoublePointInformationTimeSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueShortFloatingPointSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SinglePointInformationSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.DoublePoint;
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
			LOGGER.debug("New ASDU received: {}", msg);
			String type = msg.getClass().getAnnotation(ASDU.class).name();
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
							cache.add(type, v.getValue(), addressDPIS++);
						break;
					case "M_ME_NC_1":
						LOGGER.info("MeasuredValueShortFloatingPointSequence received.");
						MeasuredValueShortFloatingPointSequence dataMVSFPS = (MeasuredValueShortFloatingPointSequence) msg;
						int addressMVSFPS = dataMVSFPS.getStartAddress().getAddress();
						for (Value<Float> v: dataMVSFPS.getValues())
							cache.add(type, v.getValue(), addressMVSFPS++);
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
					default:
						LOGGER.warn("Unknown message: {}. No data will be collected", msg);
				}
			} catch (Exception e) {
				LOGGER.error("Exception processing ASDU {}", msg, e);
			}
		}
	}

}
