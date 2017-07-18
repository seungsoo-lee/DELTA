package org.deltaproject.manager.handler;

import org.apache.commons.lang3.StringUtils;
import org.deltaproject.manager.utils.AgentLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;

public class FloodlightHandler implements ControllerHandler {
    private Process proc = null;
    private boolean isRunning = false;

    public String version = "";
    public String controllerPath = "";
    public String sshAddr = "";

    private int currentPID = -1;

    private BufferedWriter stdIn;
    private String stdOut;

    private Thread loggerThd;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public FloodlightHandler(String controllerPath, String version, String ssh) {
        this.controllerPath = controllerPath;
        this.version = version;
        this.sshAddr = ssh;
    }

    public boolean createController() {
        isRunning = false;

        String str;
        String[] cmdArray = null;

        try {
            if (version.equals("1.2")) {
                cmdArray = new String[]{"ssh", sshAddr, "sudo", "java", "-jar", "floodlight-1.2.jar", "-cf", "./floodlightdefault.properties"};
            } else if (version.equals("0.91")) {
                cmdArray = new String[]{"ssh", sshAddr, "sudo", "java", "-jar", "floodlight-0.91.jar"};
            } else {
                log.error("Unavailable Floodlight version.. Exit..");
                return false;
            }

            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            pb.redirectErrorStream(true);
            proc = pb.start();
            loggerThd = new Thread(AgentLogger.getLoggerThreadInstance(proc, AgentLogger.APP_AGENT));
            loggerThd.start();

            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(proc);

            this.currentPID = (Integer) value;

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            stdIn = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));

            do {
                stdOut = AgentLogger.readLogFile(AgentLogger.APP_AGENT);
            }
            while (!stdOut.contains("Starting DebugServer on :6655"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public void killController() {
        Process pc = null;
        try {
            pc = Runtime.getRuntime().exec("ssh " + sshAddr + " sudo kill -9 " + this.currentPID);
            pc.getErrorStream().close();
            pc.getInputStream().close();
            pc.getOutputStream().close();
            pc.waitFor();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.currentPID = -1;
    }

    public Process getProc() {
        return this.proc;
    }


    /*
     * In the case of Floodlight, App-Agent is automatically installed when the
     * controller starts
     */
    public boolean installAppAgent() {

        return true;
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return "Floodlight";
    }

    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return this.version;
    }

    @Override
    public String getPath() {
        // TODO Auto-generated method stub
        return this.controllerPath;
    }

    @Override
    public int getPID() {
        // TODO Auto-generated method stub
        return this.currentPID;
    }

    @Override
    public BufferedReader getStdOut() {
        return null;
    }
}
