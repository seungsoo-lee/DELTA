package org.deltaproject.odlagent.utils;

/**
 * Created by seungsoo on 09/07/2017.
 */

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchUtils {
    private static final Logger logger = LoggerFactory.getLogger(MatchUtils.class);
    public static final short ICMP_SHORT = 1;
    public static final short TCP_SHORT = 6;
    public static final short UDP_SHORT = 17;
    public static final String TCP = "tcp";
    public static final String UDP = "udp";
    public static final int TCP_SYN = 0x0002;
    public static final long IPV4_LONG = (long) 0x800;
    public static final long LLDP_LONG = (long) 0x88CC;
    public static final long VLANTAGGED_LONG = (long) 0x8100;
    public static final long MPLSUCAST_LONG = (long) 0x8847;


    /**
     * Create Ingress Port Match dpidLong, inPort
     *
     * @param matchBuilder Map matchBuilder MatchBuilder Object without a match
     * @param dpidLong     Long the datapath ID of a switch/node
     * @param inPort       Long ingress port on a switch
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createInPortMatch(MatchBuilder matchBuilder, Long dpidLong, Long inPort) {

        NodeConnectorId ncid = new NodeConnectorId("openflow:" + dpidLong + ":" + inPort);
        logger.debug("createInPortMatch() Node Connector ID is - Type=openflow: DPID={} inPort={} ", dpidLong, inPort);
        matchBuilder.setInPort(ncid);

        return matchBuilder;
    }

    /**
     * Create Ingress Port Match dpidLong, inPort
     *
     * @param matchBuilder    Map matchBuilder MatchBuilder Object without a match
     * @param ncId NodeConnectorId for the ingress port
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createInPortMatch(MatchBuilder matchBuilder, NodeConnectorId ncId) {
        logger.debug("createInPortMatch() Node Connector ID is {} ", ncId);
        matchBuilder.setInPort(ncId);

        return matchBuilder;
    }

    /**
     * Create EtherType Match
     *
     * @param matchBuilder Map matchBuilder MatchBuilder Object without a match
     * @param etherType    Long EtherType
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createEtherTypeMatch(MatchBuilder matchBuilder, Long etherType) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(etherType));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        return matchBuilder;
    }

    /**
     * Create Ethernet Source Match
     *
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param sMacAddr     String representing a source MAC
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createEthSrcMatch(MatchBuilder matchBuilder, MacAddress sMacAddr) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
        ethSourceBuilder.setAddress(new MacAddress(sMacAddr));
        ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        return matchBuilder;
    }

    /**
     * Create Ethernet Destination Match
     *
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param vlanId       Integer representing a VLAN ID Integer representing a VLAN ID
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createVlanIdMatch(MatchBuilder matchBuilder, Integer vlanId, boolean present) {
        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(VLANTAGGED_LONG));
        eth.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(eth.build());

        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        vlanIdBuilder.setVlanId(new VlanId(vlanId));
        vlanIdBuilder.setVlanIdPresent(present);
        vlanMatchBuilder.setVlanId(vlanIdBuilder.build());
        matchBuilder.setVlanMatch(vlanMatchBuilder.build());

        return matchBuilder;
    }

    /**
     * Create MPLS label Match
     *
     * @param matchBuilder MatchBuilder Object
     * @param label        Long representing a Label value
     * @param bos          Boolean indicating bottom of stack for this label
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createMplsLabelBosMatch(MatchBuilder matchBuilder, Long label, boolean bos) {
        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(MPLSUCAST_LONG));
        eth.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(eth.build());

        ProtocolMatchFieldsBuilder matchFieldsBuilder = new ProtocolMatchFieldsBuilder()
                .setMplsLabel(label)
                .setMplsBos((short) (bos ? 1 : 0));
        matchBuilder.setProtocolMatchFields(matchFieldsBuilder.build());
        return matchBuilder;
    }

    /**
     * Create Ethernet Destination Match
     *
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param dMacAddr     String representing a destination MAC
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createEthDstMatch(MatchBuilder matchBuilder, MacAddress dMacAddr, MacAddress mask) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
        ethDestinationBuilder.setAddress(new MacAddress(dMacAddr));
        if (mask != null) {
            ethDestinationBuilder.setMask(mask);
        }
        ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        return matchBuilder;
    }

    /**
     * Tunnel ID Match Builder
     *
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param tunnelId     BigInteger representing a tunnel ID
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createTunnelIDMatch(MatchBuilder matchBuilder, BigInteger tunnelId) {

        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(tunnelId);
        matchBuilder.setTunnel(tunnelBuilder.build());

        return matchBuilder;
    }

    /**
     * Match ICMP code and type
     *
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param type         short representing an ICMP type
     * @param code         short representing an ICMP code
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createICMPv4Match(MatchBuilder matchBuilder, short type, short code) {

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        eth.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(eth.build());

        // Build the IPv4 Match requied per OVS Syntax
        IpMatchBuilder ipmatch = new IpMatchBuilder();
        ipmatch.setIpProtocol((short) 1);
        matchBuilder.setIpMatch(ipmatch.build());

        // Build the ICMPv4 Match
        Icmpv4MatchBuilder icmpv4match = new Icmpv4MatchBuilder();
        icmpv4match.setIcmpv4Type(type);
        icmpv4match.setIcmpv4Code(code);
        matchBuilder.setIcmpv4Match(icmpv4match.build());

        return matchBuilder;
    }

    /**
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param dstip        String containing an IPv4 prefix
     * @return matchBuilder Map Object with a match
     */
    public static MatchBuilder createDstL3IPv4Match(MatchBuilder matchBuilder, Ipv4Prefix dstip) {

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        eth.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(eth.build());

        Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
        ipv4match.setIpv4Destination(dstip);

        matchBuilder.setLayer3Match(ipv4match.build());

        return matchBuilder;

    }

    /**
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param dstip        String containing an IPv4 prefix
     * @return matchBuilder Map Object with a match
     */
    public static MatchBuilder createArpDstIpv4Match(MatchBuilder matchBuilder, Ipv4Prefix dstip) {
        ArpMatchBuilder arpDstMatch = new ArpMatchBuilder();
        arpDstMatch.setArpTargetTransportAddress(dstip);
        matchBuilder.setLayer3Match(arpDstMatch.build());

        return matchBuilder;
    }

    /**
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param srcip        String containing an IPv4 prefix
     * @return matchBuilder Map Object with a match
     */
    public static MatchBuilder createSrcL3IPv4Match(MatchBuilder matchBuilder, Ipv4Prefix srcip) {

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        eth.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(eth.build());

        Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
        ipv4match.setIpv4Source(srcip);
        matchBuilder.setLayer3Match(ipv4match.build());

        return matchBuilder;

    }

    /**
     * Create Source TCP Port Match
     *
     * @param matchBuilder @param matchbuilder MatchBuilder Object without a match yet
     * @param tcpport      Integer representing a source TCP port
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createSetSrcTcpMatch(MatchBuilder matchBuilder, PortNumber tcpport) {

        EthernetMatchBuilder ethType = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethType.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethType.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder();
        ipmatch.setIpProtocol((short) 6);
        matchBuilder.setIpMatch(ipmatch.build());

        TcpMatchBuilder tcpmatch = new TcpMatchBuilder();
        tcpmatch.setTcpSourcePort(tcpport);
        matchBuilder.setLayer4Match(tcpmatch.build());

        return matchBuilder;

    }

    /**
     * Create Destination TCP Port Match
     *
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param tcpDstPort   Integer representing a destination TCP port
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createSetDstTcpMatch(MatchBuilder matchBuilder, PortNumber tcpDstPort) {

        EthernetMatchBuilder ethType = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethType.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethType.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder();
        ipmatch.setIpProtocol((short) 6);
        matchBuilder.setIpMatch(ipmatch.build());

        TcpMatchBuilder tcpmatch = new TcpMatchBuilder();
        tcpmatch.setTcpDestinationPort(tcpDstPort);
        matchBuilder.setLayer4Match(tcpmatch.build());

        return matchBuilder;
    }

    /**
     * Test match for TCP_Flags
     *
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param tcpPort      PortNumber representing a destination TCP port
     * @param tcpFlag      int representing a tcp_flag
     * @return match containing TCP_Flag (), IP Protocol (TCP), TCP_Flag (SYN)
     * <p/>
     * Defined TCP Flag values in OVS v2.1+
     * TCP_FIN 0x001 / TCP_SYN 0x002 / TCP_RST 0x004
     * TCP_PSH 0x008 / TCP_ACK 0x010 / TCP_URG 0x020
     * TCP_ECE 0x040 / TCP_CWR 0x080 / TCP_NS  0x100
     */
    public static MatchBuilder createTcpFlagMatch(MatchBuilder matchBuilder, PortNumber tcpPort, int tcpFlag) {

        // Ethertype match
        EthernetMatchBuilder ethernetType = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetType.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethernetType.build());

        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol((short) 6);
        matchBuilder.setIpMatch(ipMatch.build());

        // TCP Port Match
        PortNumber dstPort = new PortNumber(tcpPort);
        TcpMatchBuilder tcpMatch = new TcpMatchBuilder();
        tcpMatch.setTcpDestinationPort(dstPort);
        matchBuilder.setLayer4Match(tcpMatch.build());

        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(tcpFlag);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());
        return matchBuilder;
    }

    /**
     * Create Source UDP Port Match
     *
     * @param matchBuilder @param matchbuilder MatchBuilder Object without a match yet
     * @param udpport      Integer representing a source UDP port
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createSetSrcUdpMatch(MatchBuilder matchBuilder, PortNumber udpport) {

        EthernetMatchBuilder ethType = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethType.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethType.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder();
        ipmatch.setIpProtocol((short) 17);
        matchBuilder.setIpMatch(ipmatch.build());

        UdpMatchBuilder udpmatch = new UdpMatchBuilder();
        udpmatch.setUdpSourcePort(udpport);
        matchBuilder.setLayer4Match(udpmatch.build());

        return matchBuilder;

    }

    /**
     * Create Destination UDP Port Match
     *
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param udpDstPort   Integer representing a destination UDP port
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createSetDstUdpMatch(MatchBuilder matchBuilder, PortNumber udpDstPort) {

        EthernetMatchBuilder ethType = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethType.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethType.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder();
        ipmatch.setIpProtocol((short) 17);
        matchBuilder.setIpMatch(ipmatch.build());

        UdpMatchBuilder udpmatch = new UdpMatchBuilder();
        udpmatch.setUdpDestinationPort(udpDstPort);
        matchBuilder.setLayer4Match(udpmatch.build());

        return matchBuilder;
    }

    /**
     * @return MatchBuilder containing the metadata match values
     */
    public static MatchBuilder createMetadataMatch(MatchBuilder matchBuilder, BigInteger metaData, BigInteger metaDataMask) {

        // metadata matchbuilder
        MetadataBuilder metadata = new MetadataBuilder();
        metadata.setMetadata(metaData);
        // Optional metadata mask
        if (metaDataMask != null) {
            metadata.setMetadataMask(metaDataMask);
        }
        matchBuilder.setMetadata(metadata.build());

        return matchBuilder;
    }

    /**
     * Create  TCP Port Match
     *
     * @param matchBuilder @param matchbuilder MatchBuilder Object without a match yet
     * @param ipProtocol      Integer representing a source TCP port
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createIpProtocolMatch(MatchBuilder matchBuilder, short ipProtocol) {

        EthernetMatchBuilder ethType = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethType.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethType.build());

        IpMatchBuilder ipMmatch = new IpMatchBuilder();
        if (ipProtocol == TCP_SHORT) {
            ipMmatch.setIpProtocol(TCP_SHORT);
        } else if (ipProtocol == UDP_SHORT) {
            ipMmatch.setIpProtocol(UDP_SHORT);
        } else if (ipProtocol == ICMP_SHORT) {
            ipMmatch.setIpProtocol(ICMP_SHORT);
        }
        matchBuilder.setIpMatch(ipMmatch.build());
        return matchBuilder;
    }

    /**
     * Create tcp syn with proto match.
     *
     * @param matchBuilder the match builder
     * @return matchBuilder match builder
     */
    public static MatchBuilder createTcpSynWithProtoMatch(MatchBuilder matchBuilder) {

        // Ethertype match
        EthernetMatchBuilder ethernetType = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetType.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethernetType.build());

        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol((short) 6);
        matchBuilder.setIpMatch(ipMatch.build());

        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(TCP_SYN);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());
        return matchBuilder;
    }

    /**
     * Create tcp proto syn match.
     *
     * @param matchBuilder the match builder
     * @return matchBuilder match builder
     */
    public static MatchBuilder createTcpProtoSynMatch(MatchBuilder matchBuilder) {

        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol((short) 6);
        matchBuilder.setIpMatch(ipMatch.build());

        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(TCP_SYN);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());
        return matchBuilder;
    }

    /**
     * Create dmac tcp port with flag match.
     *
     * @param matchBuilder the match builder
     * @param attachedMac  the attached mac
     * @param tcpFlag      the tcp flag
     * @param tunnelID     the tunnel iD
     * @return match containing TCP_Flag (), IP Protocol (TCP), TCP_Flag (SYN)
     */
    public static MatchBuilder createDmacTcpPortWithFlagMatch(MatchBuilder matchBuilder,
                                                              String attachedMac, Integer tcpFlag, String tunnelID) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());

        EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
        ethDestinationBuilder.setAddress(new MacAddress(attachedMac));
        ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol(TCP_SHORT);
        matchBuilder.setIpMatch(ipMatch.build());

        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(tcpFlag);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());

        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(new BigInteger(tunnelID));
        matchBuilder.setTunnel(tunnelBuilder.build());

        return matchBuilder;
    }

    /**
     * Create dmac tcp syn match.
     *
     * @param matchBuilder the match builder
     * @param attachedMac  the attached mac
     * @param tcpPort      the tcp port
     * @param tcpFlag      the tcp flag
     * @param tunnelID     the tunnel iD
     * @return the match builder
     */
    public static MatchBuilder createDmacTcpSynMatch(MatchBuilder matchBuilder,
                                                     String attachedMac, PortNumber tcpPort, Integer tcpFlag, String tunnelID) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());

        EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
        ethDestinationBuilder.setAddress(new MacAddress(attachedMac));
        ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol((short) 6);
        matchBuilder.setIpMatch(ipMatch.build());

        // TCP Port Match
        PortNumber dstPort = new PortNumber(tcpPort);
        TcpMatchBuilder tcpMatch = new TcpMatchBuilder();
        tcpMatch.setTcpDestinationPort(dstPort);
        matchBuilder.setLayer4Match(tcpMatch.build());

        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(tcpFlag);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());

        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(new BigInteger(tunnelID));
        matchBuilder.setTunnel(tunnelBuilder.build());

        return matchBuilder;
    }

    /**
     * Create dmac tcp syn dst ip prefix tcp port.
     *
     * @param matchBuilder   the match builder
     * @param attachedMac    the attached mac
     * @param tcpPort        the tcp port
     * @param tcpFlag        the tcp flag
     * @param segmentationId the segmentation id
     * @param dstIp          the dst ip
     * @return the match builder
     */
    public static MatchBuilder createDmacTcpSynDstIpPrefixTcpPort(MatchBuilder matchBuilder,
                                                                  MacAddress attachedMac, PortNumber tcpPort, Integer tcpFlag, String segmentationId,
                                                                  Ipv4Prefix dstIp) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());

        EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
        ethDestinationBuilder.setAddress(new MacAddress(attachedMac));
        ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());

        matchBuilder.setEthernetMatch(ethernetMatch.build());

        Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
        ipv4match.setIpv4Destination(dstIp);
        matchBuilder.setLayer3Match(ipv4match.build());

        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol(TCP_SHORT);
        matchBuilder.setIpMatch(ipMatch.build());

        // TCP Port Match
        PortNumber dstPort = new PortNumber(tcpPort);
        TcpMatchBuilder tcpMatch = new TcpMatchBuilder();
        tcpMatch.setTcpDestinationPort(dstPort);
        matchBuilder.setLayer4Match(tcpMatch.build());

        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(tcpFlag);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());

        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(new BigInteger(segmentationId));
        matchBuilder.setTunnel(tunnelBuilder.build());

        return matchBuilder;
    }

    /**
     * Create dmac ip tcp syn match.
     *
     * @param matchBuilder the match builder
     * @param dMacAddr     the d mac addr
     * @param mask         the mask
     * @param ipPrefix     the ip prefix
     * @return MatchBuilder containing the metadata match values
     */
    public static MatchBuilder createDmacIpTcpSynMatch(MatchBuilder matchBuilder,
                                                       MacAddress dMacAddr, MacAddress mask, Ipv4Prefix ipPrefix) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetDestinationBuilder ethDestBuilder = new EthernetDestinationBuilder();
        ethDestBuilder.setAddress(new MacAddress(dMacAddr));
        if (mask != null) {
            ethDestBuilder.setMask(mask);
        }
        ethernetMatch.setEthernetDestination(ethDestBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());
        // Ethertype match
        EthernetMatchBuilder ethernetType = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetType.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethernetType.build());
        if (ipPrefix != null) {
            Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
            ipv4match.setIpv4Destination(ipPrefix);
            matchBuilder.setLayer3Match(ipv4match.build());
        }
        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol(TCP_SHORT);
        matchBuilder.setIpMatch(ipMatch.build());
        // TCP Flag Match
        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(TCP_SYN);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());

        return matchBuilder;
    }

    /**
     * Create smac tcp syn dst ip prefix tcp port.
     *
     * @param matchBuilder   the match builder
     * @param attachedMac    the attached mac
     * @param tcpPort        the tcp port
     * @param tcpFlag        the tcp flag
     * @param segmentationId the segmentation id
     * @param dstIp          the dst ip
     * @return the match builder
     */
    public static MatchBuilder createSmacTcpSynDstIpPrefixTcpPort(MatchBuilder matchBuilder, MacAddress attachedMac,
                                                                  PortNumber tcpPort, Integer tcpFlag, String segmentationId, Ipv4Prefix dstIp) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());

        EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
        ethSourceBuilder.setAddress(new MacAddress(attachedMac));
        ethernetMatch.setEthernetSource(ethSourceBuilder.build());

        matchBuilder.setEthernetMatch(ethernetMatch.build());

        Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
        ipv4match.setIpv4Destination(dstIp);
        matchBuilder.setLayer3Match(ipv4match.build());

        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol(TCP_SHORT);
        matchBuilder.setIpMatch(ipMatch.build());

        // TCP Port Match
        PortNumber dstPort = new PortNumber(tcpPort);
        TcpMatchBuilder tcpMatch = new TcpMatchBuilder();
        tcpMatch.setTcpDestinationPort(dstPort);
        matchBuilder.setLayer4Match(tcpMatch.build());

        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(tcpFlag);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());

        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(new BigInteger(segmentationId));
        matchBuilder.setTunnel(tunnelBuilder.build());

        return matchBuilder;
    }

    /**
     * Create smac tcp port with flag match.
     *
     * @param matchBuilder the match builder
     * @param attachedMac  the attached mac
     * @param tcpFlag      the tcp flag
     * @param tunnelID     the tunnel iD
     * @return matchBuilder
     */
    public static MatchBuilder createSmacTcpPortWithFlagMatch(MatchBuilder matchBuilder, String attachedMac,
                                                              Integer tcpFlag, String tunnelID) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());

        EthernetSourceBuilder ethSrcBuilder = new EthernetSourceBuilder();
        ethSrcBuilder.setAddress(new MacAddress(attachedMac));
        ethernetMatch.setEthernetSource(ethSrcBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol(TCP_SHORT);
        matchBuilder.setIpMatch(ipMatch.build());

        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(tcpFlag);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());

        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(new BigInteger(tunnelID));
        matchBuilder.setTunnel(tunnelBuilder.build());

        return matchBuilder;
    }

    /**
     * Create smac ip tcp syn match.
     *
     * @param matchBuilder the match builder
     * @param dMacAddr     the d mac addr
     * @param mask         the mask
     * @param ipPrefix     the ip prefix
     * @return MatchBuilder containing the metadata match values
     */
    public static MatchBuilder createSmacIpTcpSynMatch(MatchBuilder matchBuilder, MacAddress dMacAddr,
                                                       MacAddress mask, Ipv4Prefix ipPrefix) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetSourceBuilder ethSrcBuilder = new EthernetSourceBuilder();
        ethSrcBuilder.setAddress(new MacAddress(dMacAddr));
        if (mask != null) {
            ethSrcBuilder.setMask(mask);
        }
        ethernetMatch.setEthernetSource(ethSrcBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());
        // Ethertype match
        EthernetMatchBuilder ethernetType = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetType.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethernetType.build());
        if (ipPrefix != null) {
            Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
            ipv4match.setIpv4Destination(ipPrefix);
            matchBuilder.setLayer3Match(ipv4match.build());
        }
        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol(TCP_SHORT);
        matchBuilder.setIpMatch(ipMatch.build());

        // TCP Flag Match
        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(TCP_SYN);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());

        return matchBuilder;
    }

    /**
     * Create smac tcp syn.
     *
     * @param matchBuilder the match builder
     * @param attachedMac  the attached mac
     * @param tcpPort      the tcp port
     * @param tcpFlag      the tcp flag
     * @param tunnelID     the tunnel iD
     * @return the match builder
     */
    public static MatchBuilder createSmacTcpSyn(MatchBuilder matchBuilder,
                                                String attachedMac, PortNumber tcpPort, Integer tcpFlag, String tunnelID) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());

        EthernetSourceBuilder ethSrcBuilder = new EthernetSourceBuilder();
        ethSrcBuilder.setAddress(new MacAddress(attachedMac));
        ethernetMatch.setEthernetSource(ethSrcBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol((short) 6);
        matchBuilder.setIpMatch(ipMatch.build());

        // TCP Port Match
        PortNumber dstPort = new PortNumber(tcpPort);
        TcpMatchBuilder tcpMatch = new TcpMatchBuilder();
        tcpMatch.setTcpDestinationPort(dstPort);
        matchBuilder.setLayer4Match(tcpMatch.build());


        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(tcpFlag);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());

        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(new BigInteger(tunnelID));
        matchBuilder.setTunnel(tunnelBuilder.build());

        return matchBuilder;
    }

    /**
     * @return MatchBuilder containing the metadata match values
     */
    public static MatchBuilder createMacSrcIpTcpSynMatch(MatchBuilder matchBuilder,
                                                         MacAddress dMacAddr, MacAddress mask, Ipv4Prefix ipPrefix) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
        ethDestinationBuilder.setAddress(new MacAddress(dMacAddr));
        if (mask != null) {
            ethDestinationBuilder.setMask(mask);
        }
        ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        // Ethertype match
        EthernetMatchBuilder ethernetType = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(IPV4_LONG));
        ethernetType.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethernetType.build());
        if (ipPrefix != null) {
            Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
            ipv4match.setIpv4Source(ipPrefix);
            matchBuilder.setLayer3Match(ipv4match.build());
        }

        // TCP Protocol Match
        IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol(TCP_SHORT);
        matchBuilder.setIpMatch(ipMatch.build());

        // TCP Flag Match
        TcpFlagsMatchBuilder tcpFlagMatch = new TcpFlagsMatchBuilder();
        tcpFlagMatch.setTcpFlags(TCP_SYN);
        matchBuilder.setTcpFlagsMatch(tcpFlagMatch.build());

        return matchBuilder;
    }

    public static EthernetMatch ethernetMatch(MacAddress srcMac,
                                              MacAddress dstMac,
                                              Long etherType) {
        EthernetMatchBuilder emb = new EthernetMatchBuilder();
        if (srcMac != null)
            emb.setEthernetSource(new EthernetSourceBuilder()
                    .setAddress(srcMac)
                    .build());
        if (dstMac != null)
            emb.setEthernetDestination(new EthernetDestinationBuilder()
                    .setAddress(dstMac)
                    .build());
        if (etherType != null)
            emb.setEthernetType(new EthernetTypeBuilder()
                    .setType(new EtherType(etherType))
                    .build());
        return emb.build();
    }
}
