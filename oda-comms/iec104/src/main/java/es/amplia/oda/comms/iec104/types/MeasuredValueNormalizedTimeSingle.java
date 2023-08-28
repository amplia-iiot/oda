package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractMeasuredValueScaled;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ASDU ( id = 34, name = "M_ME_TD_1", informationStructure = InformationStructure.SINGLE )
public class MeasuredValueNormalizedTimeSingle extends AbstractMeasuredValueScaled
{
    private MeasuredValueNormalizedTimeSingle(final ASDUHeader header, final List<InformationEntry<Short>> entries )
    {
        super ( header, entries, true );
    }

    public static MeasuredValueNormalizedTimeSingle parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        return new MeasuredValueNormalizedTimeSingle( header, parseEntries ( options, length, data, true ) );
    }

    public static MeasuredValueNormalizedTimeSingle create (final ASDUHeader header, final InformationObjectAddress address, final Value<Short> value )
    {
        return createInternal ( header, Collections.singletonList ( new InformationEntry<> ( address, value ) ) );
    }

    public static MeasuredValueNormalizedTimeSingle create (final ASDUHeader header, final List<InformationEntry<Short>> values )
    {
        if ( values.size () > MAX_INFORMATION_ENTRIES )
        {
            throw new IllegalArgumentException ( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
        }
        return createInternal ( header, new ArrayList<> ( values ) );
    }

    private static MeasuredValueNormalizedTimeSingle createInternal (final ASDUHeader header, final List<InformationEntry<Short>> values )
    {
        return new MeasuredValueNormalizedTimeSingle( header, values );
    }

}
