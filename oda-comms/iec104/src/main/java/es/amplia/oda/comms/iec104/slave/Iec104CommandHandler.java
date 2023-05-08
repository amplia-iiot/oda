package es.amplia.oda.comms.iec104.slave;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.types.BitStringCommand;
import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DataTransmissionMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.InterrogationCommand;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.CauseOfTransmission;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.StandardCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iec104CommandHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(Iec104CommandHandler.class);

	private final Iec104Cache cache;
	private final ScadaDispatcherProxy dispatcher;
	private final int commonAddress;

	public Iec104CommandHandler(Iec104Cache cache, ScadaDispatcherProxy dispatcher, int commonAddress) {
		this.cache = cache;
		this.dispatcher = dispatcher;
		this.commonAddress = commonAddress;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		ctx.write(DataTransmissionMessage.CONFIRM_START, ctx.newPromise());
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
		if (msg.getClass().isAnnotationPresent(ASDU.class)) {
			LOGGER.debug("New ASDU received: {}", msg);
			try {
				switch (msg.getClass().getAnnotation(ASDU.class).name()) {
					case "C_IC_NA_1":
						LOGGER.info("Interrogation command received.");
						processInterrogationType(ctx, (InterrogationCommand) msg);
						return;
					case "C_BO_NA_1":
						LOGGER.info("Bit string 32 bit command received.");
						processBitStringType(ctx, (BitStringCommand) msg);
						return;
					default:
						LOGGER.warn("Unknown request: {}. No confirmation will be sent", msg);
				}
			} catch (Exception e) {
				LOGGER.error("Exception processing ASDU {}", msg, e);
			}
		}
	}

	private void processInterrogationType(final ChannelHandlerContext ctx, InterrogationCommand asdu) {
		sendConfirmationToInterrogation(ctx, asdu);
		for (Object data: cache.getASDUS(commonAddress)) {
			ctx.writeAndFlush(data, ctx.newPromise());
		}
	}

	private void processBitStringType(final ChannelHandlerContext ctx, BitStringCommand asdu) {
		sendConfirmationToBitString(ctx, asdu);
		int address = asdu.getInformationObjectAddress().getAddress();
		int value = asdu.parseBitString();
		this.dispatcher.process(ScadaDispatcher.ScadaOperation.DIRECT_OPERATE_NO_ACK, address, value, BitStringCommand.class.getAnnotation(ASDU.class).name());
	}

	private void sendConfirmationToInterrogation(final ChannelHandlerContext ctx, InterrogationCommand asdu) {
		CauseOfTransmission cot = asdu.getHeader().getCauseOfTransmission();
		if (cot.equals(new CauseOfTransmission(StandardCause.ACTIVATED))) {
			cot = new CauseOfTransmission(StandardCause.ACTIVATION_CONFIRM);
		} else if (cot.equals(new CauseOfTransmission(StandardCause.DEACTIVATED))) {
			cot = new CauseOfTransmission(StandardCause.DEACTIVATION_CONFIRM);
		}

		ctx.writeAndFlush(new InterrogationCommand(new ASDUHeader(cot, asdu.getHeader().getAsduAddress()), asdu.getQualifierOfInterrogation()), ctx.newPromise());
	}

	private void sendConfirmationToBitString(ChannelHandlerContext ctx, BitStringCommand asdu) {
		CauseOfTransmission cot = asdu.getHeader().getCauseOfTransmission();
		if (cot.equals(new CauseOfTransmission(StandardCause.ACTIVATED))) {
			cot = new CauseOfTransmission(StandardCause.ACTIVATION_CONFIRM);
		} else if (cot.equals(new CauseOfTransmission(StandardCause.DEACTIVATED))) {
			cot = new CauseOfTransmission(StandardCause.DEACTIVATION_CONFIRM);
		}

		ctx.writeAndFlush(new BitStringCommand(new ASDUHeader(cot, asdu.getHeader().getAsduAddress()), asdu.getInformationObjectAddress()), ctx.newPromise());
	}
}
