package es.amplia.oda.connector.iec104.codecs;

import es.amplia.oda.connector.iec104.types.BytestringPointInformationSequence;
import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageCodec;

public class BytestringPointSequenceCodec implements MessageCodec {
	@Override
	public Object parse(ProtocolOptions protocolOptions, byte b, ASDUHeader asduHeader, ByteBuf byteBuf) {
		return BytestringPointInformationSequence.parse(protocolOptions, b, asduHeader, byteBuf);
	}

	@Override
	public void encode(ProtocolOptions protocolOptions, Object o, ByteBuf byteBuf) {
		((BytestringPointInformationSequence) o).encode(protocolOptions, byteBuf);
	}
}
