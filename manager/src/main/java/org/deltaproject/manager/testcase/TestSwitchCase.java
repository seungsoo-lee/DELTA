package org.deltaproject.manager.testcase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.deltaproject.manager.core.Configuration;
import org.deltaproject.manager.dummy.DMController;
import org.deltaproject.manager.dummy.DummyController;
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

import java.util.*;

public class TestSwitchCase {
    private static final Logger log = LoggerFactory.getLogger(TestSwitchCase.class);

    public static final int VALID_HANDSHAKE = 0;
    public static final int HANDSHAKE_NO_HELLO = 1;
    public static final int HANDSHAKE_INCOMPATIBLE_HELLO = 2;

    private DummyController dcontroller;
    private DMController dmcnt;
    private OFFactory defaultFactory;
    private Random random;

    private long r_xid = 0xeeeeeeeel;

    private String ofversion;
    private int ofport;
    private Configuration cfg;

    public TestSwitchCase(Configuration in) {
        random = new Random();
        cfg = in;

        ofversion = cfg.getOFVer();
        if (ofversion.equals("1.0"))
            defaultFactory = OFFactories.getFactory(OFVersion.OF_10);
        else if (ofversion.equals("1.3"))
            defaultFactory = OFFactories.getFactory(OFVersion.OF_13);

        ofport = Integer.parseInt(cfg.getOFPort());
    }

    public void replayKnownAttack(String code) {
        switch (code) {
            case "1.1.010":
                testPortRangeViolation(code);
                break;
            case "1.1.020":
                testTableID(code);
                break;
            case "1.1.030":
                testGroupID(code);
                break;
            case "1.1.040":
                testMeterID(code);
                break;
            case "1.1.050":
                testTableLoop(code);
                break;
            case "1.1.060":
                testCorruptedControlMsgType(code);
                break;
            case "1.1.070":
                testUnsupportedVersionNumber(code);
                break;
            case "1.1.080":
                testMalformedVersionNumber(code);
                break;
            case "1.1.090":
                testInvalidOXMType(code);
                break;
            case "1.1.100":
                testInvalidOXMLength(code);
                break;
            case "1.1.110":
                testInvalidOXMValue(code);
                break;
            case "1.1.120":
                testDisabledTableFeatureRequest(code);
                break;
            case "1.1.130":
                testHandshakeWithoutHello(code);
                break;
            case "1.1.140":
                testControlMsgBeforeHello(code);
                break;
            case "1.1.150":
                testIncompatibleHelloAfterConnection(code);
                break;
            case "1.1.160":
                testCorruptedCookieValue(code);
                break;
            case "1.1.170":
                testMalformedBufferIDValue(code);
                break;
        }
    }

    public long genXid() {
        long l = random.nextLong();
        if (l == 0)
            return 1;

        return l;
    }

    public void setUpDummyControllerWithNoHello(int type) {
        dcontroller = new DummyController(this.ofversion, ofport);
        dcontroller.setHandShakeTest(type);
        dcontroller.bootstrapNetty();

        while (!dcontroller.isOFHandlerActive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void setUpDummyController() {
//        dcontroller = new DummyController(this.ofversion, ofport);
//        dcontroller.bootstrapNetty();
//
//        while (!dcontroller.isOFHandlerActive()) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }

        dmcnt = new DMController(ofversion, ofport);
        dmcnt.listeningSwitch();


        dmcnt.start();
    }

    public void stopDummyController() {
        dcontroller.stopNetty();
    }


    /*
     * 1.1.010 - Port Range Violation
     * Verify that the switch rejects the use of ports that are greater than
     * OFPP_MAX and are not part of the reserved ports.
     */
    public void testPortRangeViolation(String code) {
        String info = code + " - Port Range Violation";
        log.info(info);

        setUpDummyController();

        OFPortMod request = defaultFactory.buildPortMod().setXid(r_xid).setPortNo(OFPort.ofShort((short)-1)).build();
        OFMessage response = dcontroller.sendOFMessage(request);

        if (response != null) {
            log.info("response msg: " + response.toString()+", PASS");
        } else
            log.info("response is null, FAIL");

        stopDummyController();
    }

    /*
     * 1.1.020 - Table Identifier Violation
     * only 1.3 Verify that the switch rejects the use of invalid table id.
     */
    public void testTableID(String code) {
        String info = code + " - Table Identifier Violation";
        log.info(info);

        setUpDummyController();

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
        fab.setTableId(TableId.of(255));
        fab.setMatch(mb.build());
        fab.setInstructions(inst);
        fab.setBufferId(OFBufferId.NO_BUFFER);
        fab.setFlags(set);

        OFFlowAdd request = fab.build();
        OFMessage response = dcontroller.sendOFMessage(request);

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.030 - Group Identifier Violation
     * Verify that the switch rejects the use of groups that are greater than
     * OFPG_MAX and are not part of the reserved groups.
     */
    public void testGroupID(String code) {
        String info = code + " - Group Identifier Violation";
        log.info(info);

        setUpDummyController();

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

        OFGroupAdd.Builder gab = defaultFactory.buildGroupAdd();
        gab.setGroupType(OFGroupType.ALL);
        gab.setGroup(OFGroup.ANY);
        gab.setBuckets(bklist);
        gab.setXid(this.r_xid);

        OFGroupAdd request = gab.build();
        OFMessage response = dcontroller.sendOFMessage(request);

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();

    }

    /*
     * 1.1.040 - Meter Identifier Violation
     * Verify that the switch rejects the use of meters that are greater than
     * OFPM_MAX and are not part of the virtual meters.
     */
    public void testMeterID(String code) {
        String info = code + "  - Meter Identifier Violation";
        log.info(info);

        setUpDummyController();

        OFMeterMod.Builder mmb = defaultFactory.buildMeterMod();
        mmb.setXid(this.r_xid);
        mmb.setMeterId(0xFFFFFFFFl);

        OFMeterMod request = mmb.build();
        OFMessage response = dcontroller.sendOFMessage(request);

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.050 - Table Loop Violation
     * Verify that the switch rejects the use of invalid Goto table id
     * requesting a table loop.
     */
    public void testTableLoop(String code) {
        String info = code + " - Table Loop Violation";
        log.info(info);

        setUpDummyController();

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
        OFInstructionGotoTable gotoinst = defaultFactory.instructions().gotoTable(TableId.of(5));

        List<OFInstruction> inst = new ArrayList<OFInstruction>();
        inst.add(apa);
        inst.add(gotoinst);

        Set<OFFlowModFlags> set = new HashSet<OFFlowModFlags>();
        set.add(OFFlowModFlags.SEND_FLOW_REM);

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setPriority(1000);
        fab.setXid(r_xid);
        fab.setTableId(TableId.of(10));
        fab.setMatch(mb.build());
        fab.setInstructions(inst);
        fab.setBufferId(OFBufferId.NO_BUFFER);
        fab.setFlags(set);

        OFFlowAdd request = fab.build();
        OFMessage response = dcontroller.sendOFMessage(request);

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.060 - Corrupted Control Message Type
     * Verify that the switch throws an error when it receives a control message
     * with unsupported message type.
     */
    public void testCorruptedControlMsgType(String code) {
        String info = code + " - Corrupted Control Message Type";
        log.info(info);

        setUpDummyController();

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

        OFMessage response = dcontroller.sendRawPacket(rawPkt);
        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.070 - Unsupported Version Numebr
     * Verify that the switch throws an error when it receives a connection
     * setup message with an unsupported version number.
     */
    public void testUnsupportedVersionNumber(String code) {
        String info = code + " - Unsupported Version Numebr";
        log.info(info);

        setUpDummyController();

        byte[] rawPkt = new byte[8];
        rawPkt[0] = 0x05; // version 1.4
        rawPkt[1] = 0x0a; // Type == 10
        rawPkt[2] = 0x00;
        rawPkt[3] = 0x08;

        rawPkt[4] = (byte) 0xee;
        rawPkt[5] = (byte) 0xee;
        rawPkt[6] = (byte) 0xee;
        rawPkt[7] = (byte) 0xee;

        // switch disconnection
        OFMessage response = dcontroller.sendRawPacket(rawPkt);
        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.080 - Malformed Version Number
     * Verify that the switch throws an error when it receives a malformed
     * version number after establishing connection between switch and
     * controller with a different version.
     */
    public void testMalformedVersionNumber(String code) {
        String info = code + " - Malformed Version Number";
        log.info(info);

        setUpDummyController();

        byte[] rawPkt = new byte[8];
        rawPkt[0] = 0x00; // version = 0
        rawPkt[1] = 0x00; // Type == 0
        rawPkt[2] = 0x00;
        rawPkt[3] = 0x08;

        rawPkt[4] = (byte) 0xee;
        rawPkt[5] = (byte) 0xee;
        rawPkt[6] = (byte) 0xee;
        rawPkt[7] = (byte) 0xee;

        // switch disconnection
        OFMessage response = dcontroller.sendRawPacket(rawPkt);
        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.090 - Invalid OXM - Type
     * Verify that the switch throws an error when it receives a flow mod
     * message with invalid OXM type.
     */
    public void testInvalidOXMType(String code) {
        String info = code + " - Invalid OXM - Type";
        log.info(info);

        setUpDummyController();

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

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setTableId(TableId.of(10));
        fab.setXid(r_xid);
        fab.setMatch(mb.build());
        fab.setInstructions(inst);
        fab.setHardTimeout(1000);

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(88);

        OFFlowAdd request = fab.build();
        request.writeTo(buf);

        buf.setByte(1, 0x10); // type : 14 (FlowMod) -> 16 (PortMod)

        OFMessage response = dcontroller.sendRawPacket(buf);

        buf.clear();

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.100 - Invalid OXM - Length
     * Verify that the switch throws an error when it receives a flow mod
     * message with invalid OXM length.
     */
    public void testInvalidOXMLength(String code) {
        String info = code + " - Invalid OXM - Length";
        log.info(info);

        setUpDummyController();

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

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setTableId(TableId.of(10));
        fab.setXid(r_xid);
        fab.setMatch(mb.build());
        fab.setInstructions(inst);
        fab.setHardTimeout(1000);

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(88);

        OFFlowAdd request = fab.build();
        request.writeTo(buf);

        buf.setByte(2, 0x27); // length : 10000
        buf.setByte(3, 0x10); //

        OFMessage response = dcontroller.sendRawPacket(buf);

        buf.clear();

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.110 - Invalid OXM - Value
     * Verify that the switch throws an error when it receives a flow mod
     * message with invalid message value
     */
    public void testInvalidOXMValue(String code) {
        String info = code + " - Invalid OXM - Value";
        log.info(info);

        setUpDummyController();

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

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(96);

        OFFlowAdd request = fab.build();
        request.writeTo(buf);

        buf.setByte(70, 0x40); // OXM Value : 63(0x3f) -> 64(0x40)

        OFMessage response = dcontroller.sendRawPacket(buf);

        buf.clear();

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.120 - Disabled Table Features Request
     * If the switch has disabled the table feature request with non-empty body,
     * verify that the switch rejects this non-empty OFPMP_TABLE_FEATURES
     * request with a permission error
     */
    public void testDisabledTableFeatureRequest(String code) {
        String info = code + " - Disabled Table Features Request";

        log.info(info);

        // Not implemented yet.
    }

    /*
     * 1.1.130 - Handshake without Hello Message
     * Check if the control connection is disconnected if the hello message is
     * not exchanged within the specified default timeout.
     */
    public void testHandshakeWithoutHello(String code) {
        String info = code + " - Handshake without Hello Message";
        log.info(info);

        setUpDummyControllerWithNoHello(TestSwitchCase.HANDSHAKE_NO_HELLO);

        long timeout = this.dcontroller.getTimeOut();

        stopDummyController();
    }

    /*
     * 1.1.140 - Control Message before Hello Message
     * In the main connection between switch and controller, check if the switch
     * processes a control message before exchanging OpenFlow hello message
     * (connection establishment).
     */
    public void testControlMsgBeforeHello(String code) {
        String info = code + " - Control Message before Hello Message";
        log.info(info);

        OFBarrierRequest.Builder brb = defaultFactory.buildBarrierRequest();
        brb.setXid(r_xid);

        setUpDummyControllerWithNoHello(TestSwitchCase.HANDSHAKE_NO_HELLO);

        OFMessage response = dcontroller.sendOFMessage(brb.build());
        long timeout = this.dcontroller.getTimeOut();

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

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
    public void testIncompatibleHelloAfterConnection(String code) {
        String info = code + " - Incompatible Hello after Connection Establishment";
        log.info(info);

        OFHelloFailedErrorMsg.Builder hfb = defaultFactory.errorMsgs().buildHelloFailedErrorMsg();
        hfb.setXid(r_xid);
        hfb.setCode(OFHelloFailedCode.INCOMPATIBLE);

        setUpDummyControllerWithNoHello(TestSwitchCase.HANDSHAKE_INCOMPATIBLE_HELLO);
        OFMessage response = dcontroller.sendOFMessage(hfb.build());

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.160 - Corrupted Cookie Values
     * Verify that the switch throws an error when it receives a corrupted
     * cookie value in OpenFlow messages after establishing connection between
     * switch and controller.
     */
    public void testCorruptedCookieValue(String code) {
        String info = code + " - Corrupted Cookie Values";
        log.info(info);

        setUpDummyController();

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

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setTableId(TableId.of(1));
        fab.setXid(r_xid);
        fab.setMatch(mb.build());
        fab.setBufferId(OFBufferId.NO_BUFFER);
        fab.setInstructions(inst);
        fab.setCookie(U64.of(0xffffffffffffffffl));

        OFMessage response = dcontroller.sendOFMessage(fab.build());

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.170 - Malformed Buffer ID Values
     * Verify that the switch throws an error when it receives a malformed
     * buffer ID value after establishing connection between switch &
     * controller.
     */
    public void testMalformedBufferIDValue(String code) {
        String info = code + " - Malformed Buffer ID Values";
        log.info(info);

        setUpDummyController();

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

        OFFlowAdd.Builder fab = defaultFactory.buildFlowAdd();
        fab.setTableId(TableId.of(1));
        fab.setXid(r_xid);
        fab.setMatch(mb.build());
        fab.setBufferId(OFBufferId.of(1243));
        fab.setInstructions(inst);

        OFMessage response = dcontroller.sendOFMessage(fab.build());

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

    /*
     * 1.1.180 - Slave Controller Violation
     * Verify that the switch rejects unsupported control messages
     * (OFPT_PACKET_OUT, OFPT_FLOW_MOD, OFPT_GROUP_MOD, OFPT_PORT_MOD,
     * OFPT_TABLE_MOD requests, and OFPMP_TABLE_FEATURES) from slave
     * controllers.
     */
    public void testSlaveControllerViolation(String code) {
        String info = code + " - Slave Controller Violation";
        log.info(info);

        setUpDummyController();

        int inport = 1;
        int outport = 2;

        OFRoleRequest.Builder rrb = defaultFactory.buildRoleRequest();
        rrb.setRole(OFControllerRole.ROLE_NOCHANGE);
        rrb.setGenerationId(U64.ZERO);
        rrb.setXid(r_xid);

        OFMessage response = dcontroller.sendOFMessage(rrb.build());

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
            rrb2.setXid(r_xid);

            response = dcontroller.sendOFMessage(rrb2.build());
        }

        OFPacketOut pktout = defaultFactory.buildPacketOut().setXid(r_xid).setBufferId(OFBufferId.NO_BUFFER).build();
        response = dcontroller.sendOFMessage(pktout);

        if (response != null) {
            log.info(response.toString());
        } else
            log.info("response is null");

        stopDummyController();
    }

}
