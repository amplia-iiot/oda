package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.Collections;
import java.util.List;

@ASDU (id = 7, name = "M_BO_NA_1", informationStructure = InformationStructure.SINGLE)
public class BitStringPointInformationSingle extends AbstractBitStringPointInformation {

	private BitStringPointInformationSingle(ASDUHeader header, List<InformationEntry<byte[]>> entries) {
		super(header, entries, false);
	}

	public static BitStringPointInformationSingle parse (final ProtocolOptions options, final byte length, ASDUHeader header, final ByteBuf data) {
		return new BitStringPointInformationSingle(header, parseEntries(options, length, data, false));
	}

	public static BitStringPointInformationSingle create (final ASDUHeader header, final InformationObjectAddress address, final Value<byte[]> value) {
		return new BitStringPointInformationSingle(header, Collections.singletonList(new InformationEntry<>(address, value)));
	}
}
