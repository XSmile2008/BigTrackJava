package sample.command;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by olyadowgal on 04.02.16.
 * This class represents command's argument and serialize argument value
 */
public class Argument {

    public static final int KEY = 0;
    public static final int SIZE = 1;
    public static final int OFFSET = 2;
    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private byte[] bytes;

    public Argument(byte[] bytes) {
        this.bytes = bytes;
    }

    public Argument(byte key) {
        this.bytes = new byte[]{key, 0};
    }

    public Argument(byte key, byte value) {
        this.bytes = header(key, (byte) Byte.BYTES).put(value).array();
    }

    public Argument(byte key, short value) {
        this.bytes = header(key, (byte) Short.BYTES).putShort(value).array();
    }

    public Argument(byte key, int value) {
        this.bytes = header(key, (byte) Integer.BYTES).putInt(value).array();
    }

    public Argument(byte key, long value) {
        this.bytes = header(key, (byte) Long.BYTES).putLong(value).array();
    }

    public Argument(byte key, float value) {
        this.bytes = header(key, (byte) Float.BYTES).putFloat(value).array();
    }

    public Argument(byte key, double value) {
        this.bytes = header(key, (byte) Double.BYTES).putDouble(value).array();
    }

    public Argument(byte key, boolean value) {
        this.bytes = header(key, (byte) Byte.BYTES).put((byte) (value ? 1 : 0)).array();
    }

    public Argument(byte key, char value) {
        header(key, (byte) Character.BYTES).putChar(value).array();
    }

    private ByteBuffer header(byte key, byte size) {
        return ByteBuffer.allocate(OFFSET + size).put(key).put(size).order(BYTE_ORDER);
    }

    public byte[] serialize() {
        return bytes;
    }

    public byte getKey() {
        return bytes[KEY];
    }

    public int getSize() {
        return bytes[SIZE];
    }

    public byte getByte() throws ValueSizeException {
        if (getSize() != Byte.BYTES) throw new ValueSizeException();
        return ByteBuffer.wrap(bytes, OFFSET, Byte.BYTES).order(BYTE_ORDER).get();
    }

    public short getShort() throws ValueSizeException {
        if (getSize() != Short.BYTES) throw new ValueSizeException();
        return ByteBuffer.wrap(bytes, OFFSET, Short.BYTES).order(BYTE_ORDER).get();
    }

    public int getInt() throws ValueSizeException {
        if (getSize() != Integer.BYTES) throw new ValueSizeException();
        return ByteBuffer.wrap(bytes, OFFSET, Integer.BYTES).order(BYTE_ORDER).getInt();
    }

    public long getLong() throws ValueSizeException {
        if (getSize() != Long.BYTES) throw new ValueSizeException();
        return ByteBuffer.wrap(bytes, OFFSET, Long.BYTES).order(BYTE_ORDER).getLong();
    }

    public float getFloat() throws ValueSizeException {
        if (getSize() != Float.BYTES) throw new ValueSizeException();
        return ByteBuffer.wrap(bytes, OFFSET, Float.BYTES).order(BYTE_ORDER).getFloat();
    }

    public double getDouble() throws ValueSizeException {
        if (getSize() != Double.BYTES) throw new ValueSizeException();
        return ByteBuffer.wrap(bytes, OFFSET, Double.BYTES).order(BYTE_ORDER).getDouble();
    }

    public boolean getBoolean() throws ValueSizeException {
        if (getSize() != Byte.BYTES) throw new ValueSizeException();
        return ByteBuffer.wrap(bytes, OFFSET, Byte.BYTES).order(BYTE_ORDER).get() == 1;
    }

    public char getChar() throws ValueSizeException {
        if (getSize() != Character.BYTES) throw new ValueSizeException();
        return ByteBuffer.wrap(bytes, OFFSET, Character.BYTES).order(BYTE_ORDER).getChar();
    }
}
