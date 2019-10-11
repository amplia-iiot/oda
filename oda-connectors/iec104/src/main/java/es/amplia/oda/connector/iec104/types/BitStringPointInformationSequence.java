package es.amplia.oda.connector.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.EncodeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.List;

@ASDU(id = 7, name = "M_BO_NA_1", informationStructure = InformationStructure.SEQUENCE)
public class BitStringPointInformationSequence extends AbstractMessage {
	private final InformationObjectAddress startAddress;

	private final List<Value<byte[]>> values;

	private BitStringPointInformationSequence(final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<byte[]>> values) {
		super(header);
		this.startAddress = startAddress;
		this.values = values;
	}

	public static BitStringPointInformationSequence parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data) {
		final InformationObjectAddress startAddress = InformationObjectAddress.parse ( options, data );

		final List<Value<byte[]>> values = new ArrayList<> ( length );
		for ( int i = 0; i < length; i++ ) {
			values.add ( TypeHelperExt.parseBitStringValue( options, data, false ) );
		}

		return new BitStringPointInformationSequence( header, startAddress, values );
	}

	@Override
	public void encode(ProtocolOptions protocolOptions, ByteBuf byteBuf) {
		EncodeHelper.encodeHeader ( this, protocolOptions, this.values.size (), this.header, byteBuf );

		this.startAddress.encode ( protocolOptions, byteBuf );

		for ( final Value<byte[]> value : this.values ) {
			TypeHelperExt.encodeBitStringValue( protocolOptions, byteBuf, value, false );
		}
	}

	public static BitStringPointInformationSequence create (final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<byte[]>> values) {
		if ( values.size () > MAX_INFORMATION_ENTRIES ) {
			throw new IllegalArgumentException( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
		}
		return createInternal (header, startAddress, values);
	}

	private static BitStringPointInformationSequence createInternal (final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<byte[]>> values) {
		return new BitStringPointInformationSequence(header, startAddress, values);
	}
}
