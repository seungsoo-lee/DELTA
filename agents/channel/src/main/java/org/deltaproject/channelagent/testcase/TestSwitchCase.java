package org.deltaproject.channelagent.testcase;

import org.deltaproject.channelagent.dummy.DummyController;
import org.deltaproject.channelagent.dummy.DummySwitch;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by seungsoo on 11/07/2017.
 */
public class TestSwitchCase {
    private static final Logger log = LoggerFactory.getLogger(TestSwitchCase.class);

    public static final int HANDSHAKE_DEFAULT = 0;
    public static final int HANDSHAKE_NO_HELLO = 1;
    public static final int HANDSHAKE_INCOMPATIBLE_HELLO = 2;
    public static final int NO_HANDSHAKE = 3;
    public static final int DEFAULT_TIMEOUT = 5000;

    private String targetIP;
    private String targetPORT;
    private byte targetOFVersion;

    private DummyController dummyController;

    private OFFactory defaultFactory;
    private Random random;

    private long requestXid = 0xeeeeeeeel;
    private OFMessage response;

    private Process proc;
    private int pid;
    private int ofport;
    private long r_xid = 0xeeeeeeeel;

    public TestSwitchCase(String ip, byte ver, String port) {
        targetIP = ip;
        targetPORT = port;
        targetOFVersion = ver;

        random = new Random();

        if (targetOFVersion == 1)
            defaultFactory = OFFactories.getFactory(OFVersion.OF_10);
        else if (targetOFVersion == 4)
            defaultFactory = OFFactories.getFactory(OFVersion.OF_13);

        ofport = Integer.parseInt(port);
    }

    public void runDummyController(int type) {
        dummyController = new DummyController(targetOFVersion, ofport);
        dummyController.listeningSwitch();
        dummyController.setHandShakeType(type);
        dummyController.start();

        if (type != HANDSHAKE_DEFAULT)
            return;

        while (!dummyController.getHandshaked()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void stopDummyController() {
        dummyController.interrupt();

        while (!dummyController.isSockClosed()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /*
     * 1.1.010 - Port Range Violation
     * Verify that the switch rejects the use of ports that are greater than
     * OFPP_MAX and are not part of the reserved ports.
     */
    public String testPortRangeViolation(String test) throws InterruptedException {
        log.info("[Channel Agent] " + test + " - Port Range Violation test");

        String result = "";
        OFPortMod request = defaultFactory.buildPortMod().setXid(r_xid).setPortNo(OFPort.ANY).build();
        log.info("Send msg: " + request.toString());
        result += request.toString();
        dummyController.sendMsg(request, -1);

        Thread.sleep(1000);

        OFMessage response = dummyController.getResponse();

        if (response != null) {
            result += "\n" + response.toString();
        } else {
            result += "\nnull";
        }

        stopDummyController();

        return result;
    }
}
