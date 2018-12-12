package es.amplia.oda.core.commons.modbus;

public class Register {
    private final byte[] register = new byte[2];

    public Register(byte b1, byte b2) {
        this.register[0] = b1;
        this.register[1] = b2;
    }

    public Register(int value) {
        this.register[0] = (byte) (255 & value >> 8);
        this.register[1] = (byte) (255 & value);
    }

    public int getValue() {
        return (this.register[0] & 255) << 8 | this.register[1] & 255;
    }

    public final int toUnsignedShort() {
        return (this.register[0] & 255) << 8 | this.register[1] & 255;
    }

    public final short toShort() {
        return (short)(this.register[0] << 8 | this.register[1] & 255);
    }

    public byte[] toBytes() {
        byte[] dest = new byte[this.register.length];
        System.arraycopy(this.register, 0, dest, 0, dest.length);
        return dest;
    }
}
