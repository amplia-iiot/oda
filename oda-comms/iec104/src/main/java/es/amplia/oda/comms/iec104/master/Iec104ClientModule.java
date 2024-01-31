package es.amplia.oda.comms.iec104.master;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.codecs.*;
import es.amplia.oda.comms.iec104.types.*;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.event.api.EventDispatcher;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.apci.MessageChannel;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageManager;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.*;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDUAddress;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.CauseOfTransmission;
import org.eclipse.neoscada.protocol.iec60870.client.Client;
import org.eclipse.neoscada.protocol.iec60870.client.ClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Iec104ClientModule implements ClientModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ClientModule.class);
    
    private Client client;
    private MessageManager messageManager;
	private MessageChannel messageChannel;
    private final ProtocolOptions options;
    private final Map<String, Iec104Cache> cache;
	private final EventDispatcher eventDispatcher;
	private final ScadaTableTranslator scadaTables;
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> interrogationCommandTask;



	@Getter
	private final String deviceId;

	private final int commonAddress;

	@Getter
	private boolean connected;

    public Iec104ClientModule (Map<String, Iec104Cache> caches, ProtocolOptions options, String deviceId, int commonAddress,
							   EventDispatcher eventDispatcher, ScadaTableTranslator scadaTables) {
        this.cache = caches;
        this.options = options;
		this.deviceId = deviceId;
		this.connected = false;
		this.commonAddress = commonAddress;
		this.eventDispatcher = eventDispatcher;
		this.scadaTables = scadaTables;
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

        LOGGER.debug("Initialized IEC104 client");
    }

    @Override
    public void initializeChannel(SocketChannel socketChannel, MessageChannel messageChannel) {
        this.messageChannel = new Iec104MessageChannelHandler(this.options, this.messageManager);
		Iec104ResponseHandler respHandler = new Iec104ResponseHandler(cache, this.deviceId, this.commonAddress,
				this.eventDispatcher, this.scadaTables);
		// we replace the message channel introduced by neoscada library in Client class (handleInitChannel) with our message channel
		socketChannel.pipeline().replace(MessageChannel.class, this.messageChannel.toString(), this.messageChannel);
		socketChannel.pipeline().addLast(respHandler);
		LOGGER.debug("Initialized IEC104 channel");
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
		this.interrogationCommandTask.cancel(false);
		this.scheduledExecutor.shutdownNow();
	}

    public void send(Object asdu) {
        if (this.connected) {
            this.client.writeCommand(asdu);
        }
    }

	public ScheduledFuture<?> addInterrogationCommandScheduling(int initialPolling, int polling) {
		LOGGER.info("Scheduling interrogation command for deviceId {}, initial delay {}, polling every {} milliseconds"
				, this.deviceId, initialPolling, polling);

		if (initialPolling <= 0 || polling <= 0) {
			LOGGER.error("Initial delay or polling times must be bigger than zero");
			return null;
		}

		if (interrogationCommandTask != null && !interrogationCommandTask.isCancelled()) {
			interrogationCommandTask.cancel(false);
		}

		Runnable taskWithExceptionCatching = () -> {
            try {
                InterrogationCommand cmd = new InterrogationCommand(new ASDUHeader(CauseOfTransmission.ACTIVATED,
                        ASDUAddress.valueOf(commonAddress)), (short) 20);
                if (isConnected()) {
                    LOGGER.info("Sending InterrogationCommand for device {}", deviceId);
                    send(cmd);
                } else {
                    LOGGER.warn("Could not send InterrogationCommand due to no client connected for device {}", deviceId);
                }

            } catch (Throwable t) {  // Catch Throwable rather than Exception (a subclass).
                LOGGER.error("Caught exception in IEC104 TimerTask. StackTrace: ", t);
            }
        };

		interrogationCommandTask = scheduledExecutor.scheduleAtFixedRate(taskWithExceptionCatching, initialPolling, polling, TimeUnit.MILLISECONDS);
		return interrogationCommandTask;
	}

	public void cancelInterrogationCommandScheduling(){
		if(!interrogationCommandTask.isCancelled()) {
			interrogationCommandTask.cancel(false);
		}
	}
}
