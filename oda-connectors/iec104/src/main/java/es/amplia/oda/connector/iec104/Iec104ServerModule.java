package es.amplia.oda.connector.iec104;

import es.amplia.oda.connector.iec104.codecs.*;
import es.amplia.oda.connector.iec104.types.BitstringCommand;
import es.amplia.oda.connector.iec104.types.BytestringPointInformationSequence;
import es.amplia.oda.connector.iec104.types.BytestringPointInformationSingle;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.apci.MessageChannel;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageManager;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.*;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.server.Server;
import org.eclipse.neoscada.protocol.iec60870.server.ServerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iec104ServerModule implements ServerModule {

	private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ServerModule.class);

	private ServerKeepAlive server;
	private MessageManager messageManager;
	private SocketChannel socketChannel;
	private MessageChannel messageChannel;
	private Iec104CommandHandler commandHandler;
	private ProtocolOptions options;
	private ScadaDispatcherProxy dispatcher;
	private int commonAddress;

	Iec104ServerModule(ProtocolOptions options, ScadaDispatcherProxy dispatcher, int commonAddress) {
		this.options = options;
		this.dispatcher = dispatcher;
		this.commonAddress = commonAddress;
	}

	@Override
	public void initializeServer(Server server, MessageManager messageManager) {
		this.server = (ServerKeepAlive) server;
		this.messageManager = messageManager;
		this.messageManager.registerCodec(SinglePointInformationSingle.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointSingleCodec());
		this.messageManager.registerCodec(SinglePointInformationSequence.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointSequenceCodec());
		this.messageManager.registerCodec(BytestringPointInformationSingle.class.getAnnotation(ASDU.class).id(),
				BytestringPointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new BytestringPointSingleCodec());
		this.messageManager.registerCodec(BytestringPointInformationSequence.class.getAnnotation(ASDU.class).id(),
				BytestringPointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new BytestringPointSequenceCodec());
		this.messageManager.registerCodec(MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledSingleCodec());
		this.messageManager.registerCodec(MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledSequenceCodec());
		this.messageManager.registerCodec(InterrogationCommand.class.getAnnotation(ASDU.class).id(),
				InterrogationCommand.class.getAnnotation(ASDU.class).informationStructure(),
				new InterrogationCommandCodec());
		this.messageManager.registerCodec(BitstringCommand.class.getAnnotation(ASDU.class).id(),
				BitstringCommand.class.getAnnotation(ASDU.class).informationStructure(),
				new BitstringCommandCodec());
	}

	@Override
	public void initializeChannel(SocketChannel socketChannel, MessageChannel messageChannel) {
		this.messageChannel = new Iec104MessageChannelHandler(this.options,
				this.messageManager);
		this.commandHandler = new Iec104CommandHandler(this.dispatcher, this.commonAddress);
		socketChannel.pipeline().removeLast();
		socketChannel.pipeline().addLast(new ChannelHandler[]{this.messageChannel});
		socketChannel.pipeline().addLast(new ChannelHandler[]{this.commandHandler});
		this.socketChannel = socketChannel;
	}

	@Override
	public void dispose() {
		this.server = null;
		this.messageManager = null;
		this.socketChannel = null;
		this.messageChannel = null;
	}

	boolean isConnected() {
		return this.messageChannel != null &&
				this.socketChannel != null &&
				this.messageManager != null &&
				this.server != null;
	}

	void send(Object asdu) {
		try {
			messageChannel.write(this.socketChannel.pipeline().context(this.messageChannel),
					asdu, this.socketChannel.newPromise());
			LOGGER.info("ASDU sent to Master SCADA: {}", asdu);
		} catch (Exception e) {
			LOGGER.error("Error sending ASDU to Master SCADA: {}", e.getMessage());
		}
	}
}
