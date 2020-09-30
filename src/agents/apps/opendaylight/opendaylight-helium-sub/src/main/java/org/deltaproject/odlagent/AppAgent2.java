package org.deltaproject.odlagent;

import org.opendaylight.controller.forwardingrulesmanager.IForwardingRulesManager;
import org.opendaylight.controller.hosttracker.IfIptoHost;
import org.opendaylight.controller.hosttracker.IfNewHostNotify;
import org.opendaylight.controller.hosttracker.hostAware.HostNodeConnector;
import org.opendaylight.controller.hosttracker.hostAware.IHostFinder;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.routing.IListenRoutingUpdates;
import org.opendaylight.controller.sal.routing.IRouting;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.statisticsmanager.IStatisticsManager;
import org.opendaylight.controller.switchmanager.IInventoryListener;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

/*
 * Simple bundle to grab some statistics Fred Hsu
 */
@SuppressWarnings("deprecation")
public class AppAgent2 implements IListenDataPacket, IHostFinder,
        IfNewHostNotify, IListenRoutingUpdates, IInventoryListener {
    private static final Logger log = LoggerFactory.getLogger(AppAgent2.class);
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

    private Random ran;

    private boolean isLoop = false;
    private boolean isDrop = false;

    public AppAgent2() {
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

    }

    public PacketResult receiveDataPacket(RawPacket inPkt) {
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
