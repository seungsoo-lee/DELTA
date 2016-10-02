package org.deltaproject.manager.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;

public class HostAgentManager extends Thread {
    private static final Logger log = LoggerFactory.getLogger(HostAgentManager.class);
    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private Process proc;
    private int procPID;

    private Configuration cfg = Configuration.getInstance();

    public HostAgentManager() {

    }

    void setSocket(Socket in, DataOutputStream w, DataInputStream r) {
        this.socket = in;
        this.dos = w;
        this.dis = r;
    }

    public Socket getSocket() {
        if (socket != null) {
            return this.socket;
        } else
            return null;
    }

    public void write(String input) {
        try {
            dos.writeUTF(input);
            dos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String read() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "false";
    }

    public boolean runAgent() {
        String amAddr = cfg.getAMIP() + " " + cfg.getAMPort();
        String controllerAddr = cfg.getControllerIP() + " " + cfg.getOFPort();
        String version;

        if (cfg.getOFVer().equals("1.0")) {
            version = "OpenFlow10";
        } else {
            version = "OpenFlow13";
        }

        try {
            proc = Runtime.getRuntime().exec("ssh " + cfg.getHostSSH() + " sudo python test-advanced-topo.py " + controllerAddr + " " + amAddr + " " + version);
            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(proc);
            this.procPID = (Integer) value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public void stopAgent() {
        try {
            if (dos != null)
                dos.close();

            if (dis != null)
                dis.close();

            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (procPID != -1)
            try {
                Runtime.getRuntime().exec("ssh " + cfg.getHostSSH() + " sudo arp -d " + cfg.getControllerIP());
                proc = Runtime.getRuntime().exec("sudo kill -9 " + this.procPID);
                proc.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        else
            procPID = -1;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }
}
