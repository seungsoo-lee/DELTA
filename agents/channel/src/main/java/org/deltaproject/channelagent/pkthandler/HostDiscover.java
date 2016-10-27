package org.deltaproject.channelagent.pkthandler;

import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * This class finds out 
 * the online hosts in the network, it can take a list with the targets
 * you want to resolve(only some specific ips) else if you don't provide specific hosts
 * to resolve then it scans all the hosts in the network, for a big network( eg a university's network) it may take 
 * a loooong time to scan all of them, but for networks with mask /24 (only 256 hosts) it's really fast
 */
public class HostDiscover {
	private static HashMap<String, String> ip_mac_list;
	private static NetworkInterface device;
	private static byte[] gatewaymac;
	private static String gatewayip;
	private Listener ARPListener;
	private Sender ARPSender;
	private ArrayList<String> iplist;
	private PacketReceiver handler;
	private int sleepy;

	public HostDiscover(NetworkInterface mydevice, ArrayList<String> ips_to_explore) {
		this.handler = new packet_handler();
		device = mydevice;
		ip_mac_list = new HashMap<String, String>();
		this.sleepy = 100;
		gatewaymac = null;
		gatewayip = null;

		try {
			this.ARPListener = new Listener(device, this.handler);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			this.ARPSender = new Sender(device);
		} catch (Exception e) {
			e.printStackTrace();
		}

		NetworkInterfaceAddress inet4 = this.get_inet4(device);
		this.iplist = ips_to_explore;

		/*
		 * if (ips_to_explore != null) { if (ips_to_explore.isEmpty()) {
		 * this.iplist = LANExplorer.getIPs(inet4.address.toString()
		 * .split("/")[1], inet4.subnet.toString().split("/")[1]); } else {
		 * this.iplist = ips_to_explore; } }
		 */
	}

	public void discover() {
		InetAddress pingAddr;
		// System.out.println("[Channel-Agent] Attempting to resolve
		// gateway...");

		// try {
		// pingAddr = InetAddress.getByName("www.google.com");
		// this.ARPListener.setFilter("tcp and dst host " +
		// pingAddr.getHostAddress(), true);
		this.ARPListener.getListener().setPacketReadTimeout(5000);
		this.ARPListener.start();
		/*
		 * while(true){ try { new
		 * URL("http://www.google.com").openStream().close(); }catch (Exception
		 * e){ e.printStackTrace(); }
		 * 
		 * if(gatewaymac != null) break; }
		 */

		// } catch (UnknownHostException e1) {
		// e1.printStackTrace();
		// }

		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		this.ARPListener.setFilter("arp", true);

		// for (String ipaddr : this.iplist.subList(200, 202)) {

		String ipaddr = iplist.get(0);
		{
			ARPPkt packet = new ARPPkt(device);
			try {
				// Thread.currentThread();
				Thread.sleep(this.sleepy);

				ARPPacket pack = packet.build_request_packet(ipaddr);
				// System.out.printf("\rInterrogating " + ipaddr);

				this.ARPSender.send(pack);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		ipaddr = iplist.get(1);
		{
			ARPPkt packet = new ARPPkt(device);
			try {
				// Thread.currentThread();
				Thread.sleep(this.sleepy);

				ARPPacket pack = packet.build_request_packet(ipaddr);
				// System.out.printf("\rInterrogating " + ipaddr);

				this.ARPSender.send(pack);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		/*
		 * System.out.println(); System.out.println(
		 * "[Channel-Agent] Hosts Found: " + ip_mac_list.size());
		 * System.out.println("[Channel-Agent] " +
		 * ip_mac_list.keySet().toString() + "\n");
		 * 
		 * if(gatewayip == null){ System.out.println(
		 * "ERROR: No gateway found, try again later.."); System.exit(1); }
		 */
		// stop the thread listening to ARP PACKETS
		this.ARPListener.finish();
	}

	private NetworkInterfaceAddress get_inet4(NetworkInterface device) throws NullPointerException {
		if (device == null)
			throw new NullPointerException("ERROR: No device has been given! Potato");

		for (NetworkInterfaceAddress addr : device.addresses)
			if (addr.address instanceof Inet4Address)
				return addr;

		return null;
	}

	public HashMap<String, String> getHosts() {
		return ip_mac_list;
	}

	public String getGatewayIp() {
		return gatewayip;
	}

	class packet_handler implements PacketReceiver {
		public void receivePacket(Packet p_temp) {
			if (p_temp instanceof ARPPacket) {
				ARPPacket p = (ARPPacket) p_temp;
				if (p.operation == ARPPacket.ARP_REPLY) {

					String srcip = p.getSenderProtocolAddress().toString().split("/")[1];
					if (ip_mac_list.containsKey(srcip))
						return;

					if (gatewayip == null) {
						if (Arrays.equals(gatewaymac, p.sender_hardaddr)) {
							gatewayip = srcip;
							System.out.println("IP FOUND: " + gatewayip);
						}
					}
					ip_mac_list.put(srcip, p.getSenderHardwareAddress().toString());

					// System.out.printf("[Channel-Agent] HOST DISCOVERED\t-\t
					// IP %s (MAC: %s)\n",
					// p.getSenderProtocolAddress().toString().split("/")[1],
					// p.getSenderHardwareAddress());
				}
			} else if (p_temp instanceof TCPPacket) {
				if (!Arrays.equals(((EthernetPacket) p_temp.datalink).dst_mac, device.mac_address))
					gatewaymac = ((EthernetPacket) p_temp.datalink).dst_mac;
			}
		}
	}
}