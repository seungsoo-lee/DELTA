package org.deltaproject.manager.core;

import org.deltaproject.manager.target.Floodlight;
import org.deltaproject.manager.target.ONOS;
import org.deltaproject.manager.target.OpenDaylight;
import org.deltaproject.manager.target.TargetController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ControllerManager {
    private String cbechPath = "";
    private String targetController = "";
    private String targetVersion = "";
    private String ofPort = "";
    private String sshAddr = "";

    private int cbenchPID = -1;

    private ArrayList<TargetController> targetList;
    private ArrayList<String> switchList;
    private ArrayList<String> connectedSwitches;

    private Process processCbench;

    private Configuration cfg = Configuration.getInstance();

    public ControllerManager() {
        targetList = new ArrayList<TargetController>();
        switchList = new ArrayList<String>();

        this.setConfig();
        connectedSwitches = new ArrayList<String>();
        sshAddr = cfg.getAppSSH();
    }

    public void setConfig() {
        TargetController fl = new Floodlight(cfg.getFloodlightRoot(), cfg.getTargetVer(), cfg.getAppSSH());
        TargetController odl = new OpenDaylight(cfg.getODLRoot(), cfg.getTargetVer(), cfg.getAppSSH());
        TargetController onos = new ONOS(cfg.getONOSRoot(), cfg.getTargetVer(), cfg.getAppSSH());

        targetList.add(fl);
        targetList.add(odl);
        targetList.add(onos);

        cbechPath = cfg.getCbenchRoot();
        targetController = cfg.getTargetController();
        targetVersion = "v" + cfg.getTargetVer();
        ofPort = cfg.getOFPort();
        switchList = cfg.getSwitchList();
    }

    public BufferedReader getStdOut() {
        for (TargetController tc : targetList) {
            if (tc.getType().equals(this.targetController)) {
                return tc.getStdOut();
            }
        }

        return null;
    }

    public boolean createController() {
        for (TargetController tc : targetList) {
            if (tc.getType().equals(this.targetController)) {
                tc.createController();

                return true;
            }
        }
        return false;
    }

    public boolean killController() {
        for (TargetController tc : targetList) {
            if (tc.getType().equals(this.targetController)) {
                tc.killController();
                return true;
            }
        }
        return false;
    }

    public String getType() {
        return targetController;
    }

    public String getVersion() {
        return targetVersion;
    }

    public String showConfig() {
        String result = "";

        result += "Target Controller: ";

        for (TargetController tc : targetList) {
            if (tc.getType().equals(this.targetController)) {
                result += tc.getType() + " - " + tc.getVersion();
            }
        }

        result += "\nTarget Path: ";

        for (TargetController tc : targetList) {
            if (tc.getType().equals(this.targetController)) {
                result += tc.getPath();
            }
        }

        result += "\nSwitchs: ";

        for (String s : this.switchList) {
            result += s + ", ";
        }

        result += "\b\b  ";

        result += "\nCbench Path: " + this.cbechPath;

        return result;
    }

    public void addSwitchIP(String ip) {
        switchList.add(ip);
    }

    public boolean isRunning() {
        int controllerPID = -1;

        for (TargetController tc : targetList) {
            if (tc.getType().equals(this.targetController)) {
                controllerPID = tc.getPID();
            }
        }

        Process temp = null;
        String str = "";
        try {
            temp = Runtime.getRuntime().exec(new String[]{"bash", "-c", "ps -a | grep " + controllerPID});

            BufferedReader stdOut = new BufferedReader(new InputStreamReader(temp.getInputStream()));

            if ((str = stdOut.readLine()) == null) {
                controllerPID = -1;
                temp.destroy();
                return false;
            }

            stdOut.close();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public boolean executeCbench() {
        try {
            processCbench = Runtime.getRuntime().exec(cbechPath + "cbench -c " + cfg.getControllerIP() + "  -p "
                    + ofPort + " -m 10000 -l 10 -s 16 -M 1000 -t");

            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(processCbench);

            this.cbenchPID = (Integer) value;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    public void flushARPcache() {
        try {
            Process pc = Runtime.getRuntime().exec("ip -s -s neigh flush all");
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

    public boolean isListeningSwitch() {
        Process temp;
        String tempS;

        boolean flag = false;

        try {
            temp = Runtime.getRuntime().exec("ssh " + sshAddr + " sudo netstat -ap | grep " + ofPort);

            BufferedReader stdOut = new BufferedReader(new InputStreamReader(temp.getInputStream()));

            while ((tempS = stdOut.readLine()) != null) {
                if (tempS.contains("LISTEN")) {
                    return true;
                }
            }
            stdOut.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    public int isConnectedSwitch(boolean wait) {
        Process temp;
        String tempS;
        int switchCnt = this.switchList.size();
        int cnt = 0;

        int flag = 1;

        while (flag > 0) {
            try {
                cnt = 0;
                String cmd = "";
                // temp = Runtime.getRuntime().exec(new String[] { "bash", "-c", "netstat -ap | grep " + ofPort});
                temp = Runtime.getRuntime().exec("ssh " + sshAddr + " sudo netstat -ap | grep " + ofPort);

                BufferedReader stdOut = new BufferedReader(new InputStreamReader(temp.getInputStream()));

                while ((tempS = stdOut.readLine()) != null) {
                    if (tempS.contains("ESTABLISHED")) {
                        connectedSwitches.add(tempS);
                        cnt++;
                    }
                }

                stdOut.close();

                if (switchCnt == cnt) {
                    break;
                }

                Thread.sleep(1000);

                if (!wait) {
                    flag = 0;
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return cnt;
    }

    public int getSwitchCounter() {
        return this.switchList.size();
    }

    public void killCbench() {
        Process pc = null;
        try {
            if (processCbench != null) {
                processCbench.getErrorStream().close();
                processCbench.getInputStream().close();
                processCbench.getOutputStream().close();
                processCbench.destroy();
            }

            pc = Runtime.getRuntime().exec("kill -9 " + this.cbenchPID);
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
}
