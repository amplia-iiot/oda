package es.amplia.oda.connector.iec104.types;

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

public abstract class AbstractBytestringPointInformation extends AbstractMessage {

	private final List<InformationEntry<byte[]>> entries;

	private final boolean withTimestamp;

	AbstractBytestringPointInformation(ASDUHeader header, List<InformationEntry<byte[]>> entries, boolean withTimestamp) {
		super(header);
		this.entries = entries;
		this.withTimestamp = withTimestamp;
	}

	@Override
	public void encode(ProtocolOptions protocolOptions, ByteBuf byteBuf) {
		EncodeHelper.encodeHeader(this, protocolOptions, this.entries.size(), this.header, byteBuf);

		for (InformationEntry<byte[]> entry : this.entries) {
			entry.getAddress().encode(protocolOptions, byteBuf);
			TypeHelperExt.encodeBytestringValue(protocolOptions, byteBuf, entry.getValue(), this.withTimestamp);
		}
	}

	static List<InformationEntry<byte[]>> parseEntries (final ProtocolOptions options, final byte length, final ByteBuf data, final boolean withTimestamp) {

		final List<InformationEntry<byte[]>> values = new ArrayList<>(length);

		for (int i = 0; i < length; i++) {
			final InformationObjectAddress address = InformationObjectAddress.parse(options, data);
			final Value<byte[]> value = TypeHelperExt.parseBytestringValue(options, data, withTimestamp);
			values.add(new InformationEntry<>(address, value));
		}
		return values;
	}

	public List<InformationEntry<byte[]>> getEntries() {
		return this.entries;
	}
}