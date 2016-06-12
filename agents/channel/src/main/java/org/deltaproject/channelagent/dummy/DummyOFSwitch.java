package org.deltaproject.channelagent.dummy;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFAggregateStatsReply;
import org.projectfloodlight.openflow.protocol.OFBarrierReply;
import org.projectfloodlight.openflow.protocol.OFConfigFlags;
import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFeaturesReply;
import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.protocol.OFGetConfigReply;
import org.projectfloodlight.openflow.protocol.OFHello;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPortStatus;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.errormsg.OFErrorMsgs;
import org.projectfloodlight.openflow.protocol.ver10.OFPacketInReasonSerializerVer10;
import org.projectfloodlight.openflow.types.U16;

import com.google.common.primitives.Longs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

public class DummyOFSwitch extends Thread {
	static final int MINIMUM_LENGTH = 8;

	private Socket socket;
	private InputStream in;
	private OutputStream out;

	private Random random;

	/* for target controller */
	private String IP = "";
	private int PORT = 0;

	/* for OF message */
	OFFactory factory;

	OFMessageReader<OFMessage> reader;

	public DummyOFSwitch() {
		random = new Random();
	}

	public void connectTargetController(String cip, String ofPort) {
		this.IP = cip;
		this.PORT = Integer.parseInt(ofPort);

		try {
			socket = new Socket(IP, PORT);
			socket.setReuseAddress(true);

			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setOFFactory(byte ofVersion) {
		if (ofVersion == 0)
			factory = OFFactories.getFactory(OFVersion.OF_10);
		else
			factory = OFFactories.getFactory(OFVersion.OF_13);
		
		reader = factory.getReader();
	}

	public byte[] readBytes() throws IOException {
		// Again, probably better to store these objects references in the
		// support class
		InputStream in = socket.getInputStream();
		DataInputStream dis = new DataInputStream(in);

		int len = dis.readInt();
		byte[] data = new byte[len];
		if (len > 0) {
			dis.readFully(data);
		}
		return data;
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
				long xid = message.getXid();

				if (message.getType() == OFType.FEATURES_REQUEST) {
					sendFeatureReply(xid);
				} else if (message.getType() == OFType.GET_CONFIG_REQUEST) {
					sendGetConfigReply(xid);
				} else if (message.getType() == OFType.BARRIER_REQUEST) {
					sendBarrierRes(xid);
				} else if (message.getType() == OFType.STATS_REQUEST) {
					sendStatReply(xid);
				} else if (message.getType() == OFType.EXPERIMENTER) {
					sendExperimenter(xid);
				} else if (message.getType() == OFType.ECHO_REQUEST) {
					sendEchoReply(xid);
					fuzzPacketIn();
				} else if (message.getType() == OFType.ERROR) {
					printError(message);
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

	public void printError(OFMessage msg) {
		System.out.println(msg.toString());		
	}
	
	public void sendMsg(OFMessage msg, int len) {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(len);
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
		buf = null;
	}

	public void sendRawMsg(byte[] msg) {
		try {
			this.out.write(msg, 0, msg.length);
		} catch (IOException e) {
			// TODO Auto-gaenerated catch block
			e.printStackTrace();
		}
	}

	/* OF Message */
	public void sendHello() throws OFParseError {
		OFFactory factory = OFFactories.getFactory(OFVersion.OF_10);
		long r_xid = 0xeeeeeeeel;

		OFHello.Builder fab = factory.buildHello();
		fab.setXid(r_xid);

		OFHello hello = fab.build();
		sendMsg(hello, MINIMUM_LENGTH);

		return;
	}

	public void sendStatReply(long xid) {
		byte[] msg = DummyData.hexStringToByteArray(DummyData.statsReply);
		byte[] xidbytes = Longs.toByteArray(xid);
		System.arraycopy(xidbytes, 4, msg, 4, 4);

		sendRawMsg(msg);
	}

	public void sendFeatureReply(long xid) throws OFParseError {
		byte[] msg = DummyData.hexStringToByteArray(DummyData.featureReply);
		byte[] xidbytes = Longs.toByteArray(xid);
		System.arraycopy(xidbytes, 4, msg, 4, 4);

		sendRawMsg(msg);
		return;
	}

	public void sendGetConfigReply(long xid) {
		byte[] msg = DummyData.hexStringToByteArray(DummyData.getConfigReply);
		byte[] xidbytes = Longs.toByteArray(xid);
		System.arraycopy(xidbytes, 4, msg, 4, 4);

		sendRawMsg(msg);
	}

	public void sendExperimenter(long xid) {
		byte[] msg = DummyData.hexStringToByteArray(DummyData.experimenter);
		byte[] xidbytes = Longs.toByteArray(xid);
		System.arraycopy(xidbytes, 4, msg, 4, 4);

		sendRawMsg(msg);
	}

	public void sendBarrierRes(long xid) {
		OFBarrierReply.Builder builder = factory.buildBarrierReply();

		builder.setXid(xid);
		OFBarrierReply msg = builder.build();
		sendMsg(msg, 8);
	}

	public void sendEchoReply(long xid) {
		OFEchoReply.Builder builder = factory.buildEchoReply();
		builder.setXid(xid);
		OFEchoReply msg = builder.build();
		sendMsg(msg, 8);
	}

	public void sendError() throws OFParseError {
		long r_xid = 0xeeeeeeeel;

		OFErrorMsgs msg = factory.errorMsgs();
		OFFeaturesReply.Builder frb = factory.buildFeaturesReply();
		OFFeaturesReply fr = frb.build();

		return;
	}

	public boolean sendPacketIn() throws OFParseError {
		byte[] msg = DummyData.hexStringToByteArray(DummyData.packetin);
		byte[] xidbytes = Longs.toByteArray(0xeeeeeeeel);
		System.arraycopy(xidbytes, 4, msg, 4, 4);

		sendRawMsg(msg);
		return true;
	}

	public void fuzzPacketIn() throws OFParseError {
		/*
		 * length - 2 bytes xid - 4 bytes buffer_id - 4 bytes total_len - 2
		 * bytes in_port - 2 bytes reason - 1 byte pad - 1 byte
		 */
		
		byte[] msg = DummyData.hexStringToByteArray(DummyData.packetin);
//		msg[16] = (byte) 0xff;
		msg[14] = (byte) 0xff;
		msg[15] = (byte) 0xff;
		
		sendRawMsg(msg);

		return;
	}

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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		byte[] recv;
		int readlen = 0;
		boolean synack = false;

		try {
			while (true) {
				recv = new byte[2048];
				if ((readlen = in.read(recv, 0, recv.length)) != -1) {
					if (!synack) {
						synack = true;
						sendHello();
					} else {
						/* after hello */
						parseOFMsg(recv, readlen);
					}
				} else
					break; // end of connection
			}
		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();

			if (in != null)
				try {
					in.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}