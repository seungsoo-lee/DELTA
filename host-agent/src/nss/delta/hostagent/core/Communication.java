package nss.delta.hostagent.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.activation.Activator;

import nss.delta.hostagent.core.HostAgent;

public class Communication extends Thread {
	int result = 1;

	private Socket socket;
	private InputStream in;
	private DataInputStream dis;
	private String serverIP;
	private int serverPort;

	private OutputStream out;
	private DataOutputStream dos;

	private HostAgent ha;

	public void setServerAddr(String ip, int port) {
		this.serverIP = ip;
		this.serverPort = port;
	}

	public void connectServer() {
		try {
			socket = new Socket(serverIP, serverPort);
			in = socket.getInputStream();
			dis = new DataInputStream(in);

			out = socket.getOutputStream();
			dos = new DataOutputStream(out);

			dos.writeUTF("HostAgent");
			dos.flush();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setAgent(HostAgent in) {
		ha = in;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String recv = "";

		try {
			while (true) {
				// reads characters encoded with modified UTF-8
				recv = dis.readUTF();
				
				if (recv.contains("ping")) {
					dos.writeUTF(ha.executePing("10.0.0.2"));
					dos.flush();
				} else if (recv.contains("compare")) {
					dos.writeUTF(ha.comparePing("10.0.0.2"));
					dos.flush();
				}
			}
		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();

		} finally {
			// releases all system resources from the streams
			if (dis != null)
				try {
					dis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}