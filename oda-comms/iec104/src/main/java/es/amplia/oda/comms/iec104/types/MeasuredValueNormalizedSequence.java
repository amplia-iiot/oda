package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.EncodeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ASDU ( id = 9, name = "M_ME_NA_1", informationStructure = InformationStructure.SEQUENCE )
public class MeasuredValueNormalizedSequence extends AbstractMessage
{
    private final InformationObjectAddress startAddress;

    private final List<Value<Short>> values;

    private MeasuredValueNormalizedSequence(final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<Short>> values )
    {
        super ( header );
        this.startAddress = startAddress;
        this.values = values;
    }

    public List<Value<Short>> getValues ()
    {
        return this.values;
    }

    public InformationObjectAddress getStartAddress ()
    {
        return this.startAddress;
    }

    public static MeasuredValueNormalizedSequence parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        final InformationObjectAddress startAddress = InformationObjectAddress.parse ( options, data );

        final List<Value<Short>> values = new ArrayList<> ( length );
        for ( int i = 0; i < length; i++ )
        {
            values.add ( TypeHelper.parseScaledValue ( options, data, false ) );
        }

        return new MeasuredValueNormalizedSequence( header, startAddress, values );
    }

    @Override
    public void encode ( final ProtocolOptions options, final ByteBuf out )
    {
        EncodeHelper.encodeHeader ( this, options, this.values.size (), this.header, out );

        this.startAddress.encode ( options, out );

        for ( final Value<Short> value : this.values )
        {
            TypeHelper.encodeScaledValue ( options, out, value, false );
        }
    }

    public static MeasuredValueNormalizedSequence create (final InformationObjectAddress startAddress, final ASDUHeader header, final Value<Short> value )
    {
        return createInternal ( startAddress, header, Collections.singletonList ( value ) );
    }

    public static MeasuredValueNormalizedSequence create (final InformationObjectAddress startAddress, final ASDUHeader header, final List<Value<Short>> values )
    {
        if ( values.size () > MAX_INFORMATION_ENTRIES )
        {
            throw new IllegalArgumentException ( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
        }
        return createInternal ( startAddress, header, new ArrayList<> ( values ) );
    }

    private static MeasuredValueNormalizedSequence createInternal (final InformationObjectAddress startAddress, final ASDUHeader header, final List<Value<Short>> values )
    {
        return new MeasuredValueNormalizedSequence( header, startAddress, values );
    }

}
