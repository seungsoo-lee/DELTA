package org.deltaproject.appagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.lang.InterruptedException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Communication extends Thread {
	protected static Logger log = LoggerFactory.getLogger(Communication.class);
	int result = 1;

	private AppAgent app;

	private Socket socket;
	private InputStream in;
	private DataInputStream dis;
	private OutputStream out;
	private DataOutputStream dos;

	private String serverIP;
	private int serverPort;
	
//	private DataFuzzing fuzzing;
	
	public Communication(AppAgent in) {
		this.app = in;
	}
	
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
	
	public void write(String in) {
		try {
			dos.writeUTF(in);
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		// TODO Auto-generated method stub
		String recv = "";

		while (true) {
			try {
				this.setServerAddr("10.0.3.1", 3366);
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
