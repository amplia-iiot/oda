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

@ASDU( id = 30, name = "M_SP_TB_1", informationStructure = InformationStructure.SEQUENCE )
public class SinglePointInformationTimeSequence extends AbstractMessage {

    private final InformationObjectAddress startAddress;

    private final List<Value<Boolean>> values;

    private SinglePointInformationTimeSequence(final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<Boolean>> values ) {
        super ( header );
        this.startAddress = startAddress;
        this.values = values;
    }

    public List<Value<Boolean>> getValues ()
    {
        return this.values;
    }

    public InformationObjectAddress getStartAddress ()
    {
        return this.startAddress;
    }

    public static SinglePointInformationTimeSequence parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        final InformationObjectAddress startAddress = InformationObjectAddress.parse ( options, data );

        final List<Value<Boolean>> values = new ArrayList<> ( length );
        for ( int i = 0; i < length; i++ )
        {
            values.add ( TypeHelper.parseBooleanValue( options, data, true ) );
        }

        return new SinglePointInformationTimeSequence( header, startAddress, values );
    }

    @Override
    public void encode ( final ProtocolOptions options, final ByteBuf out )
    {
        EncodeHelper.encodeHeader ( this, options, this.values.size (), this.header, out );

        this.startAddress.encode ( options, out );

        for ( final Value<Boolean> value : this.values )
        {
            TypeHelper.encodeBooleanValue( options, out, value, true );
        }
    }

    public static SinglePointInformationTimeSequence create (final InformationObjectAddress startAddress, final ASDUHeader header, final Value<Boolean> value )
    {
        return createInternal ( startAddress, header, Collections.singletonList ( value ) );
    }

    public static SinglePointInformationTimeSequence create (final InformationObjectAddress startAddress, final ASDUHeader header, final List<Value<Boolean>> values )
    {
        if ( values.size () > MAX_INFORMATION_ENTRIES )
        {
            throw new IllegalArgumentException ( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
        }
        return createInternal ( startAddress, header, new ArrayList<> ( values ) );
    }

    private static SinglePointInformationTimeSequence createInternal (final InformationObjectAddress startAddress, final ASDUHeader header, final List<Value<Boolean>> values )
    {
        return new SinglePointInformationTimeSequence( header, startAddress, values );
    }

}
