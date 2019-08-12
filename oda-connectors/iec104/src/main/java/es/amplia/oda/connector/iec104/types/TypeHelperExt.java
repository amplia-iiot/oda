package es.amplia.oda.connector.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.QualityInformation;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.TypeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.Value;

import java.util.Calendar;
import java.util.GregorianCalendar;

class TypeHelperExt extends TypeHelper {

	static void encodeBytestringValue(ProtocolOptions options, ByteBuf out, Value<byte[]> value, boolean withTimestamp) {
		byte qds = (byte)(value.isOverflow() ? 1 : 0);
		byte siq = value.getQualityInformation().apply(qds);
		out.writeBytes(value.getValue());
		out.writeByte(siq);
		if (withTimestamp) {
			encodeTimestamp(options, out, value.getTimestamp());
		}

	}

	static Value<byte[]> parseBytestringValue(ProtocolOptions options, ByteBuf data, boolean withTimestamp) {
		byte[] value = new byte[data.readableBytes()];
		data.getBytes(data.readerIndex(), value);
		byte siq = data.readByte();
		QualityInformation qualityInformation = QualityInformation.parse(siq);
		long timestamp = withTimestamp ? parseTimestamp(options, data) : System.currentTimeMillis();
		return new Value(value, timestamp, qualityInformation);
	}

	private static long parseTimestamp(ProtocolOptions options, ByteBuf data) {
		int ms = data.readUnsignedShort();
		int minutes = data.readUnsignedByte();
		minutes = minutes & 63;
		int hours = data.readUnsignedByte();
		hours = hours & 31;
		int dayOfMonth = data.readUnsignedByte() & 31;
		int month = data.readUnsignedByte() & 15;
		int year = data.readUnsignedByte() & 127;
		year += 2000;
		Calendar c = new GregorianCalendar(options.getTimeZone());
		c.set(year, month, dayOfMonth, hours, minutes, ms / 1000);
		c.set(14, ms % 1000);
		return c.getTimeInMillis();
	}

	private static void encodeTimestamp(ProtocolOptions options, ByteBuf out, long timestamp) {
		Calendar c = new GregorianCalendar(options.getTimeZone());
		c.setTimeInMillis(timestamp);
		int ms = c.get(13) * 1000 + c.get(14);
		int minutes = c.get(12);
		int hours = c.get(11);
		int dayOfMonth = c.get(5);
		int month = c.get(2) + 1;
		int year = c.get(1) % 100;
		byte hourField = (byte)(hours & 31);
		if (!options.isIgnoreDaylightSavingTime() && c.get(16) > 0) {
			hourField = (byte)(hourField | 128);
		}

		out.writeShort(ms);
		out.writeByte(minutes);
		out.writeByte(hourField);
		out.writeByte(dayOfMonth);
		out.writeByte(month);
		out.writeByte(year);
	}
}
