package org.deltaproject.manager.target;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;

public class ONOS implements TargetController {
    private Process process = null;
    private boolean isRunning = false;

    public String version = "";
    public String controllerPath = "";
    public String karafPath = "";

    private int currentPID = -1;

    private BufferedWriter stdIn;
    private BufferedReader stdOut;

    public ONOS(String controllerPath, String v) {
        this.controllerPath = controllerPath;
        this.version = v;
    }

    public ONOS setKarafPath(String p) {
        this.karafPath = p;

        return this;
    }

    public int createController() {
        isRunning = false;

        String str;

        try {
            if (version.equals("1.1")) {
                process = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 /home/vagrant/Applications/apache-karaf-3.0.5/bin/karaf clean");
            }

            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(process);

            this.currentPID = (Integer) value;

            stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            while ((str = stdOut.readLine()) != null) {
                // System.out.println(str);
                if (str.contains("ONOS.")) {
                    isRunning = true;
                    break;
                }
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return currentPID;
    }

    public Process getProc() {
        return this.process;
    }

    public void killController() {
        try {
            if (stdIn != null) {
                stdIn.write("system:shutdown -f\n");
                stdIn.flush();
                stdIn.close();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.currentPID = -1;
    }

    /* ONOS, AppAgent is automatically installed when the controller starts */
    public boolean installAppAgent() {

        return true;
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return "ONOS";
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
        // TODO Auto-generated method stub
        return this.stdOut;
    }
}
