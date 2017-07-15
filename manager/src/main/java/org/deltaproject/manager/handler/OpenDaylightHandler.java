package org.deltaproject.manager.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class OpenDaylightHandler implements ControllerHandler {

    public static final String HYDROGEN = "";
    private static final Logger log = LoggerFactory.getLogger(OpenDaylightHandler.class.getName());

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


    public OpenDaylightHandler(String path, String v, String ssh) {
        this.version = v;
        this.sshAddr = ssh;

        String user = sshAddr.substring(0, sshAddr.indexOf('@'));
        controllerPath = "/home/" + user + "/odl-helium-sr3/opendaylight/distribution/opendaylight/handler/distribution.opendaylight-osgipackage/opendaylight/run.sh -Xmx2g";
    }

    public OpenDaylightHandler setAppAgentPath(String path) {
        this.appPath = path;

        return this;
    }

    public boolean createController() {
        isRunning = false;
        String str;

        try {
            if (version.equals("helium")) {
                process = Runtime.getRuntime().exec("ssh " + sshAddr + " sudo " + controllerPath);

                stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                stdIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

                while ((str = stdOut.readLine()) != null) {
                    log.debug(str);
                    if (str.contains("initialized successfully")) {
                        log.info("OpenDaylightHandler is activated");
                        isRunning = true;
                        break;
                    }
                }

                if (!isRunning) {
                    log.info("Failed to start OpenDaylightHandler");
                    return false;
                }

                installAppAgent();

            } else if (version.equals("carbon")) {
                process = Runtime.getRuntime().exec("ssh " + sshAddr + " /home/vagrant/distribution-karaf-0.6.0-Carbon/bin/karaf");

                stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                stdIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

                while ((str = stdOut.readLine()) != null) {
                    log.debug(str);
                    if (str.contains("shutdown OpenDaylightHandler")) {
                        log.info("OpenDaylightHandler is activated");
                        isRunning = true;
                        break;
                    }
                }
            }

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
                if (version.equals("helium")) {
                    stdIn.write("exit\n");
                    stdIn.flush();
                    stdIn.write("y\n");
                    stdIn.flush();
                    stdIn.close();
                } else {
                    stdIn.write("system:shutdown\n");
                    stdIn.flush();
                    stdIn.write("yes\n");
                    stdIn.flush();
                    stdIn.close();
                }
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
        return "OpenDaylightHandler";
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