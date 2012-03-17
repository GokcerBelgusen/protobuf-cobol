package com.legstar.protobuf.cobol;

import junit.framework.TestCase;

public class Ieee754Test extends TestCase {

    private static final char[] HEXCODES = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    private static final int[] SHIFTS = { 28, 24, 20, 16, 12, 8, 4, 0 };

    public void testFloat() {
        print(-375.256f);
        print(0f);
        print(-0f);
        print(3.40282347e+38f);
        print(1.40239846e-45f);
        print(1.40239846e-1f);
        print(1.1754949E-38f);
        print(2.3509886E-38f);
        print(5.88E-39f);
        print(1234f);
        print(-1234f);
    }

    public void testFloatBytes() {
        printFloat("C3BBA0C5");
        printFloat("00000000");
        printFloat("80000000");
        printFloat("7F7FFFFF");
        printFloat("00000001");
        printFloat("3E0F9B09");
        printFloat("00800004");
        printFloat("00FFFFFF");
        printFloat("0040070C");
        printFloat("449A4000");
        printFloat("C49A4000");
    }

    public void testDouble() {
        print(-375.256d);
        print(0d);
        print(-0d);
        print(3.40282347e+38d);
        print(1.40239846e-45d);
        print(1.40239846e-1d);
        print(1.1754949E-38d);
        print(2.3509886E-38d);
        print(5.88E-39d);
        print(1234d);
        print(-1234d);
        print(2.23e-308d);
        print(1.80e307d);
    }

    public void testDoubleBytes() {
        printDouble("C07774189374BC6A");
        printDouble("0000000000000000");
        printDouble("8000000000000000");
        printDouble("47EFFFFFE091FF3D");
        printDouble("36A003371D4EF338");
        printDouble("3FC1F361181541FE");
        printDouble("381000007D68F70D");
        printDouble("381FFFFFE8C9D9FB");
        printDouble("380001C30DDC0BB1");
        printDouble("4093480000000000");
        printDouble("C093480000000000");
        printDouble("0010091177587F83");
        printDouble("7FB9A2028368022E");
    }

    protected void print(float f) {
        StringBuilder sb = new StringBuilder();
        dump(sb, intToByteArray(Float.floatToRawIntBits(f)));
        System.out.println("Float=" + f + " hex=" + sb.toString());
    }

    protected void print(double d) {
        StringBuilder sb = new StringBuilder();
        dump(sb, longToByteArray(Double.doubleToRawLongBits(d)));
        System.out.println("Double=" + d + " hex=" + sb.toString());
    }

    protected void printFloat(String hex) {
        Float f = Float.intBitsToFloat(byteArrayToInt(toByteArray(hex)));
        System.out.println("Hex=" + hex + " float=" + f);
    }

    protected void printDouble(String hex) {
        Double d = Double.longBitsToDouble(byteArrayToLong(toByteArray(hex)));
        System.out.println("Hex=" + hex + " double=" + d);
    }

    private static byte[] intToByteArray(int data) {
        return new byte[] { (byte) ((data >> 24) & 0xff),
                (byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff),
                (byte) ((data >> 0) & 0xff) };
    }

    private static byte[] longToByteArray(long data) {
        return new byte[] { (byte) ((data >> 56) & 0xff),
                (byte) ((data >> 48) & 0xff), (byte) ((data >> 40) & 0xff),
                (byte) ((data >> 32) & 0xff), (byte) ((data >> 24) & 0xff),
                (byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff),
                (byte) ((data >> 0) & 0xff) };
    }

    private static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < b.length; i++) {
            value = (value << 8) + (b[i] & 0xff);
        }
        return value;
    }

    private static long byteArrayToLong(byte[] b) {
        long value = 0;
        for (int i = 0; i < b.length; i++) {
            value = (value << 8) + (b[i] & 0xff);
        }
        return value;
    }

    /**
     * Dump a byte array into a StringBuilder.
     * 
     * @param buffer the StringBuilder to dump the value in
     * @param value the byte value to be dumped
     * @return StringBuilder containing the dumped value.
     */
    private static StringBuilder dump(StringBuilder buffer, byte[] value) {
        for (int i = 0; i < value.length; i++) {
            dump(buffer, value[i]);
        }
        return buffer;
    }

    /**
     * Dump a byte value into a StringBuilder.
     * 
     * @param buffer the StringBuilder to dump the value in
     * @param value the byte value to be dumped
     * @return StringBuilder containing the dumped value.
     */
    private static StringBuilder dump(StringBuilder buffer, byte value) {
        for (int j = 0; j < 2; j++) {
            buffer.append(HEXCODES[(value >> SHIFTS[j + 6]) & 15]);
        }
        return buffer;
    }

    /**
     * Helper method to populate a byte array from a hex string representation.
     * 
     * @param string a string of hexadecimal characters to be turned into a byte
     *            array
     * @return an initialized byte array
     */
    private static byte[] toByteArray(final String string) {
        if (string == null) {
            return new byte[0];
        }
        byte[] hostBytes = new byte[string.length() / 2];
        for (int i = 0; i < string.length(); i += 2) {
            hostBytes[i / 2] = (byte) Integer.parseInt(
                    string.substring(i, i + 2), 16);
        }
        return hostBytes;
    }

}
