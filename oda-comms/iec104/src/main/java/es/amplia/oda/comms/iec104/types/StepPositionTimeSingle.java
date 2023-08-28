package es.amplia.oda.comms.iec104.types;


import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.InformationEntry;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.InformationStructure;

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
}
