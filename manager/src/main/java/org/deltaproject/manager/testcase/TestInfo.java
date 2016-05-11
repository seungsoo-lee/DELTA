package org.deltaproject.manager.testcase;

import java.util.HashMap;

public class TestInfo {

	public static void updateControllerCase(HashMap<String, String> map) {

	}

	public static void updateSwitchCase(HashMap<String, String> map) {
		map.put("1.1.10", "Port Range Violation");
		map.put("1.1.11", "TTP Port Range Violation");
		map.put("1.1.20", "Table Number Violation");
		map.put("1.1.30", "Group Identifier Violation");
		map.put("1.1.40", "Meter Identifier Violation");
		map.put("1.1.50", "Table Loop Violation");
		map.put("1.1.60", "Corrupted Control Message Type");
		map.put("1.1.70", "Unsupported Version Number (bad version)");
		map.put("1.1.80", "Malformed Version Number (supported but not negotiated version)");
		map.put("1.1.90", "Invalid OXM – Type");
		map.put("1.1.100", "Invalid OXM – Length");
		map.put("1.1.110", "Invalid OXM – Value");
		map.put("1.1.120", "Disabled Table Features Request");
		map.put("1.1.130", "Handshake without Hello Message");
		map.put("1.1.140", "Control Message before Hello Message (Main Connection)");
		map.put("1.1.150", "Incompatible Hello after Connection Establishment");
		map.put("1.1.160", "Corrupted Cookie Values");
		map.put("1.1.170", "Malformed Buffer ID Values");
		map.put("1.2.10", "Slave Controller Violation");
		map.put("1.2.20", "Corrupted Generation ID");
		map.put("1.2.30", "Auxiliary Connection – Terminate when main connection is down");
		map.put("1.2.40", "Auxiliary Connection – Initiate Non-Hello");
		map.put("1.2.50", "Auxiliary Connection – Unsupported Messages");
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
