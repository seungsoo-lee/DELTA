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
            } else if (version.equals("1.6")) {
                process = Runtime.getRuntime().exec("ssh ubuntu@10.0.4.17 /home/ubuntu/onos-1.6.0/bin/onos-service start");
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

            Process temp = Runtime.getRuntime().exec("ssh ubuntu@10.0.4.17 sudo ps -ef | grep karaf");
            String tempS;

            BufferedReader stdOut2 = new BufferedReader(new InputStreamReader(temp.getInputStream()));

            while ((tempS = stdOut2.readLine()) != null && !tempS.isEmpty()) {
                if (tempS.contains("apache-karaf")) {
                    String[] list = StringUtils.split(tempS);

                    currentPID = Integer.parseInt(list[1]);
                }
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

            if (stdOut != null) {
                stdOut.close();
            }

            if (this.currentPID != -1) {
                Process pc = null;
                try {
                    pc = Runtime.getRuntime().exec("ssh ubuntu@10.0.4.17 sudo kill -9 " + this.currentPID);
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
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
