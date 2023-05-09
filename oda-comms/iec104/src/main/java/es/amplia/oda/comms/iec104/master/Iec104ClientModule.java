package es.amplia.oda.comms.iec104.master;

import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.apci.MessageChannel;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageManager;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DoublePointInformationSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DoublePointInformationSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DoublePointInformationTimeSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.InterrogationCommand;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledTimeSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueShortFloatingPointSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueShortFloatingPointSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueShortFloatingPointTimeSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SinglePointInformationSequence;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SinglePointInformationSingle;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.client.Client;
import org.eclipse.neoscada.protocol.iec60870.client.ClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.codecs.BitStringCommandCodec;
import es.amplia.oda.comms.iec104.codecs.BitStringPointSequenceCodec;
import es.amplia.oda.comms.iec104.codecs.BitStringPointSingleCodec;
import es.amplia.oda.comms.iec104.codecs.DoublePointInformationSequenceCodec;
import es.amplia.oda.comms.iec104.codecs.InterrogationCommandCodec;
import es.amplia.oda.comms.iec104.codecs.MeasuredValueFloatingPointSequenceCodec;
import es.amplia.oda.comms.iec104.codecs.MeasuredValueFloatingPointSingleCodec;
import es.amplia.oda.comms.iec104.codecs.MeasuredValueFloatingPointTimeSingleCodec;
import es.amplia.oda.comms.iec104.codecs.MeasuredValueScaledSequenceCodec;
import es.amplia.oda.comms.iec104.codecs.MeasuredValueScaledSingleCodec;
import es.amplia.oda.comms.iec104.codecs.MeasuredValueScaledTimeSingleCodec;
import es.amplia.oda.comms.iec104.codecs.SinglePointSequenceCodec;
import es.amplia.oda.comms.iec104.codecs.DoublePointInformationSingleCodec;
import es.amplia.oda.comms.iec104.codecs.DoublePointInformationTimeSingleCodec;
import es.amplia.oda.comms.iec104.types.BitStringCommand;
import es.amplia.oda.comms.iec104.types.BitStringPointInformationSequence;
import es.amplia.oda.comms.iec104.types.BitStringPointInformationSingle;
import io.netty.channel.socket.SocketChannel;

public class Iec104ClientModule implements ClientModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ClientModule.class);
    
    private Client client;
    private MessageManager messageManager;
	//private SocketChannel socketChannel;
	private MessageChannel messageChannel;
    private final ProtocolOptions options;
    private Iec104Cache cache;

    public Iec104ClientModule (Iec104Cache cache, ProtocolOptions options) {
        this.cache = cache;
        this.options = options;
    }

    @Override
    public void initializeClient(Client client, MessageManager messageManager) {
        this.client = client;
        this.messageManager = messageManager;

        this.messageManager.registerCodec(SinglePointInformationSingle.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new DoublePointInformationSingleCodec());
		this.messageManager.registerCodec(SinglePointInformationSequence.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointSequenceCodec());
        
        this.messageManager.registerCodec(DoublePointInformationSingle.class.getAnnotation(ASDU.class).id(),
                DoublePointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new DoublePointInformationSingleCodec());
        this.messageManager.registerCodec(DoublePointInformationTimeSingle.class.getAnnotation(ASDU.class).id(),
                DoublePointInformationTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new DoublePointInformationTimeSingleCodec());
		this.messageManager.registerCodec(DoublePointInformationSequence.class.getAnnotation(ASDU.class).id(),
				DoublePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new DoublePointInformationSequenceCodec());

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
        this.messageManager.registerCodec(MeasuredValueScaledTimeSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledTimeSingleCodec());
        
        this.messageManager.registerCodec(MeasuredValueShortFloatingPointSingle.class.getAnnotation(ASDU.class).id(),
                MeasuredValueShortFloatingPointSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueFloatingPointSingleCodec());
		this.messageManager.registerCodec(MeasuredValueShortFloatingPointSequence.class.getAnnotation(ASDU.class).id(),
        MeasuredValueShortFloatingPointSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueFloatingPointSequenceCodec());
        this.messageManager.registerCodec(MeasuredValueShortFloatingPointTimeSingle.class.getAnnotation(ASDU.class).id(),
                MeasuredValueShortFloatingPointTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueFloatingPointTimeSingleCodec());

		this.messageManager.registerCodec(InterrogationCommand.class.getAnnotation(ASDU.class).id(),
				InterrogationCommand.class.getAnnotation(ASDU.class).informationStructure(),
				new InterrogationCommandCodec());
		this.messageManager.registerCodec(BitStringCommand.class.getAnnotation(ASDU.class).id(),
				BitStringCommand.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringCommandCodec());
        LOGGER.info("Initialized IEC104 client");
    }

    @Override
    public void initializeChannel(SocketChannel socketChannel, MessageChannel messageChannel) {
        this.messageChannel = new Iec104MessageChannelHandler(this.options,
				this.messageManager);
		Iec104ResponseHandler respHandler = new Iec104ResponseHandler(cache);
		socketChannel.pipeline().removeLast();
		socketChannel.pipeline().addLast(this.messageChannel);
		socketChannel.pipeline().addLast(respHandler);
		//this.socketChannel = socketChannel;
		LOGGER.info("Initialized IEC104 channel");
    }

    public boolean isConnected() {
		return this.messageChannel != null &&
				this.messageManager != null &&
				this.client != null;
	}

    @Override
    public void dispose() {
        this.client = null;
		this.messageManager = null;
		//this.socketChannel = null;
		this.messageChannel = null;
    }

    public void send(Object asdu) {
        if (this.client != null) {
            this.client.writeCommand(asdu);
            /*try {
                this.messageChannel.write(this.socketChannel.pipeline().context(this.messageChannel), asdu,
                                this.socketChannel.newPromise());
                LOGGER.debug("ASDU sent to Slave SCADA: {}", asdu);
            } catch (Exception e) {
                LOGGER.error("Error sending ASDU to Slave SCADA", e);
            }*/
        }
    }
    
}
