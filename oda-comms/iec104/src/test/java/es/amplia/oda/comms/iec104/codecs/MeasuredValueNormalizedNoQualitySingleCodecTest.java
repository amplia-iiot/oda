package es.amplia.oda.comms.iec104.codecs;

import es.amplia.oda.comms.iec104.types.MeasuredValueNormalizedNoQualitySingle;
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

import java.util.TimeZone;

import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MeasuredValueNormalizedNoQualitySingleCodec.class)
public class MeasuredValueNormalizedNoQualitySingleCodecTest {

	private static final byte[] bytes = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00};
	private static final ByteBuf bytebuf = Unpooled.copiedBuffer(bytes);


	private final MeasuredValueNormalizedNoQualitySingleCodec codec = new MeasuredValueNormalizedNoQualitySingleCodec();

	@Test
	public void testParse() {
		ProtocolOptions options = new ProtocolOptions(0 , 0, 0, ASDUAddressType.SIZE_1,
				InformationObjectAddressType.SIZE_1, CauseOfTransmissionType.SIZE_1, (short) 0, (short) 0,
				TimeZone.getDefault(), true);

		Object o = codec.parse(options, (byte) 0x00, null, bytebuf);

		assertTrue(o instanceof MeasuredValueNormalizedNoQualitySingle);
	}

	@Test
	public void testEncode() {
		ProtocolOptions options = new ProtocolOptions(0 , 0, 0, ASDUAddressType.SIZE_1,
				InformationObjectAddressType.SIZE_1, CauseOfTransmissionType.SIZE_1, (short) 0, (short) 0,
				TimeZone.getDefault(), true);
		ByteBuf buffer = Unpooled.buffer();

		Value<Short> v = new Value<>(Short.parseShort("10"), System.currentTimeMillis(), QualityInformation.OK);

		codec.encode(options, MeasuredValueNormalizedNoQualitySingle.create(new ASDUHeader(CauseOfTransmission.ACTIVATED,
				ASDUAddress.valueOf(1)), InformationObjectAddress.DEFAULT, v), buffer);

		assertTrue(buffer.readableBytes() > 0);
	}
}
