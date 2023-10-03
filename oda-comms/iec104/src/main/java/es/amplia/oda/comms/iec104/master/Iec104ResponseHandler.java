package es.amplia.oda.comms.iec104.master;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.types.*;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.event.api.EventDispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.*;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Iec104ResponseHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ResponseHandler.class);

    private final EventDispatcher eventDispatcher;
    private final ScadaTableTranslator scadaTables;

    private final Iec104Cache cache;
    @Getter
    private final String deviceId;
    private final int commonAddress;

    public Iec104ResponseHandler(Iec104Cache cache, String deviceId, int commonAddress,
                                 EventDispatcher eventDispatcher, ScadaTableTranslator scadaTables) {
        this.cache = cache;
        this.deviceId = deviceId;
        this.commonAddress = commonAddress;
        this.eventDispatcher = eventDispatcher;
        this.scadaTables = scadaTables;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.write(DataTransmissionMessage.REQUEST_START, ctx.newPromise());
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        LOGGER.trace("New message received: {}", msg);

        if (!msg.getClass().isAnnotationPresent(ASDU.class)) {
            LOGGER.warn("Unknown message received: {}", msg);
            return;
        }

        // to this point we know it is an ASDU
        // cast to AbstractMessage to extract the commonAddres of the message
        AbstractMessage message = (AbstractMessage) msg;
        int commonAddressReceived = message.getHeader().getAsduAddress().getAddress();

        // if the common address of the message received is not the same as the responseHandler, pass message to next responseHandler
        // we will have as much responseHandlers as remote devices with the same IP and port
        if (commonAddressReceived != this.commonAddress) {
            LOGGER.debug("Common address received {} is different to commonAddress of handler {}", commonAddressReceived, this.commonAddress);
            // pass message to next responseHandler registered in socket pipeline
            ctx.fireChannelRead(msg);
            return;
        }

        // get cause of transmission
        short causeOfTransmission = message.getHeader().getCauseOfTransmission().getCause().getValue();

        // get ASDU information
        String type = msg.getClass().getAnnotation(ASDU.class).name();
        InformationStructure msgInfoStruct = msg.getClass().getAnnotation(ASDU.class).informationStructure();
        LOGGER.info("ASDU received - {} {}, {}, {} from device {}",
                type, translateASDU(type), msgInfoStruct, translateCauseOfTransmission(causeOfTransmission), this.deviceId);

        if (!(msgInfoStruct.equals(InformationStructure.SINGLE) || msgInfoStruct.equals(InformationStructure.SEQUENCE))) {
            LOGGER.error("Unknown ASDU informationStructure {}", msgInfoStruct);
        }

        // parse ASDU value
        Map<Integer, Value<?>> valuesParsed = parseASDU(type, msgInfoStruct, msg);

        if (!valuesParsed.isEmpty()) {

            valuesParsed.forEach((address, value) -> {
                LOGGER.info("Value received - type {}, address {} and value {}", type, address, value.getValue());
                LOGGER.debug("Quality information {}, overflow {}", value.getQualityInformation(), value.isOverflow());
            });

            // spontaneous messages must be published right away
            if (causeOfTransmission == StandardCause.SPONTANEOUS.getValue()) {
                sendImmediately(type, valuesParsed, this.deviceId);
            }
            // else, add it to the cache
            else {
                valuesParsed.forEach((address, value) -> cache.add(type, value, address));
            }
        }
    }

    private void sendImmediately(String type, Map<Integer, Value<?>> valuesParsed, String connectionDeviceId)
    {
        // parse to ODA events
        List<Event> eventsToPublish = new ArrayList<>();

        valuesParsed.forEach((address, value) -> {
            Event event = parseEvent(type, value, address, connectionDeviceId);
            eventsToPublish.add(event);
        });

        eventDispatcher.publishImmediately(eventsToPublish);
    }

    private Event parseEvent(String type, Value<?> value, int address,  String connectionDeviceId) {

        ScadaTableTranslator.ScadaInfo scadaInfo = new ScadaTableTranslator.ScadaInfo(address, type);

        // get datastreamId, deviceId and feed from scada tables
        ScadaTableTranslator.ScadaTranslationInfo datastreamInfo = scadaTables.getTranslationInfo(scadaInfo);

        // transform value, scada tables can have scripts associated
        Object transformedValue = scadaTables.transformValue(address, type, value.getValue());

        String deviceId = datastreamInfo.getDeviceId() != null ? datastreamInfo.getDeviceId() : connectionDeviceId;

        return new Event(datastreamInfo.getDatastreamId(), deviceId, null, datastreamInfo.getFeed(),
                value.getTimestamp(), transformedValue);
    }

    private Map<Integer, Value<?>> parseASDU(String type, InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        try {
            switch (type) {
                case "C_IC_NA_1":
                    // interrogation command, don't do anything, avoid to log it as an unknown message
                    break;
                case "M_DP_TB_1":
                    valuesParsed = parseDoublePointInformationTime(msgInfoStruct, msg);
                    break;
                case "M_DP_NA_1":
                    valuesParsed = parseDoublePointInformation(msgInfoStruct, msg);
                    break;
                case "M_ME_NC_1":
                    valuesParsed = parseMeasuredValueFloatingPoint(msgInfoStruct, msg);
                    break;
                case "M_ME_TF_1":
                    valuesParsed = parseMeasuredValueFloatingPointTime(msgInfoStruct, msg);
                    break;
                case "M_ME_NB_1":
                    valuesParsed = parseMeasuredValueScaled(msgInfoStruct, msg);
                    break;
                case "M_ME_TE_1":
                    valuesParsed = parseMeasuredValueScaledTime(msgInfoStruct, msg);
                    break;
                case "M_SP_NA_1":
                    valuesParsed = parseSinglePointInformation(msgInfoStruct, msg);
                    break;
                case "M_SP_TB_1":
                    valuesParsed = parseSinglePointInformationTime(msgInfoStruct, msg);
                    break;
                case "M_BO_NA_1":
                    valuesParsed = parseBitstring(msgInfoStruct, msg);
                    break;
                case "M_BO_TB_1":
                    valuesParsed = parseBitstringTime(msgInfoStruct, msg);
                    break;
                case "M_ST_NA_1":
                    valuesParsed = parseStepPosition(msgInfoStruct, msg);
                    break;
                case "M_ST_TB_1":
                    valuesParsed = parseStepPositionTime(msgInfoStruct, msg);
                    break;
                case "M_ME_NA_1":
                    valuesParsed = parseMeasureValueNormalized(msgInfoStruct, msg);
                    break;
                case "M_ME_TD_1":
                    valuesParsed = parseMeasureValueNormalizedTime(msgInfoStruct, msg);
                    break;
                case "M_ME_ND_1":
                    valuesParsed = parseMeasureValueNormalizedNoQuality(msgInfoStruct, msg);
                    break;
                default:
                    LOGGER.warn("Unknown ASDU: type {}, informationStructure {}", type, msgInfoStruct);
            }
        } catch (Exception e) {
            LOGGER.error("Exception processing ASDU {}", msg, e);
            return valuesParsed;
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseDoublePointInformationTime(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("DoublePointInformation single with seven octets time received.");
            DoublePointInformationTimeSingle dataDPIT = (DoublePointInformationTimeSingle) msg;
            dataDPIT.getEntries().forEach(e -> {
                // transform enum value to string for ODA to be able to store it in database
                Value<String> value = new Value<>(e.getValue().getValue().toString(),
                        e.getValue().getTimestamp(), e.getValue().getQualityInformation());
                valuesParsed.put(e.getAddress().getAddress(), value);
            });
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("DoublePointInformation sequence with seven octets time received.");
            DoublePointInformationTimeSequence dataDPITSeq = (DoublePointInformationTimeSequence) msg;
            int addressMVNNQSeq = dataDPITSeq.getStartAddress().getAddress();
            for (Value<DoublePoint> v : dataDPITSeq.getValues()) {
                // transform enum value to string for ODA to be able to store it
                Value<String> value = new Value<>(v.getValue().toString(), v.getTimestamp(), v.getQualityInformation());
                valuesParsed.put(addressMVNNQSeq++, value);
            }
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseDoublePointInformation(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("DoublePointInformation single received.");
            DoublePointInformationSingle dataDPIS = (DoublePointInformationSingle) msg;
            dataDPIS.getEntries().forEach(e -> {
                // transform enum value to string for ODA to be able to store it in database
                Value<String> value = new Value<>(e.getValue().getValue().toString(),
                        e.getValue().getTimestamp(), e.getValue().getQualityInformation());
                valuesParsed.put(e.getAddress().getAddress(), value);
            });
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("DoublePointInformation sequence received.");
            DoublePointInformationSequence dataDPISeq = (DoublePointInformationSequence) msg;
            int addressDPISeq = dataDPISeq.getStartAddress().getAddress();
            for (Value<DoublePoint> v : dataDPISeq.getValues()) {
                // transform enum value to string for ODA to be able to store it
                Value<String> value = new Value<>(v.getValue().toString(), v.getTimestamp(), v.getQualityInformation());
                valuesParsed.put(addressDPISeq++, value);
            }
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseMeasuredValueFloatingPoint(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("MeasuredValueFloatingPoint single received.");
            MeasuredValueShortFloatingPointSingle dataMVSFPS = (MeasuredValueShortFloatingPointSingle) msg;
            dataMVSFPS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("MeasuredValueFloatingPoint sequence received.");
            MeasuredValueShortFloatingPointSequence dataMVSFPSeq = (MeasuredValueShortFloatingPointSequence) msg;
            int addressMVSFPSeq = dataMVSFPSeq.getStartAddress().getAddress();
            for (Value<Float> v : dataMVSFPSeq.getValues())
                valuesParsed.put(addressMVSFPSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseMeasuredValueFloatingPointTime(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("MeasuredValueFloatingPoint single with seven octets time received.");
            MeasuredValueShortFloatingPointTimeSingle dataMVSFPTS = (MeasuredValueShortFloatingPointTimeSingle) msg;
            dataMVSFPTS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("MeasuredValueFloatingPoint sequence with seven octets time received.");
            MeasuredValueShortFloatingPointTimeSequence dataMVSFPTSeq = (MeasuredValueShortFloatingPointTimeSequence) msg;
            int addressMVSFPTSeq = dataMVSFPTSeq.getStartAddress().getAddress();
            for (Value<Float> v : dataMVSFPTSeq.getValues())
                valuesParsed.put(addressMVSFPTSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseMeasuredValueScaled(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("MeasuredValueScaled single received.");
            MeasuredValueScaledSingle dataMVSS = (MeasuredValueScaledSingle) msg;
            dataMVSS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("MeasuredValueScaled sequence received.");
            MeasuredValueScaledSequence dataMVSSeq = (MeasuredValueScaledSequence) msg;
            int addressMVSSeq = dataMVSSeq.getStartAddress().getAddress();
            for (Value<Short> v : dataMVSSeq.getValues())
                valuesParsed.put(addressMVSSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseMeasuredValueScaledTime(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("MeasuredValueScaled single with seven octets time received.");
            MeasuredValueScaledTimeSingle dataMVSTS = (MeasuredValueScaledTimeSingle) msg;
            dataMVSTS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("MeasuredValueScaled sequence with seven octets time received.");
            MeasuredValueScaledSequence dataMVSTSeq = (MeasuredValueScaledSequence) msg;
            int addressMVSTSeq = dataMVSTSeq.getStartAddress().getAddress();
            for (Value<Short> v : dataMVSTSeq.getValues())
                valuesParsed.put(addressMVSTSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseSinglePointInformation(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("SinglePointInformation single received.");
            SinglePointInformationSingle dataSPIS = (SinglePointInformationSingle) msg;
            dataSPIS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("SinglePointInformation sequence received.");
            SinglePointInformationSequence dataSPISeq = (SinglePointInformationSequence) msg;
            int addressSPISeq = dataSPISeq.getStartAddress().getAddress();
            for (Value<Boolean> v : dataSPISeq.getValues())
                valuesParsed.put(addressSPISeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseSinglePointInformationTime(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("SinglePointInformation single with seven octets time received.");
            SinglePointInformationTimeSingle dataSPITS = (SinglePointInformationTimeSingle) msg;
            dataSPITS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("SinglePointInformation sequence with seven octets time received.");
            SinglePointInformationTimeSequence dataSPITSeq = (SinglePointInformationTimeSequence) msg;
            int addressSPITSeq = dataSPITSeq.getStartAddress().getAddress();
            for (Value<Boolean> v : dataSPITSeq.getValues())
                valuesParsed.put(addressSPITSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseBitstring(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("Bitstring single received.");
            BitStringPointInformationSingle dataBSS = (BitStringPointInformationSingle) msg;
            dataBSS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("Bitstring sequence received.");
            BitStringPointInformationSequence dataBSSeq = (BitStringPointInformationSequence) msg;
            int addressBSSeq = dataBSSeq.getStartAddress().getAddress();
            for (Value<Long> v : dataBSSeq.getValues())
                valuesParsed.put(addressBSSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseBitstringTime(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("Bitstring single with seven octets time received.");
            BitStringPointInformationTimeSingle dataBSTS = (BitStringPointInformationTimeSingle) msg;
            dataBSTS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("Bitstring sequence with seven octets time received.");
            BitStringPointInformationTimeSequence dataBSTSeq = (BitStringPointInformationTimeSequence) msg;
            int addressBSTSeq = dataBSTSeq.getStartAddress().getAddress();
            for (Value<Long> v : dataBSTSeq.getValues())
                valuesParsed.put(addressBSTSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseStepPosition(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("Step position single received.");
            StepPositionSingle dataSPS = (StepPositionSingle) msg;
            dataSPS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("Step position sequence received.");
            StepPositionSequence dataSPSeq = (StepPositionSequence) msg;
            int addressSPSeq = dataSPSeq.getStartAddress().getAddress();
            for (Value<Byte> v : dataSPSeq.getValues())
                valuesParsed.put(addressSPSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseStepPositionTime(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("Step position single with seven octets time received.");
            StepPositionTimeSingle dataSPTS = (StepPositionTimeSingle) msg;
            dataSPTS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("Step position sequence with seven octets time received.");
            StepPositionTimeSequence dataSPTSeq = (StepPositionTimeSequence) msg;
            int addressSPTSeq = dataSPTSeq.getStartAddress().getAddress();
            for (Value<Byte> v : dataSPTSeq.getValues())
                valuesParsed.put(addressSPTSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseMeasureValueNormalized(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("MeasuredValueNormalized single received.");
            MeasuredValueNormalizedSingle dataMVNS = (MeasuredValueNormalizedSingle) msg;
            dataMVNS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("MeasuredValueNormalized sequence received.");
            MeasuredValueNormalizedSequence dataMVNSeq = (MeasuredValueNormalizedSequence) msg;
            int addressMVNSeq = dataMVNSeq.getStartAddress().getAddress();
            for (Value<Short> v : dataMVNSeq.getValues())
                valuesParsed.put(addressMVNSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseMeasureValueNormalizedTime(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("MeasuredValueNormalized single with seven octets time received.");
            MeasuredValueNormalizedTimeSingle dataMVNTS = (MeasuredValueNormalizedTimeSingle) msg;
            dataMVNTS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("MeasuredValueNormalized sequence with seven octets time received.");
            MeasuredValueNormalizedTimeSequence dataMVNTSeq = (MeasuredValueNormalizedTimeSequence) msg;
            int addressMVNTSeq = dataMVNTSeq.getStartAddress().getAddress();
            for (Value<Short> v : dataMVNTSeq.getValues())
                valuesParsed.put(addressMVNTSeq++, v);
        }
        return valuesParsed;
    }

    private Map<Integer, Value<?>> parseMeasureValueNormalizedNoQuality(InformationStructure msgInfoStruct, final Object msg) {
        Map<Integer, Value<?>> valuesParsed = new HashMap<>();

        if (msgInfoStruct.equals(InformationStructure.SINGLE)) {
            LOGGER.debug("MeasuredValueNormalized NoQuality single received.");
            MeasuredValueNormalizedNoQualitySingle dataMVNNQS = (MeasuredValueNormalizedNoQualitySingle) msg;
            dataMVNNQS.getEntries().forEach(e -> valuesParsed.put(e.getAddress().getAddress(), e.getValue()));
        } else if (msgInfoStruct.equals(InformationStructure.SEQUENCE)) {
            LOGGER.debug("MeasuredValueNormalized NoQuality sequence received.");
            MeasuredValueNormalizedNoQualitySequence dataMVNNQSeq = (MeasuredValueNormalizedNoQualitySequence) msg;
            int addressMVNNQSeq = dataMVNNQSeq.getStartAddress().getAddress();
            for (Value<Short> v : dataMVNNQSeq.getValues())
                valuesParsed.put(addressMVNNQSeq++, v);
        }
        return valuesParsed;
    }

    private String translateASDU(String type) {
        switch (type) {
            case "C_IC_NA_1":
                return "(Interrogation Command)";
            case "M_DP_TB_1":
                return "(DoublePointInformation with seven octets time)";
            case "M_DP_NA_1":
                return "(DoublePointInformation)";
            case "M_ME_NC_1":
                return "(MeasuredValueFloatingPoint)";
            case "M_ME_TF_1":
                return "(MeasuredValueFloatingPoint with seven octets time)";
            case "M_ME_NB_1":
                return "(MeasuredValueScaled)";
            case "M_ME_TE_1":
                return "(MeasuredValueScaled with seven octets time)";
            case "M_SP_NA_1":
                return "(SinglePointInformation)";
            case "M_SP_TB_1":
                return "(SinglePointInformation with seven octets time)";
            case "M_BO_NA_1":
                return "(Bitstring)";
            case "M_BO_TB_1":
                return "(Bitstring with seven octets time)";
            case "M_ST_NA_1":
                return "(StepPosition)";
            case "M_ST_TB_1":
                return "(StepPositionTime with seven octets time)";
            case "M_ME_NA_1":
                return "(MeasuredValueNormalized)";
            case "M_ME_TD_1":
                return "(MeasuredValueNormalized with seven octets time)";
            case "M_ME_ND_1":
                return "(MeasuredValueNormalizedNoQuality)";
            default:
                return "()";
        }
    }

    private String translateCauseOfTransmission(Short cause) {
        switch (cause) {
            case 1:
                return "Periodic";
            case 3:
                return "Spontaneous";
            case 7:
                return "Confirmation activation";
            case 20:
                return "Interrogation command";
            default:
                return "";
        }
    }

}
