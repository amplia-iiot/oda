package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.EncodeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.List;

public class AbstractStepPositionSingle extends AbstractMessage {

    private final List<InformationEntry<Byte>> entries;

    private final boolean withTimestamp;

    public AbstractStepPositionSingle (final ASDUHeader header, final List<InformationEntry<Byte>> entries, final boolean withTimestamp )
    {
        super ( header );
        this.withTimestamp = withTimestamp;
        this.entries = entries;
    }

    public List<InformationEntry<Byte>> getEntries ()
    {
        return this.entries;
    }

    @Override
    public void encode (final ProtocolOptions options, final ByteBuf out )
    {
        EncodeHelper.encodeHeader ( this, options, this.entries.size (), this.header, out );

        for ( final InformationEntry<Byte> entry : this.entries )
        {
            entry.getAddress ().encode ( options, out );
            TypeHelperExt.encodeByteValue( options, out, entry.getValue (), this.withTimestamp );
        }
    }

    protected static List<InformationEntry<Byte>> parseEntries ( final ProtocolOptions options, final byte length, final ByteBuf data, final boolean withTimestamp )
    {
        final List<InformationEntry<Byte>> values = new ArrayList<>( length );
        for ( int i = 0; i < length; i++ )
        {
            final InformationObjectAddress address = InformationObjectAddress.parse ( options, data );
            final Value<Byte> value = TypeHelperExt.parseByteValue( options, data, withTimestamp );
            values.add ( new InformationEntry<> ( address, value ) );
        }
        return values;
    }
}
