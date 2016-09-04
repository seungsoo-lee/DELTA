package org.deltaproject.manager.testcase;

import org.deltaproject.manager.core.ChannelAgentManager;
import org.deltaproject.manager.core.Configuration;
import org.deltaproject.manager.core.ControllerManager;
import org.deltaproject.manager.core.HostAgentManager;
import org.deltaproject.manager.dummy.DummyController;
import org.projectfloodlight.openflow.protocol.OFBarrierRequest;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by seungsoo on 9/3/16.
 */
public class TestControllerCase {
    private static final Logger log = LoggerFactory.getLogger(TestControllerCase.class);

    public static final int VALID_HANDSHAKE = 0;
    public static final int HANDSHAKE_NO_HELLO = 1;
    public static final int HANDSHAKE_INCOMPATIBLE_HELLO = 2;

    private DummyController dcontroller;
    private OFFactory defaultFactory;
    private Random random;

    private String ofversion;
    private int ofport;
    private Configuration cfg;

    private ChannelAgentManager chm;
    private ControllerManager cm;
    private HostAgentManager hm;

    public TestControllerCase(Configuration config, ChannelAgentManager cm, ControllerManager ctrm, HostAgentManager hm) {
        this.cfg = config;
        this.chm = cm;
        this.cm = ctrm;
        this.hm = hm;
    }

    public void replayKnownAttack(String code) {
        if (code.equals("2.1.010")) {
            testMalformedVersionNumber(code);
        } else if (code.equals("2.1.020")) {
            testCorruptedControlMsgType(code);
        } else if (code.equals("2.1.030")) {
            testHandShakeWithoutHello(code);
        } else if (code.equals("2.1.040")) {
            testControlMsgBeforeHello(code);
        } else if (code.equals("2.1.050")) {
            testMultipleMainConnectionReq(code);
        } else if (code.equals("2.1.060")) {
            testUnFlaggedFlowRemoveMsgNotification(code);
        } else if (code.equals("2.1.070")) {
            testTLSupport(code);
        }
    }

    public void initController() {
        log.info("Target controller: " + cm.getType() + " " + cm.getVersion());
        log.info("Target controller is starting..");
        cm.createController();
        log.info("Target controller setup is completed");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void isSWconnected() {
        /* waiting for switches */
        log.info("Listening to switches..");
        cm.isConnectedSwitch(true);
        log.info("All switches are connected");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
    * 2.1.010 - Malformed Version Number
    * Verify that the controller throws an error when it receives a malformed
    * version number after establishing connection between switch and
    * controller with a different version.
    */
    public void testMalformedVersionNumber(String code) {
        String info = code + " - Malformed Version Number";
        log.info(info);

        initController();

        log.info("Channel-agent starts");
        chm.write("startsw");
        isSWconnected();

        chm.write(code);

        String response = chm.read();

        log.info(response);
        cm.killController();
    }

    /*
    * 2.1.020 - Corrupted Control Message Type
    * Verify that the controller throws an error when it receives a control message
    * with unsupported message type.
    */
    public void testCorruptedControlMsgType(String code) {
        String info = code + " - Corrupted Control Message Type";
        log.info(info);

        initController();

        log.info("Channel-agent starts");
        chm.write("startsw");
        isSWconnected();

        chm.write(code);

        String response = chm.read();

        log.info(response);
        cm.killController();
    }


    /*
     * 2.1.030 - Handshake without Hello Message
     * Check if the control connection is disconnected if the hello message is
     * not exchanged within the specified default timeout.
     */
    public void testHandShakeWithoutHello(String code) {
        String info = code + " - Handshake without Hello Message";
        log.info(info);

        initController();
        chm.write("startsw|nohello");

        chm.write(code);
        log.info("Channel-agent starts");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.info("Check switch connections");
        int cnt = cm.isConnectedSwitch(false);

        if(cnt == 0) {
            log.info("switch disconnected, PASS");
        } else {
            log.info("switch connected, FAIL");
        }

        cm.killController();
    }

    /*
     * 2.1.040 - Control Message before Hello Message
     * In the main connection between switch and controller, check if the controller
     * processes a control message before exchanging OpenFlow hello message
     * (connection establishment).
     */
    public void testControlMsgBeforeHello(String code) {
        String info = code + " - Control Message before Hello Message";
        log.info(info);

        initController();
        chm.write("startsw|nohello");

        chm.write(code);
        log.info("Channel-agent starts");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.info("Check switch connections");
        int cnt = cm.isConnectedSwitch(false);

        if(cnt == 0) {
            log.info("switch disconnected, PASS");
        } else {
            log.info("switch connected, FAIL");
        }

        cm.killController();
    }

    /*
     * 2.1.050 - Multiple main connection requests from same switch
     * Check if the controller accepts multiple main connections from the same switch.
     */
    public void testMultipleMainConnectionReq(String code) {
        String info = code + " - Multiple main connection requests from same switch";
        log.info(info);

        initController();
        chm.write("startsw");

        isSWconnected();
        chm.write(code);
        log.info("Channel-agent starts");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.info("Check switch connections");
        int cnt = cm.isConnectedSwitch(false);

        if(cnt == 0) {
            log.info("switch disconnected, PASS");
        } else {
            log.info("switch connected, FAIL");
        }

        cm.killController();
    }

    /*
     * 2.1.060 - Un-flagged Flow Remove Message Notification
     * Check if the controller accepts multiple main connections from the same switch.
     */
    public void testUnFlaggedFlowRemoveMsgNotification(String code) {
        String info = code + " - Multiple main connection requests from same switch";
        log.info(info);

        initController();
        chm.write("startsw");

        isSWconnected();
        chm.write(code);
        log.info("Channel-agent starts");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String response = chm.read();

        log.info(response);
        cm.killController();
    }

    /*
     * 2.1.070 - Test TLS Support
     * Check if the controller supports Transport Layer Security (TLS).
     */
    public void testTLSupport(String code) {
        String info = code + " - Test TLS Support";
        log.info(info);

        initController();

        chm.write(code);
        log.info("Channel-agent starts");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.info("Check switch connections");
        int cnt = cm.isConnectedSwitch(false);

        if(cnt == 0) {
            log.info("switch disconnected, FAIL");
        } else {
            log.info("switch connected, PASS");
        }

        chm.write(code+":exit");
        cm.killController();
    }
}