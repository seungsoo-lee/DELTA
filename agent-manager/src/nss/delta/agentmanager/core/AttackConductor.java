package nss.delta.agentmanager.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nss.delta.agentmanager.targetcon.ControllerManager;
import nss.delta.agentmanager.testcase.TestAdvancedCase;
import nss.delta.agentmanager.testcase.TestInfo;
import nss.delta.agentmanager.utils.ProgressBar;


public class AttackConductor {
	private static final Logger log = LoggerFactory.getLogger(AttackConductor.class);

	public static final int UMODE_DEFAULT_COUNT = 100;

	private HashMap<String, String> infoSwitchCase;
	private HashMap<String, String> infoControllerCase;
	private HashMap<String, String> infoAdvancedCase;

	private AppAgentManager appm;
	private HostAgentManager hostm;
	private ChannelAgentManager channelm;
	private ControllerManager controllerm;

	private ProgressBar pb;

	private DataOutputStream dos;
	private DataInputStream dis;

	private TestAdvancedCase testAdvancedCase;

	public AttackConductor(String config) {
		infoControllerCase = new HashMap<String, String>();
		infoSwitchCase = new HashMap<String, String>();
		infoAdvancedCase = new HashMap<String, String>();	
		
		this.controllerm = new ControllerManager(config);		

		this.appm = new AppAgentManager();
		this.appm.setControllerType(controllerm.getType());
		
		this.hostm = new HostAgentManager();
		this.channelm = new ChannelAgentManager();

		TestInfo.updateAdvancedCase(infoAdvancedCase);
		TestInfo.updateControllerCase(infoControllerCase);
		testAdvancedCase = new TestAdvancedCase(appm, hostm, channelm, controllerm);
	}

	public String showConfig() {
		return controllerm.showConfig();
	}
	
	public void setSocket(Socket socket) throws IOException {
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());

		String agentType = dis.readUTF();

		if (agentType.contains("AppAgent")) {
			appm.setAppSocket(socket, dos, dis);
		} else if (agentType.contains("ActAgent")) { /* for OpenDaylight */
			appm.setActSocket(socket, dos, dis);
		} else if (agentType.contains("ChannelAgent")) {
			channelm.setSocket(socket, dos, dis);
			// System.out.println("\nchannel-agent connected");

			/* OF version : Controller IP : Channel IP : Switch IP */
			channelm.write("config," + "version:1.0," + "nic:eth0," + "port:6653," + "controller_ip:192.168.100.195,"
					+ "switch_ip:192.168.100.185");

			// String temp = channelm.read();
			// System.out.println(temp);
		} else if (agentType.contains("HostAgent")) {
			// System.out.println("\nhost-agent connected");
			hostm.setSocket(socket, dos, dis);
		}
	}

	public void replayKnownAttack(String code) {
		if(code.charAt(0) == '3')
			testAdvancedCase.replayKnownAttack(code);
	}

	public void printAttackList() {
		
		System.out.println("\nControl Plane Test Set");

		Iterator<String> treeMapIter = infoControllerCase.keySet().iterator();

		while (treeMapIter.hasNext()) {

			String key = treeMapIter.next();
			String value = infoControllerCase.get(key);
			System.out.println(String.format("%s\t: %s", key, value));
		}

		System.out.println("\nData Plane Test Set");
		treeMapIter = infoSwitchCase.keySet().iterator();

		while (treeMapIter.hasNext()) {

			String key = treeMapIter.next();
			String value = infoSwitchCase.get(key);
			System.out.println(String.format("%s\t: %s", key, value));
		}

		System.out.println("\nAdvanced Test Set");
		
		TreeMap treeMap = new TreeMap(infoAdvancedCase);
		treeMapIter = treeMap.keySet().iterator();
		while (treeMapIter.hasNext()) {

			String key = (String) treeMapIter.next();
			String value = (String) treeMap.get(key);
			System.out.println(String.format("%s\t: %s", key, value));
		}
	}

	public boolean isPossibleAttack(String code) {
		if (infoControllerCase.containsKey(code))
			return true;
		else if (infoSwitchCase.containsKey(code))
			return true;
		else if (infoAdvancedCase.containsKey(code))
			return true;
		else
			return false;
	}

	public void test(String code) {
		this.appm.write(code);
	}

	public void replayAllKnownAttacks() {

	}

	public void initProgressBar(String code) {
		ProgressBar.clearConsole();
		pb = new ProgressBar(code);
		pb.clearMsg();
		pb.start();
	}
}
