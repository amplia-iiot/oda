package es.amplia.oda.comms.iec104.codecs;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageCodec;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MeasuredValueScaledSingle;

public class MeasuredValueScaledSingleCodec implements MessageCodec {

	@Override
	public Object parse(ProtocolOptions protocolOptions, byte b, ASDUHeader asduHeader, ByteBuf byteBuf) {
		return MeasuredValueScaledSingle.parse(protocolOptions, b, asduHeader, byteBuf);
	}

	@Override
	public void encode(ProtocolOptions protocolOptions, Object o, ByteBuf byteBuf) {
		((MeasuredValueScaledSingle) o).encode(protocolOptions, byteBuf);
	}
}
