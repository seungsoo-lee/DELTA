package org.deltaproject.manager.testcase;

import java.util.HashMap;

public class CaseInfo {
    public static void updateSwitchCase(HashMap<String, String> map) {
        map.put("1.1.010", "Port Range Violation");
        map.put("1.1.011", "TTP Port Range Violation");
        map.put("1.1.020", "Table Number Violation");
        map.put("1.1.030", "Group Identifier Violation");
        map.put("1.1.040", "Meter Identifier Violation");
        map.put("1.1.050", "Table Loop Violation");
        map.put("1.1.060", "Corrupted Control Message Type");
        map.put("1.1.070", "Unsupported Version Number (bad version)");
        map.put("1.1.080", "Malformed Version Number (supported but not negotiated version)");
        map.put("1.1.090", "Invalid OXM – Type");
        map.put("1.1.100", "Invalid OXM – Length");
        map.put("1.1.110", "Invalid OXM – Value");
        map.put("1.1.120", "Disabled Table Features Request");
        map.put("1.1.130", "Handshake without Hello Message");
        map.put("1.1.140", "Control Message before Hello Message (Main Connection)");
        map.put("1.1.150", "Incompatible Hello after Connection Establishment");
        map.put("1.1.160", "Corrupted Cookie Values");
        map.put("1.1.170", "Malformed Buffer ID Values");
        map.put("1.2.010", "Slave Controller Violation");
        map.put("1.2.020", "Corrupted Generation ID");
        map.put("1.2.030", "Auxiliary Connection – Terminate when main connection is down");
        map.put("1.2.040", "Auxiliary Connection – Initiate Non-Hello");
        map.put("1.2.050", "Auxiliary Connection – Unsupported Messages");
    }

    public static void updateControllerCase(HashMap<String, String> map) {
        map.put("2.1.010", "Malformed Version Number (supported but not negotiated version)");
        map.put("2.1.020", "Corrupted Control Message Type");
        map.put("2.1.030", "Handshake without Hello Message");
        map.put("2.1.040", "Control Message before Hello Message (Main Connection)");
        map.put("2.1.050", "Multiple main connection request from same switch");
        map.put("2.1.060", "Un-flagged Flow Remove Message notification");
        map.put("2.1.070", "TLS Support");
        map.put("2.1.071", "Startup Behaviour with Failed TLS Connection");
        map.put("2.1.072", "Handling Invalid Authentication Credentials");
        map.put("2.1.073", "Handling Control Packet Modification");
        map.put("2.1.080", "Auxiliary Connection Mismatch with main connection");
    }

    public static void updateAdvancedCase(HashMap<String, String> map) {
        map.put("3.1.010", "Packet-In Flooding");
        map.put("3.1.020", "Control Message Drop");
        map.put("3.1.030", "Infinite Loops");
        map.put("3.1.040", "Internal Storage Abuse");
        map.put("3.1.050", "Switch Table Flooding");
        map.put("3.1.060", "Switch Identification Spoofing");
        map.put("3.1.070", "Flow Rule Modification");
        map.put("3.1.080", "Flow Table Clearance");
        map.put("3.1.090", "Event Listener Unsubscription");
        map.put("3.1.100", "Application Eviction");
        map.put("3.1.110", "Memory Exhaustion");
        map.put("3.1.120", "CPU Exhaustion");
        map.put("3.1.130", "System Variable Manipulation");
        map.put("3.1.140", "System Command Execution");
        map.put("3.1.150", "Host Location Hijacking");
        map.put("3.1.160", "Link Fabrication");
        map.put("3.1.170", "Eavesdrop");
        map.put("3.1.180", "Man-In-The-Middle");
        map.put("3.1.190", "Flow Rule Flooding");
        map.put("3.1.200", "Switch Firmware Abuse");
    }
}
