package org.deltaproject.channelagent.dummy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFeaturesReply;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFHello;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U16;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import jpcap.packet.Packet;

public class DummyOFSwitch extends Thread {
	static final int MINIMUM_LENGTH = 8;

	private Socket socket;
	private InputStream in;
	private OutputStream out;

	/* for target controller */
	private String IP = "";
	private int PORT = 0;
	
	/* for OF message */
	OFFactory factory;
	OFMessageReader<OFMessage> reader;
	
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
		if(ofVersion == 1)
			factory = OFFactories.getFactory(OFVersion.OF_10);
		
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

	public void parseOFMessage(Packet p) {

	}

	public static ByteBuf getByteBuf(Packet p_temp) {
		// for OpenFlow Message
		byte[] rawMsg = new byte[p_temp.data.length];
		System.arraycopy(p_temp.data, 0, rawMsg, 0, p_temp.data.length);
		// ByteBuf byteMsg = Unpooled.copiedBuffer(rawMsg);

		return Unpooled.wrappedBuffer(rawMsg);
	}

	public ByteBuf sendHello() throws OFParseError {
		OFFactory factory = OFFactories.getFactory(OFVersion.OF_10);
		long r_xid = 0xeeeeeeeel;

		OFHello.Builder fab = factory.buildHello();
		fab.setXid(r_xid);

		OFHello hello = fab.build();

		ByteBuf buf = null;
		buf = PooledByteBufAllocator.DEFAULT.directBuffer(8);
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

	public ByteBuf sendFeatureRes() throws OFParseError {
		
		long r_xid = 0xeeeeeeeel;

		OFFeaturesReply.Builder frb = factory.buildFeaturesReply();
		OFFeaturesReply hello = frb.build();

		ByteBuf buf = null;
		buf = PooledByteBufAllocator.DEFAULT.directBuffer(8);
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

	public static ByteBuf testMITM(Packet p_temp) throws OFParseError {
		OFFactory factory = OFFactories.getFactory(OFVersion.OF_10);
		OFMessageReader<OFMessage> reader = factory.getReader();

		byte ofversion = 1;
		ByteBuf bb = getByteBuf(p_temp);
		int totalLen = bb.readableBytes();
		int offset = bb.readerIndex();

		OFFlowMod newfm = null;
		OFPacketOut newoutput = null;

		ByteBuf buf = null;

		while (offset < totalLen) {
			bb.readerIndex(offset);

			byte version = bb.readByte();
			bb.readByte();
			int length = U16.f(bb.readShort());
			bb.readerIndex(offset);

			if (version != ofversion) {
				// segmented TCP pkt
				System.out.println("OFVersion Missing " + version + " : " + offset + "-" + totalLen);
				return null;
			}

			if (length < MINIMUM_LENGTH)
				throw new OFParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);

			OFMessage message = reader.readFrom(bb);

			if (message == null)
				return null;

			System.out.println(message.toString());
			offset += length;
		}

		bb.clear();
		return buf;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		byte[] recv;

		boolean synack = false;

		try {
			while (true) {
				recv = new byte[2048];
				if (in.read(recv, 0, recv.length) != -1) {
					if (!synack) {
						synack = true;
						sendHello();
					} else {
						/* after hello */
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