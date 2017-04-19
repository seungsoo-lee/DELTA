package org.deltaproject.channelagent.core;

import jpcap.NetworkInterface;
import org.deltaproject.channelagent.dummy.DummySwitch;
import org.deltaproject.channelagent.pkthandler.NIC;
import org.deltaproject.channelagent.pkthandler.PktListener;
import org.deltaproject.channelagent.testcase.TestCase;
import org.deltaproject.channelagent.testcase.TestControllerCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class AMInterface extends Thread {
    private static final Logger log = LoggerFactory.getLogger(AMInterface.class);
    private int result = 1;

    private Socket socket;

    private InputStream in;
    private DataInputStream dis;
    private OutputStream out;
    private DataOutputStream dos;

    private Process processCbench;

    // for Agent-manager
    private String amIP;
    private int amPort;

    private PktListener pktListener;

    private NetworkInterface device;
    private byte OFVersion;
    private String ofPort;
    private String handler;
    private String cbench;
    private String controllerIP;
    private String switchIP;

    private DummySwitch dummysw;

    private TestControllerCase testController;

    public AMInterface(String ip, String port) {
        amIP = ip;
        amPort = Integer.parseInt(port);

        dummysw = new DummySwitch();
    }

    public AMInterface(String config) {
        BufferedReader br = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        File file = new File(config);
        String temp = "";

        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);

            while ((temp = br.readLine()) != null) {
                if (temp.contains("AM_IP")) {
                    this.amIP = temp.substring(temp.indexOf("=") + 1);
                } else if (temp.contains("AM_PORT")) {
                    this.amPort = Integer.valueOf(temp.substring(temp.indexOf("=") + 1));
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean executeCbench() {
        try {
            cbench += "cbench";
            String[] cmdCbench = {cbench, "-c", this.controllerIP, "-p", ofPort, "-m", "1000", "-l", "20",
                    "-s", "32", "-M", "2000", "-t", "-o", "1000"};
            ProcessBuilder pb = new ProcessBuilder(cmdCbench);
            pb.redirectErrorStream(true);
            processCbench = pb.start();

            BufferedReader stderr = new BufferedReader(new InputStreamReader(processCbench.getInputStream()));

            String line;
            while ((line = stderr.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    public void setConfiguration(String str) {
        String[] list = new String(str).split(",");
        String nic = "";

        for (String s : list) {
            if (s.startsWith("version")) {
                String OFVersion = s.substring(s.indexOf(":") + 1);
                if (OFVersion.equals("1.0"))
                    this.OFVersion = 1;
                else if (OFVersion.equals("1.3"))
                    this.OFVersion = 4;
            } else if (s.startsWith("nic")) {
                nic = s.substring(s.indexOf(":") + 1);
                this.device = NIC.getInterfaceByName(nic);
            } else if (s.startsWith("controller_ip")) {
                controllerIP = s.substring(s.indexOf(":") + 1);
            } else if (s.startsWith("switch_ip")) {
                String temp = s.substring(s.indexOf(":") + 1);

                if (temp.contains(",")) {
                    switchIP = temp.substring(0, s.indexOf(","));
                } else {
                    switchIP = temp;
                }
            } else if (s.startsWith("port")) {
                this.ofPort = s.substring(s.indexOf(":") + 1);
            } else if (s.startsWith("handler")) {
                this.handler = s.substring(s.indexOf(":") + 1);
            } else if (s.startsWith("cbench")) {
                this.cbench = s.substring(s.indexOf(":") + 1);
            }
        }

        log.info("Configuration setup");
        log.info("OpenFlow version/port\t: " + OFVersion + "/" + ofPort);
        log.info("MITM Network Interface\t: " + nic);
        log.info("Target Controller IP\t: " + controllerIP);
        log.info("Target Switch IP\t\t: " + switchIP);
        log.info("Cbench Root Path\t\t: " + cbench);

        pktListener = new PktListener(device, controllerIP, switchIP, OFVersion, this.ofPort, this.handler);
        testController = new TestControllerCase(controllerIP, OFVersion, ofPort);
    }

    public void connectAgentManager() {
        try {
            socket = new Socket(amIP, amPort);
            // socket.setReuseAddress(true);

            in = socket.getInputStream();
            dis = new DataInputStream(in);

            out = socket.getOutputStream();
            dos = new DataOutputStream(out);

            dos.writeUTF("ChannelAgent");
            dos.flush();

            String ack = dis.readUTF();
            if (ack.contains("OK")) {
                log.info("Connected with Agent-Manager");
            } else {
                log.info("Connection failed");
                System.exit(1);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testFunc() {

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        String recv = "";

        try {
            while (true) {
                // reads characters encoded with modified UTF-8
                recv = dis.readUTF();

                if (recv.startsWith("config")) {
                    this.setConfiguration(recv);
                    continue;
                } else if (recv.equalsIgnoreCase("3.1.010")) {
                    System.out.println("\n[Channel-Agent] Pacekt-In Flooding test starts");
                    this.executeCbench();
                    dos.writeUTF("success");
                } else if (recv.equalsIgnoreCase("3.1.160")) {
                    System.out.println("\n[Channel-Agent] LinkFabrication test starts");
                    pktListener.setTypeOfAttacks(TestCase.LINKFABRICATION);
                    pktListener.startListening();
                    pktListener.startARPSpoofing();

                    Thread.sleep(40000);

                    dos.writeUTF("success");
                } else if (recv.equalsIgnoreCase("3.1.170")) {
                    System.out.println("\n[Channel-Agent] Evaesdrop test starts");
                    pktListener.setTypeOfAttacks(TestCase.EVAESDROP);
                    pktListener.startListening();
                    pktListener.startARPSpoofing();
                } else if (recv.equalsIgnoreCase("3.1.170-2")) {
                    String result = pktListener.getTopoInfo();

                    if (result != null && !result.isEmpty() && result.length() > 0)
                        result = "success|\n:: Result for Building Topology ::\n" + result;
                    else
                        result = "fail";

                    System.out.println("\n[Channel-Agent] Topology Information " + result);
                    dos.writeUTF(result);
                } else if (recv.equalsIgnoreCase("3.1.180")) {
                    System.out.println("\n[Channel-Agent] MITM test starts");
                    pktListener.setTypeOfAttacks(TestCase.MITM);
                    pktListener.startListening();
                    pktListener.startARPSpoofing();
                    dos.writeUTF("success");
                } else if (recv.equalsIgnoreCase("3.1.050")) { // Switch Table
                    // Flooding

                    Thread.sleep(15000);

                    dos.writeUTF("success");
                } else if (recv.equalsIgnoreCase("3.1.060")) {
                    System.out.println("\n[Channel-Agent] Switch Identification Spoofing Test");
                    pktListener.testSwitchIdentification();

                    dos.writeUTF("success");
                } else if (recv.startsWith("fuzzing")) {
                    dummysw.connectTargetController(controllerIP, ofPort);
                    dummysw.setOFFactory(this.OFVersion);
                    // dummysw.setSeed(pktListener.getSeedPackets());
                    dummysw.start();
                } else if (recv.equalsIgnoreCase("exit")) {
                    pktListener.setTypeOfAttacks(TestCase.EMPTY);
                    pktListener.stopARPSpoofing();
                } else if (recv.contains("startsw")) {
                    if (recv.contains("nohello")) {
                        testController.startSW(DummySwitch.HANDSHAKE_NO_HELLO);
                    } else {
                        testController.startSW(DummySwitch.HANDSHAKE_DEFAULT);
                    }
                    dos.writeUTF("switchok");
                } else if (recv.equalsIgnoreCase("stoptemp")) {
                    testController.stopTempSW();
                } else if (recv.equalsIgnoreCase("2.1.010")) {
                    String res = testController.testMalformedVersionNumber(recv);
                    dos.writeUTF(res);
                } else if (recv.equalsIgnoreCase("2.1.020")) {
                    String res = testController.testCorruptedControlMsgType(recv);
                    dos.writeUTF(res);
                } else if (recv.equalsIgnoreCase("2.1.040")) {
                    String res = testController.testControlMsgBeforeHello(recv);
                    dos.writeUTF(res);
                } else if (recv.equalsIgnoreCase("2.1.050")) {
                    String res = testController.testMultipleMainConnectionReq(recv);
                    dos.writeUTF(res);
                } else if (recv.equalsIgnoreCase("2.1.060")) {
                    String res = testController.testUnFlaggedFlowRemoveMsgNotification(recv);
                    dos.writeUTF(res);
                } else if (recv.contains("2.1.070")) {
                    if (recv.contains("exit")) {
                        testController.exitTopo();
                        continue;
                    } else {
                        String res = testController.testTLSSupport(recv);
                        dos.writeUTF(res);
                    }
                } else if (recv.contains("0.0.011")) {
                    if (recv.contains("0.0.011")) {
                        log.info("Control plane fuzzing test starts");
                        pktListener.setTypeOfAttacks(TestCase.CONTROLPLANE_FUZZING);
                    } else {
                        log.info("Data plane fuzzing test starts");
                        pktListener.setTypeOfAttacks(TestCase.DATAPLANE_FUZZING);
                    }

                    pktListener.startListening();
                    pktListener.startARPSpoofing();

                    dos.writeUTF("success");
                } else if (recv.contains("0.0.010")) {
                    log.info("Seed-based fuzzing test starts");
                    pktListener.setTypeOfAttacks(TestCase.SEED_BASED_FUZZING);
                    pktListener.setFuzzingMode(1);

                    pktListener.startListening();
                    pktListener.startARPSpoofing();

                    dos.writeUTF("success");
                } else if (recv.contains("seedstop")) {
                    // after stopping get seeds, start fuzzing
                    pktListener.setFuzzingMode(0);
                    pktListener.setTypeOfAttacks(TestCase.CONTROLPLANE_FUZZING);

                    /*
                    dummysw.connectTargetController(controllerIP, ofPort);
                    dummysw.setOFFactory(this.OFVersion);
                    dummysw.setSeed(pktListener.getSeedPackets());
                    dummysw.start();
                    */
                } else if (recv.contains("getmsg")) {
                    dos.writeUTF(pktListener.getFuzzingMsg());
                    //dos.writeUTF("MSG");
                } else if (recv.contains("close")) {
                    System.out.println("[Channel-Agent] Closing...");
                    dis.close();
                    dos.close();
                    System.exit(0);
                }

                dos.flush();
            }
        } catch (Exception e) {
            // if any error occurs
            e.printStackTrace();

            if (dis != null)
                try {
                    dis.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }

            if (dos != null)
                try {
                    dos.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }

            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            // System.exit(0);
        }

        log.info("Thread exit");
    }
}
