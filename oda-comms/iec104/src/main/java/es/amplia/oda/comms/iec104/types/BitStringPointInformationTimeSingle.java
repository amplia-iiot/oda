package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.Collections;
import java.util.List;

@ASDU (id = 33, name = "M_BO_TB_1", informationStructure = InformationStructure.SINGLE)
public class BitStringPointInformationTimeSingle extends AbstractBitStringPointInformation {

	private BitStringPointInformationTimeSingle(ASDUHeader header, List<InformationEntry<byte[]>> entries) {
		super(header, entries, true);
	}

	public static BitStringPointInformationTimeSingle parse (final ProtocolOptions options, final byte length, ASDUHeader header, final ByteBuf data) {
		return new BitStringPointInformationTimeSingle(header, parseEntries(options, length, data, true));
	}

	public static BitStringPointInformationTimeSingle create (final ASDUHeader header, final InformationObjectAddress address, final Value<byte[]> value) {
		return new BitStringPointInformationTimeSingle(header, Collections.singletonList(new InformationEntry<>(address, value)));
	}
}
