package org.deltaproject.appagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class Communication extends Thread {
    protected static Logger log = LoggerFactory.getLogger(Communication.class);

    int result = 1;

    private AppAgent app;

    private Socket socket;
    private InputStream in;
    private DataInputStream dis;
    private OutputStream out;
    private DataOutputStream dos;

    private String serverIP = "10.0.2.2";   // by default
    private int serverPort = 3366;

    public Communication(AppAgent in) {
        this.app = in;
    }

    public void setServerAddr() {
        String path = "~";

        String home = System.getenv("HOME");
        if (home != null) {
            path = home;
        }

        BufferedReader br = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        File file = new File(path + "/agent.cfg");
        String temp;

        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);

            while ((temp = br.readLine()) != null) {
                if (temp.contains("MANAGER_IP"))
                    this.serverIP = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("MANAGER_PORT"))
                    this.serverPort = Integer.parseInt(temp.substring(temp.indexOf("=") + 1));
            }

        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            try {
                fis.close();
                isr.close();
                br.close();
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
    }

    public void connectServer(String agent) throws Exception {
        socket = new Socket(serverIP, serverPort);
        in = socket.getInputStream();
        dis = new DataInputStream(in);
        out = socket.getOutputStream();
        dos = new DataOutputStream(out);

        dos.writeUTF(agent);
        dos.flush();
    }

    public void write(String in) {
        try {
            dos.writeUTF(in);
            dos.flush();
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    public void replayingKnownAttack(String recv) throws IOException {
        String result = "";

        if (recv.equals("3.1.210")) {
            app.testSwappingList();
            return;
        }

        dos.flush();
    }

    @Override
    public void run() {
        String recv;

        while (true) {
            try {
                this.setServerAddr();
                this.connectServer("AppAgent");
                while (true) {
                    recv = dis.readUTF();
                    log.info("[App-Agent] Received " + recv);
                    replayingKnownAttack(recv);
                }
            } catch (ConnectException e) {
                log.error("[App-Agent] Agent Manager is not listening");
            } catch (Exception e) {
                log.error(e.toString());
            } finally {
                try {
                    if (dis != null) {
                        dis.close();
                    }

                    if (dos != null) {
                        dos.close();
                    }
                } catch (IOException e) {
                    log.error(e.toString());
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error(e.toString());
            }
        }
    }
}

