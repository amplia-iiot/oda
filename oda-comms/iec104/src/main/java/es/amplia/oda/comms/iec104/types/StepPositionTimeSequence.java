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

@ASDU( id = 32, name = "M_ST_TB_1", informationStructure = InformationStructure.SEQUENCE )
public class StepPositionTimeSequence extends AbstractMessage {

    private final InformationObjectAddress startAddress;

    private final List<Value<Byte>> values;

    private StepPositionTimeSequence(final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<Byte>> values )
    {
        super ( header );
        this.startAddress = startAddress;
        this.values = values;
    }

    public List<Value<Byte>> getValues ()
    {
        return this.values;
    }

    public InformationObjectAddress getStartAddress ()
    {
        return this.startAddress;
    }

    public static StepPositionTimeSequence parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        final InformationObjectAddress startAddress = InformationObjectAddress.parse ( options, data );

        final List<Value<Byte>> values = new ArrayList<> ( length );
        for ( int i = 0; i < length; i++ )
        {
            values.add ( TypeHelperExt.parseByteValue( options, data, true ) );
        }

        return new StepPositionTimeSequence( header, startAddress, values );
    }

    @Override
    public void encode ( final ProtocolOptions options, final ByteBuf out )
    {
        EncodeHelper.encodeHeader ( this, options, this.values.size (), this.header, out );

        this.startAddress.encode ( options, out );

        for ( final Value<Byte> value : this.values )
        {
            TypeHelperExt.encodeByteValue( options, out, value, true );
        }
    }
}
