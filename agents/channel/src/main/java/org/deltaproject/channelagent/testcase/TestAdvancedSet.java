package org.deltaproject.channelagent.testcase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import org.deltaproject.channelagent.core.Utils;
import org.deltaproject.channelagent.networknode.NetworkInfo;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U16;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestAdvancedSet {
	static final int MINIMUM_LENGTH = 8;

	private OFFactory factory;
	private OFMessageReader<OFMessage> reader;
	private byte ofversion;

	public TestAdvancedSet(OFFactory f, OFMessageReader<OFMessage> r, byte of) {
		this.factory = f;
		this.reader = r;
		this.ofversion = of;
	}

	public ByteBuf getByteBuf(Packet p_temp) {
		// for OpenFlow Message
		byte[] rawMsg = new byte[p_temp.data.length];
		System.arraycopy(p_temp.data, 0, rawMsg, 0, p_temp.data.length);
		// ByteBuf byteMsg = Unpooled.copiedBuffer(rawMsg);

		return Unpooled.wrappedBuffer(rawMsg);
	}

	public boolean testEvaseDrop(NetworkInfo nodes, Packet p_temp) throws OFParseError {
		ByteBuf bb = getByteBuf(p_temp);

		int totalLen = bb.readableBytes();
		int offset = bb.readerIndex();

		EthernetPacket p_eth = (EthernetPacket) p_temp.datalink;

		// check if the packet is mine just return do not send it again
		String incoming_src_mac = Utils.decalculate_mac(p_eth.src_mac);

		IPPacket p = ((IPPacket) p_temp);
		String src_ip = p.src_ip.toString().split("/")[1];

		while (offset < totalLen) {
			bb.readerIndex(offset);

			byte version = bb.readByte();
			bb.readByte();
			int length = U16.f(bb.readShort());
			bb.readerIndex(offset);

			if (version != this.ofversion) {
				// segmented TCP pkt
				System.out.println("OFVersion Missing " + offset + ":" + totalLen);
				return false;
			}

			if (length < MINIMUM_LENGTH)
				throw new OFParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);

			try {
				OFMessage message = reader.readFrom(bb);

				if (message == null)
					return false;

				if (message.getType() == OFType.PACKET_IN) {
					OFPacketIn fi = (OFPacketIn) message;
					fi.getData();
					EthernetPacket temp = new EthernetPacket();

					NetworkInfo child = new NetworkInfo();
					// TCP PDU SIZE
					// p.length : total length value from ip packet
					// p.header.length : sum of header length of each layer
					// (length of ethernet header = 14)
					int tcpPDUsize = p.length - (p.header.length - 14);

					// if type code is 0x0800 (IP) ..
					if ((p.data)[30] == 0x08 && (p.data)[31] == 0x00) {
						System.out.println(fi.toString());
						int portNum = (p.data)[15];
						int ipArray[] = new int[4];
						ipArray[0] = (p.data)[44];
						ipArray[1] = (p.data)[45];
						ipArray[2] = (p.data)[46];
						ipArray[3] = (p.data)[47];

						for (int i = 0; i < ipArray.length; i++)
							if (ipArray[i] < 0)
								ipArray[i] += 256;

						if (nodes.getNodeType() == nodes.isEmpty) {
							nodes.setMacAddr(incoming_src_mac);
							nodes.setIpAddr(src_ip);
							nodes.setPortNum(nodes.isEmpty);
							nodes.setNodeType(nodes.isHost);
						}
						child.setPortNum(portNum);
						child.setMacAddr(Utils.decalculate_mac(Arrays.copyOfRange(p.data, 24, 30)));
						child.setIpAddr(ipArray[0] + "." + ipArray[1] + "." + ipArray[2] + "." + ipArray[3]);
						child.setNodeType(child.isHost);

						if (!(nodes.insertNode(child, src_ip, incoming_src_mac))) {
							// System.out.println("There are no parents!");
						}
					}

					// if type code is 0x88cc (LLDP) and TCP PDU size is 285
					if ((p.data)[30] == -120 && (p.data)[31] == -52 && tcpPDUsize == 285) {
						System.out.println(fi.toString());
						int portNum = (p.data)[15];
						int ipArray[] = new int[4];
						ipArray[0] = (p.data)[245];
						ipArray[1] = (p.data)[246];
						ipArray[2] = (p.data)[247];
						ipArray[3] = (p.data)[248];

						for (int i = 0; i < ipArray.length; i++)
							if (ipArray[i] < 0)
								ipArray[i] += 256;

						if (nodes.getNodeType() == nodes.isEmpty) {
							nodes.setMacAddr(incoming_src_mac);
							nodes.setIpAddr(src_ip);
							nodes.setPortNum(nodes.isEmpty);
							nodes.setNodeType(nodes.isSwitch);
						}
						child.setPortNum(portNum);
						child.setMacAddr(Utils.decalculate_mac(Arrays.copyOfRange(p.data, 35, 41)));
						child.setIpAddr(ipArray[0] + "." + ipArray[1] + "." + ipArray[2] + "." + ipArray[3]);
						child.setNodeType(child.isSwitch);

						if (!(nodes.insertNode(child, src_ip, incoming_src_mac))) {
							// System.out.println("There are no parents!");
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
				System.out.println("OFVersion Missing " + version+" : "+offset + "-" + totalLen);
				return null;
			}

			if (length < MINIMUM_LENGTH)
				throw new OFParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);

			try {
				OFMessage message = reader.readFrom(bb);

				if (message == null)
					return null;

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
						
						if(buf == null)
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
					
					if(buf == null)
						buf = PooledByteBufAllocator.DEFAULT.directBuffer(totalLen);
					
					newoutput.writeTo(buf);
					// System.out.println("after " + newout.toString());
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
