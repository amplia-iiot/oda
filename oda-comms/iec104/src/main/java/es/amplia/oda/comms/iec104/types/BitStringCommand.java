package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.AbstractInformationObjectMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.EncodeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.MirrorableMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;

@ASDU( id = 51, name = "C_BO_NA_1", informationStructure = InformationStructure.SINGLE)
public class BitStringCommand extends AbstractInformationObjectMessage implements MirrorableMessage<BitStringCommand> {

	private byte[] bitString;


	public BitStringCommand(final ASDUHeader header, final InformationObjectAddress informationObjectAddress)
	{
		super(header, informationObjectAddress);
	}

	public BitStringCommand(final ASDUHeader header, final InformationObjectAddress informationObjectAddress, final byte[] bitString)
	{
		this(header, informationObjectAddress);
		this.bitString = bitString;
	}

	public static BitStringCommand parse (final ProtocolOptions options, final ASDUHeader header, final ByteBuf data)
	{
		final InformationObjectAddress informationObjectAddress = InformationObjectAddress.parse ( options, data );
		final byte[] bytes = data.readBytes(4).array();
		return new BitStringCommand(header, informationObjectAddress, bytes);
	}

	@Override
	public void encode(final ProtocolOptions options, final ByteBuf out)
	{
		EncodeHelper.encodeHeader ( this, options, null, this.header, out );
		this.informationObjectAddress.encode ( options, out );
		if(bitString != null)
			out.writeBytes(bitString);
		else
			out.writeBytes(new byte[]{0x00, 0x00, 0x00, 0x00});
	}

	@Override
	public BitStringCommand mirror(Cause cause, boolean b) {
		return new BitStringCommand(this.header.clone ( cause ), getInformationObjectAddress());
	}

	public int parseBitString() {
		return (bitString[0] << 24)
				+ ((bitString[1] & 0xFF) << 16)
				+ ((bitString[2] & 0xFF) << 8)
				+ (bitString[3] & 0xFF);
	}
}
