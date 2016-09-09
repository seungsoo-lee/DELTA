package org.deltaproject.webui;

import java.util.concurrent.ConcurrentHashMap;

import static org.deltaproject.webui.TestCase.Category.*;
import static org.deltaproject.webui.TestCase.Status.*;
import static org.deltaproject.webui.TestCase.ControllerType.*;

/**
 * Created by changhoon on 7/6/16.
 */
public class TestCaseDirectory {

    private static final ConcurrentHashMap<String, TestCase> TEST_CASE_DIRECTORY =
            initializeDirectory();

    protected TestCaseDirectory() {
    }

    public static ConcurrentHashMap<String, TestCase> getDirectory() {
        return TEST_CASE_DIRECTORY;
    }

    private static ConcurrentHashMap<String, TestCase> initializeDirectory() {
        ConcurrentHashMap<String, TestCase> directory =
                new ConcurrentHashMap<>();

        directory.put("1.1.010",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Port Range Violation"));
        directory.put("1.1.011",
                new TestCase(DATA_PLANE_OF, "1.1.010", "TTP Port Range Violation"));
        directory.put("1.1.020",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Table Number Violation"));
        directory.put("1.1.030",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Group Identifier Violation"));
        directory.put("1.1.040",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Meter Identifier Violation"));
        directory.put("1.1.050",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Table Loop Violation"));
        directory.put("1.1.060",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Corrupted Control Message Type"));
        directory.put("1.1.070",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Unsupported Version Number (bad version)"));
        directory.put("1.1.080",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Malformed Version Number (supported but not negotiated version)"));
        directory.put("1.1.090",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Invalid OXM - Type"));
        directory.put("1.1.100",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Invalid OXM - Length"));
        directory.put("1.1.110",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Invalid OXM - Value"));
        directory.put("1.1.120",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Disabled Table Features Request"));
        directory.put("1.1.130",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Handshake without Hello Message"));
        directory.put("1.1.140",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Control Message before Hello Message (Main Connection)"));
        directory.put("1.1.150",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Incompatible Hello after Connection Establishment"));
        directory.put("1.1.160",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Corrupted Cookie Values"));
        directory.put("1.1.170",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Malformed Buffer ID Values"));
        directory.put("1.2.010",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Slave Controller Violation"));
        directory.put("1.2.020",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Corrupted Generation ID"));
        directory.put("1.2.030",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Auxiliary Connection - Terminate when main connection is down"));
        directory.put("1.2.040",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Auxiliary Connection - Initiate Non-Hello"));
        directory.put("1.2.050",
                new TestCase(DATA_PLANE_OF, "1.1.010", "Auxiliary Connection - Unsupported Messages"));

        directory.put("2.1.010",
                new TestCase(CONTROL_PLANE_OF, "2.1.010", "Malformed Version Number (supported but not negotiated version)"));
        directory.put("2.1.020",
                new TestCase(CONTROL_PLANE_OF, "2.1.020", "Corrupted Control Message Type"));
        directory.put("2.1.030",
                new TestCase(CONTROL_PLANE_OF, "2.1.030", "Handshake without Hello Message"));
        directory.put("2.1.040",
                new TestCase(CONTROL_PLANE_OF, "2.1.040", "Control Message before Hello Message (Main Connection)"));
        directory.put("2.1.050",
                new TestCase(CONTROL_PLANE_OF, "2.1.050", "Multiple main connection request from same switch"));
        directory.put("2.1.060",
                new TestCase(CONTROL_PLANE_OF, "2.1.060", "n-flagged Flow Remove Message notification"));
        directory.put("2.1.070",
                new TestCase(CONTROL_PLANE_OF, "2.1.070", "TLS Support"));
        directory.put("2.1.071",
                new TestCase(CONTROL_PLANE_OF, "2.1.071", "Startup Behaviour with Failed TLS Connection"));
        directory.put("2.1.072",
                new TestCase(CONTROL_PLANE_OF, "2.1.072", "Handling Invalid Authentication Credentials"));
        directory.put("2.1.073",
                new TestCase(CONTROL_PLANE_OF, "2.1.073", "Handling Control Packet Modification"));
        directory.put("2.1.080",
                new TestCase(CONTROL_PLANE_OF, "2.1.080", "Auxiliary Connection Mismatch with main connection"));

        directory.put("3.1.010",
                new TestCase(ADVANCED, "3.1.010", "Packet-In Flooding"));
        directory.put("3.1.020",
                new TestCase(ADVANCED, "3.1.010", "Control Message Drop"));
        directory.put("3.1.030",
                new TestCase(ADVANCED, "3.1.010", "Infinite Loops"));
        directory.put("3.1.040",
                new TestCase(ADVANCED, "3.1.010", "Internal Storage Abuse"));
        directory.put("3.1.050",
                new TestCase(ADVANCED, "3.1.010", "Switch Table Flooding"));
        directory.put("3.1.060",
                new TestCase(ADVANCED, "3.1.010", "Switch ID spoofing"));
        directory.put("3.1.070",
                new TestCase(ADVANCED, "3.1.010", "Flow Rule Modification"));
        directory.put("3.1.080",
                new TestCase(ADVANCED, "3.1.010", "Flow Table Clearance"));
        directory.put("3.1.090",
                new TestCase(ADVANCED, "3.1.010", "Event Listener Unsubscription"));
        directory.put("3.1.100",
                new TestCase(ADVANCED, "3.1.010", "Application Eviction"));
        directory.put("3.1.110",
                new TestCase(ADVANCED, "3.1.010", "Memory Exhaustion"));
        directory.put("3.1.120",
                new TestCase(ADVANCED, "3.1.010", "CPU Exhaustion"));
        directory.put("3.1.130",
                new TestCase(ADVANCED, "3.1.010", "System Variable Manipulation"));
        directory.put("3.1.140",
                new TestCase(ADVANCED, "3.1.010", "System Command Execution"));
        directory.put("3.1.150",
                new TestCase(ADVANCED, "3.1.010", "Host Location Hijacking"));
        directory.put("3.1.160",
                new TestCase(ADVANCED, "3.1.010", "Link Fabrication"));

        directory.put("3.1.170",
                new TestCase(ADVANCED, "3.1.010", "Eavesdrop"));
        directory.put("3.1.180",
                new TestCase(ADVANCED, "3.1.010", "Man-In-The-Middle"));
        directory.put("3.1.190",
                new TestCase(ADVANCED, "3.1.010", "Flow Rule Flooding"));
        directory.put("3.1.200",
                new TestCase(ADVANCED, "3.1.010", "Switch Firmware Abuse"));


        return directory;
    }
}
