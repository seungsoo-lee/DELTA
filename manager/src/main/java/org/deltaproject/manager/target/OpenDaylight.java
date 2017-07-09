package org.deltaproject.manager.target;

import org.apache.commons.lang3.StringUtils;
import org.deltaproject.manager.utils.AgentLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;

public class OpenDaylight implements TargetController {

    public static final String HYDROGEN = "";
    private static final Logger log = LoggerFactory.getLogger(OpenDaylight.class.getName());

    private Process process = null;
    private boolean isRunning = false;

    public String version = "";
    public String controllerPath = "";
    public String appPath = "";
    public String sshAddr = "";

    private int currentPID = -1;
    private int bundleID;

    private BufferedWriter stdIn;
    private BufferedReader stdOut;


    public OpenDaylight(String path, String v, String ssh) {
        this.version = v;
        this.sshAddr = ssh;

        String user = sshAddr.substring(0, sshAddr.indexOf('@'));
        controllerPath = "/home/" + user + "/odl-helium-sr3/opendaylight/distribution/opendaylight/target/distribution.opendaylight-osgipackage/opendaylight/run.sh -Xmx2g";
    }

    public OpenDaylight setAppAgentPath(String path) {
        this.appPath = path;

        return this;
    }

    public boolean createController() {
        isRunning = false;
        String str;

        try {
            if (version.equals("helium")) {
                process = Runtime.getRuntime().exec("ssh " + sshAddr + " sudo " + controllerPath);
            } else if (version.equals("carbon")) {
                process = Runtime.getRuntime().exec("ssh " + sshAddr + " /home/vagrant/distribution-karaf-0.2.4-Helium-SR4/bin/karaf");
            }

            /*
            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(process);
            this.currentPID = (Integer) value;
            */

            stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            while ((str = stdOut.readLine()) != null) {
                log.debug(str);
                if (str.contains("initialized successfully")) {
                    log.info("OpenDaylight is activated");
                    isRunning = true;
                    break;
                }
            }

            if (!isRunning) {
                log.info("Failed to start OpenDaylight");
                return false;
            }

            /*
            Process temp = Runtime.getRuntime().exec("ssh " + sshAddr + " sudo ps -ef | grep java");
            String tempS;
            BufferedReader stdOut2 = new BufferedReader(new InputStreamReader(temp.getInputStream()));

            while ((tempS = stdOut2.readLine()) != null && !tempS.isEmpty()) {
                if (tempS.contains("opendaylight")) {
                    String[] list = StringUtils.split(tempS);
                    currentPID = Integer.parseInt(list[1]);
                }
            }
            */

            installAppAgent();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean installAppAgent() {
        boolean isInstalled = false;

        String str = "";
        String user = sshAddr.substring(0, sshAddr.indexOf('@'));

        try {
            stdIn.write("install file:" + "/home/" + user + "/delta-agent-app-odl-helium-sr3-1.0-SNAPSHOT.jar" + "\n");
            stdIn.flush();

            while (!isInstalled) {
                str = stdOut.readLine();
                if (str.contains("Installed")) {
                    isInstalled = true;

                    int idx = str.indexOf("Installed");
                    this.bundleID = Integer.parseInt(str.substring(idx - 4,
                            idx - 1));

                    stdIn.write("start " + bundleID + "\n");
                    stdIn.flush();

                    log.info("AppAgent bundle ID [" + bundleID + "] Installed");
                }
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // for service chain interference
            stdIn.write("install file:" + "/home/" + user + "/delta-agent-app-odl-helium-sr3-sub-1.0-SNAPSHOT.jar" + "\n");
            stdIn.flush();

            isInstalled = false;
            while (!isInstalled) {
                str = stdOut.readLine();
                if (str.contains("Installed")) {
                    isInstalled = true;

                    int idx = str.indexOf("Installed");
                    this.bundleID = Integer.parseInt(str.substring(idx - 4,
                            idx - 1));

                    stdIn.write("start " + bundleID + "\n");
                    stdIn.flush();
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    public void killController() {
        try {
            if (stdIn != null) {
                stdIn.write("exit\n");
                stdIn.flush();
                stdIn.write("y\n");
                stdIn.flush();
                stdIn.close();
            }

//            if (this.currentPID != -1) {
//                Process pc = null;
//                try {
//                    pc = Runtime.getRuntime().exec("ssh " + sshAddr + " sudo kill -9 " + this.currentPID);
//                    pc.getErrorStream().close();
//                    pc.getInputStream().close();
//                    pc.getOutputStream().close();
//                    pc.waitFor();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Process getProc() {
        return this.process;
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return "OpenDaylight";
    }

    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return this.version;
    }

    @Override
    public String getPath() {
        // TODO Auto-generated method stub
        return this.controllerPath + "\n" + this.appPath;
    }

    @Override
    public int getPID() {
        // TODO Auto-generated method stub
        return this.currentPID;
    }

    @Override
    public BufferedReader getStdOut() {
        // TODO Auto-generated method stub
        return this.stdOut;
    }

}