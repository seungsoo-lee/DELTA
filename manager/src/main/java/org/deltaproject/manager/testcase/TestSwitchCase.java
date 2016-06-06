package org.deltaproject.manager.testcase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

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
	private OFFactory defaultFactory;
	private Random random;

	private long r_xid = 0xeeeeeeeel;

	private String ofversion;

	public TestSwitchCase() {
		random = new Random();
		ofversion = "1.3";
		defaultFactory = OFFactories.getFactory(OFVersion.OF_13);
	}

	public void replayKnownAttack(String code) {
//		if (code.equals("1.1.10")) {
//			testPacketInFlooding(code);
//		} else if (code.equals("1.1.11")) {
//			testControlMessageDrop(code);
//		} else if (code.equals("1.1.20")) {
//			testInfiniteLoop(code);
//		} else if (code.equals("1.1.30")) {
//			testInternalStorageAbuse(code);
//		} else if (code.equals("1.1.40")) {
//			testSwitchTableFlooding(code);
//		} else if (code.equals("1.1.50")) {
//			testSwitchIdentificationSpoofing(code);
//		} else if (code.equals("1.1.60")) {
//			testMalformedControlMessage(code);
//		} else if (code.equals("1.1.70")) {
//			testFlowRuleModification(code);
//		} else if (code.equals("1.1.80")) {
//			testFlowTableClearance(code);
//		} else if (code.equals("1.1.90")) {
//			testEventUnsubscription(code);
//		} else if (code.equals("1.1.100")) {
//			testApplicationEviction(code);
//		} else if (code.equals("1.1.110")) {
//			testMemoryExhaustion(code);
//		} else if (code.equals("1.1.120")) {
//			testCPUExhaustion(code);
//		} else if (code.equals("1.1.130")) {
//			testSystemVariableManipulation(code);
//		} else if (code.equals("1.1.140")) {
//			testSystemCommandExecution(code);
//		} else if (code.equals("1.1.150")) {
//			testLinkFabrication(code);
//		} else if (code.equals("1.1.160")) {
//			testEvaseDrop(code);
//		} else if (code.equals("1.1.170")) {
//			testManInTheMiddle(code);
//		} else if (code.equals("1.2.10")) {
//			testFlowRuleFlooding(code);
//		} else if (code.equals("1.2.20")) {
//			testSwitchFirmwareMisuse(code);
//		} else if (code.equals("1.2.30")) {
//			testControlMessageManipulation(code);
//		} 
	}
	public long genXid() {
		long l = random.nextLong();
		if (l == 0)
			return 1;

		return l;
	}

	public void setUpDummyControllerWithNoHello(int type) {
		dcontroller = new DummyController();
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
		dcontroller = new DummyController();
		dcontroller.bootstrapNetty();

		while (!dcontroller.isOFHandlerActive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// echo req-res
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopDummyController() {
		dcontroller.stopNetty();
	}

	/*
	 * Verify that the switch rejects the use of ports that are greater than
	 * OFPP_MAX and are not part of the reserved ports.
	 */
	public void testPortRange() {
		String info = "1.1.10 - Port Range Violation";

		setUpDummyController();

		log.info(info);

		OFPortMod request = defaultFactory.buildPortMod().setXid(r_xid).setPortNo(OFPort.ANY).build();
		OFMessage response = dcontroller.sendOFMessage(request);

		if (response != null) {
			log.info(response.toString());
		} else
			log.info("response is null");

		stopDummyController();
	}

	/*
	 * only 1.3 Verify that the switch rejects the use of invalid table id.
	 */
	public void testTableID() {
		String info = "1.1.20 - Table Identifier Violation";

		setUpDummyController();

		log.info(info);

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
	 * Verify that the switch rejects the use of groups that are greater than
	 * OFPG_MAX and are not part of the reserved groups.
	 */
	public void testGroupID() {
		String info = "1.1.30 - Group Identifier Violation";
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
	 * Verify that the switch rejects the use of meters that are greater than
	 * OFPM_MAX and are not part of the virtual meters.
	 */
	public void testMeterID() {
		String info = "1.1.40 - Meter Identifier Violation";
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
	 * Verify that the switch rejects the use of invalid Goto table id
	 * requesting a table loop.
	 */
	public void testTableLoop() {
		String info = "1.1.50 - Table Loop Violation";
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
	 * Verify that the switch throws an error when it receives a control message
	 * with unsupported message type.
	 */
	public void testUnsupportedMessageType() {
		String info = "1.1.60 - Unsupported Message Type";
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
	 * Verify that the switch throws an error when it receives a connection
	 * setup message with an unsupported version number.
	 */
	public void testUnsupportedVersionNumber() {
		String info = "1.1.70 - Unsupported Version Numebr";
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
	 * Verify that the switch throws an error when it receives a malformed
	 * version number after establishing connection between switch and
	 * controller with a different version.
	 */
	public void testMalformedVersionNumber() {
		String info = "1.1.80 - Malformed Version Number";
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
	 * Verify that the switch throws an error when it receives a flow mod
	 * message with invalid OXM type.
	 */
	public void testInvalidOXMType() {
		String info = "1.1.90 - Invalid OXM - Type";

		setUpDummyController();

		log.info(info);

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
	 * Verify that the switch throws an error when it receives a flow mod
	 * message with invalid OXM length.
	 */
	public void testInvalidOXMLength() {
		String info = "1.1.100 - Invalid OXM - Length";

		setUpDummyController();

		log.info(info);

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
	 * Verify that the switch throws an error when it receives a flow mod
	 * message with invalid message value
	 */
	public void testInvalidOXMValue() {
		String info = "1.1.110 - Invalid OXM - Value";

		setUpDummyController();

		log.info(info);

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
	 * If the switch has disabled the table feature request with non-empty body,
	 * verify that the switch rejects this non-empty OFPMP_TABLE_FEATURES
	 * request with a permission error
	 */
	public void testDisabledTableFeatureRequest() {
		String info = "1.1.120 - Disabled Table Features Request";

		log.info(info);

		// Not implemented yet.
	}

	/*
	 * Check if the control connection is disconnected if the hello message is
	 * not exchanged within the specified default timeout.
	 */
	public void testHandshakeWithoutHello() {
		String info = "1.1.130 - Handshake without Hello Message";
		log.info(info);

		setUpDummyControllerWithNoHello(TestSwitchCase.HANDSHAKE_NO_HELLO);

		long timeout = this.dcontroller.getTimeOut();

		stopDummyController();
	}

	/*
	 * In the main connection between switch and controller, check if the switch
	 * processes a control message before exchanging OpenFlow hello message
	 * (connection establishment).
	 */
	public void testControlMsgBeforeHello() {
		String info = "1.1.140 - Control Message before Hello Message";
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
	 * Verify that the switch will properly handle the abnormal condition, when
	 * it receives an OFPT_ERROR message with a type field of
	 * OFPET_HELLO_FAILED, a code field of OFPHFC_INCOMPATIBLE after
	 * establishing connection between switch and controller with a both agreed
	 * version.
	 */
	public void testIncompatibleHelloAfterConnection() {
		String info = "1.1.150 - Incompatible Hello after Connection Establishment";
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
	 * Verify that the switch throws an error when it receives a corrupted
	 * cookie value in OpenFlow messages after establishing connection between
	 * switch and controller.
	 */
	public void testCorruptedCookieValue() {
		String info = " 1.1.160 - Corrupted Cookie Values";

		setUpDummyController();

		log.info(info);

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
	 * Verify that the switch throws an error when it receives a malformed
	 * buffer ID value after establishing connection between switch &
	 * controller.
	 */
	public void testMalformedBufferIDValue() {
		String info = "1.1.170 - Malformed Buffer ID Values";

		setUpDummyController();

		log.info(info);

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
	 * Verify that the switch rejects unsupported control messages
	 * (OFPT_PACKET_OUT, OFPT_FLOW_MOD, OFPT_GROUP_MOD, OFPT_PORT_MOD,
	 * OFPT_TABLE_MOD requests, and OFPMP_TABLE_FEATURES) from slave
	 * controllers.
	 */
	public void testSlaveControllerViolation() {
		String info = " 1.2.10 - Slave Controller Violation";

		setUpDummyController();

		log.info(info);

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
