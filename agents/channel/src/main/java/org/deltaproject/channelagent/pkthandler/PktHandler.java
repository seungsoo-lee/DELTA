package org.deltaproject.channelagent.pkthandler;

import io.netty.buffer.ByteBuf;
import org.deltaproject.channelagent.fuzzing.TestFuzzing;
import org.deltaproject.channelagent.networknode.TopoInfo;
import org.deltaproject.channelagent.testcase.TestCase;
import org.deltaproject.channelagent.fuzzing.SeedPackets;
import org.deltaproject.channelagent.testcase.TestAdvancedCase;
import org.deltaproject.channelagent.utils.Utils;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.util.MacAddress;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PktHandler {
    private static class Task implements Runnable {
        private PcapHandle handle;
        private PacketListener listener;

        public Task(PcapHandle handle, PacketListener listener) {
            this.handle = handle;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                handle.loop(COUNT, listener);
            } catch (PcapNativeException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NotOpenException e) {
                e.printStackTrace();
            }
        }
    }

    class PktListner implements PacketListener {
        private PcapHandle sendHandle;

        private String dl_src;
        private String dl_dst;
        private String dst_ip;
        private String src_ip;

        Map<Long, byte[]> tcpBodys = new HashMap<>();

        public PktListner() {
            try {
                sendHandle = device.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeout);
            } catch (PcapNativeException e) {
                e.printStackTrace();
            }
        }

        // for fragmented tcp data
        private byte[] addBodyData(TcpPacket packet) {
            byte[] tcpBodyData;
            Long ack = (long) packet.getHeader().getAcknowledgmentNumber();

            if (tcpBodys.containsKey(ack)) {
                byte[] a = tcpBodys.get(ack);   // already
                byte[] b = packet.getPayload().getRawData().clone(); // new

                // update = already + new
                tcpBodyData = new byte[a.length + b.length];

                System.arraycopy(a, 0, tcpBodyData, 0, a.length);
                System.arraycopy(b, 0, tcpBodyData, a.length, b.length);

                tcpBodys.put(ack, tcpBodyData);
            } else {
                tcpBodyData = packet.getPayload().getRawData().clone();
                tcpBodys.put(ack, tcpBodyData);
            }

            if (packet.getHeader().getPsh()) {
                tcpBodys.remove(ack);
            }

            return tcpBodyData;
        }

        private Packet spoofPacket(Packet p, String dstIp) {
            byte[] dstmac = Utils.calculate_mac(ip_mac_list.get(dstIp));
            byte[] srcmac = device.getLinkLayerAddresses().get(0).getAddress();

            Packet.Builder b = p.getBuilder();
            b.get(EthernetPacket.Builder.class).srcAddr(MacAddress.getByAddress(srcmac));
            b.get(EthernetPacket.Builder.class).dstAddr(MacAddress.getByAddress(dstmac));

            return b.build();
        }

        public void sendPkt(Packet p_temp) {
            try {
                // switch -> [channel agent] -> controller
                if (this.dst_ip.equals(controllerIp)) {
                    sendHandle.sendPacket(spoofPacket(p_temp, controllerIp));
                }

                // controller -> [channel agent] -> switch
                if (this.dst_ip.equals(switchIp)) {
                    sendHandle.sendPacket(spoofPacket(p_temp, switchIp));
                }
            } catch (PcapNativeException e) {
                e.printStackTrace();
            } catch (NotOpenException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void gotPacket(Packet p_temp) {
            //System.out.println("[Receive]" + p_temp);

            EthernetPacket p_eth = p_temp.get(EthernetPacket.class);

            // check if the packet is channel-agent's, return it
            String mine_mac = Utils.decalculate_mac(device.getLinkLayerAddresses().get(0).getAddress());
            String src_mac = Utils.decalculate_mac(p_eth.getHeader().getSrcAddr().getAddress());

            if (mine_mac.equals(src_mac)) {
                return;
            }

            this.dl_src = Utils.decalculate_mac(p_eth.getHeader().getSrcAddr().getAddress());
            this.dl_dst = Utils.decalculate_mac(p_eth.getHeader().getDstAddr().getAddress());

            IpV4Packet p_ip = p_temp.get(IpV4Packet.class);

            this.src_ip = p_ip.getHeader().getSrcAddr().getHostAddress();
            this.dst_ip = p_ip.getHeader().getDstAddr().getHostAddress();

            // ignore channel-agent's packets
            if (this.dst_ip.equals(localIp) || this.src_ip.equals(localIp)) {
                return;
            }

            TcpPacket p_tcp = p_temp.get(TcpPacket.class);
            Packet payload = p_tcp.getPayload();

            // if tcp payload is empty, return it
            if (payload == null)
                return;

            byte[] tcpbody = addBodyData(p_tcp);
            if (p_tcp.getHeader().getPsh()) {
                // body is complete, do something here

                //p_temp.data = body;
                ByteBuf newBuf = null;

                switch (attackType) {
                    case TestCase.EVAESDROP:
                        break;

                    case TestCase.LINKFABRICATION:
                        break;

                    case TestCase.MITM:
                        break;

                    case TestCase.CONTROLMESSAGEMANIPULATION:
                        break;

                    case TestCase.MALFORMEDCONTROLMESSAGE:
                        break;

                    default:
                        break;
                }

//                if (attackType == TestCase.EVAESDROP) {
//                    this.sendPkt(p_temp);
//
//                    if (p_temp.data.length > 8) {
//                        try {
//                            testAdvanced.testEvaseDrop(topo, p_temp);
//                            return;
//                        } catch (OFParseError e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                    }
//                } else if (attackType == TestCase.LINKFABRICATION) {
//                    if (p_temp.data.length > 8) {
//                        try {
//                            newBuf = testAdvanced.testLinkFabrication(p_temp);
//
//                            if (newBuf != null) {
//                                p_temp.data = byteBufToArray(newBuf);
//                                sendPkt(p_temp);
//                                return;
//                            }
//                        } catch (OFParseError e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                    }
//                } else if (attackType == TestCase.MITM) {
//                    if (p_temp.data.length > 8) {
//                        try {
//                            if (this.src_ip.equals(controllerIP) && this.dst_ip.equals(switchIP)) {
//                                newBuf = testAdvanced.testMITM(p_temp);
//
//                                if (newBuf != null) {
//                                    p_temp.data = byteBufToArray(newBuf);
//                                    sendPkt(p_temp);
//                                    return;
//                                }
//                            }
//                        } catch (OFParseError e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                    }
//                } else if (attackType == TestCase.CONTROLMESSAGEMANIPULATION) {
//                    log.info("\n[ATTACK] Control Message Manipulation");
//                    /* Modify a Packet Here */
//                    if (this.dst_ip.equals(controllerIP)) {
//                        (p.data)[2] = 0x77;
//                        (p.data)[3] = 0x77;
//                    }
//                } else if (attackType == TestCase.MALFORMEDCONTROLMESSAGE) {
//                    log.info("\n[ATTACK] Malformed Control Message");
//                    /* Modify a Packet Here */
//                    if (this.dst_ip.equals(switchIP)) {
//                        // if ( (p.data)[1] != 0x0a ) {
//                        (p.data)[2] = 0x00;
//                        (p.data)[3] = 0x01;
//                        // }
//                    }
//                }
//                sendPkt(p_temp);
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(PktHandler.class);
    private static final int COUNT = 5000;

    public static final int MINIMUM_LENGTH = 8;

    private static HashMap<String, String> ip_mac_list;
    private static PcapNetworkInterface device;
    private static ArrayList<String> ips_to_explore;

    // used to filter packets for and from the attacker
    private static String localIp;

    // victim A, B
    private static String controllerIp;
    private static String switchIp;

    private PcapHandle receiveHandle;

    private String output;
    private TopoInfo topo;
    private ARPSpoof arpSpoof;

    // flags for distinguish the kind of attacks
    private int attackType;
    private String ofPort;
    private byte ofversion;

    private TestAdvancedCase testAdvanced;
    private TestFuzzing testFuzzing;

    private SeedPackets seedPkts;

    private int snapshotLength = 65536; // in bytes
    private int readTimeout = 50; // in milliseconds

    public PktHandler(PcapNetworkInterface mydevice, String controllerip, String switchip, byte OFversion, String port) {
        ofversion = OFversion;
        device = mydevice;
        ofPort = port;

        // set IP list
        localIp = mydevice.getAddresses().get(0).getAddress().getHostAddress();
        controllerIp = controllerip;
        switchIp = switchip;

        topo = new TopoInfo();

        // set OF version
        OFFactory factory = null;
        if (OFversion == 0x01)
            factory = OFFactories.getFactory(OFVersion.OF_10);
        else if (OFversion == 0x04)
            factory = OFFactories.getFactory(OFVersion.OF_13);
        if (factory != null) {
            testAdvanced = new TestAdvancedCase(factory, this.ofversion);
            testFuzzing = new TestFuzzing(factory, this.ofversion);
        }

        attackType = TestCase.EMPTY;
        seedPkts = new SeedPackets(factory);

        try {
            startListening();
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }
    }

    public void startListening() throws PcapNativeException {
        log.info("[Channel Agent] Start listening packets");

        receiveHandle = device.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeout);
        try {
            receiveHandle.setFilter("tcp", BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (NotOpenException e) {
            e.printStackTrace();
        }

        ExecutorService pool = Executors.newSingleThreadExecutor();
        PktListner listener = new PktListner();
        Task t = new Task(receiveHandle, listener);
        pool.execute(t);
    }

    public void startARPSpoofing() {
        log.info("[Channel-Agent] Start ARP Spoofing");

        arpSpoof = new ARPSpoof(device);
        arpSpoof.setIps(localIp, controllerIp, switchIp);
        arpSpoof.run();
    }

    public void stopARPSpoofing() {
        log.info("[Channel-Agent] Stop ARP Spoofing");

        arpSpoof.setARPspoof(false);
    }

    public void setARPspoofing(boolean value) {
        arpSpoof.setARPspoof(value);
    }

    public SeedPackets getSeedPackets() {
        return this.seedPkts;
    }

    public String getTopoInfo() {
        if (topo == null)
            return "null";
        else
            return topo.getTopoInfo();
    }

    public void setIpsToExplore(String contip, String switchip) {
        ips_to_explore.add(contip);
        ips_to_explore.add(switchip);
    }

    public String getOutput() {
        return this.output;
    }

    public int getTypeOfAttacks() {
        return attackType;
    }

    public void setTypeOfAttacks(int attackType) {
        this.attackType = attackType;
    }

    public boolean testSwitchIdentification() {
        testAdvanced.testSwitchIdentificationSpoofing(this.controllerIp, this.ofPort, this.ofversion);
        return true;
    }


    class middle_handler {
        private String dl_src;
        private String dl_dst;
        private String dst_ip;
        private String src_ip;

        private boolean isTested = false;
//        Map<Long, TCPBodyData> tcpBodys = new HashMap<Long, TCPBodyData>();
//        // for fragmented tcp data
//        private class TCPBodyData {
//            byte[] bytes = null;
//
//            public TCPBodyData(byte[] bytes) {
//                this.bytes = bytes;
//            }
//
//            public void addBytes(byte[] bytes) {
//                try {
//                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                    outputStream.write(this.bytes);
//                    outputStream.write(bytes);
//                    this.bytes = outputStream.toByteArray();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            public byte[] getBytes() {
//                return bytes;
//            }
//        }
//
//        private byte[] addBodyData(TCPPacket packet) {
//            TCPBodyData tcpBodyData;
//            Long ack = new Long(packet.ack_num);
//
//            if (tcpBodys.containsKey(ack)) {
//                tcpBodyData = tcpBodys.get(ack);
//                tcpBodyData.addBytes(packet.data);
//            } else {
//                tcpBodyData = new TCPBodyData(packet.data);
//                tcpBodys.put(ack, tcpBodyData);
//            }
//
//            if (packet.psh) {
//                tcpBodys.remove(ack);
//            }
//
//            return tcpBodyData.getBytes();
//        }
//
//        public void sendPkt(Packet p_temp) {
//            if (this.dst_ip.equals(controllerIP)) {
//                traffic_sender.send(spoofPacket(p_temp, controllerIP));
//            } else if (this.dst_ip.equals(switchIP)) {
//                traffic_sender.send(spoofPacket(p_temp, switchIP));
//            }
//        }
//
//        public byte[] byteBufToArray(ByteBuf newBuf) {
//            byte[] bytes;
//            int length = newBuf.readableBytes();
//
//            if (newBuf.hasArray()) {
//                bytes = newBuf.array();
//            } else {
//                bytes = new byte[length];
//                newBuf.getBytes(newBuf.readerIndex(), bytes);
//            }
//
//            // replace packet data
//            newBuf.clear();
//            return bytes;
//        }
//
//        private Packet spoofPacket(Packet p, String victim) {
//            EthernetPacket p_eth = (EthernetPacket) p.datalink;
//            EthernetPacket ether = new EthernetPacket();
//            ether.frametype = p_eth.frametype;
//
//            ether.src_mac = device.mac_address;// p_eth.src_mac;
//            // only difference now is that for dst mac now is the official
//            ether.dst_mac = Utils.calculate_mac(ip_mac_list.get(victim));
//
//            p.datalink = ether;
//
//            return p;
//        }

//        @Override
//        public synchronized void receivePacket(Packet p_temp) {
//            if (p_temp instanceof ARPPacket) return;
//
//            EthernetPacket p_eth = (EthernetPacket) p_temp.datalink;
//
//            // check if the packet is mine just return do not send it again
//            String mine_mac = Utils.decalculate_mac(device.mac_address);
//            String incoming_src_mac = Utils.decalculate_mac(p_eth.src_mac);
//
//            if (mine_mac.equals(incoming_src_mac)) {
//                return;
//            }
//
//            this.dl_src = Utils.decalculate_mac(p_eth.src_mac);
//            this.dl_dst = Utils.decalculate_mac(p_eth.dst_mac);
//
//            if (p_temp instanceof IPPacket) {
//                IPPacket p = ((IPPacket) p_temp);
//                this.dst_ip = p.dst_ip.toString().split("/")[1];
//                this.src_ip = p.src_ip.toString().split("/")[1];
//
//                // ignore channel-agent's packets
//                if (this.dst_ip.equals(localIp) || this.src_ip.equals(localIp)) {
//                    return;
//                }
//
//                if (p_temp.data.length < 8 || p_temp.data.length > 1500) {
//                    sendPkt(p_temp);
//                    return;
//                }
//
//
//                TCPPacket tcppacl = (TCPPacket) p_temp;
//                byte[] body = addBodyData(tcppacl);
//
//                if (tcppacl.psh) {
//                    // body is complete
//                    // do something else...
//                    p_temp.data = body;
//
//                    ByteBuf newBuf = null;
//
//                    if (attackType == TestCase.EVAESDROP) {
//                        this.sendPkt(p_temp);
//
//                        if (p_temp.data.length > 8) {
//                            try {
//                                testAdvanced.testEvaseDrop(topo, p_temp);
//                                return;
//                            } catch (OFParseError e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        }
//                    } else if (attackType == TestCase.LINKFABRICATION) {
//                        if (p_temp.data.length > 8) {
//                            try {
//                                newBuf = testAdvanced.testLinkFabrication(p_temp);
//
//                                if (newBuf != null) {
//                                    p_temp.data = byteBufToArray(newBuf);
//                                    sendPkt(p_temp);
//                                    return;
//                                }
//                            } catch (OFParseError e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        }
//                    } else if (attackType == TestCase.MITM) {
//                        if (p_temp.data.length > 8) {
//                            try {
//                                if (this.src_ip.equals(controllerIP) && this.dst_ip.equals(switchIP)) {
//                                    newBuf = testAdvanced.testMITM(p_temp);
//
//                                    if (newBuf != null) {
//                                        p_temp.data = byteBufToArray(newBuf);
//                                        sendPkt(p_temp);
//                                        return;
//                                    }
//                                }
//                            } catch (OFParseError e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        }
//                    } else if (attackType == TestCase.CONTROLMESSAGEMANIPULATION) {
//                        log.info("\n[ATTACK] Control Message Manipulation");
//                        /* Modify a Packet Here */
//                        if (this.dst_ip.equals(controllerIP)) {
//                            (p.data)[2] = 0x77;
//                            (p.data)[3] = 0x77;
//                        }
//                    } else if (attackType == TestCase.MALFORMEDCONTROLMESSAGE) {
//                        log.info("\n[ATTACK] Malformed Control Message");
//                        /* Modify a Packet Here */
//                        if (this.dst_ip.equals(switchIP)) {
//                            // if ( (p.data)[1] != 0x0a ) {
//                            (p.data)[2] = 0x00;
//                            (p.data)[3] = 0x01;
//                            // }
//                        }
//                    } else if (attackType == TestCase.CONTROLPLANE_FUZZING) {
//                        /* Switch -> Controller */
//                        if (p_temp.data.length > 8) {
//                            try {
//                                if (this.src_ip.equals(switchIP) && this.dst_ip.equals(controllerIP)) {
//                                    byte[] msg = testFuzzing.testControlPlane(p_temp);
//
//                                    if (msg != null) {
//                                        p_temp.data = msg;
//                                        sendPkt(p_temp);
//                                        return;
//                                    }
//                                }
//                            } catch (OFParseError e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        }
//                    } else if (attackType == TestCase.DATAPLANE_FUZZING) {
//                        /* Controller -> Switch */
//                        if (p_temp.data.length > 8) {
//                            try {
//                                if (this.src_ip.equals(controllerIP) && this.dst_ip.equals(switchIP)) {
//                                    byte[] msg = testFuzzing.testDataPlane(p_temp);
//
//                                    if (msg != null) {
//                                        p_temp.data = msg;
//                                        sendPkt(p_temp);
//                                        return;
//                                    }
//                                }
//                            } catch (OFParseError e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        }
//                    } else if (attackType == TestCase.SEED_BASED_FUZZING) {
//                        if (fuzzingMode == 1 && p_temp.data.length >= 8) {
//                            if (this.dst_ip.equals(switchIP)) {
//                                seedPkts.saveSeedPacket(this.dl_dst, p_temp.data);
//                            } else {
//                                seedPkts.saveSeedPacket(this.dl_src, p_temp.data);
//                            }
//                        }
//                    }
//
//                    sendPkt(p_temp);
//                }
//            }
//        }
    }
}
