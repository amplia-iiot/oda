package es.amplia.oda.comms.iec104.slave;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.codecs.*;
import es.amplia.oda.comms.iec104.types.*;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;

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

	private final Iec104Cache cache;
	private final ProtocolOptions options;
	private final ScadaDispatcherProxy dispatcher;
	private final int commonAddress;
	private Server server;
	private MessageManager messageManager;
	private SocketChannel socketChannel;
	private MessageChannel messageChannel;

	public Iec104ServerModule(Iec104Cache cache, ProtocolOptions options, ScadaDispatcherProxy dispatcher, int commonAddress) {
		this.cache = cache;
		this.options = options;
		this.dispatcher = dispatcher;
		this.commonAddress = commonAddress;
	}

	@Override
	public void initializeServer(Server server, MessageManager messageManager) {
		this.server = server;
		this.messageManager = messageManager;
		this.messageManager.registerCodec(SinglePointInformationSingle.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointSingleCodec());
		this.messageManager.registerCodec(SinglePointInformationSequence.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointSequenceCodec());
		this.messageManager.registerCodec(BitStringPointInformationSingle.class.getAnnotation(ASDU.class).id(),
				BitStringPointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringPointSingleCodec());
		this.messageManager.registerCodec(BitStringPointInformationSequence.class.getAnnotation(ASDU.class).id(),
				BitStringPointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringPointSequenceCodec());
		this.messageManager.registerCodec(MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledSingleCodec());
		this.messageManager.registerCodec(MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledSequenceCodec());
		this.messageManager.registerCodec(InterrogationCommand.class.getAnnotation(ASDU.class).id(),
				InterrogationCommand.class.getAnnotation(ASDU.class).informationStructure(),
				new InterrogationCommandCodec());
		this.messageManager.registerCodec(BitStringCommand.class.getAnnotation(ASDU.class).id(),
				BitStringCommand.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringCommandCodec());
		this.messageManager.registerCodec(StepPositionSingle.class.getAnnotation(ASDU.class).id(),
				StepPositionSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new StepPositionSingleCodec());
		this.messageManager.registerCodec(StepPositionSequence.class.getAnnotation(ASDU.class).id(),
				StepPositionSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new StepPositionSequenceCodec());
		LOGGER.info("Initialized IEC104 server");
	}

	@Override
	public void initializeChannel(SocketChannel socketChannel, MessageChannel messageChannel) {
		this.messageChannel = new Iec104MessageChannelHandler(this.options,
				this.messageManager);
		Iec104CommandHandler commandHandler = new Iec104CommandHandler(cache, this.dispatcher, this.commonAddress);
		socketChannel.pipeline().removeLast();
		socketChannel.pipeline().addLast(this.messageChannel);
		socketChannel.pipeline().addLast(commandHandler);
		this.socketChannel = socketChannel;
		LOGGER.info("Initialized IEC104 channel");
	}

	@Override
	public void dispose() {
		this.server = null;
		this.messageManager = null;
		this.socketChannel = null;
		this.messageChannel = null;
	}

	public boolean isConnected() {
		return this.messageChannel != null &&
				this.socketChannel != null &&
				this.messageManager != null &&
				this.server != null;
	}

	public void send(Object asdu) {
		try {
			messageChannel.write(this.socketChannel.pipeline().context(this.messageChannel), asdu,
					this.socketChannel.newPromise());
			LOGGER.debug("ASDU sent to Master SCADA: {}", asdu);
		} catch (Exception e) {
			LOGGER.error("Error sending ASDU to Master SCADA", e);
		}
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public MessageChannel getMessageChannel() {
		return messageChannel;
	}
}
