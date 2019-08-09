package es.amplia.oda.connector.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractInformationObjectMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.EncodeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MirrorableMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

@ASDU( id = 51, name = "C_BO_NA_1", informationStructure = InformationStructure.SINGLE)
public class BitstringCommand extends AbstractInformationObjectMessage implements MirrorableMessage<BitstringCommand> {
	private byte[] bytestring;

	public BitstringCommand (final ASDUHeader header, final InformationObjectAddress informationObjectAddress)
	{
		super(header, informationObjectAddress);
	}

	public BitstringCommand (final ASDUHeader header, final InformationObjectAddress informationObjectAddress, final byte[] bytestring)
	{
		this(header, informationObjectAddress);
		this.bytestring = bytestring;
	}

	public static BitstringCommand parse (final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
	{
		final InformationObjectAddress informationObjectAddress = InformationObjectAddress.parse ( options, data );
		final byte[] bytes = data.readBytes(4).array();
		return new BitstringCommand (header, informationObjectAddress, bytes);
	}

	@Override
	public void encode(final ProtocolOptions options, final ByteBuf out)
	{
		EncodeHelper.encodeHeader ( this, options, null, this.header, out );
		this.informationObjectAddress.encode ( options, out );
		if(bytestring != null)
			out.writeBytes(bytestring);
		else
			out.writeBytes(new byte[]{0x00, 0x00, 0x00, 0x00});
	}

	@Override
	public BitstringCommand mirror(Cause cause, boolean b) {
		return new BitstringCommand(this.header.clone ( cause ), getInformationObjectAddress());
	}

	public byte[] getBytestring() {
		return bytestring;
	}

	public int parseBytestring() {
		return (getBytestring()[0] << 24)
				+ ((getBytestring()[1] & 0xFF) << 16)
				+ ((getBytestring()[2] & 0xFF) << 8)
				+ (getBytestring()[3] & 0xFF);
	}
}
