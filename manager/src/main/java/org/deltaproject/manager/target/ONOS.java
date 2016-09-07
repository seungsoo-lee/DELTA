package org.deltaproject.manager.target;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;

public class ONOS implements TargetController {
    private Process process = null;
    private boolean isRunning = false;

    public String version = "";
    public String karafPath = "";
    public String onosPath = "";
    public String sshAddr = "";

    private int currentPID = -1;

    private BufferedWriter stdIn;
    private BufferedReader stdOut;

    public ONOS(String path, String v, String ssh) {
        this.karafPath = path + "/apache-karaf-3.0.5/bin/karaf";
        onosPath = path + "/bin/onos-service";
        this.version = v;
        this.sshAddr = ssh;
    }

    public int createController() {
        isRunning = false;

        String str;

        try {
            if (this.version.contains("1.1")) {
                process = Runtime.getRuntime().exec("ssh " + sshAddr + " " + karafPath + " clean");
            } else {
                process = Runtime.getRuntime().exec("ssh " + sshAddr + " " + onosPath + " start");
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

            Process temp = Runtime.getRuntime().exec("ssh " + sshAddr + " sudo ps -ef | grep karaf");
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
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public Process getProc() {
        return this.process;
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
        return this.karafPath;
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
