package org.deltaproject.channelagent.testcase;

import com.google.common.primitives.Longs;
import org.deltaproject.channelagent.dummy.DummyOFData;
import org.deltaproject.channelagent.dummy.DummyOFSwitch;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by seungsoo on 9/3/16.
 */
public class TestControllerCase {
    private static final Logger log = LoggerFactory.getLogger(TestControllerCase.class);

    private DummyOFSwitch ofSwitch;
    private String targetIP;
    private String targetPORT;

    private long requestXid = 0xeeeeeeaal;
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
        ofSwitch = new DummyOFSwitch();
        ofSwitch.setTestHandShakeType(type);
        ofSwitch.setOFFactory(targetOFVersion);
        ofSwitch.connectTargetController(targetIP, targetPORT);
        ofSwitch.start();

        if(type == DummyOFSwitch.HANDSHAKE_DEFAULT) {
            while(!isHandshaked()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    public String testMalformedVersionNumber(String code) {
        while(!isHandshaked()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        byte[] msg = DummyOFData.hexStringToByteArray(DummyOFData.packetin);
        byte[] xidbytes = Longs.toByteArray(requestXid);
        System.arraycopy(xidbytes, 4, msg, 4, 4);

        msg[0] = (byte) 0x05;               // malformed version (0x05)

        ofSwitch.sendRawMsg(msg);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // switch disconnection
        OFMessage response = ofSwitch.getResponse();
        if (response != null) {
            return response.toString() +", PASS";
        } else
            return ("response is null, FAIL");
    }

    public String testCorruptedControlMsgType(String code) {
        while(!isHandshaked()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        byte[] msg = DummyOFData.hexStringToByteArray(DummyOFData.packetin);
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
            return response.toString() +", PASS";
        } else
            return ("response is null, FAIL");
    }

    public String testControlMsgBeforeHello(String code) {
        byte[] msg = DummyOFData.hexStringToByteArray(DummyOFData.packetin);
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
            return response.toString() +", PASS";
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
            return response.toString() +", PASS";
        } else
            return ("response is null, FAIL");
    }

    public String testUnFlaggedFlowRemoveMsgNotification(String code) throws InterruptedException {
        String info = code + " - Un-flagged Flow Remove Message Notification";
        log.info(info);

        while(!isHandshaked()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info("building msg");

        OFFlowAdd fa = ofSwitch.getBackupFlowAdd();
        if(fa == null)
            return "nothing";

        OFFlowRemoved.Builder fm = ofSwitch.getFactory().buildFlowRemoved();
        fm.setMatch(fa.getMatch());
        fm.setXid(this.requestXid);
        fm.setReason((short)1);

        OFFlowRemoved msg = fm.build();

        log.info("before sending msg");

        ofSwitch.sendMsg(msg, -1);

        // switch disconnection
        OFMessage response = ofSwitch.getResponse();
        if (response != null) {
            return response.toString() +", FAIL";
        } else
            return ("response is null, PASS");
    }

    public String testTLSSupport(String code) {
        log.info("Test TLS Support");
        try {
            proc = Runtime.getRuntime().exec("python ./test-controller-topo.py "+targetIP+" "+targetPORT);
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
