package org.deltaproject.manager.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChannelAgentManager extends Thread {
    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private int targetType;

    public ChannelAgentManager() {

    }

    void setSocket(Socket in, DataOutputStream w, DataInputStream r) {
        this.socket = in;
        this.dos = w;
        this.dis = r;
    }

    public Socket getAppSocket() {
        if (socket != null) {
            return this.socket;
        } else
            return null;
    }

    public String read() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("FASLE");
        }
        return "false";
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

    public void setTargetType(int in) {
        write(Integer.toString(in));
    }

    public void startFuzzing() {
        write("fuzzing|" + Integer.toString(this.targetType));
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
    }
}
