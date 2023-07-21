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
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.EncodeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ASDU ( id = 3, name = "M_DP_TB_1", informationStructure = InformationStructure.SEQUENCE )
public class DoublePointInformationTimeSequence extends AbstractMessage
{
    private final InformationObjectAddress startAddress;

    private final List<Value<DoublePoint>> values;

    private DoublePointInformationTimeSequence(final ASDUHeader header, final InformationObjectAddress startAddress, final List<Value<DoublePoint>> values )
    {
        super ( header );
        this.startAddress = startAddress;
        this.values = values;
    }

    public InformationObjectAddress getStartAddress ()
    {
        return this.startAddress;
    }

    public List<Value<DoublePoint>> getValues ()
    {
        return this.values;
    }

    public static DoublePointInformationTimeSequence parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        final InformationObjectAddress startAddress = InformationObjectAddress.parse ( options, data );

        final List<Value<DoublePoint>> values = new ArrayList<> ( length );
        for ( int i = 0; i < length; i++ )
        {
            values.add ( TypeHelper.parseDoublePointValue ( options, data, true ) );
        }

        return new DoublePointInformationTimeSequence( header, startAddress, values );
    }

    @Override
    public void encode ( final ProtocolOptions options, final ByteBuf out )
    {
        EncodeHelper.encodeHeader ( this, options, this.values.size (), this.header, out );

        this.startAddress.encode ( options, out );

        for ( final Value<DoublePoint> value : this.values )
        {
            TypeHelper.encodeDoublePointValue ( options, out, value, true );
        }
    }

    public static DoublePointInformationTimeSequence create (final InformationObjectAddress startAddress, final ASDUHeader header, final Value<DoublePoint> value )
    {
        return createInternal ( startAddress, header, Collections.singletonList ( value ) );
    }

    public static DoublePointInformationTimeSequence create (final InformationObjectAddress startAddress, final ASDUHeader header, final List<Value<DoublePoint>> values )
    {
        if ( values.size () > MAX_INFORMATION_ENTRIES )
        {
            throw new IllegalArgumentException ( String.format ( "A maximum of %s values can be transmitted", MAX_INFORMATION_ENTRIES ) );
        }
        return createInternal ( startAddress, header, new ArrayList<> ( values ) );
    }

    private static DoublePointInformationTimeSequence createInternal (final InformationObjectAddress startAddress, final ASDUHeader header, final List<Value<DoublePoint>> values )
    {
        return new DoublePointInformationTimeSequence( header, startAddress, values );
    }

}
