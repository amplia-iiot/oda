package es.amplia.oda.comms.iec104.codecs;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageCodec;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DoublePointInformationSequence;

public class DoublePointInformationSequenceCodec implements MessageCodec {

	@Override
	public Object parse(ProtocolOptions protocolOptions, byte b, ASDUHeader asduHeader, ByteBuf byteBuf) {
		return DoublePointInformationSequence.parse(protocolOptions, b, asduHeader, byteBuf);
	}

	@Override
	public void encode(ProtocolOptions protocolOptions, Object o, ByteBuf byteBuf) {
		((DoublePointInformationSequence) o).encode(protocolOptions, byteBuf);
	}
}
