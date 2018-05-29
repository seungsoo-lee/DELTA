package org.deltaproject.channelagent.pkthandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.ByteArrays;
import org.pcap4j.util.MacAddress;

public class GetAddr {
    public static String getMacString(MacAddress mac) {
        StringBuilder sb = new StringBuilder(18);
        for (byte b : mac.getAddress()) {
            if (sb.length() > 0)
                sb.append(':');
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static MacAddress getLocalMac(InetAddress ip) throws IOException {
        NetworkInterface inter = null;
        while ((inter = NetworkInterface.getByInetAddress(ip)) == null) ;
        byte[] mac = inter.getHardwareAddress();
        return MacAddress.getByAddress(mac);
    }

    public static MacAddress getMac(PcapHandle handle, InetAddress localIP, MacAddress localMac, InetAddress ip) throws IOException, PcapNativeException, NotOpenException {
        handle.sendPacket(buildArpPacket(ArpOperation.REQUEST, localIP, ip, localMac, MacAddress.ETHER_BROADCAST_ADDRESS));
        while (true) {
            Packet packet = handle.getNextPacket();
            if (packet == null) continue;
            ArpPacket arp = packet.get(ArpPacket.class);
            if (arp.getHeader().getSrcProtocolAddr().equals(ip) && arp.getHeader().getOperation().equals(ArpOperation.REPLY)) {
                return arp.getHeader().getSrcHardwareAddr();
            }
        }
    }

    public static InetAddress getGateWayIP(String localIP) throws IOException {
        Process result = Runtime.getRuntime().exec("ipconfig");

        BufferedReader output = new BufferedReader(new InputStreamReader(result.getInputStream()));
        String str = new String();
        while ((str = output.readLine()) != null) {
            if (str.indexOf(localIP) >= 0) {
                str = output.readLine();
                str = output.readLine();
                int length = str.length();
                for (int i = 0; i < length; i++) {
                    if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
                        str = str.substring(i);
                        return InetAddress.getByName(str);
                    }
                }
            }
        }
        return InetAddress.getByName(str);
    }

    private static Packet buildArpPacket(ArpOperation type, InetAddress srcIP, InetAddress dstIP, MacAddress srcMac, MacAddress dstMac) {

        ArpPacket.Builder arpBuilder = new ArpPacket.Builder();
        arpBuilder
                .hardwareType(ArpHardwareType.ETHERNET)
                .protocolType(EtherType.IPV4)
                .hardwareAddrLength((byte) MacAddress.SIZE_IN_BYTES)
                .protocolAddrLength((byte) ByteArrays.INET4_ADDRESS_SIZE_IN_BYTES)
                .operation(type)
                .srcHardwareAddr(srcMac)
                .srcProtocolAddr(srcIP)
                .dstHardwareAddr(dstMac)
                .dstProtocolAddr(dstIP);

        EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
        etherBuilder
                .dstAddr(dstMac)
                .srcAddr(srcMac)
                .type(EtherType.ARP)
                .payloadBuilder(arpBuilder)
                .paddingAtBuild(true);

        return etherBuilder.build();
    }
}