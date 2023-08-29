package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.EncodeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.InformationStructure;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.Value;

import java.util.ArrayList;
import java.util.List;

@ASDU (id = 33, name = "M_BO_TB_1", informationStructure = InformationStructure.SEQUENCE)
public class BitStringPointInformationTimeSequence extends AbstractMessage {
	private final InformationObjectAddress startAddress;

	private final List<Value<Long>> values;

	private BitStringPointInformationTimeSequence(final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<Long>> values) {
		super(header);
		this.startAddress = startAddress;
		this.values = values;
	}

	public InformationObjectAddress getStartAddress() {
		return this.startAddress;
	}

	public List<Value<Long>> getValues() {
		return this.values;
	}

	public static BitStringPointInformationTimeSequence parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data) {
		final InformationObjectAddress startAddress = InformationObjectAddress.parse ( options, data );

		final List<Value<Long>> values = new ArrayList<> ( length );
		for ( int i = 0; i < length; i++ ) {
			values.add ( TypeHelperExt.parseBitStringValue( options, data, true ) );
		}

		return new BitStringPointInformationTimeSequence( header, startAddress, values );
	}

	@Override
	public void encode(ProtocolOptions protocolOptions, ByteBuf byteBuf) {
		EncodeHelper.encodeHeader ( this, protocolOptions, this.values.size (), this.header, byteBuf );

		this.startAddress.encode ( protocolOptions, byteBuf );

		for ( final Value<Long> value : this.values ) {
			TypeHelperExt.encodeBitStringValue( protocolOptions, byteBuf, value, true );
		}
	}

	public static BitStringPointInformationTimeSequence create (final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<Long>> values) {
		if ( values.size () > MAX_INFORMATION_ENTRIES ) {
			throw new IllegalArgumentException( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
		}
		return createInternal (header, startAddress, values);
	}

	private static BitStringPointInformationTimeSequence createInternal (final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<Long>> values) {
		return new BitStringPointInformationTimeSequence(header, startAddress, values);
	}
}
