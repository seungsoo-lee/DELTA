package org.deltaproject.channelagent.fuzzing;

import java.util.Random;

import org.deltaproject.channelagent.dummy.DummyOF10;
import org.deltaproject.channelagent.fuzzing.OFPktStructure.PACKET_IN;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFType;

public class OFFuzzer {
    private static Random random = new Random();

    public static OFType[] toControlMsg = {OFType.ERROR, OFType.ECHO_REPLY, OFType.EXPERIMENTER, OFType.FEATURES_REPLY, OFType.GET_CONFIG_REPLY, OFType.PACKET_IN, OFType.FLOW_REMOVED, OFType.PORT_STATUS, OFType.STATS_REPLY};
    public static OFType[] toDataMsg = {OFType.EXPERIMENTER, OFType.PACKET_OUT, OFType.FLOW_MOD, OFType.PORT_MOD, OFType.STATS_REQUEST};

    public int toControlMsgLen;
    public int toDatalMsgLen;

    public OFFuzzer() {

    }

    public static byte[] fuzzPacketIn() throws OFParseError {
        /*
         * length - 2 bytes xid - 4 bytes buffer_id - 4 bytes total_len - 2
		 * bytes in_port - 2 bytes reason - 1 byte pad - 1 byte
		 */

        byte[] msg = DummyOF10.hexStringToByteArray(DummyOF10.PACKET_IN);

        PACKET_IN[] fields = PACKET_IN.values();
        int idx = random.nextInt(fields.length);
        PACKET_IN target = fields[idx];

        byte[] crafted = new byte[target.getLen()];
        byte[] original = new byte[target.getLen()];

        System.arraycopy(msg, target.getStartOff(), crafted, 0, crafted.length);
        System.arraycopy(crafted, 0, original, 0, crafted.length);

        random.nextBytes(crafted);
        System.arraycopy(crafted, 0, msg, target.getStartOff(), crafted.length);

        System.out.println("PACKET_IN\t" + target.name() + ":" + DummyOF10.bytesToHex(original) + " -> "
                + DummyOF10.bytesToHex(crafted));

        return msg;
    }
}