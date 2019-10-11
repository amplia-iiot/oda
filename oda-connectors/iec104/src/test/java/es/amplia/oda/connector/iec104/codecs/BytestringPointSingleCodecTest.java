package es.amplia.oda.connector.iec104.codecs;

import es.amplia.oda.connector.iec104.types.BytestringPointInformationSequence;
import es.amplia.oda.connector.iec104.types.BytestringPointInformationSingle;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.eclipse.neoscada.protocol.iec60870.ASDUAddressType;
import org.eclipse.neoscada.protocol.iec60870.CauseOfTransmissionType;
import org.eclipse.neoscada.protocol.iec60870.InformationObjectAddressType;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BytestringPointSingleCodec.class)
public class BytestringPointSingleCodecTest {

	private static final byte[] bytes = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00};
	private static final ByteBuf bytebuf = Unpooled.copiedBuffer(bytes);


	private BytestringPointSingleCodec codec = new BytestringPointSingleCodec();

	@Test
	public void testParse() {
		ProtocolOptions options = new ProtocolOptions(0 , 0, 0, ASDUAddressType.SIZE_1,
				InformationObjectAddressType.SIZE_1, CauseOfTransmissionType.SIZE_1, (short) 0, (short) 0,
				TimeZone.getDefault(), true);

		Object o = codec.parse(options, (byte) 0x00, null, bytebuf);

		assertTrue(o instanceof BytestringPointInformationSingle);
	}

	@Test
	public void testEncode() {
		ProtocolOptions options = new ProtocolOptions(0 , 0, 0, ASDUAddressType.SIZE_1,
				InformationObjectAddressType.SIZE_1, CauseOfTransmissionType.SIZE_1, (short) 0, (short) 0,
				TimeZone.getDefault(), true);
		ByteBuf buffer = Unpooled.buffer();

		Value<byte[]> v = new Value(bytes, System.currentTimeMillis(), QualityInformation.OK);

		codec.encode(options, BytestringPointInformationSingle.create(new ASDUHeader(CauseOfTransmission.ACTIVATED, ASDUAddress.valueOf(1)), InformationObjectAddress.DEFAULT, v), buffer);

		assertTrue(buffer.readableBytes() > 0);
	}
}
