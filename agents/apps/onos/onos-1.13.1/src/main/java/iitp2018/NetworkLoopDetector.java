package org.deltaproject.onosagent;

import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.graph.AdjacencyListsGraph;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.Deserializer;
import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flow.criteria.EthTypeCriterion;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.topology.*;
import org.onosproject.openflow.controller.*;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.StreamSupport;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * TODO: for SDN Security Research, 2018IITP
 * written by Jinwoo Kim
 */
public class NetworkLoopDetector {

    protected OpenFlowController controller;
    protected DeviceService deviceService;
    protected FlowRuleService flowRuleService;
    protected CoreService coreService;
    protected LinkService linkService;

    private Timer timer;
    private TimerTask task;

    private final Logger log = getLogger(getClass());
    private final InternalPacketInListener listener = new InternalPacketInListener();

    private ConcurrentHashMap<MacAddress, LongAdder> invalidTTlCountMap = new ConcurrentHashMap<>();

    private Deserializer<Ethernet> deserializer;

    private final static long TTL_THRESHOLD = 5;

    public NetworkLoopDetector(OpenFlowController controller, DeviceService deviceService,
                                  FlowRuleService flowRuleService, CoreService coreService, LinkService linkService) {
        this.controller = controller;
        this.deviceService = deviceService;
        this.flowRuleService = flowRuleService;
        this.coreService = coreService;
        this.linkService = linkService;
    }

    public void start() {
        deserializer = Ethernet.deserializer();

        setAsyncInvalidTtl();

        timer = new Timer("delta-appagent-dec-ttl-fixer");
        task = new SetTTLRules();
        timer.scheduleAtFixedRate(task, 3000, 3000);

        controller.addMessageListener(listener);
    }

    public void stop() {
        task.cancel();
    }

    // set asynchronous messages on OF switch for receiving packet-in when arriving invalid ttl packets
    public void setAsyncInvalidTtl() {
        controller.getSwitches().forEach(sw -> {

            OFAsyncSet ofAsyncSet = sw.factory().buildAsyncSet()
                    .setPacketInMaskEqualMaster(1 << OFPacketInReason.INVALID_TTL.ordinal() |
                            1 << OFPacketInReason.ACTION.ordinal())
                    .setPortStatusMaskEqualMaster(1 << OFPortReason.ADD.ordinal() |
                            1 << OFPortReason.DELETE.ordinal() |
                            1 << OFPortReason.MODIFY.ordinal())
                    .setFlowRemovedMaskEqualMaster(1 << OFFlowRemovedReason.IDLE_TIMEOUT.ordinal() |
                            1 << OFFlowRemovedReason.HARD_TIMEOUT.ordinal() |
                            1 << OFFlowRemovedReason.DELETE.ordinal())
                    .build();

            log.info("send ofAsyncSet : {}", ofAsyncSet);

            sw.sendMsg(Lists.newArrayList(ofAsyncSet));
        });
    }

    class InternalPacketInListener implements OpenFlowMessageListener {
        @Override
        public void handleIncomingMessage(Dpid dpid, OFMessage ofMessage) {
            try {
                if (ofMessage.getType() == OFType.PACKET_IN) {
                    OFPacketIn ofPacketIn = (OFPacketIn) ofMessage;
                    if (ofPacketIn.getReason() == OFPacketInReason.INVALID_TTL) {

                        // count invalid ttl messages
                        byte[] data = ofPacketIn.getData();
                        Ethernet pkt = deserializer.deserialize(data, 0, data.length);
                        MacAddress sourceMAC = pkt.getSourceMAC();
                        invalidTTlCountMap.computeIfAbsent(sourceMAC, (t) -> new LongAdder()).increment();

                        Long longAdder = invalidTTlCountMap.get(sourceMAC).longValue();

                        log.info("ttl count: {} {}", sourceMAC, longAdder);
                        // if the count is greater than TTL_THRESHOLD, find and fix the network loop
                        if (longAdder > TTL_THRESHOLD) {
                            findAndfixNetworkLoop(pkt);
                        }
                    }
                }
            } catch (DeserializationException e) {
                log.error("{}", e);
            }
        }

        @Override
        public void handleOutgoingMessage(Dpid dpid, List<OFMessage> list) {

        }
    }

    // find and fix network


    void findAndfixNetworkLoop(Ethernet pkt) {
        Set<FlowEntry> rules = getFlowRulesFor(pkt);

        DefaultGraphDescription dgd = new DefaultGraphDescription(0L, System.currentTimeMillis(),
                Collections.emptyList(),
                Collections.emptyList());

        ImmutableSet.Builder<TopologyVertex> vertexes = ImmutableSet.builder();
        ImmutableSet.Builder<TopologyEdge> edges = ImmutableSet.builder();

        for (FlowEntry rule : rules) {
            TopologyVertex src = new DefaultTopologyVertex(rule.deviceId());
            vertexes.add();
        }

        for (FlowEntry rule : rules) {
            TopologyVertex src = new DefaultTopologyVertex(rule.deviceId());
            rule.treatment().allInstructions().forEach(i -> {
                if (i.type() == Instruction.Type.OUTPUT) {
                    PortNumber port = ((Instructions.OutputInstruction) i).port();
                    ConnectPoint srcCp = new ConnectPoint(rule.deviceId(), port);

                    linkService.getIngressLinks(srcCp).forEach(link -> {
                        TopologyVertex dst = new DefaultTopologyVertex(link.src().deviceId());
                        ConnectPoint dstCp = new ConnectPoint(link.src().deviceId(), link.src().port());
                        DefaultLink dLink = DefaultLink.builder()
                                .src(srcCp)
                                .dst(dstCp)
                                .type(Link.Type.DIRECT)
                                .state(Link.State.ACTIVE)
                                .isExpected(true)
                                .build();
                        edges.add(new DefaultTopologyEdge(src, dst, dLink));
                    });
                }
            });
        }

        AdjacencyListsGraph alg = new AdjacencyListsGraph(vertexes.build(), edges.build());

        log.info("topology: {}", alg);
    }

    private Set<FlowEntry> getFlowRulesFor(Ethernet pkt) {
        ImmutableSet.Builder<FlowEntry> builder = ImmutableSet.builder();
        deviceService.getAvailableDevices().forEach(d -> {
            flowRuleService.getFlowEntries(d.id()).forEach(r -> {

                SrcDstPair pair_pkt = new SrcDstPair(pkt.getSourceMAC(), pkt.getDestinationMAC());

                MacAddress src_mac_cri = ((EthCriterion) r.selector().getCriterion(Criterion.Type.ETH_SRC)).mac();
                MacAddress dst_mac_cri = ((EthCriterion) r.selector().getCriterion(Criterion.Type.ETH_DST)).mac();
                SrcDstPair pair_cri = new SrcDstPair(src_mac_cri, dst_mac_cri);

                if (pair_cri.equals(pair_pkt)) {
                    r.treatment().allInstructions().forEach(i -> {
                        if (i.type() == Instruction.Type.OUTPUT) {
                            builder.add(r);
                        }
                    });
                }
            });
        });

        return builder.build();
    }

    private class SetTTLRules extends TimerTask {
        @Override
        public void run() {
            deviceService.getAvailableDevices().forEach(d -> {
                // pick only rules having ipv4 related
                StreamSupport.stream(flowRuleService.getFlowEntries(d.id()).spliterator(), false)
                        .filter(entry -> (((EthTypeCriterion) entry.selector().getCriterion(Criterion.Type.ETH_TYPE))
                                .ethType().toShort() == Ethernet.TYPE_IPV4))
                        .forEach(entry -> {
                            TrafficTreatment treatment = entry.treatment();
                            // pick a rule that has an output action
                            Instruction originInst = treatment.immediate().stream()
                                    .filter(inst -> inst.type().equals(Instruction.Type.OUTPUT))
                                    .findAny().orElse(null);
                            if (originInst != null) {
                                if (!treatment.immediate().stream().anyMatch(inst ->
                                        inst.type().equals(Instruction.Type.L3MODIFICATION))) {
                                    // remove first
//                          flowRuleService.removeFlowRules((FlowRule) entry);

                                    // reinstall it having a dec ttl action
                                    Instructions.OutputInstruction outputInst = (Instructions.OutputInstruction) originInst;
                                    if (outputInst.port() != PortNumber.CONTROLLER) {
                                        log.info("target entry: {}", entry);

                                        // make a new treatment where a decNwTtl action is placed in a first order
                                        TrafficTreatment newTreatment = DefaultTrafficTreatment.builder()
                                                .decNwTtl()
                                                .setOutput(outputInst.port())
                                                .build();

                                        ApplicationId targetAppId = coreService.getAppId(entry.appId());

                                        FlowRule rule = DefaultFlowRule.builder()
                                                .forDevice(d.id())
                                                .withSelector(entry.selector())
                                                .withTreatment(newTreatment)
                                                .withPriority(entry.priority())
                                                .fromApp(targetAppId)
                                                .makeTemporary(entry.timeout())
                                                .build();

                                        log.info("flow rule: {}", rule);

                                        FlowRuleOperations.Builder ops = FlowRuleOperations.builder();
                                        ops.add(rule);
                                        String description = "[AppAgent] Modify flow rules having decrement TTL values";
                                        flowRuleService.apply(ops.build(new FlowRuleOperationsContext() {
                                            @Override
                                            public void onSuccess(FlowRuleOperations ops) {
                                                log.info(description + " success: " + ops.toString() + ", " + rule.toString());
                                            }

                                            @Override
                                            public void onError(FlowRuleOperations ops) {
                                                log.info(description + " error: " + ops.toString() + ", " + rule.toString());
                                            }
                                        }));
                                    }
                                }
                            }
                        });
            });
        }
    }

    // Wrapper class for a source and destination pair of MAC addresses
    private final class SrcDstPair {
        final MacAddress src;
        final MacAddress dst;

        private SrcDstPair(MacAddress src, MacAddress dst) {
            this.src = src;
            this.dst = dst;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SrcDstPair that = (SrcDstPair) o;
            return Objects.equals(src, that.src) &&
                    Objects.equals(dst, that.dst);
        }

        @Override
        public int hashCode() {
            return Objects.hash(src, dst);
        }
    }
}
