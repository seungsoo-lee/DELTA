package org.deltaproject.manager.testcase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.deltaproject.manager.core.Configuration;
import org.deltaproject.manager.dummy.DummyController;
import org.deltaproject.webui.TestCase;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.errormsg.OFHelloFailedErrorMsg;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionGotoTable;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;

import static org.deltaproject.webui.TestCase.TestResult.*;

public class TestSwitchCase {
    private static final Logger log = LoggerFactory.getLogger(TestSwitchCase.class);

    public static final int HANDSHAKE_DEFAULT = 0;
    public static final int HANDSHAKE_NO_HELLO = 1;
    public static final int HANDSHAKE_INCOMPATIBLE_HELLO = 2;
    public static final int NO_HANDSHAKE = 3;

    public static final int DEFAULT_TIMEOUT = 5000;

    private Configuration cfg = Configuration.getInstance();

    private DummyController dmcnt;
    private OFFactory defaultFactory;
    private Random random;

    private long r_xid = 0xeeeeeeeel;

    private String ofversion;
    private int ofport;

    private Process proc;
    private int procPID;

    public TestSwitchCase() {
        random = new Random();

        ofversion = cfg.getOFVer();
        if (ofversion.equals("1.0"))
            defaultFactory = OFFactories.getFactory(OFVersion.OF_10);
        else if (ofversion.equals("1.3"))
            defaultFactory = OFFactories.getFactory(OFVersion.OF_13);

        ofport = Integer.parseInt(cfg.getOFPort());
    }

    public void runRemoteAgents() {
        log.info("Run test mininet topology");

        String mininet;

        if (ofversion.equals("1.0"))
            mininet = " sudo python test-switch-topo.py " + cfg.getDMCIP() + " " + cfg.getDMCPort() + " OpenFlow10";
        else
            mininet = " sudo python test-switch-topo.py " + cfg.getDMCIP() + " " + cfg.getDMCPort() + " OpenFlow13";

        try {
            proc = Runtime.getRuntime().exec("ssh " + cfg.getHostSSH() + mininet);

            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(proc);
            this.procPID = (Integer) value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopRemoteAgents() {
        if (procPID != -1)
            try {
                proc = Runtime.getRuntime().exec("sudo kill -9 " + this.procPID);
                proc.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        else
            procPID = -1;

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void replayKnownAttack(TestCase test) throws InterruptedException {
        runRemoteAgents();

        switch (test.getcasenum()) {
            case "1.1.010":
                testPortRangeViolation(test);
                break;
            case "1.1.020":
                testTableID(test);
                break;
            case "1.1.030":
                testGroupID(test);
                break;
            case "1.1.040":
                testMeterID(test);
                break;
            case "1.1.050":
                testTableLoop(test);
                break;
            case "1.1.060":
                testCorruptedControlMsgType(test);
                break;
            case "1.1.070":
                testUnsupportedVersionNumber(test);
                break;
            case "1.1.080":
                testMalformedVersionNumber(test);
                break;
            case "1.1.090":
                testInvalidOXMType(test);
                break;
            case "1.1.100":
                testInvalidOXMLength(test);
                break;
            case "1.1.110":
                testInvalidOXMValue(test);
                break;
            case "1.1.120":
                testDisabledTableFeatureRequest(test);
                break;
            case "1.1.130":
                testHandshakeWithoutHello(test);
                break;
            case "1.1.140":
                testControlMsgBeforeHello(test);
                break;
            case "1.1.150":
                testIncompatibleHelloAfterConnection(test);
                break;
            case "1.1.160":
                testCorruptedCookieValue(test);
                break;
            case "1.1.170":
                testMalformedBufferIDValue(test);
                break;
        }

        stopRemoteAgents();
    }

    public long genXid() {
        long l = random.nextLong();
        if (l == 0)
            return 1;

        return l;
    }

    public void setUpDummyController(int type) {
        dmcnt = new DummyController(ofversion, ofport);
        dmcnt.listeningSwitch();
        dmcnt.setHandShakeType(type);
        dmcnt.start();

        if (type != HANDSHAKE_DEFAULT)
            return;

        while (!dmcnt.getHandshaked()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void stopDummyController() {
        dmcnt.interrupt();

        while (!dmcnt.isSockClosed()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public int getConnectedSwitch() {
        Process temp;
        String tempS;

        int cnt = 0;

        String cmd = "";

        try {
            temp = Runtime.getRuntime().exec("sudo netstat -ap | grep " + this.ofport);
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(temp.getInputStream()));

            while ((tempS = stdOut.readLine()) != null) {
                if (tempS.contains("ESTABLISHED")) {
                    cnt++;
                }
            }

            stdOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cnt;
    }


    /*
     * 1.1.010 - Port Range Violation
     * Verify that the switch rejects the use of ports that are greater thanc
     * OFPP_MAX and are not part of the reserved ports.
     */
    public void testPortRangeViolation(TestCase test) throws InterruptedException {
        log.info(test.getcasenum() + " - Port Range Violation - Test switch protection against disallowed ports");

        setUpDummyController(HANDSHAKE_DEFAULT);

        OFPortMod request = defaultFactory.buildPortMod().setXid(r_xid).setPortNo(OFPort.ANY).build();
        log.info("Send msg: " + request.toString());
        dmcnt.sendMsg(request, -1);

        Thread.sleep(999);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.020 - Table Identifier Violation (OF 1.3~)
     * Verify that the switch rejects the use of invalid table id.
     */
    public void testTableID(TestCase test) throws InterruptedException {
        if (this.ofversion.equals("1.0")) {
            log.info("OF 1.0 is not available.");
            return;
        }

        log.info(test.getcasenum() + " - Table Identifier Violation - Test switch protection against invalid table ID");

        setUpDummyController(HANDSHAKE_DEFAULT);

        int inport = 1;
        int outport = 2;

        Match.Builder mb = defaultFactory.buildMatch();
        mb.setExact(MatchField.IN_PORT, OFPort.ofInt(inport));

        OFActionOutput.Builder aob = defaultFactory.actions().buildOutput();
        aob.setMaxLen(Integer.MAX_VALUE);
        aob.setPort(OFPort.ofInt(outport));

        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(aob.build());

        OFInstructionApplyActions apa = defaultFactory.instructions().applyActions(actions);
        List<OFInstruction> inst = new ArrayList<OFInstruction>();
        inst.add(apa);

        Set<OFFlowModFlags> set = new HashSet<OFFlowModFlags>();
        set.add(OFFlowModFlags.SEND_FLOW_REM);

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setPriority(10);
        fab.setXid(r_xid);

        Random ran = new Random();
        fab.setTableId(TableId.of(255));                // table num
        fab.setMatch(mb.build());
        fab.setInstructions(inst);
        fab.setBufferId(OFBufferId.NO_BUFFER);
        fab.setFlags(set);

        OFFlowAdd request = fab.build();
        log.info("Send msg: " + request.toString());
        dmcnt.sendMsg(request, -1);

        Thread.sleep(1000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.030 - Group Identifier Violation (OF 1.3~)
     * Verify that the switch rejects the use of groups that are greater than
     * OFPG_MAX and are not part of the reserved groups.
     */
    public void testGroupID(TestCase test) throws InterruptedException {
        if (this.ofversion.equals("1.0")) {
            log.info("OF 1.0 is not available.");
            return;
        }

        log.info(test.getcasenum() + " - Group Identifier Violation - Test switch protection against disallowed group numbers");

        setUpDummyController(HANDSHAKE_DEFAULT);

        int outport = 2;

        OFActionOutput.Builder aob = defaultFactory.actions().buildOutput();
        aob.setMaxLen(Integer.MAX_VALUE);
        aob.setPort(OFPort.ofInt(outport));

        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(aob.build());

        OFBucket.Builder bb = defaultFactory.buildBucket();
        bb.setActions(actions);

        List<OFBucket> bklist = new ArrayList<OFBucket>();
        bklist.add(bb.build());

        OFGroupModify.Builder gab = defaultFactory.buildGroupModify();
        gab.setGroupType(OFGroupType.ALL);
        gab.setGroup(OFGroup.MAX);
        gab.setBuckets(bklist);
        gab.setXid(this.r_xid);

        OFGroupModify request = gab.build();
        log.info("Send msg: " + request.toString());
        dmcnt.sendMsg(request, -1);

        Thread.sleep(1000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();

    }

    /*
     * 1.1.040 - Meter Identifier Violation (OF 1.3~)
     * Verify that the switch rejects the use of meters that are greater than
     * OFPM_MAX and are not part of the virtual meters.
     */
    public void testMeterID(TestCase test) throws InterruptedException {
        if (this.ofversion.equals("1.0")) {
            log.info("OF 1.0 is not available.");
            return;
        }

        log.info(test.getcasenum() + " - Meter Identifier Violation - Test switch protection against disallowed meter numbers");

        setUpDummyController(HANDSHAKE_DEFAULT);

        OFMeterMod.Builder mmb = defaultFactory.buildMeterMod();
        mmb.setXid(this.r_xid);
        mmb.setMeterId(0xFFFFFFFFl);

        OFMeterMod request = mmb.build();

        log.info("Send msg: " + request.toString());
        dmcnt.sendMsg(request, -1);

        Thread.sleep(1000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.050 - Table Loop Violation (OF 1.3~)
     * Verify that the switch rejects the use of invalid Goto table id
     * requesting a table loop.
     */
    public void testTableLoop(TestCase test) throws InterruptedException {
        if (this.ofversion.equals("1.0")) {
            log.info("OF 1.0 is not available.");
            return;
        }

        log.info(test.getcasenum() + " - Table Loop Violation - Test switch protection against invalid GoToTable request");

        setUpDummyController(HANDSHAKE_DEFAULT);

        int inport = 1;
        int outport = 2;

        Match.Builder mb = defaultFactory.buildMatch();
        mb.setExact(MatchField.IN_PORT, OFPort.ofInt(inport));

        OFActionOutput.Builder aob = defaultFactory.actions().buildOutput();
        aob.setMaxLen(Integer.MAX_VALUE);
        aob.setPort(OFPort.ofInt(outport));

        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(aob.build());

        OFInstructionApplyActions apa = defaultFactory.instructions().applyActions(actions);
        OFInstructionGotoTable gotoinst = defaultFactory.instructions().gotoTable(TableId.of(1));

        List<OFInstruction> inst = new ArrayList<OFInstruction>();
        inst.add(apa);
        inst.add(gotoinst);                     // go to table id 1

        Set<OFFlowModFlags> set = new HashSet<OFFlowModFlags>();
        set.add(OFFlowModFlags.SEND_FLOW_REM);

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setPriority(1000);
        fab.setXid(r_xid);
        fab.setTableId(TableId.of(10));         // table id 10
        fab.setMatch(mb.build());
        fab.setInstructions(inst);
        fab.setBufferId(OFBufferId.NO_BUFFER);
        fab.setFlags(set);

        OFFlowAdd request = fab.build();
        log.info("Send msg: " + request.toString());
        dmcnt.sendMsg(request, -1);

        Thread.sleep(1000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.060 - Corrupted Control Message Type
     * Verify that the switch throws an error when it receives a control message
     * with unsupported message type.
     */
    public void testCorruptedControlMsgType(TestCase test) throws InterruptedException {
        log.info(test.getcasenum() + " - Corrupted Control Message Type - Test switch protection against control message with unsupported type");

        setUpDummyController(HANDSHAKE_DEFAULT);

        byte[] rawPkt = new byte[8];
        if (this.ofversion.equals("1.3"))
            rawPkt[0] = 0x04; // version
        else if (this.ofversion.equals("1.0"))
            rawPkt[0] = 0x01;

        rawPkt[1] = 0x61; // Type == 97
        rawPkt[2] = 0x00;
        rawPkt[3] = 0x08;

        rawPkt[4] = (byte) 0xee;
        rawPkt[5] = (byte) 0xee;
        rawPkt[6] = (byte) 0xee;
        rawPkt[7] = (byte) 0xee;

        log.info("Send msg with unsupported of type (97)");
        dmcnt.sendRawMsg(rawPkt);

        Thread.sleep(1000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.070 - Unsupported Version Numeber
     * Verify that the switch throws an error when it receives a connection
     * setup message with an unsupported version number.
     */
    public void testUnsupportedVersionNumber(TestCase test) throws InterruptedException {
        log.info(test.getcasenum() + " - Unsupported Version Numeber - Test switch protection against connection setup request with unsupported version number");

        setUpDummyController(HANDSHAKE_INCOMPATIBLE_HELLO);

        Thread.sleep(1000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.080 - Malformed Version Number
     * Verify that the switch throws an error when it receives a malformed
     * version number after establishing connection between switch and
     * controller with a different version.
     */
    public void testMalformedVersionNumber(TestCase test) throws InterruptedException {
        log.info(test.getcasenum() + " - Malformed Version Number - Test switch protection against communication with mismatched OpenFlow versions");

        setUpDummyController(HANDSHAKE_DEFAULT);

        OFPortMod request = defaultFactory.buildPortMod().setXid(r_xid).setPortNo(OFPort.of(1)).setConfig(1).build();

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(1024);

        request.writeTo(buf);

        int length = buf.readableBytes();
        byte[] bytes = new byte[length];
        buf.getBytes(buf.readerIndex(), bytes);

        if (this.ofversion.equals("1.3"))
            bytes[0] = 0x01; // version
        else if (this.ofversion.equals("1.0"))
            bytes[0] = 0x04;

        log.info("Send msg :" + request.toString());
        dmcnt.sendRawMsg(bytes);

        buf.clear();
        buf.release();

        Thread.sleep(2000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.090 - Invalid OXM - Type (Command)
     * Verify that the switch throws an error when it receives a flow mod
     * message with invalid OXM type.
     */
    public void testInvalidOXMType(TestCase test) throws InterruptedException {
        log.info(test.getcasenum() + " - Invalid OXM - Type - Test switch protection against flow mod with invalid message type");

        setUpDummyController(HANDSHAKE_DEFAULT);

        int inport = 1;
        int outport = 2;

        Match.Builder mb = defaultFactory.buildMatch();
        mb.setExact(MatchField.IN_PORT, OFPort.ofInt(inport));

        OFActionOutput.Builder aob = defaultFactory.actions().buildOutput();
        aob.setMaxLen(Integer.MAX_VALUE);
        aob.setPort(OFPort.ofInt(outport));

        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(aob.build());

        /*OFInstructionApplyActions apa = defaultFactory.instructions().applyActions(actions);
        List<OFInstruction> inst = new ArrayList<OFInstruction>();
        inst.add(apa);*/

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setXid(r_xid);
        fab.setMatch(mb.build());
        fab.setActions(actions);
        fab.setHardTimeout(1000);

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(1024);

        OFFlowAdd request = fab.build();
        request.writeTo(buf);

        if (this.ofversion.equals("1.3"))
            buf.setByte(25, 0x5);                   // unknown command 0x05
        else if (this.ofversion.equals("1.0"))
            buf.setByte(57, 0x5);

        log.info("Send msg :" + request.toString() + " with unknown command (0x05) FlowMod");
        dmcnt.sendRawMsg(buf);

        Thread.sleep(2000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.100 - Invalid OXM - Length (OF 1.3~)
     * Verify that the switch throws an error when it receives a flow mod
     * message with invalid OXM length.
     */
    public void testInvalidOXMLength(TestCase test) throws InterruptedException {
        if (this.ofversion.equals("1.0")) {
            log.info("OF 1.0 is not available.");
            return;
        }

        log.info(test.getcasenum() + " - Invalid OXM - Length - Test switch protection against flow mod with invalid message length");

        setUpDummyController(HANDSHAKE_DEFAULT);

        int inport = 1;
        int outport = 2;

        Match.Builder mb = defaultFactory.buildMatch();
        mb.setExact(MatchField.IN_PORT, OFPort.ofInt(inport));

        OFActionOutput.Builder aob = defaultFactory.actions().buildOutput();
        aob.setMaxLen(Integer.MAX_VALUE);
        aob.setPort(OFPort.ofInt(outport));

        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(aob.build());

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setXid(r_xid);
        fab.setMatch(mb.build());
        fab.setActions(actions);
        fab.setHardTimeout(1000);

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(1024);

        OFFlowAdd request = fab.build();
        request.writeTo(buf);

        if (this.ofversion.equals("1.3"))
            buf.setByte(55, 0x5);                   // before length 4 -> after 5

        log.info("Send msg :" + request.toString() + " with invalid OXM length");
        dmcnt.sendRawMsg(buf);

        Thread.sleep(2000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.110 - Invalid OXM - Value (OF 1.3~)
     * Verify that the switch throws an error when it receives a flow mod
     * message with invalid message value
     */
    public void testInvalidOXMValue(TestCase test) throws InterruptedException {
        if (this.ofversion.equals("1.0")) {
            log.info("OF 1.0 is not available.");
            return;
        }

        log.info(test.getcasenum() + " - Invalid OXM - Value - Test switch protection against flow mod with invalid message value");

        setUpDummyController(HANDSHAKE_DEFAULT);

        int inport = 1;
        int outport = 2;

        Match.Builder mb = defaultFactory.buildMatch();
        mb.setExact(MatchField.IN_PORT, OFPort.ofInt(inport));
        mb.setExact(MatchField.ETH_TYPE, EthType.of(0x800));
        mb.setExact(MatchField.IP_DSCP, IpDscp.DSCP_63);

        OFActionOutput.Builder aob = defaultFactory.actions().buildOutput();
        aob.setMaxLen(Integer.MAX_VALUE);
        aob.setPort(OFPort.ofInt(outport));

        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(aob.build());

        OFInstructionApplyActions apa = defaultFactory.instructions().applyActions(actions);

        List<OFInstruction> inst = new ArrayList<OFInstruction>();
        inst.add(apa);

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setTableId(TableId.of(10));
        fab.setXid(r_xid);
        fab.setMatch(mb.build());
        fab.setInstructions(inst);
        fab.setHardTimeout(1000);

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(1024);

        OFFlowAdd request = fab.build();
        request.writeTo(buf);

        buf.setByte(70, 0x40); // DSCP_63 Value : 63(0x3f) -> 64(0x40)

        log.info("Send msg :" + request.toString() + "with invalid IP_DSCP value 63(0x3f) -> 64(0x40)");
        dmcnt.sendRawMsg(buf);

        Thread.sleep(2000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.120 - Disabled Table Features Request (OF 1.3 ~)
     * If the switch has disabled the table feature request with non-empty body,
     * verify that the switch rejects this non-empty OFPMP_TABLE_FEATURES
     * request with a permission error
     */
    public void testDisabledTableFeatureRequest(TestCase test) throws InterruptedException {
        if (this.ofversion.equals("1.0")) {
            log.info("OF 1.0 is not available.");
            return;
        }

        log.info(test.getcasenum() + " - Disabled Table Features Request - Test for switch protection against table features request when feature is disabled");

        setUpDummyController(HANDSHAKE_DEFAULT);

        OFTableFeaturesStatsRequest.Builder table = defaultFactory.buildTableFeaturesStatsRequest();
        table.setXid(r_xid);
        OFMessage request = table.build();

        dmcnt.sendMsg(request, -1);

        Thread.sleep(2000);

        OFMessage response = dmcnt.getResponse();
        log.info("Send msg :" + request.toString());
        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.130 - Handshake without Hello Message
     * Check if the control connection is disconnected if the hello message is
     * not exchanged within the specified default timeout.
     */
    public void testHandshakeWithoutHello(TestCase test) throws InterruptedException {
        log.info(test.getcasenum() + " - Handshake without Hello Message - Test for switch protection against incomplete control connection left open");

        setUpDummyController(TestSwitchCase.NO_HANDSHAKE);
        log.info("The hello message is not exchanged within the specified default timeout");

        Thread.sleep(DEFAULT_TIMEOUT);

        if (getConnectedSwitch() == 0) {
            test.setResult(PASS);
            log.info("Switch disconnected, PASS");
        } else {
            test.setResult(FAIL);
            log.info("Switch is not disconnected, FAIL");
        }
        stopDummyController();
    }

    /*
     * 1.1.140 - Control Message before Hello Message
     * In the main connection between switch and controller, check if the switch
     * processes a control message before exchanging OpenFlow hello message
     * (connection establishment).
     */
    public void testControlMsgBeforeHello(TestCase test) throws InterruptedException {
        log.info(test.getcasenum() + " - Control Message before Hello Message - Test for switch protection against control communication prior to completed connection establishment");

        OFBarrierRequest.Builder brb = defaultFactory.buildBarrierRequest();
        brb.setXid(r_xid);

        setUpDummyController(TestSwitchCase.NO_HANDSHAKE);

        OFMessage request = brb.build();
        dmcnt.sendMsg(request, -1);

        Thread.sleep(1000);

        OFMessage response = dmcnt.getResponse();
        log.info("Send msg :" + request.toString());
        if (response != null) {
            test.setResult(FAIL);
            log.info("Response msg: " + response.toString() + ", FAIL");
        } else {
            test.setResult(PASS);
            log.info("Response is ignored, PASS");
        }

        stopDummyController();
    }

    /*
     * 1.1.150 - Incompatible Hello after Connection Establishment
     * Verify that the switch will properly handle the abnormal condition, when
     * it receives an OFPT_ERROR message with a type field of
     * OFPET_HELLO_FAILED, a code field of OFPHFC_INCOMPATIBLE after
     * establishing connection between switch and controller with a both agreed
     * version.
     */
    public void testIncompatibleHelloAfterConnection(TestCase test) throws InterruptedException {
        log.info(test.getcasenum() + " - Incompatible Hello after Connection Establishment - Test for switch protection against abuse of the Hello_Failed error message");

        setUpDummyController(TestSwitchCase.HANDSHAKE_DEFAULT);

        OFHelloFailedErrorMsg.Builder hfb = defaultFactory.errorMsgs().buildHelloFailedErrorMsg();
        hfb.setXid(r_xid);
        hfb.setCode(OFHelloFailedCode.INCOMPATIBLE);

        OFMessage request = hfb.build();
        dmcnt.sendMsg(request, -1);

        Thread.sleep(1000);

        OFMessage response = dmcnt.getResponse();
        log.info("Send msg :" + request.toString());
        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.160 - Corrupted Cookie Values
     * Verify that the switch throws an error when it receives a corrupted
     * cookie value in OpenFlow messages after establishing connection between
     * switch and controller.
     */
    public void testCorruptedCookieValue(TestCase test) throws InterruptedException {
        log.info(test.getcasenum() + " - Corrupted Cookie Values - Test for switch protection against replay attacks");

        setUpDummyController(HANDSHAKE_DEFAULT);

        int inport = 1;
        int outport = 2;

        Match.Builder mb = defaultFactory.buildMatch();
        mb.setExact(MatchField.IN_PORT, OFPort.ofInt(inport));

        OFActionOutput.Builder aob = defaultFactory.actions().buildOutput();
        aob.setMaxLen(Integer.MAX_VALUE);
        aob.setPort(OFPort.ofInt(outport));

        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(aob.build());

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setXid(r_xid);
        fab.setMatch(mb.build());
        fab.setActions(actions);
        fab.setHardTimeout(1000);

        fab.setCookie(U64.of(0xFFFFFFFFFFFFFFFFl));

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(1024);

        OFFlowAdd request = fab.build();
        request.writeTo(buf);

        log.info("Send msg :" + request.toString() + " with corrupted cookie value");
        dmcnt.sendRawMsg(buf);

        Thread.sleep(1000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();
    }

    /*
     * 1.1.170 - Malformed Buffer ID Values
     * Verify that the switch throws an error when it receives a malformed
     * buffer ID value after establishing connection between switch &
     * controller.
     */
    public void testMalformedBufferIDValue(TestCase test) throws InterruptedException {
        log.info(test.getcasenum() + " - Malformed Buffer ID Values - Test for switch protection against disallowed buffer ID values");

        setUpDummyController(HANDSHAKE_DEFAULT);

        int inport = 1;
        int outport = 2;

        Match.Builder mb = defaultFactory.buildMatch();
        mb.setExact(MatchField.IN_PORT, OFPort.ofInt(inport));

        OFActionOutput.Builder aob = defaultFactory.actions().buildOutput();
        aob.setMaxLen(Integer.MAX_VALUE);
        aob.setPort(OFPort.ofInt(outport));

        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(aob.build());

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setXid(r_xid);
        fab.setMatch(mb.build());
        fab.setActions(actions);
        fab.setHardTimeout(1000);

        Random ran = new Random();

        fab.setBufferId(OFBufferId.of(ran.nextInt(0xffff) + 1));

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(1024);

        OFFlowAdd request = fab.build();
        request.writeTo(buf);

        log.info("Send msg :" + request.toString() + " with malformed buffer ID values");
        dmcnt.sendRawMsg(buf);

        Thread.sleep(1000);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            test.setResult(PASS);
            log.info("Response err msg: " + response.toString() + ", PASS");
        } else {
            test.setResult(FAIL);
            log.info("Response is null, FAIL");
        }

        stopDummyController();


    }

    /*
     * 1.2.010 - Slave Controller Violation (OF 1.3 ~)
     * Verify that the switch rejects unsupported control messages
     * (OFPT_PACKET_OUT, OFPT_FLOW_MOD, OFPT_GROUP_MOD, OFPT_PORT_MOD,
     * OFPT_TABLE_MOD requests, and OFPMP_TABLE_FEATURES) from slave
     * controllers.
     */
    public void testSlaveControllerViolation(TestCase test) {
        if (this.ofversion.equals("1.0")) {
            log.info("OF 1.0 is not available.");
            return;
        }
        log.info(test.getcasenum() + " - Slave Controller Violation - Test for switch protection against manipulation of the network by the slave controller");

        setUpDummyController(HANDSHAKE_DEFAULT);

        int inport = 1;
        int outport = 2;

        OFRoleRequest.Builder rrb = defaultFactory.buildRoleRequest();
        rrb.setRole(OFControllerRole.ROLE_NOCHANGE);
        rrb.setGenerationId(U64.ZERO);
        rrb.setXid(r_xid);

        OFRoleRequest request = rrb.build();
        dmcnt.sendMsg(request, -1);

        OFMessage response = dmcnt.getResponse();

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        if (response != null) {
            OFRoleReply rr = (OFRoleReply) response;
            U64 gen = rr.getGenerationId();

            OFRoleRequest.Builder rrb2 = defaultFactory.buildRoleRequest();
            rrb2.setRole(OFControllerRole.ROLE_SLAVE);
            rrb2.setGenerationId(gen);
            rrb2.setXid(r_xid - 1);

            request = rrb2.build();
            dmcnt.sendMsg(request, -1);
        }

        OFPacketOut pktout = defaultFactory.buildPacketOut().setXid(r_xid).setBufferId(OFBufferId.NO_BUFFER).build();
        dmcnt.sendMsg(pktout, -1);
        response = dmcnt.getResponse();

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

}
