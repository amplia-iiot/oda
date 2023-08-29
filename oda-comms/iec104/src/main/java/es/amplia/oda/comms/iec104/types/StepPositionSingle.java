package es.amplia.oda.comms.iec104.types;


import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ASDU( id = 5, name = "M_ST_NA_1", informationStructure = InformationStructure.SINGLE )
public class StepPositionSingle extends AbstractStepPositionSingle {

    private StepPositionSingle ( final ASDUHeader header, final List<InformationEntry<Byte>> entries )
    {
        super ( header, entries, false );
    }

    public static StepPositionSingle parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        return new StepPositionSingle ( header, parseEntries ( options, length, data, false ) );
    }

    public static StepPositionSingle create (final ASDUHeader header, final InformationObjectAddress address, final Value<Byte> value )
    {
        return createInternal ( header, Collections.singletonList ( new InformationEntry<> ( address, value ) ) );
    }

    public static StepPositionSingle create (final ASDUHeader header, final List<InformationEntry<Byte>> values )
    {
        if ( values.size () > MAX_INFORMATION_ENTRIES )
        {
            throw new IllegalArgumentException ( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
        }
        return createInternal ( header, new ArrayList<>( values ) );
    }

    private static StepPositionSingle createInternal (final ASDUHeader header, final List<InformationEntry<Byte>> values )
    {
        return new StepPositionSingle( header, values );
    }
}
