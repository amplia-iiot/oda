package es.amplia.oda.comms.iec104.types;

import io.netty.buffer.ByteBuf;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.QualityInformation;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.TypeHelper;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.Value;

import java.util.Calendar;
import java.util.GregorianCalendar;

class TypeHelperExt extends TypeHelper {

	static void encodeBitStringValue(ProtocolOptions options, ByteBuf out, Value<Long> value, boolean withTimestamp) {
		byte qds = (byte)(value.isOverflow() ? 1 : 0);
		byte siq = value.getQualityInformation().apply(qds);
		out.writeLong(value.getValue());
		out.writeByte(siq);
		if (withTimestamp) {
			encodeTimestamp(options, out, value.getTimestamp());
		}

	}

	static Value<Long> parseBitStringValue(ProtocolOptions options, ByteBuf data, boolean withTimestamp) {
		final long value = data.readUnsignedInt();
		final byte siq = data.readByte();
		QualityInformation qualityInformation = QualityInformation.parse(siq);
		long timestamp = withTimestamp ? parseTimestamp(options, data) : System.currentTimeMillis();
		return new Value<>(value, timestamp, qualityInformation);
	}

	public static Value<Byte> parseByteValue(final ProtocolOptions options, final ByteBuf data, final boolean withTimestamp)
	{
		final byte value = data.readByte();
		final byte qds = data.readByte ();

		final QualityInformation qualityInformation = QualityInformation.parse ( qds );
		final boolean overflow = ( qds & 0b00000001 ) > 0;

		final long timestamp = withTimestamp ? TypeHelperExt.parseTimestamp ( options, data ) : System.currentTimeMillis ();

		return new Value<> ( value, timestamp, qualityInformation, overflow );
	}

	public static void encodeByteValue ( final ProtocolOptions options, final ByteBuf out, final Value<Byte> value, final boolean withTimestamp )
	{
		final byte qds = (byte) ( value.isOverflow () ? 0b00000001 : 0b00000000 );
		final byte siq = value.getQualityInformation ().apply ( qds );

		out.writeByte ( value.getValue () );
		out.writeByte ( siq );

		if ( withTimestamp )
		{
			TypeHelperExt.encodeTimestamp ( options, out, value.getTimestamp () );
		}
	}

	public static void encodeScaledValueNoQuality ( final ProtocolOptions options, final ByteBuf out, final Value<Short> value, final boolean withTimestamp )
	{
		out.writeShort ( value.getValue () );

		if ( withTimestamp )
		{
			encodeTimestamp ( options, out, value.getTimestamp () );
		}
	}

	/**
	 * Parse Short integer number without quality descriptor
	 */
	public static Value<Short> parseScaledValueNoQuality ( final ProtocolOptions options, final ByteBuf data, final boolean withTimestamp )
	{
		final short value = data.readShort ();

		final long timestamp = withTimestamp ? parseTimestamp ( options, data ) : System.currentTimeMillis ();

		return new Value<> ( value, timestamp, null, false );
	}

	@SuppressWarnings("all")
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

	@SuppressWarnings("all")
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
