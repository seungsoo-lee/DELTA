package nss.delta.agentmanager.testcase;

import java.util.HashMap;
import java.util.Iterator;

public class TestInfo {

	public static void updateControllerCase(HashMap<String, String> map) {

	}

	public static void updateSwitchCase(HashMap<String, String> map) {

	}

	public static void updateAdvancedCase(HashMap<String, String> map) {
		map.put("3.1.10", "Packet-In Flooding");
		map.put("3.1.20", "Control Message Drop");
		map.put("3.1.30", "Infinite Loops");
		map.put("3.1.40", "Internal Storage Abuse");
		map.put("3.1.50", "Switch Table Flooding");
		map.put("3.1.60", "Switch Identification Spoofing");
		map.put("3.1.70", "Flow Rule Modification");
		map.put("3.1.80", "Flow Table Clearance");
		map.put("3.1.90", "Event Listener Unsubscription");
		map.put("3.1.100", "Application Eviction");
		map.put("3.1.110", "Memory Exhaustion");
		map.put("3.1.120", "CPU Exhaustion");
		map.put("3.1.130", "System Variable Manipulation");
		map.put("3.1.140", "System Command Execution");
		map.put("3.1.150", "Host Location Hijacking");
		map.put("3.1.160", "Link Fabrication");
		map.put("3.1.170", "Evasedrop");
		map.put("3.1.180", "Man-In-The-Middle");
		map.put("3.1.190", "Flow Rule Flooding");
		map.put("3.1.200", "Switch Firmware Absuse");
	}
}
