package es.amplia.oda.core.commons.modbus;

public class Register {

    private static final int BITS_PER_BYTE = 8;


    private final byte[] bytes = new byte[2];


    public Register(byte b1, byte b2) {
        this.bytes[0] = b1;
        this.bytes[1] = b2;
    }

    public Register(int value) {
        this.bytes[0] = (byte) (0xFF & value >> BITS_PER_BYTE);
        this.bytes[1] = (byte) (0xFF & value);
    }

    public int getValue() {
        return (this.bytes[0] & 0xFF) << BITS_PER_BYTE | this.bytes[1] & 0xFF;
    }

    public final int toUnsignedShort() {
        return (this.bytes[0] & 0xFF) << BITS_PER_BYTE | this.bytes[1] & 0xFF;
    }

    public final short toShort() {
        return (short)(this.bytes[0] << BITS_PER_BYTE | this.bytes[1] & 0xFF);
    }

    public byte[] toBytes() {
        byte[] dest = new byte[this.bytes.length];
        System.arraycopy(this.bytes, 0, dest, 0, dest.length);
        return dest;
    }
}
