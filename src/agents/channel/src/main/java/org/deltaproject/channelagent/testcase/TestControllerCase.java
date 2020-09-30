package org.deltaproject.channelagent.testcase;

import com.google.common.primitives.Longs;
import org.deltaproject.channelagent.core.Configuration;
import org.deltaproject.channelagent.utils.Utils;
import org.deltaproject.channelagent.dummy.DummyOF10;
import org.deltaproject.channelagent.dummy.DummyOF13;
import org.deltaproject.channelagent.dummy.DummySwitch;
import org.deltaproject.manager.utils.AgentLogger;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by seungsoo on 9/3/16.
 */
public class TestControllerCase {
    private static final Logger log = LoggerFactory.getLogger(TestControllerCase.class);

    private DummySwitch ofSwitch;
    private DummySwitch temp;
    private String targetIP;
    private String targetPORT;
    private String targetSwitchIP;

    private long requestXid = 0xeeeeeeeel;
    private OFMessage response;
    private byte targetOFVersion;

    private Process proc;
    private int pid;

    public TestControllerCase() {
        targetIP = Configuration.getInstance().getControllerIp();
        targetPORT = Configuration.getInstance().getOfPort();
        targetOFVersion = Configuration.getInstance().getOfVersion();
        targetSwitchIP = Configuration.getInstance().getSwitchIp();
    }

    public boolean isHandshaked() {
        return ofSwitch.getHandshaked();
    }

    public boolean startSW(int type) {
        log.info("[Channel Agent] Start Dummy Switch");

        ofSwitch = new DummySwitch();
        ofSwitch.setTestHandShakeType(type);
        ofSwitch.setOFFactory(targetOFVersion);
        ofSwitch.connectTargetController(targetIP, targetPORT);
        ofSwitch.sendHello(0);
        ofSwitch.start();

        if (type == DummySwitch.HANDSHAKE_DEFAULT) {
            while (!isHandshaked()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("[Channel Agent] Handshake completed");
        }

        return true;
    }

    public void stopSW() {
        if (ofSwitch != null) {
            ofSwitch.interrupt();
            ofSwitch = null;
        }

        log.info("[Channel Agent] Stop Dummny Switch");
    }

    public void stopTempSW() {
        if (temp != null)
            temp.interrupt();

        log.info("[Channel Agent] Stop Sub Switch");
    }

    // 2.1.010
    public String testMalformedVersionNumber(String code) {
        log.info("[Channel Agent] " + code + " - Malformed Version Number test");

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
            msg = Utils.hexStringToByteArray(DummyOF13.PORT_STATUS);
            msg[0] = (byte) 0x01;

            result = "Send Packet-In msg with OF version 1.0\n";
			log.info("* SendPKT | PKT_IN : Hub --> Core = OF1.0");
			log.info("* Test | Send Packet-In msg with OF version 1.0");
        } else {
            msg = Utils.hexStringToByteArray(DummyOF10.PACKET_IN);
            msg[0] = (byte) 0x04;
            result = "Send Packet-In msg with OF version 1.3\n";
			log.info("* SendPKT | PKT_IN : Hub --> Core = OF1.3");
			log.info("* Test | Send Packet-In msg with OF version 1.3");
        }

        byte[] xidbytes = Longs.toByteArray(requestXid);
        System.arraycopy(xidbytes, 4, msg, 4, 4);

        ofSwitch.sendRawMsg(msg);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


        OFMessage response = ofSwitch.getResponse();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        if (response != null) {
			log.info("* RecvPKT | OF : Core --> Hub = " + response.toString());
			log.info("* Test | Response:" + response.toString());
            if (response.getType() == OFType.PACKET_OUT)
                result += "Response msg : " + response.toString() + ", FAIL";
            else
                result += "Response msg : " + response.toString() + ", PASS";
        } else{
            result += "Response is NULL (expected msg is ERR), FAIL";
			log.info("* Result | No response");
			log.info("* Test | Response: Null");
		}

//        stopSW();
        return result;
    }

    // 2.1.020
    public String testCorruptedControlMsgType(String code) {
        log.info("[Channel Agent] " + code + " - Corrupted Control Message Type test");

        while (!isHandshaked()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String result = "Send a packet-in message with unknown message type";
        log.info("[Channel Agent] " + result);
        result = result + '\n';

        byte[] msg;
        if (targetOFVersion == 4) {
            msg = Utils.hexStringToByteArray(DummyOF13.PACKET_IN);
        } else {
            msg = Utils.hexStringToByteArray(DummyOF10.PACKET_IN);
        }

        msg[1] = (byte) 0xcc;   // malformed msg type
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
            result += "Response msg : " + response.toString() + ", PASS";
        } else
            result += "Response is NULL (expected msg is ERR), FAIL";

        stopSW();
        return result;
    }

    // 2.1.030
    public String testHandShakeWithoutHello(String code) {
        log.info("[Channel Agent] " + code + " - Handshake without Hello Message test");

        startSW(DummySwitch.HANDSHAKE_NO_HELLO);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (this.ofSwitch.getHandshaked())
            return "switchConnected";
        else
            return "switchNotConnected";
    }

    // 2.1.040
    public String testControlMsgBeforeHello(String code) throws Exception {
        log.info("[Channel Agent] " + code + " - Control Message before Hello Message (Main Connection) test");

        String result = "Send a packet-in message before handshake";
        log.info("[Channel Agent] " + result);
        result = result + '\n';

        startSW(DummySwitch.NO_HANDSHAKE);

        byte[] msg;
        if (targetOFVersion == 4) {
            msg = Utils.hexStringToByteArray(DummyOF13.PACKET_IN);
        } else {
            msg = Utils.hexStringToByteArray(DummyOF10.PACKET_IN);
        }

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
            result += "Response msg : " + response.toString() + ", FAIL";
        } else
            result += "Response is ignored, PASS";

        return result;
    }

    //2.1.050
    public String testMultipleMainConnectionReq(String code) {
        log.info("[Channel Agent] " + code + " - Multiple Main Connection Request test");

        while (!isHandshaked()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info("Start another dummy switch");
        temp = new DummySwitch();
        temp.setTestHandShakeType(DummySwitch.HANDSHAKE_DEFAULT);
        temp.setOFFactory(targetOFVersion);
        temp.connectTargetController(targetIP, targetPORT);
        temp.sendHello(0);
        temp.start();

        return "Start another dummy switch";
    }

    //2.1.060
    public String testUnFlaggedFlowRemoveMsgNotification(String code) throws InterruptedException {
        log.info("[Channel Agent] " + code + " - no-flagged Flow Remove Message notification test");

        String result = "Send an un-flagged flow remove msg";
        log.info("[Channel Agent] " + result);
        result = result + '\n';

        while (!isHandshaked()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        OFFlowAdd fa = ofSwitch.getBackupFlowAdd();
        if (fa == null)
            return "nothing";

        OFFlowRemoved.Builder fm = ofSwitch.getFactory().buildFlowRemoved();
        fm.setMatch(fa.getMatch());
        fm.setXid(fa.getXid());
        fm.setReason(OFFlowRemovedReason.HARD_TIMEOUT);
        fm.setCookie(fa.getCookie());
        fm.setTableId(fa.getTableId());
        fm.setPriority(fa.getPriority());

        OFFlowRemoved msg = fm.build();
        ofSwitch.sendMsg(msg, -1);

        OFMessage response = ofSwitch.getResponse();
        if (response != null) {
            result += response.toString() + ", SUCCESS";
        } else
            result += ("response is null, FAIL");

        return result;
    }

    //2.1.070
    public String testTLSSupport(String code) {
        log.info("[Channel Agent] " + code + " - Test TLS Support");
        try {
            proc = Runtime.getRuntime().exec("python $HOME/test-controller-topo.py " + targetIP + " " + targetPORT);
            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(proc);
            this.pid = (Integer) value;

            log.info("TLS " + String.valueOf(pid));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "success";
    }

    public void exitTopo() {
        log.info("Exit test topology - ");
        try {
            Runtime.getRuntime().exec("sudo kill -9 " + this.pid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //3.1.050
    public void testSwitchTableFlooding() {

        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            DummySwitch dummySwitch = new DummySwitch(DatapathId.of(i));
            dummySwitch.setTestHandShakeType(DummySwitch.HANDSHAKE_DEFAULT);
            dummySwitch.setOFFactory(targetOFVersion);
            dummySwitch.connectTargetController(targetIP, targetPORT);
            dummySwitch.sendHello(requestXid);
            dummySwitch.start();

            while (!dummySwitch.getHandshaked()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error(e.toString());
                }
            }
            dummySwitch.interrupt();
        }
    }

    //3.1.220, temporary for demo
    public void dropContorlPacketsTemporary() {
        try {
            log.info("[Channel-Agent] Start arp spoofing and session reset attack..");
            String cmdArray[] = {"sudo", "python", "arp_spoofing_drop.py", targetIP, targetSwitchIP, "eth1"};
            Process proc;
            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            pb.redirectErrorStream(true);
            proc = pb.start();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            ArpSpoofingThread ast = new ArpSpoofingThread(proc);
            executor.execute(ast);
            Thread.sleep(3000);

            log.info("[Channel-Agent] Stop arp spoofing and session reset attack..");
            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(proc);
            log.info("pid: {}", value);
            Runtime.getRuntime().exec("sudo kill -SIGINT " + value);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class ArpSpoofingThread implements Runnable {
        BufferedWriter stdinBr;
        BufferedReader stderrBr;
        Process proc;

        public ArpSpoofingThread(Process proc) {
            this.proc = proc;
        }

        @Override
        public void run() {
            stdinBr = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
            stderrBr = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            try {
                while ((line = stderrBr.readLine()) != null) {
                    log.info(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
