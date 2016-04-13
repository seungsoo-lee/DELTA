package com.example.appagent2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Communication extends Thread {
	int result = 1;

	private AppAgent2 app;
	private Activator act;

	private Socket socket;
	private InputStream in;
	private DataInputStream dis;
	private OutputStream out;
	private DataOutputStream dos;

	private String serverIP;
	private int serverPort;

	public void setServerAddr(String ip, int port) {
		this.serverIP = ip;
		this.serverPort = port;
	}

	public void connectServer(String agent) {
		try {
			socket = new Socket(serverIP, serverPort);
			in = socket.getInputStream();
			dis = new DataInputStream(in);
			out = socket.getOutputStream();
			dos = new DataOutputStream(out);

			dos.writeUTF(agent);
			dos.flush();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setAgent(AppAgent2 in) {
		this.app = in;
	}

	public void setActivator(Activator in) {
		this.act = in;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String recv = "";

		try {
			while (true) {
				// reads characters encoded with modified UTF-8
				recv = dis.readUTF();

				if (recv.endsWith("A-5-M-1")) {
					if (app.Flow_Rule_Modification())
						dos.writeUTF("success");
					else
						dos.writeUTF("fail");
				} else if (recv.endsWith("A-5-M-2")) {
					if (app.Flow_Table_Cleareance())
						dos.writeUTF("success");
					else
						dos.writeUTF("fail");
				} else if (recv.endsWith("A-9-M")) {
					app.System_Command_Execution();
					dos.writeUTF("success");
				} else if (recv.endsWith("A-6-M-2")) {
					if(act.Application_Eviction("arp"))
						dos.writeUTF("success");
					else
						dos.writeUTF("fail");
				}
				
				dos.flush();
			}
		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();

		} finally {
			// releases all system resources from the streams
			if (dis != null)
				try {
					dis.close();
					dos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}
