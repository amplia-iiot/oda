package es.amplia.oda.comms.iec104.codecs;

import es.amplia.oda.comms.iec104.types.StepPositionSequence;
import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageCodec;

public class StepPositionSequenceCodec implements MessageCodec {

    @Override
    public Object parse(ProtocolOptions protocolOptions, byte b, ASDUHeader asduHeader, ByteBuf byteBuf) {
        return StepPositionSequence.parse(protocolOptions, b, asduHeader, byteBuf);
    }

    @Override
    public void encode(ProtocolOptions protocolOptions, Object o, ByteBuf byteBuf) {
        ((StepPositionSequence) o).encode(protocolOptions, byteBuf);
    }
}
