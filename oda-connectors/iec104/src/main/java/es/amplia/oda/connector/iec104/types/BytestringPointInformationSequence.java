package es.amplia.oda.connector.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.EncodeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ASDU(id = 7, name = "M_BO_NA_1", informationStructure = InformationStructure.SEQUENCE)
public class BytestringPointInformationSequence extends AbstractMessage {
	private final InformationObjectAddress startAddress;

	private final List<Value<byte[]>> values;

	private BytestringPointInformationSequence(final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<byte[]>> values) {
		super(header);
		this.startAddress = startAddress;
		this.values = values;
	}

	public InformationObjectAddress getStartAddress() {
		return this.startAddress;
	}

	public List<Value<byte[]>> getValues() {
		return this.values;
	}

	public static BytestringPointInformationSequence parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data) {
		final InformationObjectAddress startAddress = InformationObjectAddress.parse ( options, data );

		final List<Value<byte[]>> values = new ArrayList<> ( length );
		for ( int i = 0; i < length; i++ ) {
			values.add ( TypeHelperExt.parseBytestringValue ( options, data, false ) );
		}

		return new BytestringPointInformationSequence( header, startAddress, values );
	}

	@Override
	public void encode(ProtocolOptions protocolOptions, ByteBuf byteBuf) {
		EncodeHelper.encodeHeader ( this, protocolOptions, this.values.size (), this.header, byteBuf );

		this.startAddress.encode ( protocolOptions, byteBuf );

		for ( final Value<byte[]> value : this.values ) {
			TypeHelperExt.encodeBytestringValue ( protocolOptions, byteBuf, value, false );
		}
	}

	public static BytestringPointInformationSequence create (final ASDUHeader header, final InformationObjectAddress address, final Value<byte[]> value) {
		return new BytestringPointInformationSequence(header, address, Collections.singletonList(value));
	}

	public static BytestringPointInformationSequence create (final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<byte[]>> values) {
		if ( values.size () > MAX_INFORMATION_ENTRIES ) {
			throw new IllegalArgumentException( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
		}
		return createInternal (header, startAddress, values);
	}

	private static BytestringPointInformationSequence createInternal (final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<byte[]>> values) {
		return new BytestringPointInformationSequence(header, startAddress, values);
	}
}
