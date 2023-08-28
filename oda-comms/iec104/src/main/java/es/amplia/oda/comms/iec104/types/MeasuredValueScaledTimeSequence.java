/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractMeasuredValueScaled;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ASDU ( id = 35, name = "M_ME_TE_1", informationStructure = InformationStructure.SEQUENCE )
public class MeasuredValueScaledTimeSequence extends AbstractMeasuredValueScaled
{
    private MeasuredValueScaledTimeSequence(final ASDUHeader header, final List<InformationEntry<Short>> entries )
    {
        super ( header, entries, true );
    }

    public static MeasuredValueScaledTimeSequence parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        return new MeasuredValueScaledTimeSequence( header, parseEntries ( options, length, data, true ) );
    }

    public static MeasuredValueScaledTimeSequence create (final ASDUHeader header, final InformationObjectAddress address, final Value<Short> value )
    {
        return createInternal ( header, Collections.singletonList ( new InformationEntry<> ( address, value ) ) );
    }

    public static MeasuredValueScaledTimeSequence create (final ASDUHeader header, final List<InformationEntry<Short>> values )
    {
        if ( values.size () > MAX_INFORMATION_ENTRIES )
        {
            throw new IllegalArgumentException ( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
        }
        return createInternal ( header, new ArrayList<> ( values ) );
    }

    private static MeasuredValueScaledTimeSequence createInternal (final ASDUHeader header, final List<InformationEntry<Short>> values )
    {
        return new MeasuredValueScaledTimeSequence( header, values );
    }

}
