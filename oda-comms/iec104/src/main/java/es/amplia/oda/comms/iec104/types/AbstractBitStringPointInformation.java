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

	private final boolean withTimestamp;

	private final List<InformationEntry<Long>> entries;


	AbstractBitStringPointInformation(ASDUHeader header, List<InformationEntry<Long>> entries, final boolean withTimestamp ) {
		super(header);
		this.entries = entries;
		this.withTimestamp = withTimestamp;
	}

	public List<InformationEntry<Long>> getEntries() {
		return this.entries;
	}

	@Override
	public void encode(ProtocolOptions protocolOptions, ByteBuf byteBuf) {
		EncodeHelper.encodeHeader(this, protocolOptions, this.entries.size(), this.header, byteBuf);

		for (InformationEntry<Long> entry : this.entries) {
			entry.getAddress().encode(protocolOptions, byteBuf);
			TypeHelperExt.encodeBitStringValue(protocolOptions, byteBuf, entry.getValue(), this.withTimestamp);
		}
	}

	static List<InformationEntry<Long>> parseEntries (ProtocolOptions options, byte length, ByteBuf data, final boolean withTimestamp ) {
		final List<InformationEntry<Long>> values = new ArrayList<>(length);

		for (int i = 0; i < length; i++) {
			final InformationObjectAddress address = InformationObjectAddress.parse(options, data);
			final Value<Long> value = TypeHelperExt.parseBitStringValue(options, data, withTimestamp);
			values.add(new InformationEntry<>(address, value));
		}
		return values;
	}
}