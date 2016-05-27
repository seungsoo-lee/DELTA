package org.deltaproject.onosagent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utils {
	public static short bytesToShort(byte[] bytes) {
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}

	public static byte[] shortToBytes(short value) {
		byte[] returnByteArray = new byte[2];
		returnByteArray[0] = (byte) (value & 0xff);
		returnByteArray[1] = (byte) ((value >>> 8) & 0xff);
		return returnByteArray;
	}
	
	public static byte shortToByteIPProto(short value) {
		byte[] returnByteArray = new byte[2];
		returnByteArray[0] = (byte) (value & 0xff);
		returnByteArray[1] = (byte) ((value >>> 8) & 0xff);
		return returnByteArray[0];
	}
}
