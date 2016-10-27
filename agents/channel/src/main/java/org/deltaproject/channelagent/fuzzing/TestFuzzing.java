package org.deltaproject.channelagent.fuzzing;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import jpcap.packet.Packet;
import org.deltaproject.channelagent.dummy.DummySwitch;
import org.deltaproject.channelagent.utils.Utils;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U16;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by seungsoo on 10/26/16.
 */
public class TestFuzzing {
    static final int HEADER_LEN = 8;

    private OFFactory factory;
    private OFMessageReader<OFMessage> reader;
    private byte ofversion;

    private ArrayList<DummySwitch> switches;

    private String modifiedMsg = "nothing";

    private OFType targetType;

    private int toControlMsgLen;
    private int toDatalMsgLen;

    private Random random = new Random();

    public TestFuzzing(OFFactory f, byte of) {
        this.factory = f;
        this.reader = factory.getReader();
        this.ofversion = of;

        toControlMsgLen = OFFuzzer.toControlMsg.length;
        toDatalMsgLen = OFFuzzer.toDataMsg.length;
    }

    public ByteBuf getByteBuf(Packet p_temp) {
        // for OpenFlow Message
        byte[] rawMsg = new byte[p_temp.data.length];
        System.arraycopy(p_temp.data, 0, rawMsg, 0, p_temp.data.length);
        // ByteBuf byteMsg = Unpooled.copiedBuffer(rawMsg);

        return Unpooled.wrappedBuffer(rawMsg);
    }

    public String getFuzzingMsg() {
        return modifiedMsg;
    }

    public byte[] testControlPlane(Packet p_temp) throws OFParseError {
        ByteBuf bb = getByteBuf(p_temp);
        int totalLen = bb.readableBytes();
        int offset = bb.readerIndex();

        byte[] newbytes = null;

        targetType = OFFuzzer.toControlMsg[random.nextInt(toControlMsgLen)];

        while (offset < totalLen) {
            bb.readerIndex(offset);

            byte version = bb.readByte();
            bb.readByte();
            int length = U16.f(bb.readShort());
            bb.readerIndex(offset);

            if (version != this.ofversion) {
                // segmented TCP pkt
                // System.out.println("OFVersion Missing " + version + " : " + offset + "-" + totalLen);
                return null;
            }

            if (length < HEADER_LEN)
                throw new OFParseError("Wrong length: Expected to be >= " + HEADER_LEN + ", was: " + length);

            try {
                OFMessage message = reader.readFrom(bb);
                System.out.println("OFMsg " + message.toString());

                if (message.getType() == targetType) {
                    modifiedMsg = message.toString() + " -> ";

                    byte[] origin = bb.array();
                    byte[] cratedbytes = new byte[totalLen - HEADER_LEN];
                    newbytes = new byte[totalLen];

                    System.arraycopy(origin, 8, cratedbytes, 0, totalLen - HEADER_LEN);
                    random.nextBytes(cratedbytes);

                    System.arraycopy(origin, 0, newbytes, 0, HEADER_LEN);
                    System.arraycopy(cratedbytes, 0, newbytes, 8, totalLen - HEADER_LEN);

                    modifiedMsg += Utils.bytesToHex(cratedbytes);
                }
            } catch (OFParseError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            offset += length;
        }

        bb.clear();
        return newbytes;
    }

    public byte[] testDataPlane(Packet p_temp) throws OFParseError {
        ByteBuf bb = getByteBuf(p_temp);
        int totalLen = bb.readableBytes();
        int offset = bb.readerIndex();

        byte[] newbytes = null;

        targetType = OFFuzzer.toDataMsg[random.nextInt(toDatalMsgLen)];

        while (offset < totalLen) {
            bb.readerIndex(offset);

            byte version = bb.readByte();
            bb.readByte();
            int length = U16.f(bb.readShort());
            bb.readerIndex(offset);

            if (version != this.ofversion) {
                // segmented TCP pkt
                // System.out.println("OFVersion Missing " + version + " : " + offset + "-" + totalLen);
                return null;
            }

            if (length < HEADER_LEN)
                throw new OFParseError("Wrong length: Expected to be >= " + HEADER_LEN + ", was: " + length);

            try {
                OFMessage message = reader.readFrom(bb);
                System.out.println("OFMsg " + message.toString());

                if (message.getType() == targetType) {
                    modifiedMsg = message.toString() + " -> ";

                    byte[] origin = bb.array();
                    byte[] cratedbytes = new byte[totalLen - HEADER_LEN];
                    newbytes = new byte[totalLen];

                    System.arraycopy(origin, 8, cratedbytes, 0, totalLen - HEADER_LEN);
                    random.nextBytes(cratedbytes);

                    System.arraycopy(origin, 0, newbytes, 0, HEADER_LEN);
                    System.arraycopy(cratedbytes, 0, newbytes, 8, totalLen - HEADER_LEN);

                    modifiedMsg += Utils.bytesToHex(newbytes);
                }
            } catch (OFParseError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            offset += length;
        }

        bb.clear();
        return newbytes;
    }
}
