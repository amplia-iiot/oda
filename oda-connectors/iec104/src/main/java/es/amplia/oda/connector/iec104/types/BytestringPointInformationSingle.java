package es.amplia.oda.connector.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ASDU (id = 7, name = "M_BO_NA_1", informationStructure = InformationStructure.SINGLE)
public class BytestringPointInformationSingle extends AbstractBytestringPointInformation{

	private BytestringPointInformationSingle(ASDUHeader header, List<InformationEntry<byte[]>> entries) {
		super(header, entries, false);
	}

	public static BytestringPointInformationSingle parse (final ProtocolOptions options, final byte length, ASDUHeader header, final ByteBuf data) {
		return new BytestringPointInformationSingle(header, parseEntries(options, length, data, false));
	}

	public static BytestringPointInformationSingle create (final ASDUHeader header, final InformationObjectAddress address, final Value<byte[]> value) {
		return new BytestringPointInformationSingle(header, Collections.singletonList(new InformationEntry<>(address, value)));
	}

	public static BytestringPointInformationSingle create (final ASDUHeader header, final List<InformationEntry<byte[]>> value) {
		if(value.size() > MAX_INFORMATION_ENTRIES) {
			throw new IllegalArgumentException(String.format("A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES));
		}
		return new BytestringPointInformationSingle(header, new ArrayList<>(value));
	}

	private static BytestringPointInformationSingle createInternal (final ASDUHeader header, final List<InformationEntry<byte[]>> value) {
		return new BytestringPointInformationSingle(header, value);
	}
}
