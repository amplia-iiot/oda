package es.amplia.oda.comms.iec104.master;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.codecs.*;
import es.amplia.oda.comms.iec104.types.*;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.apci.MessageChannel;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageManager;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.*;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.client.Client;
import org.eclipse.neoscada.protocol.iec60870.client.ClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iec104ClientModule implements ClientModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ClientModule.class);
    
    private Client client;
    private MessageManager messageManager;
	private MessageChannel messageChannel;
    private final ProtocolOptions options;
    private final Iec104Cache cache;

	@Getter
	private final String deviceId;

	private final int commonAddress;

	@Getter
	private boolean connected;

    public Iec104ClientModule (Iec104Cache cache, ProtocolOptions options, String deviceId, int commonAddress) {
        this.cache = cache;
        this.options = options;
		this.deviceId = deviceId;
		this.connected = false;
		this.commonAddress = commonAddress;
    }

    @Override
    public void initializeClient(Client client, MessageManager messageManager) {
        this.client = client;
        this.messageManager = messageManager;

		// SinglePointInformation
        this.messageManager.registerCodec(SinglePointInformationSingle.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointInformationSingleCodec());
		this.messageManager.registerCodec(SinglePointInformationTimeSingle.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointInformationTimeSingleCodec());
		this.messageManager.registerCodec(SinglePointInformationSequence.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointInformationSequenceCodec());
		this.messageManager.registerCodec(SinglePointInformationTimeSequence.class.getAnnotation(ASDU.class).id(),
				SinglePointInformationTimeSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new SinglePointInformationTimeSequenceCodec());

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
		this.messageManager.registerCodec(DoublePointInformationTimeSequence.class.getAnnotation(ASDU.class).id(),
				DoublePointInformationTimeSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new DoublePointInformationTimeSequenceCodec());

		// BitStringPointInformation
		this.messageManager.registerCodec(BitStringPointInformationSingle.class.getAnnotation(ASDU.class).id(),
				BitStringPointInformationSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringPointSingleCodec());
		this.messageManager.registerCodec(BitStringPointInformationTimeSingle.class.getAnnotation(ASDU.class).id(),
				BitStringPointInformationTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringPointTimeSingleCodec());
		this.messageManager.registerCodec(BitStringPointInformationSequence.class.getAnnotation(ASDU.class).id(),
				BitStringPointInformationSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringPointSequenceCodec());
		this.messageManager.registerCodec(BitStringPointInformationTimeSequence.class.getAnnotation(ASDU.class).id(),
				BitStringPointInformationTimeSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new BitStringPointTimeSequenceCodec());

		// MeasuredValueScaled
		this.messageManager.registerCodec(MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledSingleCodec());
		this.messageManager.registerCodec(MeasuredValueScaledTimeSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledTimeSingleCodec());
		this.messageManager.registerCodec(MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledSequenceCodec());
		this.messageManager.registerCodec(MeasuredValueScaledTimeSequence.class.getAnnotation(ASDU.class).id(),
				MeasuredValueScaledTimeSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueScaledTimeSequenceCodec());

		// MeasuredValueFloatingPoint
        this.messageManager.registerCodec(MeasuredValueShortFloatingPointSingle.class.getAnnotation(ASDU.class).id(),
                MeasuredValueShortFloatingPointSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueFloatingPointSingleCodec());
		this.messageManager.registerCodec(MeasuredValueShortFloatingPointTimeSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueShortFloatingPointTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueFloatingPointTimeSingleCodec());
		this.messageManager.registerCodec(MeasuredValueShortFloatingPointSequence.class.getAnnotation(ASDU.class).id(),
        MeasuredValueShortFloatingPointSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueFloatingPointSequenceCodec());
        this.messageManager.registerCodec(MeasuredValueShortFloatingPointTimeSequence.class.getAnnotation(ASDU.class).id(),
				MeasuredValueShortFloatingPointTimeSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueFloatingPointTimeSequenceCodec());

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
		this.messageManager.registerCodec(StepPositionTimeSingle.class.getAnnotation(ASDU.class).id(),
				StepPositionTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new StepPositionTimeSingleCodec());
		this.messageManager.registerCodec(StepPositionSequence.class.getAnnotation(ASDU.class).id(),
				StepPositionSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new StepPositionSequenceCodec());
		this.messageManager.registerCodec(StepPositionTimeSequence.class.getAnnotation(ASDU.class).id(),
				StepPositionTimeSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new StepPositionTimeSequenceCodec());

		// MeasureValueNormalized
		this.messageManager.registerCodec(MeasuredValueNormalizedSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueNormalizedSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueNormalizedSingleCodec());
		this.messageManager.registerCodec(MeasuredValueNormalizedTimeSingle.class.getAnnotation(ASDU.class).id(),
				MeasuredValueNormalizedTimeSingle.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueNormalizedTimeSingleCodec());
		this.messageManager.registerCodec(MeasuredValueNormalizedSequence.class.getAnnotation(ASDU.class).id(),
				MeasuredValueNormalizedSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueNormalizedSequenceCodec());
		this.messageManager.registerCodec(MeasuredValueNormalizedTimeSequence.class.getAnnotation(ASDU.class).id(),
				MeasuredValueNormalizedSequence.class.getAnnotation(ASDU.class).informationStructure(),
				new MeasuredValueNormalizedTimeSequenceCodec());

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
		Iec104ResponseHandler respHandler = new Iec104ResponseHandler(cache, this.deviceId, this.commonAddress);
		// we replace the message channel introduced by neoscada library in Client class (handleInitChannel) with our message channel
		socketChannel.pipeline().replace(MessageChannel.class, this.messageChannel.toString(), this.messageChannel);
		socketChannel.pipeline().addLast(respHandler);
		LOGGER.info("Initialized IEC104 channel");
    }

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	@Override
	public void dispose() {
		this.client = null;
		this.messageManager = null;
		this.messageChannel = null;
		this.connected = false;
	}

    public void send(Object asdu) {
        if (this.connected) {
            this.client.writeCommand(asdu);
        }
    }
    
}
