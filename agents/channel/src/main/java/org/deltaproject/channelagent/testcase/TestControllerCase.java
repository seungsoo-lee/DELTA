package org.deltaproject.channelagent.testcase;

import com.google.common.primitives.Longs;
import org.deltaproject.channelagent.core.Utils;
import org.deltaproject.channelagent.dummy.DMDataOF10;
import org.deltaproject.channelagent.dummy.DMDataOF13;
import org.deltaproject.channelagent.dummy.DMOFSwitch;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Created by seungsoo on 9/3/16.
 */
public class TestControllerCase {
    private static final Logger log = LoggerFactory.getLogger(TestControllerCase.class);

    private DMOFSwitch ofSwitch;
    private String targetIP;
    private String targetPORT;

    private long requestXid = 0xeeeeeeeel;
    private OFMessage response;
    private byte targetOFVersion;

    private Process proc;
    private int pid;

    public TestControllerCase(String ip, byte ver, String port) {
        targetIP = ip;
        targetPORT = port;
        targetOFVersion = ver;
    }

    public boolean isHandshaked() {
        return ofSwitch.getHandshaked();
    }

    public boolean startSW(int type) {
        log.info("Start dummy switch");
        ofSwitch = new DMOFSwitch();
        ofSwitch.setTestHandShakeType(type);
        ofSwitch.setOFFactory(targetOFVersion);
        ofSwitch.connectTargetController(targetIP, targetPORT);
        try {
            ofSwitch.sendHello(0);
        } catch (OFParseError ofParseError) {
            ofParseError.printStackTrace();
        }
        ofSwitch.start();

        if (type == DMOFSwitch.HANDSHAKE_DEFAULT) {
            while (!isHandshaked()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("OF Handshake completed");
        return true;
    }

    public String testMalformedVersionNumber(String code) {
        while (!isHandshaked()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String result;

        byte[] msg;
        if (targetOFVersion == 4) {
            msg = Utils.hexStringToByteArray(DMDataOF13.PACKET_IN);
            msg[0] = (byte) 0x01;
            result = "Send Packet-In msg with OF version 1.0\n";
        } else {
            msg = Utils.hexStringToByteArray(DMDataOF10.PACKET_IN);
            msg[0] = (byte) 0x04;
            result = "Send Packet-In msg with OF version 1.3\n";
        }

        byte[] xidbytes = Longs.toByteArray(requestXid);
        System.arraycopy(xidbytes, 4, msg, 4, 4);

        ofSwitch.sendRawMsg(msg);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // switch disconnection
        OFMessage response = ofSwitch.getResponse();
        if (response != null) {
            result += "Response msg : " + response.toString() + ", PASS";
        } else
            result += "response is null, FAIL";

        return result;
    }

    public String testCorruptedControlMsgType(String code) {
        while (!isHandshaked()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        byte[] msg = DMDataOF10.hexStringToByteArray(DMDataOF10.PACKET_IN);
        byte[] xidbytes = Longs.toByteArray(requestXid);
        System.arraycopy(xidbytes, 4, msg, 4, 4);

        msg[1] = (byte) 0xff;               // malformed type

        ofSwitch.sendRawMsg(msg);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // switch disconnection
        OFMessage response = ofSwitch.getResponse();
        if (response != null) {
            return response.toString() + ", PASS";
        } else
            return ("response is null, FAIL");
    }

    public String testControlMsgBeforeHello(String code) {
        byte[] msg = DMDataOF10.hexStringToByteArray(DMDataOF10.PACKET_IN);
        byte[] xidbytes = Longs.toByteArray(requestXid);
        System.arraycopy(xidbytes, 4, msg, 4, 4);

        ofSwitch.sendRawMsg(msg);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // switch disconnection
        OFMessage response = ofSwitch.getResponse();
        if (response != null) {
            return response.toString() + ", PASS";
        } else
            return ("response is null, FAIL");
    }

    public String testMultipleMainConnectionReq(String code) {
        try {
            ofSwitch.sendHello(0);
        } catch (OFParseError ofParseError) {
            ofParseError.printStackTrace();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // switch disconnection
        OFMessage response = ofSwitch.getResponse();
        if (response != null) {
            return response.toString() + ", PASS";
        } else
            return ("response is null, FAIL");
    }

    public String testUnFlaggedFlowRemoveMsgNotification(String code) throws InterruptedException {
        String info = code + " - Un-flagged Flow Remove Message Notification";
        log.info(info);

        while (!isHandshaked()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info("building msg");

        OFFlowAdd fa = ofSwitch.getBackupFlowAdd();
        if (fa == null)
            return "nothing";

        OFFlowRemoved.Builder fm = ofSwitch.getFactory().buildFlowRemoved();
        fm.setMatch(fa.getMatch());
        fm.setXid(this.requestXid);
        fm.setReason((short) 1);

        OFFlowRemoved msg = fm.build();

        log.info("before sending msg");

        ofSwitch.sendMsg(msg, -1);

        // switch disconnection
        OFMessage response = ofSwitch.getResponse();
        if (response != null) {
            return response.toString() + ", FAIL";
        } else
            return ("response is null, PASS");
    }

    public String testTLSSupport(String code) {
        log.info("Test TLS Support");
        try {
            proc = Runtime.getRuntime().exec("python ./test-controller-topo.py " + targetIP + " " + targetPORT);
            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(proc);
            this.pid = (Integer) value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "success";
    }

    public void exitTopo() {
        log.info("Exit test topology");
        // proc.destroy();
    }
}
