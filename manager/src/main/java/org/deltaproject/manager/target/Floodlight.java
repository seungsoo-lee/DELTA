package org.deltaproject.manager.target;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;

public class Floodlight implements TargetController {
    private Process process = null;
    private boolean isRunning = false;

    public String version = "";
    public String controllerPath = "";

    private int currentPID = -1;

    private BufferedWriter stdIn;
    private BufferedReader stdOut;

    public Floodlight(String controllerPath, String version) {
        this.controllerPath = controllerPath;
        this.version = version;
    }

    public int createController() {
        isRunning = false;

        String str;
        String name;

        try {
            if(version.equals("1.2")) {
                process = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 java -jar floodlight-1.2.jar -cf ./floodlightdefault.properties");
                name = "floodlight-1.2.jar";
            } else {
                process = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 java -jar floodlight-0.91.jar");
                name = "floodlight-0.91.jar";
            }

            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(process);

            this.currentPID = (Integer) value;

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));


            while ((str = stdOut.readLine()) != null) {
                // System.out.println(str);
                if (str.contains("Starting DebugServer on :6655")) {
                    isRunning = true;
                    break;
                }
            }

            Process temp = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 sudo ps -ef | grep java");
            String tempS;

            BufferedReader stdOut2 = new BufferedReader(new InputStreamReader(temp.getInputStream()));

            while ((tempS = stdOut2.readLine()) != null && !tempS.isEmpty()) {
                if (tempS.contains(name)) {
                    String[] list = StringUtils.split(tempS);

                    currentPID = Integer.parseInt(list[1]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return currentPID;
    }

    public void killController() {
        Process pc = null;
        try {
            pc = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 sudo kill -9 " + this.currentPID);
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
        return this.process;
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
        // TODO Auto-generated method stub
        return this.stdOut;
    }
}
