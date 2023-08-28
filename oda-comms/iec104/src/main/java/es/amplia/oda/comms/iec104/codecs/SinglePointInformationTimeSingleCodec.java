package es.amplia.oda.comms.iec104.codecs;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageCodec;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SinglePointInformationTimeSingle;

public class SinglePointInformationTimeSingleCodec implements MessageCodec {

	@Override
	public Object parse(ProtocolOptions protocolOptions, byte b, ASDUHeader asduHeader, ByteBuf byteBuf) {
		return SinglePointInformationTimeSingle.parse(protocolOptions, b, asduHeader, byteBuf);
	}

	@Override
	public void encode(ProtocolOptions protocolOptions, Object o, ByteBuf byteBuf) {
		((SinglePointInformationTimeSingle) o).encode(protocolOptions, byteBuf);
	}
}
