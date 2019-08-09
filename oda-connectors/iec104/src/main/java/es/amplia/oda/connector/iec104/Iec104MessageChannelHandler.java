package es.amplia.oda.connector.iec104;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.apci.MessageChannel;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageManager;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DataTransmissionMessage;

public class Iec104MessageChannelHandler extends MessageChannel {

	Iec104MessageChannelHandler(ProtocolOptions options, MessageManager manager) {
		super(options, manager);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		super.write(ctx, DataTransmissionMessage.CONFIRM_START, ctx.newPromise());
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		super.write(ctx, msg, promise);
		ctx.flush();
	}
}
