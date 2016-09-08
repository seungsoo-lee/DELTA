package org.deltaproject.manager.testcase;

import org.deltaproject.manager.core.AppAgentManager;
import org.deltaproject.manager.core.ChannelAgentManager;
import org.deltaproject.manager.core.ControllerManager;
import org.deltaproject.manager.core.HostAgentManager;
import org.deltaproject.webui.TestCase;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static org.deltaproject.webui.TestCase.TestResult.*;

/**
 * Created by seungsoo on 9/3/16.
 */
public class TestControllerCase {
    private static final Logger log = LoggerFactory.getLogger(TestControllerCase.class);

    public static final int VALID_HANDSHAKE = 0;
    public static final int HANDSHAKE_NO_HELLO = 1;
    public static final int HANDSHAKE_INCOMPATIBLE_HELLO = 2;

    private OFFactory defaultFactory;
    private Random random;

    private String ofversion;
    private int ofport;

    private ChannelAgentManager chm;
    private ControllerManager cm;
    private HostAgentManager hm;
    private AppAgentManager am;

    public TestControllerCase(AppAgentManager am, HostAgentManager hm, ChannelAgentManager cm, ControllerManager ctm) {
        this.chm = cm;
        this.cm = ctm;
        this.hm = hm;
        this.am = am;
    }

    public void replayKnownAttack(TestCase test) throws InterruptedException {
        switch (test.getcasenum()) {
            case "2.1.010":
                testMalformedVersionNumber(test);
                break;
            case "2.1.020":
                testCorruptedControlMsgType(test);
                break;
            case "2.1.030":
                testHandShakeWithoutHello(test);
                break;
            case "2.1.040":
                testControlMsgBeforeHello(test);
                break;
            case "2.1.050":
                testMultipleMainConnectionReq(test);
                break;
            case "2.1.060":
                testUnFlaggedFlowRemoveMsgNotification(test);
                break;
            case "2.1.070":
                testTLSupport(test);
                break;
        }
    }

    public void initController() {
        log.info("Target controller: " + cm.getType() + " " + cm.getVersion());
        log.info("Target controller is starting..");
        cm.createController();

        while (!cm.isListeningSwitch()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info("Target controller setup is completed");
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
    public void testMalformedVersionNumber(TestCase test) {
        String info = test.getcasenum() + " - Malformed Version Number";
        log.info(info);

        initController();

        log.info("Channel-agent starts");
        chm.write("startsw");
        isSWconnected();

        chm.write(test.getcasenum());

        String response = chm.read();

        log.info(response);
        cm.killController();
    }

    /*
    * 2.1.020 - Corrupted Control Message Type
    * Verify that the controller throws an error when it receives a control message
    * with unsupported message type.
    */
    public void testCorruptedControlMsgType(TestCase test) {
        String info = test.getcasenum() + " - Corrupted Control Message Type";
        log.info(info);

        initController();

        log.info("Channel-agent starts");
        chm.write("startsw");
        isSWconnected();

        chm.write(test.getcasenum());

        String response = chm.read();

        log.info(response);
        cm.killController();
    }


    /*
     * 2.1.030 - Handshake without Hello Message
     * Check if the control connection is disconnected if the hello message is
     * not exchanged within the specified default timeout.
     */
    public void testHandShakeWithoutHello(TestCase test) {
        String info = test.getcasenum() + " - Handshake without Hello Message";
        log.info(info);

        initController();
        chm.write("startsw|nohello");

        chm.write(test.getcasenum());
        log.info("Channel-agent starts");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.info("Check switch connections");
        int cnt = cm.isConnectedSwitch(false);

        if (cnt == 0) {
            test.setResult(PASS);
            log.info("switch disconnected, PASS");
        } else {
            test.setResult(FAIL);
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
    public void testControlMsgBeforeHello(TestCase test) {
        String info = test.getcasenum() + " - Control Message before Hello Message";
        log.info(info);

        initController();
        chm.write("startsw|nohello");

        chm.write(test.getcasenum());
        log.info("Channel-agent starts");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.info("Check switch connections");
        int cnt = cm.isConnectedSwitch(false);

        if (cnt == 0) {
            test.setResult(PASS);
            log.info("switch disconnected, PASS");
        } else {
            test.setResult(FAIL);
            log.info("switch connected, FAIL");
        }

        cm.killController();
    }

    /*
     * 2.1.050 - Multiple main connection requests from same switch
     * Check if the controller accepts multiple main connections from the same switch.
     */
    public void testMultipleMainConnectionReq(TestCase test) {
        String info = test.getcasenum() + " - Multiple main connection requests from same switch";
        log.info(info);

        initController();
        chm.write("startsw");

        isSWconnected();
        chm.write(test.getcasenum());
        log.info("Channel-agent starts");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.info("Check switch connections");
        int cnt = cm.isConnectedSwitch(false);

        if (cnt == 0) {
            test.setResult(PASS);
            log.info("switch disconnected, PASS");
        } else {
            test.setResult(FAIL);
            log.info("switch connected, FAIL");
        }

        cm.killController();
    }

    /*
     * 2.1.060 - Un-flagged Flow Remove Message Notification
     * Check if the controller accepts multiple main connections from the same switch.
     */
    public void testUnFlaggedFlowRemoveMsgNotification(TestCase test) throws InterruptedException {
        String info = test.getcasenum() + " - Un-flagged Flow Remove Message Notification";
        log.info(info);

        initController();
        chm.write("startsw");
        chm.read();
        isSWconnected();

        am.write(test.getcasenum());
        log.info("App-agent starts");
        log.info(am.read());

        Thread.sleep(2000);
        chm.write(test.getcasenum());
        Thread.sleep(2000);

        String response = chm.read();

        log.info(response);
        cm.killController();
    }

    /*
     * 2.1.070 - Test TLS Support
     * Check if the controller supports Transport Layer Security (TLS).
     */
    public void testTLSupport(TestCase test) {
        String info = test.getcasenum() + " - Test TLS Support";
        log.info(info);

        initController();
        chm.write("startsw");

        isSWconnected();

        chm.write(test.getcasenum());
        log.info("Channel-agent starts");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.info("Check switch connections");
        int cnt = cm.isConnectedSwitch(false);

        if (cnt == 0) {
            log.info("switch disconnected, FAIL");
        } else {
            log.info("switch connected, PASS");
        }

        chm.write(test.getcasenum() + ":exit");
        cm.killController();
    }
}