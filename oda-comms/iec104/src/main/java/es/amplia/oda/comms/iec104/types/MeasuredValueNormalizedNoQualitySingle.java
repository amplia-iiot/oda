package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractMeasuredValueScaled;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ASDU ( id = 21, name = "M_ME_ND_1", informationStructure = InformationStructure.SINGLE )
public class MeasuredValueNormalizedNoQualitySingle extends AbstractMeasuredValueScaled
{
    private MeasuredValueNormalizedNoQualitySingle ( final ASDUHeader header, final List<InformationEntry<Short>> entries )
    {
        super ( header, entries, false );
    }

    public static MeasuredValueNormalizedNoQualitySingle parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        return new MeasuredValueNormalizedNoQualitySingle ( header, parseEntries ( options, length, data, false ) );
    }

    public static MeasuredValueNormalizedNoQualitySingle create ( final ASDUHeader header, final InformationObjectAddress address, final Value<Short> value )
    {
        return createInternal ( header, Collections.singletonList ( new InformationEntry<> ( address, value ) ) );
    }

    public static MeasuredValueNormalizedNoQualitySingle create ( final ASDUHeader header, final List<InformationEntry<Short>> values )
    {
        if ( values.size () > MAX_INFORMATION_ENTRIES )
        {
            throw new IllegalArgumentException ( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
        }
        return createInternal ( header, new ArrayList<> ( values ) );
    }

    private static MeasuredValueNormalizedNoQualitySingle createInternal ( final ASDUHeader header, final List<InformationEntry<Short>> values )
    {
        return new MeasuredValueNormalizedNoQualitySingle ( header, values );
    }

}
