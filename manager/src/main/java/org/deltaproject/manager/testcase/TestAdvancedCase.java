package org.deltaproject.manager.testcase;

import org.deltaproject.manager.analysis.ResultAnalyzer;
import org.deltaproject.manager.analysis.ResultInfo;
import org.deltaproject.manager.core.AppAgentManager;
import org.deltaproject.manager.core.ChannelAgentManager;
import org.deltaproject.manager.core.ControllerManager;
import org.deltaproject.manager.core.HostAgentManager;
import org.deltaproject.webui.TestCase;
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
            log.info("Target controller: " + controllerm.getType() + " " + controllerm.getVersion());

            log.info("Target controller is starting..");
            controllerm.createController();
            log.info("Target controller setup is completed");

			/* waiting for switches */
            log.info("Listening to switches..");
            controllerm.isConnectedSwitch(true);
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

    public void replayKnownAttack(TestCase test) {
        switch (test.getcasenum()) {
            case "3.1.010":
                testPacketInFlooding(test);
                break;
            case "3.1.020":
                testControlMessageDrop(test);
                break;
            case "3.1.030":
                testInfiniteLoop(test);
                break;
            case "3.1.040":
                testInternalStorageAbuse(test);
                break;
            case "3.1.050":
                // testSwitchTableFlooding(test);
                return;
            case "3.1.060":
                testSwitchIdentificationSpoofing(test);
                break;
            case "------1":         // testSwitchOFCase
                testMalformedControlMessage(test);
                break;
            case "3.1.070":
                testFlowRuleModification(test);
                break;
            case "3.1.080":
                testFlowTableClearance(test);
                break;
            case "3.1.090":
                testEventUnsubscription(test);
                break;
            case "3.1.100":
                testApplicationEviction(test);
                break;
            case "3.1.110":
                testMemoryExhaustion(test);
                break;
            case "3.1.120":
                testCPUExhaustion(test);
                break;
            case "3.1.130":
                testSystemVariableManipulation(test);
                break;
            case "3.1.140":
                testSystemCommandExecution(test);
                break;
            case "3.1.160":
                testLinkFabrication(test);
                break;
            case "3.1.170":
                testEvaseDrop(test);
                break;
            case "3.1.180":
                testManInTheMiddle(test);
                break;
            case "3.1.190":
                testFlowRuleFlooding(test);
                break;
            case "3.1.200":
                testSwitchFirmwareMisuse(test);
                break;
            case "------2":          // testControllerOFCase
                testControlMessageManipulation(test);
                break;
        }
    }

    /*
     * 3.1.010 - Packet-In Flooding
     */
    public boolean testPacketInFlooding(TestCase test) {
        log.info(test.getcasenum() + " - Packet-In Flooding");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();
        String before = generateFlow("ping");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		/* clear flow rules */
        appm.write("3.1.080|false");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		/* step 2: run cbench */
        log.info("Cbench starts");
        channelm.write(test.getcasenum());

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
        analyzer.checkResult(test, result);

        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start) + "ms");

        controllerm.killController();
        return true;
    }

    /*
     * 3.1.020 - Control Message Drop
     */
    public boolean testControlMessageDrop(TestCase test) {
        log.info(test.getcasenum() + " - Control Message Drop");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

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

        analyzer.checkResult(test, result);

        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * 3.1.030 - Infinite Loop
     */
    public boolean testInfiniteLoop(TestCase test) {
        log.info(test.getcasenum() + " - Infinite Loop");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

		/* step 3: try communication */
        log.info("Host-Agent sends packets to others");
        String flowResult = generateFlow("ping");

        log.info("Agent-Manager retrieves result from Host-Agent");

		/* step 4: decide if the attack is feasible */
        ResultInfo result = new ResultInfo();
        result.addType(ResultInfo.COMMUNICATON);
        result.setResult(flowResult);

        analyzer.checkResult(test, result);

        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * 3.1.040 - Internal Storage Abuse
     */
    public boolean testInternalStorageAbuse(TestCase test) {
        log.info(test.getcasenum() + " - Internal Storage Abuse");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: try communication */
        log.info("HostAgent starts communication");
        String flowResult = generateFlow("ping");
        log.info("Gethering result from HostAgent");

		/* step 3: conduct the attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

        log.info("Agent-Manager retrieves result from App-Agent");
        String removedItem = appm.read();
        log.info("Removed Item: ");

		/* step 4: decide if the attack is feasible */
        ResultInfo result = new ResultInfo();
        result.addType(ResultInfo.APPAGENT_REPLY);
        result.setResult(removedItem);

        analyzer.checkResult(test, result);

        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();

        return true;
    }

    /*
     * 3.1.050 - Switch Table Flooding
     */
    public boolean testSwitchTableFlooding(TestCase test) {
        log.info(test.getcasenum() + " - Switch Table Flooding");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("Channel-Agent starts");
        channelm.write(test.getcasenum());
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
     * 3.1.060 - Switch Identification Spoofing
     */
    public boolean testSwitchIdentificationSpoofing(TestCase test) {
        log.info(test.getcasenum() + " - Switch Identification Spoofing");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("Channel-Agent starts");
        channelm.write(test.getcasenum());

        log.info("Agent-Manager retrieves result from Channel-Agent");
        String resultChannel = channelm.read();


		/* step 3: try communication */
        log.info("Host-Agent sends packets to others");
        String flowResult = generateFlow("ping");

        log.info("Agent-Manager retrieves result from Host-Agent");

		/* step 4: decide if the attack is feasible */
        ResultInfo result = new ResultInfo();
        result.addType(ResultInfo.COMMUNICATON);
        result.setResult(flowResult);

        analyzer.checkResult(test, result);

        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * ---- Malformed Control Message
     */
    public boolean testMalformedControlMessage(TestCase test) {
        log.info(test.getcasenum() + " - Malformed Control Message");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("ChannelAgent starts");
        channelm.write(test.getcasenum());

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

        analyzer.checkResult(test, result);
        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        channelm.write("exit");
        controllerm.flushARPcache();
        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * 3.1.070 - Flow Rule Modification
     */
    public boolean testFlowRuleModification(TestCase test) {
        log.info(test.getcasenum() + " - Flow Rule Modification");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

        log.info("Host-Agent sends packets to others (before)");
        /* step 2 : generate before flow (ping) */

        String before = generateFlow("ping");

		/* step 3 : replay attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

        String modified = "";

        log.info("Agent-Manager retrieves result from App-Agent");
        modified = appm.read();

        log.info("Host-Agent sends packets to others (after)");
        String after = generateFlow("ping");

        ResultInfo result = new ResultInfo();

		/* step 4: decide if the attack is feasible */
        result.addType(ResultInfo.APPAGENT_REPLY);
        result.setResult(modified);

        analyzer.checkResult(test, result);
        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * 3.1.080 - Flow Table Clearance
     */
    public boolean testFlowTableClearance(TestCase test) {
        log.info(test.getcasenum() + " - Flow Table Clearance");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

        log.info("Host-Agent sends packets to others (before)");
        /* step 2 : generate before flow (ping) */

        String before = generateFlow("compare");

		/* step 3 : replay attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

        log.info("Host-Agent sends packets to others (after)");
        String after = generateFlow("compare");

        ResultInfo result = new ResultInfo();

		/* step 4: decide if the attack is feasible */
        result.addType(ResultInfo.LATENCY_TIME);
        result.setLatency(before, after);

        analyzer.checkResult(test, result);
        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * 3.1.090 - Event Unsubscription
     */
    public boolean testEventUnsubscription(TestCase test) {
        if (controllerm.getType().equals("ONOS")) {
            System.out.println("\nONOS is impossible to replay [" + test.getcasenum() + "] ");
            return false;
        }

        log.info(test.getcasenum() + " - Event Unsubscription");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

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
        analyzer.checkResult(test, result);

        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * 3.1.100 - Application Eviction
     */
    public boolean testApplicationEviction(TestCase test) {
        if (controllerm.getType().equals("Floodlight")) {
            System.out.println("\nFloodlight is impossible to replay [" + test.getcasenum() + "] ");
            return false;
        }

        log.info(test.getcasenum() + " - Application Eviction");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

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
        analyzer.checkResult(test, result);

        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * 3.1.110 - Memory Exhaustion
     */
    public boolean testMemoryExhaustion(TestCase test) {
        log.info(test.getcasenum() + " - Memory Exhaustion");

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
        appm.write(test.getcasenum());

        try {
            Thread.sleep(1000);
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

        analyzer.checkResult(test, result);
        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * 3.1.120 - CPU Exhaustion
     */
    public boolean testCPUExhaustion(TestCase test) {
        log.info(test.getcasenum() + " - CPU Exhaustion");

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
        appm.write(test.getcasenum());

        try {
            Thread.sleep(1000);
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

        analyzer.checkResult(test, result);
        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * 3.1.130 - System Variable Manipulation
     */
    public boolean testSystemVariableManipulation(TestCase test) {
        log.info(test.getcasenum() + " - System Variable Manipulation");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

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
        analyzer.checkResult(test, result);

        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();
        return true;
    }

    /*
     * 3.1.140 - System Command Execution
     */
    public boolean testSystemCommandExecution(TestCase test) {
        log.info(test.getcasenum() + " - System Command Execution");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.info("Agent-Manager checks the status of target controller");

        ResultInfo result = new ResultInfo();
        result.addType(ResultInfo.CONTROLLER_STATE);
        if (analyzer.checkResult(test, result)) {
            appm.closeSocket();
            controllerm.killController();
        }
        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));
        return true;
    }

	/*
     * 3.1.150 - Host Location Hijacking : Not implemented yet
	 */

    /*
     * 3.1.160 - Link Fabrication ; incomplete
     */
    public boolean testLinkFabrication(TestCase test) {
        log.info(test.getcasenum() + " - Link Fabrication");
        long start = System.currentTimeMillis();

		/* step 1: conduct the attack */
        log.info("Channel-Agent starts");
        channelm.write(test.getcasenum());
        channelm.read();

		/* step 2: create controller */
        initController();

        try {
            Thread.sleep(30000);
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
        analyzer.checkResult(test, result);

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
     * 3.1.170 - Evaesdrop
     */
    public boolean testEvaseDrop(TestCase test) {
        controllerm.flushARPcache();
        log.info(test.getcasenum() + " - Evaesdrop");
        String resultChannel;

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("Channel-Agent starts");
        channelm.write(test.getcasenum());

		/* step 3: try communication */
        log.info("Host-Agent sends packets to others");

        try {
            Thread.sleep(30000);    // 30 seconds
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        generateFlow("ping");

        log.info("Agent-Manager retrieves the result from Channel-Agent");
        channelm.write(test.getcasenum() + "-2");
        resultChannel = channelm.read();

		/* step 4: decide if the attack is feasible */
        ResultInfo result = new ResultInfo();
        result.addType(ResultInfo.CHANNELAGENT_REPLY);
        result.setResult(resultChannel);
        analyzer.checkResult(test, result);
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
    public boolean testManInTheMiddle(TestCase test) {
        controllerm.flushARPcache();
        log.info(test.getcasenum() + " - Man-In-The-Middle attack");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("Channel-Agent starts");
        channelm.write(test.getcasenum());
        String resultChannel = channelm.read();

		/* step 3: try communication */
        log.info("Host-Agent sends packets to others");
        String resultFlow = generateFlow("ping");
        log.info("Agent-Manager retrieves the result from Host-Agent");

		/* step 4: decide if the attack is feasible */
        ResultInfo result = new ResultInfo();
        result.addType(ResultInfo.COMMUNICATON);
        result.setResult(resultFlow);
        analyzer.checkResult(test, result);
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
    public boolean testFlowRuleFlooding(TestCase test) {
        log.info(test.getcasenum() + " - Flow Rule Flooding");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

        log.info("Host-Agent sends packets to others (before)");
        String before = generateFlow("compare");

		/* remove flow rules */
        appm.write("3.1.80|false");

		/* step 2: conduct the attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

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

        analyzer.checkResult(test, result);
        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();

        return true;
    }

    /*
     * 3.1.200 - Switch Firmware Misuse
     */
    public boolean testSwitchFirmwareMisuse(TestCase test) {
        log.info(test.getcasenum() + " - Switch Firmware Misuse");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

        log.info("Host-Agent sends packets to others (before)");
        String before = generateFlow("compare");

		/* step 2: conduct the attack */
        log.info("App-Agent starts");
        appm.write(test.getcasenum());

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
        analyzer.checkResult(test, result);
        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        appm.closeSocket();
        controllerm.killController();

        return true;
    }

    /*
     * ---- Control Message Manipulation
     */
    public boolean testControlMessageManipulation(TestCase test) {
        log.info(test.getcasenum() + " - Control Message Manipulation");

		/* step 1: create controller */
        initController();
        long start = System.currentTimeMillis();

		/* step 2: conduct the attack */
        log.info("ChannelAgent starts");
        channelm.write(test.getcasenum());
        String resultChannel = channelm.read();

		/* step 3: try communication */
        log.info("HostAgent generates flow");
        generateFlow("ping");

		/* step 4: decide if the attack is feasible */
        log.info("Check switch state");

        ResultInfo result = new ResultInfo();
        result.addType(ResultInfo.SWITCH_STATE);
        analyzer.checkResult(test, result);
        long end = System.currentTimeMillis();
        log.info("Running Time: " + (end - start));

        channelm.write("exit");
        controllerm.flushARPcache();
        appm.closeSocket();
        controllerm.killController();
        return true;
    }
}
