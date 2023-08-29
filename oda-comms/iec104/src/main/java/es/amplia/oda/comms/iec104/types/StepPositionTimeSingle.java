package es.amplia.oda.comms.iec104.types;


import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ASDU( id = 32, name = "M_ST_TB_1", informationStructure = InformationStructure.SINGLE )
public class StepPositionTimeSingle extends AbstractStepPositionSingle {

    private StepPositionTimeSingle(final ASDUHeader header, final List<InformationEntry<Byte>> entries )
    {
        super ( header, entries, true );
    }

    public static StepPositionTimeSingle parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        return new StepPositionTimeSingle( header, parseEntries ( options, length, data, true ) );
    }

    public static StepPositionTimeSingle create (final ASDUHeader header, final InformationObjectAddress address, final Value<Byte> value )
    {
        return createInternal ( header, Collections.singletonList ( new InformationEntry<> ( address, value )));
    }

    public static StepPositionTimeSingle create (final ASDUHeader header, final List<InformationEntry<Byte>> values )
    {
        if ( values.size () > MAX_INFORMATION_ENTRIES )
        {
            throw new IllegalArgumentException ( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
        }
        return createInternal ( header, new ArrayList<>( values ) );
    }

    private static StepPositionTimeSingle createInternal (final ASDUHeader header, final List<InformationEntry<Byte>> values )
    {
        return new StepPositionTimeSingle( header, values );
    }
}
