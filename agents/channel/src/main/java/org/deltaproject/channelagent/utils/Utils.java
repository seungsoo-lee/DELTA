package org.deltaproject.channelagent.utils;

import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.StringTokenizer;

public class Utils {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static final char[] hexchars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
            'd', 'e', 'f'};

    public static String format(byte[] buf) {
        return format(buf, 80);
    }

    public static String format(byte[] buf, int width) {
        int bs = (width - 8) / 4;
        int i = 0;
        StringBuffer sb = new StringBuffer();
        do {
            for (int j = 0; j < 6; j++) {
                sb.append(hexchars[(i << (j * 4) & 0xF00000) >> 20]);
            }
            sb.append('\t');
            sb.append(toHex(buf, i, bs));
            sb.append(' ');
            sb.append(toAscii(buf, i, bs));
            sb.append('\n');
            i += bs;
        } while (i < buf.length);
        return sb.toString();
    }

    public static void print(byte[] buf) {
        print(buf, System.err);
    }

    public static void print(byte[] buf, int width) {
        print(buf, width, System.err);
    }

    public static void print(byte[] buf, int width, PrintStream out) {
        out.print(format(buf, width));
    }

    public static void print(byte[] buf, PrintStream out) {
        out.print(format(buf));
    }

    public static String toAscii(byte[] buf) {
        return toAscii(buf, 0, buf.length);
    }

    public static String toAscii(byte[] buf, int ofs, int len) {
        StringBuffer sb = new StringBuffer();
        int j = ofs + len;
        for (int i = ofs; i < j; i++) {
            if (i < buf.length) {
                if ((20 <= buf[i]) && (126 >= buf[i])) {
                    sb.append((char) buf[i]);
                } else {
                    sb.append('.');
                }
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    /**
     * Returns a string which can be written to a Java source file as part of a
     * static initializer for a byte array. Returns data in the format 0xAB,
     * 0xCD, .... use like: javafile.print("byte[] data = {")
     * javafile.print(Hexdump.toByteArray(data)); javafile.println("};");
     */
    public static String toByteArray(byte[] buf) {
        return toByteArray(buf, 0, buf.length);
    }

    /**
     * Returns a string which can be written to a Java source file as part of a
     * static initializer for a byte array. Returns data in the format 0xAB,
     * 0xCD, .... use like: javafile.print("byte[] data = {")
     * javafile.print(Hexdump.toByteArray(data)); javafile.println("};");
     */
    public static String toByteArray(byte[] buf, int ofs, int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = ofs; (i < len) && (i < buf.length); i++) {
            sb.append('0');
            sb.append('x');
            sb.append(hexchars[(buf[i] & 0xF0) >> 4]);
            sb.append(hexchars[buf[i] & 0x0F]);
            if (((i + 1) < len) && ((i + 1) < buf.length)) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    public static String toHex(byte[] buf) {
        return toHex(buf, 0, buf.length);
    }

    public static String toHex(byte[] buf, int ofs, int len) {
        StringBuffer sb = new StringBuffer();
        int j = ofs + len;
        for (int i = ofs; i < j; i++) {
            if (i < buf.length) {
                sb.append(hexchars[(buf[i] & 0xF0) >> 4]);
                sb.append(hexchars[buf[i] & 0x0F]);
                sb.append(' ');
            } else {
                sb.append(' ');
                sb.append(' ');
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    public static String getUserString() {
        String input = "";
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in));
        try {
            input = bufferedreader.readLine();
        } catch (IOException ioexception) {
        }
        return input;
    }

    public static String byteArrayToAsciiString(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length);
        for (int i = 0; i < data.length; ++i) {
            if (data[i] < 0)
                throw new IllegalArgumentException();
            sb.append((char) data[i]);
        }
        return sb.toString();
    }

    public static String byteArrayToIPString(byte[] data) {
        int i = 4;
        String ipAddress = "";
        for (byte raw : data) {
            ipAddress += (raw & 0xFF);
            if (--i > 0) {
                ipAddress += ".";
            }
        }

        return ipAddress;
    }

    public static String byteArrayToHexString(byte[] b) {
        final int newline = 16;

        if (b == null || b.length == 0) {
            return null;
        }

        StringBuffer sb = new StringBuffer(b.length * 2);
        String hexNumber;
        String ascii = "";
        int idx = 1;
        for (int x = 0; x < b.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xFF & b[x]);
            sb.append(hexNumber.substring(hexNumber.length() - 2));
            sb.append(" ");

            if (0x20 <= (char) (0xFF & b[x]) && (char) (0xFF & b[x]) < 128) {
                ascii += (char) (0xFF & b[x]);
            } else {
                ascii += ".";
            }

            if (idx % newline == 0) {
                sb.append(" | " + ascii);
                ascii = "";
                sb.append("\n");
            }
            idx++;
        }

        while ((idx % newline) != 0) {
            sb.append("__ ");

            idx++;
        }
        sb.append("__ ");
        idx++;
        sb.append(" | " + ascii);
        ascii = "";
        // sb.append("\n");

        return sb.toString();
    }

    public static String byteArrayToHexBoldString(byte[] b, int start, int end) {
        final int newline = 16;

        if (b == null || b.length == 0) {
            return null;
        }

        StringBuffer sb = new StringBuffer(b.length * 2);
        String hexNumber;
        String ascii = "";
        int idx = 1;
        for (int x = 0; x < b.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xFF & b[x]);

            if (start == x) {
                sb.append("[");
                ascii += "[";
            }

            sb.append(hexNumber.substring(hexNumber.length() - 2));

            if (end == x + 1) {
                sb.append("]");
            } else if (end == x) {
                ascii += "]";
            }

            sb.append(" ");

            if (0x20 <= (char) (0xFF & b[x]) && (char) (0xFF & b[x]) < 128) {
                ascii += (char) (0xFF & b[x]);
            } else {
                ascii += ".";
            }

            if (idx % newline == 0) {
                sb.append(" | " + ascii);
                ascii = "";
                sb.append("\n");
            }
            idx++;
        }

        while ((idx % newline) != 0) {
            sb.append("__ ");

            idx++;
        }
        sb.append("__ ");
        idx++;
        sb.append(" | " + ascii);
        ascii = "";
        // sb.append("\n");

        return sb.toString();
    }

    public static String byteArrayToBinaryString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; ++i) {
            sb.append(byteToBinaryString(b[i]));
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String byteToBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    }

    public static byte[] binaryStringToByteArray(String s) {
        int count = s.length() / 8;
        byte[] b = new byte[count];
        for (int i = 1; i < count; ++i) {
            String t = s.substring((i - 1) * 8, i * 8);
            b[i - 1] = binaryStringToByte(t);
        }
        return b;
    }

    public static byte binaryStringToByte(String s) {
        byte ret = 0, total = 0;
        for (int i = 0; i < 8; ++i) {
            ret = (s.charAt(7 - i) == '1') ? (byte) (1 << i) : 0;
            total = (byte) (ret | total);
        }
        return total;
    }

    public static int parseIPString(String ip_str) // format 192.168.0.0
    {
        int ret = 0;
        int temp = 0;

        StringTokenizer tokens = new StringTokenizer(ip_str, ".");

        temp = Integer.parseInt(tokens.nextToken());
        temp <<= 24;
        ret += temp;

        temp = Integer.parseInt(tokens.nextToken());
        temp <<= 16;
        ret += temp;

        temp = Integer.parseInt(tokens.nextToken());
        temp <<= 8;
        ret += temp;

        temp = Integer.parseInt(tokens.nextToken());
        ret += temp;

        return ret;
    }

    public static byte[] intToByteArrL(int myInteger) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(myInteger).array();
    }

    public static int byteArrToIntL(byte[] byteBarray) {
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static byte[] intToByteArrB(int myInteger) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(myInteger).array();
    }

    public static int byteArrToIntB(byte[] byteBarray) {
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    public static long byteToInt(byte[] bytes, int length) {
        int val = 0;
        if (length > 4)
            throw new RuntimeException("Too big to fit in int");
        for (int i = 0; i < length; i++) {
            val = val << 8;
            val = val | (bytes[i] & 0xFF);
        }
        return val;
    }

    public static boolean cmpIPWithSubnet(String ip_str1, String ip_str2, int subnetmask) {
        int ip1 = 0;
        int ip2 = 0;
        int subnet = 0xFFFFFFFF;

        subnet = (subnetmask == 0) ? 0 : subnet << (32 - subnetmask);

        ip1 = parseIPString(ip_str1);
        ip2 = parseIPString(ip_str2);

        // System.out.println(Integer.toBinaryString(ip1));
        // System.out.println(Integer.toBinaryString(ip2));
        // System.out.println(Integer.toBinaryString(subnet));

        ip1 &= subnet;
        ip2 &= subnet;

        return (ip2 == (ip1 & ip2)) ? true : false;
    }

    public static boolean cmpIPWithSubnet(int ip_int1, int ip_int2, int subnetmask) {
        int ip1 = ip_int1;
        int ip2 = ip_int2;
        int subnet = 0xFFFFFFFF;
        subnet <<= (32 - subnetmask);

        ip1 &= subnet;
        ip2 &= subnet;

        return (ip2 == (ip1 & ip2)) ? true : false;
    }

    public static boolean cmpPort(int port_int1, int port_int2) {
        return (port_int2 == (port_int1 & port_int2)) ? true : false;
    }

    /*
     * public static int bytesIndexOf(byte[] outer, byte[] inner) { String
     * bigStr = new String(outer); String smallStr = new String(inner);
     *
     *
     * return bigStr.indexOf(smallStr);
     *
     * }
     */
    public static int bytesIndexOf(byte[] largeArray, byte[] subArray) {

		/* If any of the arrays is empty then not found */
        if (largeArray.length == 0 || subArray.length == 0) {
            return -1;
        }

		/* If subarray is larger than large array then not found */
        if (subArray.length > largeArray.length) {
            return -1;
        }

        for (int i = 0; i < largeArray.length; i++) {
            /*
			 * Check if the next element of large array is the same as the first
			 * element of subarray
			 */
            if (largeArray[i] == subArray[0]) {

                boolean subArrayFound = true;
                for (int j = 0; j < subArray.length; j++) {
					/*
					 * If outside of large array or elements not equal then
					 * leave the loop
					 */
                    if (largeArray.length <= i + j || subArray[j] != largeArray[i + j]) {
                        subArrayFound = false;
                        break;
                    }
                }

				/* Sub array found - return its index */
                if (subArrayFound) {
                    return i;
                }

            }
        }

		/* Return default value */
        return -1;
    }

    public static byte[] ListToByteArray(List<Byte> in) {
        final int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }

    public static NetworkInterfaceAddress __get_inet4(NetworkInterface device) throws NullPointerException {
        if (device == null)
            throw new NullPointerException("No device has been given! potato");

        for (NetworkInterfaceAddress addr : device.addresses)
            if (addr.address instanceof Inet4Address)
                return addr;

        return null;
    }

//    public static String bytesToHex(byte[] bytes) {
//        char[] hexArray = "0123456789ABCDEF".toCharArray();
//        char[] hexChars = new char[bytes.length * 2];
//        for (int j = 0; j < bytes.length; j++) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }

    public static byte[] calculate_mac(String mac) {
        String[] macAddressParts = mac.split(":");

        // convert hex string to byte values
        byte[] macAddressBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            Integer hex = Integer.parseInt(macAddressParts[i], 16);
            macAddressBytes[i] = hex.byteValue();
        }

        return macAddressBytes;
    }

    public static String decalculate_mac(byte[] mac) {
        StringBuilder sb = new StringBuilder(18);
        for (byte b : mac) {
            if (sb.length() > 0)
                sb.append(':');
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}