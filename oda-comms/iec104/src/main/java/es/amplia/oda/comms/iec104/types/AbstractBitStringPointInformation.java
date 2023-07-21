package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.EncodeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.InformationEntry;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.Value;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBitStringPointInformation extends AbstractMessage {

	private static final boolean WITH_TIMESTAMP = false;


	private final List<InformationEntry<byte[]>> entries;


	AbstractBitStringPointInformation(ASDUHeader header, List<InformationEntry<byte[]>> entries) {
		super(header);
		this.entries = entries;
	}

	public List<InformationEntry<byte[]>> getEntries ()
	{
		return this.entries;
	}

	@Override
	public void encode(ProtocolOptions protocolOptions, ByteBuf byteBuf) {
		EncodeHelper.encodeHeader(this, protocolOptions, this.entries.size(), this.header, byteBuf);

		for (InformationEntry<byte[]> entry : this.entries) {
			entry.getAddress().encode(protocolOptions, byteBuf);
			TypeHelperExt.encodeBitStringValue(protocolOptions, byteBuf, entry.getValue(), WITH_TIMESTAMP);
		}
	}

	static List<InformationEntry<byte[]>> parseEntries (ProtocolOptions options, byte length, ByteBuf data) {
		final List<InformationEntry<byte[]>> values = new ArrayList<>(length);

		for (int i = 0; i < length; i++) {
			final InformationObjectAddress address = InformationObjectAddress.parse(options, data);
			final Value<byte[]> value = TypeHelperExt.parseBitStringValue(options, data, WITH_TIMESTAMP);
			values.add(new InformationEntry<>(address, value));
		}
		return values;
	}
}