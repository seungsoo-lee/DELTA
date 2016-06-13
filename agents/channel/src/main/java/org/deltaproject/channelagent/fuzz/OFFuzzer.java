package org.deltaproject.channelagent.fuzz;

import java.util.Random;

import org.deltaproject.channelagent.dummy.DummyOFData;
import org.deltaproject.channelagent.fuzz.OFPktStructure.PACKET_IN;
import org.projectfloodlight.openflow.exceptions.OFParseError;

public class OFFuzzer {
	private static Random random = new Random();
	
	public OFFuzzer() {

	}
	
	public static byte[] fuzzPacketIn() throws OFParseError {
		/*
		 * length - 2 bytes xid - 4 bytes buffer_id - 4 bytes total_len - 2
		 * bytes in_port - 2 bytes reason - 1 byte pad - 1 byte
		 */

		byte[] msg = DummyOFData.hexStringToByteArray(DummyOFData.packetin);

		PACKET_IN[] fields = PACKET_IN.values();
		int idx = random.nextInt(fields.length);
		PACKET_IN target = fields[idx];

		byte[] crafted = new byte[target.getLen()];
		byte[] original = new byte[target.getLen()];

		System.arraycopy(msg, target.getStartOff(), crafted, 0, crafted.length);
		System.arraycopy(crafted, 0, original, 0, crafted.length);

		random.nextBytes(crafted);
		System.arraycopy(crafted, 0, msg, target.getStartOff(), crafted.length);

		System.out.println("FUZZ|PACKET_IN|" + target.name() + ":" + DummyOFData.bytesToHex(original) + " -> "
				+ DummyOFData.bytesToHex(crafted));

		return msg;
	}
}
