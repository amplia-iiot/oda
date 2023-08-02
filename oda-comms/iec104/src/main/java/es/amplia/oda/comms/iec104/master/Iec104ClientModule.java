package es.amplia.oda.comms.iec104.master;

import es.amplia.oda.comms.iec104.codecs.*;
import es.amplia.oda.comms.iec104.types.*;
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
import io.netty.channel.socket.SocketChannel;

public class Iec104ClientModule implements ClientModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ClientModule.class);
    
    private Client client;
    private MessageManager messageManager;
	//private SocketChannel socketChannel;
	private MessageChannel messageChannel;
    private final ProtocolOptions options;
    private Iec104Cache cache;
	private final String deviceId;

	private boolean connected;

    public Iec104ClientModule (Iec104Cache cache, ProtocolOptions options, String deviceId) {
        this.cache = cache;
        this.options = options;
		this.deviceId = deviceId;
		this.connected = false;
    }

    @Override
    public void initializeClient(Client client, MessageManager messageManager) {
        this.client = client;
        this.messageManager = messageManager;

		// SinglePointInformation
        this.messageManager.registerCodec(SinglePointInformationSingle.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointInformationSingleCodec());
		this.messageManager.registerCodec(SinglePointInformationSequence.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointInformationSequenceCodec());

		// DoublePointInformation
        this.messageManager.registerCodec(DoublePointInformationSingle.class.getAnnotation(ASDU.class).id(),
                DoublePointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new DoublePointInformationSingleCodec());
        this.messageManager.registerCodec(DoublePointInformationTimeSingle.class.getAnnotation(ASDU.class).id(),
                DoublePointInformationTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new DoublePointInformationTimeSingleCodec());
		this.messageManager.registerCodec(DoublePointInformationSequence.class.getAnnotation(ASDU.class).id(),
				DoublePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new DoublePointInformationSequenceCodec());

		// BitStringPointInformation
		this.messageManager.registerCodec(BitStringPointInformationSingle.class.getAnnotation(ASDU.class).id(),
				BitStringPointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringPointSingleCodec());
		this.messageManager.registerCodec(BitStringPointInformationSequence.class.getAnnotation(ASDU.class).id(),
				BitStringPointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringPointSequenceCodec());

		// MeasuredValueScaled
		this.messageManager.registerCodec(MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledSingleCodec());
		this.messageManager.registerCodec(MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledSequenceCodec());
        this.messageManager.registerCodec(MeasuredValueScaledTimeSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledTimeSingleCodec());

		// MeasuredValueFloatingPoint
        this.messageManager.registerCodec(MeasuredValueShortFloatingPointSingle.class.getAnnotation(ASDU.class).id(),
                MeasuredValueShortFloatingPointSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueFloatingPointSingleCodec());
		this.messageManager.registerCodec(MeasuredValueShortFloatingPointSequence.class.getAnnotation(ASDU.class).id(),
        MeasuredValueShortFloatingPointSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueFloatingPointSequenceCodec());
        this.messageManager.registerCodec(MeasuredValueShortFloatingPointTimeSingle.class.getAnnotation(ASDU.class).id(),
                MeasuredValueShortFloatingPointTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueFloatingPointTimeSingleCodec());

		// Commands
		this.messageManager.registerCodec(InterrogationCommand.class.getAnnotation(ASDU.class).id(),
				InterrogationCommand.class.getAnnotation(ASDU.class).informationStructure(),
				new InterrogationCommandCodec());
		this.messageManager.registerCodec(BitStringCommand.class.getAnnotation(ASDU.class).id(),
				BitStringCommand.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringCommandCodec());

		// Step position
		this.messageManager.registerCodec(StepPositionSingle.class.getAnnotation(ASDU.class).id(),
				StepPositionSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new StepPositionSingleCodec());
		this.messageManager.registerCodec(StepPositionSequence.class.getAnnotation(ASDU.class).id(),
				StepPositionSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new StepPositionSequenceCodec());

		// MeasureValueNormalized
		this.messageManager.registerCodec(MeasuredValueNormalizedSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueNormalizedSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueNormalizedSingleCodec());
		this.messageManager.registerCodec(MeasuredValueNormalizedSequence.class.getAnnotation(ASDU.class).id(),
				MeasuredValueNormalizedSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueNormalizedSequenceCodec());

		// MeasureValueNormalizedNoQuality
		this.messageManager.registerCodec(MeasuredValueNormalizedNoQualitySingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueNormalizedNoQualitySingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueNormalizedNoQualitySingleCodec());
		this.messageManager.registerCodec(MeasuredValueNormalizedNoQualitySequence.class.getAnnotation(ASDU.class).id(),
				MeasuredValueNormalizedNoQualitySequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueNormalizedNoQualitySequenceCodec());

        LOGGER.info("Initialized IEC104 client");
    }

    @Override
    public void initializeChannel(SocketChannel socketChannel, MessageChannel messageChannel) {
        this.messageChannel = new Iec104MessageChannelHandler(this.options,
				this.messageManager);
		Iec104ResponseHandler respHandler = new Iec104ResponseHandler(cache, this.deviceId);
		socketChannel.pipeline().removeLast();
		socketChannel.pipeline().addLast(this.messageChannel);
		socketChannel.pipeline().addLast(respHandler);
		//this.socketChannel = socketChannel;
		LOGGER.info("Initialized IEC104 channel");
    }

	public boolean isConnected() {
		return this.connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	@Override
	public void dispose() {
		this.client = null;
		this.messageManager = null;
		//this.socketChannel = null;
		this.messageChannel = null;
		this.connected = false;
	}

    public void send(Object asdu) {
        if (this.connected) {
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
