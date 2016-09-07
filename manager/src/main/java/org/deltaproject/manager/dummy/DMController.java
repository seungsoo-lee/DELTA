package org.deltaproject.manager.dummy;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.HashedWheelTimer;
import org.deltaproject.manager.utils.OFUtil;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.errormsg.OFErrorMsgs;
import org.projectfloodlight.openflow.types.U16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by seungsoo on 9/7/16.
 */
public class DMController extends Thread {
    private static final Logger log = LoggerFactory.getLogger(DMController.class);

    public static final int HANDSHAKE_DEFAULT = 0;
    public static final int HANDSHAKE_NO_HELLO = 1;

    public static final int MINIMUM_LENGTH = 8;

    private ServerSocket serverSock;
    private Socket targetSock;
    private InputStream in;
    private OutputStream out;

    /* for target controller */
    private int port = 0;

    /* for OF message */
    private OFVersion version;
    private OFFactory factory;
    private OFMessageReader<OFMessage> reader;
    private OFMessage res;

    private int testHandShakeType;

    private long requestXid = 0xeeeeeeaal;
    private long startXid = 0xffffffff;
    private boolean handshaked = false;
    private boolean synack = false;

    private OFFlowAdd backupFlowAdd;

    public DMController(String ver, int port) {
        res = null;

        if (ver.equals("1.0")) {
            version = OFVersion.OF_10;
            factory = OFFactories.getFactory(OFVersion.OF_10);
        } else if (ver.equals("1.3")) {
            version = OFVersion.OF_13;
            factory = OFFactories.getFactory(OFVersion.OF_13);
        }

        reader = factory.getReader();
        this.port = port;
    }

    public void setTestHandShakeType(int type) {
        this.testHandShakeType = type;
    }

    public OFFactory getFactory() {
        return this.factory;
    }

    public byte[] readBytes() throws IOException {
        // Again, probably better to store these objects references in the
        // support class
        InputStream in = targetSock.getInputStream();
        DataInputStream dis = new DataInputStream(in);

        int len = dis.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return data;
    }


    public void printError(OFMessage msg) {
        System.out.println(msg.toString());
    }

    public void sendMsg(OFMessage msg, int len) {
        ByteBuf buf;

        if (len == -1) {
            buf = PooledByteBufAllocator.DEFAULT.directBuffer(1024);
        } else {
            buf = PooledByteBufAllocator.DEFAULT.directBuffer(len);
        }

        msg.writeTo(buf);

        int length = buf.readableBytes();
        byte[] bytes = new byte[length];
        buf.getBytes(buf.readerIndex(), bytes);

        try {
            this.out.write(bytes, 0, length);
        } catch (IOException e) {
            // TODO Auto-gaenerated catch block
            e.printStackTrace();
        }

        buf.clear();
        buf.release();
    }

    public void setReqXid(long xid) {
        requestXid = xid;
    }

    public OFMessage getResponse() {
        return res;
    }

    public void sendRawMsg(byte[] msg) {
        try {
            this.out.write(msg, 0, msg.length);
        } catch (IOException e) {
            // TODO Auto-gaenerated catch block
            e.printStackTrace();
        }
    }

    public OFFlowAdd getBackupFlowAdd() {
        return backupFlowAdd;
    }

    /* OF Message */
    public void sendHello(long xid) throws OFParseError {
        if (testHandShakeType == HANDSHAKE_NO_HELLO) {
            // this.sendFeatureReply(requestXid);
            return;
        }

        OFFactory factory = OFFactories.getFactory(OFVersion.OF_10);

        long r_xid = 0;

        if (xid == 0)
            r_xid = 0xeeeeeeeel;
        else
            r_xid = xid;

        OFHello.Builder fab = factory.buildHello();
        fab.setXid(r_xid);

        OFHello hello = fab.build();
        sendMsg(hello, MINIMUM_LENGTH);
    }

//    public void sendStatReply(long xid) {
//        byte[] msg = DummyOFData.hexStringToByteArray(DummyOFData.statsReply);
//        byte[] xidbytes = Longs.toByteArray(xid);
//        System.arraycopy(xidbytes, 4, msg, 4, 4);
//
//        sendRawMsg(msg);
//    }
//
//    public void sendFeatureReply(long xid) throws OFParseError {
//        byte[] msg = DummyOFData.hexStringToByteArray(DummyOFData.featureReply);
//        byte[] xidbytes = Longs.toByteArray(xid);
//        System.arraycopy(xidbytes, 4, msg, 4, 4);
//
//        sendRawMsg(msg);
//        return;
//    }
//
//    public void sendGetConfigReply(long xid) {
//        byte[] msg = DummyOFData.hexStringToByteArray(DummyOFData.getConfigReply);
//        byte[] xidbytes = Longs.toByteArray(xid);
//        System.arraycopy(xidbytes, 4, msg, 4, 4);
//
//        sendRawMsg(msg);
//    }
//
//    public void sendExperimenter(long xid) {
//        byte[] msg = DummyOFData.hexStringToByteArray(DummyOFData.experimenter);
//        byte[] xidbytes = Longs.toByteArray(xid);
//        System.arraycopy(xidbytes, 4, msg, 4, 4);
//
//        sendRawMsg(msg);
//    }
//
//    public void sendBarrierRes(long xid) {
//        OFBarrierReply.Builder builder = factory.buildBarrierReply();
//
//        builder.setXid(xid);
//        OFBarrierReply msg = builder.build();
//        sendMsg(msg, 8);
//    }
//
//    public void sendEchoReply(long xid) {
//        OFEchoReply.Builder builder = factory.buildEchoReply();
//        builder.setXid(xid);
//        OFEchoReply msg = builder.build();
//        sendMsg(msg, 8);
//    }
//
//    public void sendError() throws OFParseError {
//        long r_xid = 0xeeeeeeeel;
//
//        OFErrorMsgs msg = factory.errorMsgs();
//        OFFeaturesReply.Builder frb = factory.buildFeaturesReply();
//        OFFeaturesReply fr = frb.build();
//
//        return;
//    }
//
//    public boolean sendPacketIn() throws OFParseError {
//        byte[] msg = DummyOFData.hexStringToByteArray(DummyOFData.packetin);
//        byte[] xidbytes = Longs.toByteArray(0xeeeeeeeel);
//        System.arraycopy(xidbytes, 4, msg, 4, 4);
//
//        sendRawMsg(msg);
//        return true;
//    }

    public ByteBuf sendFlowRemoved() throws OFParseError {
        OFFactory factory = OFFactories.getFactory(OFVersion.OF_10);
        long r_xid = 0xeeeeeeeel;

        OFFlowRemoved.Builder fab = factory.buildFlowRemoved();
        fab.setXid(r_xid);
        OFFlowRemoved hello = fab.build();

        ByteBuf buf = null;
        buf = PooledByteBufAllocator.DEFAULT.directBuffer(88);
        hello.writeTo(buf);

        byte[] bytes;
        int length = buf.readableBytes();
        bytes = new byte[length];
        buf.getBytes(buf.readerIndex(), bytes);

        try {
            this.out.write(bytes, 0, length);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return buf;
    }

    public ByteBuf sendPortStatus() throws OFParseError {
        OFFactory factory = OFFactories.getFactory(OFVersion.OF_10);
        long r_xid = 0xeeeeeeeel;

        OFPortStatus.Builder fab = factory.buildPortStatus();
        fab.setXid(r_xid);
        OFPortStatus hello = fab.build();

        ByteBuf buf = null;
        buf = PooledByteBufAllocator.DEFAULT.directBuffer(64);
        hello.writeTo(buf);

        byte[] bytes;
        int length = buf.readableBytes();
        bytes = new byte[length];
        buf.getBytes(buf.readerIndex(), bytes);

        try {
            this.out.write(bytes, 0, length);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return buf;
    }

    public void setTargetSock(Socket socket) {
        this.targetSock = socket;

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listeningSwitch() {
        try {
            serverSock = new ServerSocket(this.port);
            Socket temp = serverSock.accept();
            log.info(temp.toString());
            setTargetSock(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getHandshaked() {
        return this.handshaked;
    }


    public boolean parseOFMsg(byte[] recv, int len) throws OFParseError {
        // for OpenFlow Message
        byte[] rawMsg = new byte[len];
        System.arraycopy(recv, 0, rawMsg, 0, len);
        ByteBuf bb = Unpooled.copiedBuffer(rawMsg);

        int totalLen = bb.readableBytes();
        int offset = bb.readerIndex();

        while (offset < totalLen) {
            bb.readerIndex(offset);

            byte version = bb.readByte();
            bb.readByte();
            int length = U16.f(bb.readShort());
            bb.readerIndex(offset);

            if (length < MINIMUM_LENGTH)
                throw new OFParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);

            try {
                OFMessage message = reader.readFrom(bb);
                // log.info(message.toString());
                long xid = message.getXid();

                if (message.getType() == OFType.HELLO) {
                    sendHello(startXid);
                } else {
                    // log.info(message.toString());
                }

                if (xid == this.requestXid) {
                    res = message;
                }

            } catch (OFParseError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            offset += length;
        }

        bb.clear();
        return true;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        byte[] recv;
        int readlen;
        boolean ack = false;

        try {
            while (true) {
                recv = new byte[2048];
                if ((readlen = in.read(recv, 0, recv.length)) != -1) {
                    parseOFMsg(recv, readlen);
                } else
                    break; // end of connection
            }
        } catch (Exception e) {
            // if any error occurs
            // e.printStackTrace();

            if (in != null)
                try {
                    in.close();
                    log.info("Closed from Switch");
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }
}
