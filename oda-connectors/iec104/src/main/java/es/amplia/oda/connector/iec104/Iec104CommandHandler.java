package es.amplia.oda.connector.iec104;

import es.amplia.oda.connector.iec104.types.BitstringCommand;
import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableTranslatorProxy;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.apci.MessageChannel;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageManager;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DataTransmissionMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.InterrogationCommand;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.CauseOfTransmission;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.StandardCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Could be an implementation of inbound handler instead of MessageChannel?
	// This will have less priority than our own MessageChannel. This means that only do the things that the other
		// handler send to the next handler.
	// In the other hand, an implementation of MessageChannel ensure more functionally than an Inbound implementation.
// The only question that we have to answer here is: This handler need more than a simple implementation of channel read?
// Hey, listen! This will receive the fired ChannelRead's by our MessageChannel. If you wanna do with that, is here.
public class Iec104CommandHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(Iec104CommandHandler.class);

	private final ScadaDispatcherProxy dispatcher;
	private final int commonAddress;

	Iec104CommandHandler(ScadaDispatcherProxy dispatcher, int commonAddress) {
		this.dispatcher = dispatcher;
		this.commonAddress = commonAddress;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		ctx.write(DataTransmissionMessage.CONFIRM_START, ctx.newPromise());
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
		if (msg.getClass().isAnnotationPresent(ASDU.class)) {
			LOGGER.info("New ASDU received: {}", msg);
			try {
				switch (msg.getClass().getAnnotation(ASDU.class).name()) {
					case "C_IC_NA_1":
						LOGGER.info("Interrogation command received.");
						processInterrogationType(ctx, (InterrogationCommand) msg);
						return;
					case "C_BO_NA_1":
						LOGGER.info("Bitstring 32 bit command received.");
						processBitstringType(ctx, (BitstringCommand) msg);
						return;
					default:
						LOGGER.error("Unknown request: {}. No confirmation will be sent", msg);
				}
			} catch (Exception e) {
				LOGGER.error("Exception processing ASDU {}: {}", msg, e);
			}
		}
	}

	private void processInterrogationType(final ChannelHandlerContext ctx, InterrogationCommand asdu) throws Exception {
		sendConfirmationToInterrogation(ctx, asdu);
		for (Object data: Iec104Cache.getASDUS(commonAddress)) {
			ctx.writeAndFlush(data, ctx.newPromise());
		}
	}

	private void processBitstringType(final ChannelHandlerContext ctx, BitstringCommand asdu) throws Exception {
		sendConfirmationToBitstring(ctx, asdu);
		int address = asdu.getInformationObjectAddress().getAddress();
		int value = asdu.parseBytestring();
		this.dispatcher.process(ScadaDispatcher.ScadaOperation.DIRECT_OPERATE_NO_ACK, address, value, BitstringCommand.class.getAnnotation(ASDU.class).name());
	}

	private void sendConfirmationToInterrogation(final ChannelHandlerContext ctx, InterrogationCommand asdu) throws Exception {
		CauseOfTransmission cot = asdu.getHeader().getCauseOfTransmission();
		if (cot.equals(new CauseOfTransmission(StandardCause.ACTIVATED))) {
			cot = new CauseOfTransmission(StandardCause.ACTIVATION_CONFIRM);
		} else if (cot.equals(new CauseOfTransmission(StandardCause.DEACTIVATED))) {
			cot = new CauseOfTransmission(StandardCause.DEACTIVATION_CONFIRM);
		}

		ctx.writeAndFlush(new InterrogationCommand(new ASDUHeader(cot, asdu.getHeader().getAsduAddress()), asdu.getQualifierOfInterrogation()), ctx.newPromise());
	}

	private void sendConfirmationToBitstring(ChannelHandlerContext ctx, BitstringCommand asdu) throws Exception {
		CauseOfTransmission cot = asdu.getHeader().getCauseOfTransmission();
		if (cot.equals(new CauseOfTransmission(StandardCause.ACTIVATED))) {
			cot = new CauseOfTransmission(StandardCause.ACTIVATION_CONFIRM);
		} else if (cot.equals(new CauseOfTransmission(StandardCause.DEACTIVATED))) {
			cot = new CauseOfTransmission(StandardCause.DEACTIVATION_CONFIRM);
		}

		ctx.writeAndFlush(new BitstringCommand(new ASDUHeader(cot, asdu.getHeader().getAsduAddress()), asdu.getInformationObjectAddress()), ctx.newPromise());
	}
}
