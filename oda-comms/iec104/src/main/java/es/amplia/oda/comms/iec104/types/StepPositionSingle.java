package es.amplia.oda.comms.iec104.types;


import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.InformationEntry;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.InformationStructure;

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
}
