package org.deltaproject.manager.testcase;

import org.deltaproject.manager.analysis.ResultAnalyzer;
import org.deltaproject.manager.analysis.ResultInfo;
import org.deltaproject.manager.core.AppAgentManager;
import org.deltaproject.manager.core.ChannelAgentManager;
import org.deltaproject.manager.core.HostAgentManager;
import org.deltaproject.manager.target.ControllerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAdvancedCase {
	private static final Logger log = LoggerFactory.getLogger(TestAdvancedCase.class.getName());

	private AppAgentManager appm;
	private HostAgentManager hostm;
	private ChannelAgentManager channelm;
	private ControllerManager controllerm;

	private ResultAnalyzer analyzer;

	public TestAdvancedCase(AppAgentManager am, HostAgentManager hm, ChannelAgentManager cm, ControllerManager ctm) {
		this.appm = am;
		this.hostm = hm;
		this.channelm = cm;
		this.controllerm = ctm;


		this.analyzer = new ResultAnalyzer(controllerm);
	}

	public void initController() {
		if (!controllerm.isRunning()) {
			log.info("Target controller: " + controllerm.getType());
			
			log.info("Target controller is starting..");
			controllerm.createController();
			log.info("Target controller setup is completed");

			

			/* waiting for switches */
			log.info("Listening to switches..");
			controllerm.isConnectedSwitch();
			log.info("All switches are connected");

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String generateFlow(String proto) {
		hostm.write(proto);
		String result = hostm.read();

		return result;
	}

	public void replayKnownAttack(String code) {
		if (code.equals("3.1.10")) {
			testPacketInFlooding(code);
		} else if (code.equals("3.1.20")) {
			testControlMessageDrop(code);
		} else if (code.equals("3.1.30")) {
			testInfiniteLoop(code);
		} else if (code.equals("3.1.40")) {
			testInternalStorageAbuse(code);
		} else if (code.equals("3.1.50")) {
			testSwitchTableFlooding(code);
		} else if (code.equals("3.1.60")) {
			testSwitchIdentificationSpoofing(code);
		} else if (code.equals("------")) {
			testMalformedControlMessage(code);
		} else if (code.equals("3.1.70")) {
			testFlowRuleModification(code);
		} else if (code.equals("3.1.80")) {
			testFlowTableClearance(code);
		} else if (code.equals("3.1.90")) {
			testEventUnsubscription(code);
		} else if (code.equals("3.1.100")) {
			testApplicationEviction(code);
		} else if (code.equals("3.1.110")) {
			testMemoryExhaustion(code);
		} else if (code.equals("3.1.120")) {
			testCPUExhaustion(code);
		} else if (code.equals("3.1.130")) {
			testSystemVariableManipulation(code);
		} else if (code.equals("3.1.140")) {
			testSystemCommandExecution(code);
		} else if (code.equals("3.1.160")) {
			testLinkFabrication(code);
		} else if (code.equals("3.1.170")) {
			testEvaseDrop(code);
		} else if (code.equals("3.1.180")) {
			testManInTheMiddle(code);
		} else if (code.equals("3.1.190")) {
			testFlowRuleFlooding(code);
		} else if (code.equals("3.1.200")) {
			testSwitchFirmwareMisuse(code);
		} else if (code.equals("------")) {
			testControlMessageManipulation(code);
		}
	}

	/*
	 * 3.1.10 - Packet-In Flooding
	 */
	public boolean testPacketInFlooding(String code) {
		log.info(code + " - Packet-In Flooding");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();
		String before = generateFlow("ping");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* Remove flow rules */
		appm.write("3.1.80|false");

		/* step 2: run cbench */
		log.info("Cbench starts");
		controllerm.executeCbench();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* step 3: try communication */
		log.info("Host-Agent sends packets to others");
		String after = generateFlow("ping");
		log.info("Agent-Manager retrieves result from Host-Agent");

		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.COMMUNICATON).addType(ResultInfo.LATENCY_TIME);
		result.setResult(after);
		result.setLatency(before, after);

		/* step 4: decide if the attack is feasible */
		analyzer.checkResult(code, result);

		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		controllerm.killCbench();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.20 - Control Message Drop
	 */
	public boolean testControlMessageDrop(String code) {
		log.info(code + " - Control Message Drop");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("App-Agent starts");
		appm.write(code);

		/* step 3: try communication */
		log.info("Host-Agent sends packets to others");
		String flowResult = generateFlow("ping");

		log.info("Agent-Manager retrieves result from Host-Agent");

		/* step 4: decide if the attack is feasible */
		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.COMMUNICATON);

		String appresult = appm.read();
		log.info("Dropped packet: " + appresult);

		result.setResult(flowResult);

		analyzer.checkResult(code, result);

		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.30 - Infinite Loop
	 */
	public boolean testInfiniteLoop(String code) {
		log.info(code + " - Infinite Loop");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("App-Agent starts");
		appm.write(code);

		/* step 3: try communication */
		log.info("Host-Agent sends packets to others");
		String flowResult = generateFlow("ping");

		log.info("Agent-Manager retrieves result from Host-Agent");

		/* step 4: decide if the attack is feasible */
		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.COMMUNICATON);
		result.setResult(flowResult);

		analyzer.checkResult(code, result);

		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.40 - Internal Storage Abuse
	 */
	public boolean testInternalStorageAbuse(String code) {
		log.info(code + " - Internal Storage Abuse");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("App-Agent starts");
		appm.write(code);

		log.info("Agent-Manager retrieves result from App-Agent");
		String removedItem = appm.read();
		log.info("Removed Item: " + removedItem);

		/* step 3: try communication */
		log.info("HostAgent starts communication");
		String flowResult = generateFlow("ping");
		log.info("Gethering result from HostAgent");

		/* step 4: decide if the attack is feasible */
		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.COMMUNICATON);
		result.setResult(flowResult);

		analyzer.checkResult(code, result);

		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();

		return true;
	}

	/*
	 * 3.1.50 - Switch Table Flooding
	 */
	public boolean testSwitchTableFlooding(String code) {
		log.info(code + " - Switch Table Flooding");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("Channel-Agent starts");
		channelm.write(code);
		String resultChannel = channelm.read();
		log.info("Agent-Manager retrieves result from Channel-Agent");

		/* step 4: decide if the attack is feasible */
		// analyzer.checkSwirchState(code);

		channelm.write("exit");
		controllerm.flushARPcache();
		appm.closeSocket();
		controllerm.killController();

		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		return true;
	}

	/*
	 * 3.1.60 - Switch Identification Spoofing
	 */
	public boolean testSwitchIdentificationSpoofing(String code) {
		log.info(code + " - Switch Identification Spoofing");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("Channel-Agent starts");
		channelm.write(code);

		log.info("Agent-Manager retrieves result from Channel-Agent");
		String resultChannel = channelm.read();

		/* step 4: decide if the attack is feasible */
		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.SWITCH_STATE);

		analyzer.checkResult(code, result);

		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		channelm.write("exit");
		controllerm.flushARPcache();
		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * ---- Malformed Control Message
	 */
	public boolean testMalformedControlMessage(String code) {
		log.info(code + " - Malformed Control Message");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("ChannelAgent starts");
		channelm.write(code);

		log.info("Gathering result from channel agent");
		String resultChannel = channelm.read();

		/* step 3: try communication */
		log.info("HostAgent generates flow");
		this.generateFlow("ping");

		log.info("HostAgent starts communication with another host");
		String resultFlow = generateFlow("ping");

		/* step 4: decide if the attack is feasible */
		log.info("Check switch's state");

		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.SWITCH_STATE);

		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		channelm.write("exit");
		controllerm.flushARPcache();
		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.70 - Flow Rule Modification
	 */
	public boolean testFlowRuleModification(String code) {
		log.info(code + " - Flow Rule Modification");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		log.info("Host-Agent sends packets to others (before)");
		/* step 2 : generate before flow (ping) */

		String before = generateFlow("ping");

		/* step 3 : replay attack */
		log.info("App-Agent starts");
		appm.write(code);

		String modified = "";

		log.info("Agent-Manager retrieves result from App-Agent");
		modified = appm.read();

		log.info("Host-Agent sends packets to others (after)");
		String after = generateFlow("ping");

		ResultInfo result = new ResultInfo();

		/* step 4: decide if the attack is feasible */
		result.addType(ResultInfo.APPAGENT_REPLY);
		result.setResult(modified);

		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.80 - Flow Table Clearance
	 */
	public boolean testFlowTableClearance(String code) {
		log.info(code + " - Flow Table Clearance");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		log.info("Host-Agent sends packets to others (before)");
		/* step 2 : generate before flow (ping) */

		String before = generateFlow("compare");

		/* step 3 : replay attack */
		log.info("App-Agent starts");
		appm.write(code);

		log.info("Host-Agent sends packets to others (after)");
		String after = generateFlow("compare");

		ResultInfo result = new ResultInfo();

		/* step 4: decide if the attack is feasible */
		result.addType(ResultInfo.LATENCY_TIME);
		result.setLatency(before, after);

		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.90 - Event Unsubscription
	 */
	public boolean testEventUnsubscription(String code) {
		if (controllerm.getType().equals("ONOS")) {
			System.out.println("\nONOS is impossible to replay [" + code + "] ");
			return false;
		}

		log.info(code + " - Event Unsubscription");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("App-Agent starts");
		appm.write(code);

		String remove = "";

		if (controllerm.getType().equals("OpenDaylight"))
			remove = appm.read2();
		else
			remove = appm.read();

		log.info("Removed Item: " + remove);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* step 3: try communication */
		log.info("Host-Agent sends packets to others");
		String resultFlow = generateFlow("ping");
		log.info("Agent-Manager retrieves result from Host-Agent");

		/* step 4: decide if the attack is feasible */
		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.COMMUNICATON);
		result.setResult(resultFlow);
		analyzer.checkResult(code, result);

		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.100 - Application Eviction
	 */
	public boolean testApplicationEviction(String code) {
		if (controllerm.getType().equals("Floodlight")) {
			System.out.println("\nFloodlight is impossible to replay [" + code + "] ");
			return false;
		}

		log.info(code + " - Application Eviction");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("App-Agent starts");
		appm.write(code);

		String remove = "";

		if (controllerm.getType().equals("OpenDaylight"))
			remove = appm.read2();
		else
			remove = appm.read();

		log.info("Removed Item: " + remove);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* step 3: try communication */
		log.info("Host-Agent sends packets to others");
		String resultFlow = generateFlow("ping");
		log.info("Agent-Manager retrieves result from Host-Agent");

		/* step 4: decide if the attack is feasible */
		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.COMMUNICATON);
		result.setResult(resultFlow);
		analyzer.checkResult(code, result);

		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.110 - Memory Exhaustion
	 */
	public boolean testMemoryExhaustion(String code) {
		log.info(code + " - Memory Exhaustion");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("Host-Agent sends packets to others (before)");
		/* step 2 : generate before flow (ping) */
		String before = generateFlow("compare");

		/* remove flow rules */
		appm.write("3.1.80|false");

		/* step 3 : replay attack */
		log.info("App-Agent starts");
		appm.write(code);

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("Host-Agent sends packets to others (after)");
		String after = generateFlow("compare");

		ResultInfo result = new ResultInfo();

		/* step 4: decide if the attack is feasible */
		result.addType(ResultInfo.COMMUNICATON);
		result.setResult(after);
		result.addType(ResultInfo.LATENCY_TIME);
		result.setLatency(before, after);

		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.120 - CPU Exhaustion
	 */
	public boolean testCPUExhaustion(String code) {
		log.info(code + " - CPU Exhaustion");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("Host-Agent sends packets to others (before)");
		/* step 2 : generate before flow (ping) */
		String before = generateFlow("compare");

		/* remove flow rules one time */
		appm.write("3.1.80|false");

		/* step 3 : replay attack */
		log.info("App-Agent starts");
		appm.write(code);

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("Host-Agent sends packets to others (after)");
		String after = generateFlow("compare");

		ResultInfo result = new ResultInfo();

		/* step 4: decide if the attack is feasible */
		result.addType(ResultInfo.COMMUNICATON);
		result.setResult(after);
		result.addType(ResultInfo.LATENCY_TIME);
		result.setLatency(before, after);

		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.130 - System Variable Manipulation
	 */
	public boolean testSystemVariableManipulation(String code) {
		log.info(code + " - System Variable Manipulation");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("App-Agent starts");
		appm.write(code);

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* step 3: try communication */
		log.info("Host-Agent sends packets to others");
		this.generateFlow("ping");

		/* step 4: decide if the attack is feasible */
		log.info("Agent-Manager checks the status of switches");

		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.SWITCH_STATE);
		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.140 - System Command Execution
	 */
	public boolean testSystemCommandExecution(String code) {
		log.info(code + " - System Command Execution");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("App-Agent starts");
		appm.write(code);

		log.info("Agent-Manager checks the status of target controller");

		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.CONTROLLER_STATE);
		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.150 - Host Location Hijacking : Not implemented yet
	 */

	/*
	 * 3.1.160 - Link Fabrication ; incomplete
	 */
	public boolean testLinkFabrication(String code) {
		log.info(code + " - Link Fabrication");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("Channel-Agent starts");
		channelm.write(code);
		String resultChannel = channelm.read();

		/* step 4: decide if the attack is feasible */
		// analyzer.checkSwirchState(code);

		channelm.write("exit");
		controllerm.flushARPcache();
		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.170 - Evaesdrop
	 */
	public boolean testEvaseDrop(String code) {
		controllerm.flushARPcache();
		log.info(code + " - Evaesdrop");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("Channel-Agent starts");
		channelm.write(code);
		String resultChannel = channelm.read();

		/* step 3: try communication */
		log.info("Host-Agent sends packets to others");
		try {
			Thread.sleep(31000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		generateFlow("ping");

		log.info("Agent-Manager retrieves the result from Channel-Agent");
		channelm.write(code + "-V");
		resultChannel = channelm.read();

		/* step 4: decide if the attack is feasible */
		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.CHANNELAGENT_REPLY);
		result.setResult(resultChannel);
		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		channelm.write("exit");
		controllerm.flushARPcache();
		appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.180 - MITM
	 */
	public boolean testManInTheMiddle(String code) {
		controllerm.flushARPcache();
		log.info(code + " - Man-In-The-Middle attack");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("Channel-Agent starts");
		channelm.write(code);
		String resultChannel = channelm.read();

		/* step 3: try communication */
		log.info("Host-Agent sends packets to others");
		String resultFlow = generateFlow("ping");
		log.info("Agent-Manager retrieves the result from Host-Agent");

		/* step 4: decide if the attack is feasible */
		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.COMMUNICATON);
		result.setResult(resultFlow);
		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		channelm.write("exit");
		controllerm.flushARPcache();
		// appm.closeSocket();
		controllerm.killController();
		return true;
	}

	/*
	 * 3.1.190 - Flow Rule Flooding
	 */
	public boolean testFlowRuleFlooding(String code) {
		log.info(code + " - Flow Rule Flooding");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		log.info("Host-Agent sends packets to others (before)");
		String before = generateFlow("compare");

		/* remove flow rules */
		appm.write("3.1.80|false");

		/* step 2: conduct the attack */
		log.info("App-Agent starts");
		appm.write(code);

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* step 3: try communication */
		log.info("Host-Agent sends packets to others (after)");
		String after = generateFlow("compare");

		/* step 4: decide if the attack is feasible */
		log.info("Agent-Manager retrieves the result from Host-Agent");

		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.COMMUNICATON).addType(ResultInfo.LATENCY_TIME);

		result.setResult(after);
		result.setLatency(before, after);

		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();

		return true;
	}

	/*
	 * 3.1.200 - Switch Firmware Misuse
	 */
	public boolean testSwitchFirmwareMisuse(String code) {
		log.info(code + " - Switch Firmware Misuse");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		log.info("Host-Agent sends packets to others (before)");
		String before = generateFlow("compare");

		/* step 2: conduct the attack */
		log.info("App-Agent starts");
		appm.write(code);

		log.info("Agent-Manager retrieves the result from App-Agent");
		String modified = appm.read();
		log.info("Modified Item: " + modified);

		/* step 3: try communication */
		log.info("Host-Agent sends packets to others (after)");
		String after = generateFlow("compare");

		/* step 4: decide if the attack is feasible */
		log.info("Agent-Manager retrieves the result from Host-Agent");

		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.LATENCY_TIME);
		result.setLatency(before, after);
		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		appm.closeSocket();
		controllerm.killController();

		return true;
	}

	/*
	 * ---- Control Message Manipulation
	 */
	public boolean testControlMessageManipulation(String code) {
		log.info(code + " - Control Message Manipulation");

		/* step 1: create controller */
		initController();
		long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
		log.info("ChannelAgent starts");
		channelm.write(code);
		String resultChannel = channelm.read();

		/* step 3: try communication */
		log.info("HostAgent generates flow");
		generateFlow("ping");

		/* step 4: decide if the attack is feasible */
		log.info("Check switch state");

		ResultInfo result = new ResultInfo();
		result.addType(ResultInfo.SWITCH_STATE);
		analyzer.checkResult(code, result);
		long end = System.currentTimeMillis();
		log.info("Running Time: " + (end - start));

		channelm.write("exit");
		controllerm.flushARPcache();
		appm.closeSocket();
		controllerm.killController();
		return true;
	}
}
