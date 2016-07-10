package org.deltaproject.hostagent;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class AMInterface extends Thread {
	int result = 1;

	private Socket socket;
	private InputStream in;
	private DataInputStream dis;
	private String amIP;
	private int amPort;

	private OutputStream out;
	private DataOutputStream dos;

	private PktHandler ha;

	public AMInterface(String ip, String port) {
		this.amIP = ip;
		this.amPort = Integer.valueOf(port);
		
		ha = new PktHandler();
	}

	public void setServerAddr(String ip, int port) {
		this.amIP = ip;
		this.amPort = port;
	}

	public void readConfigFile(String config) {
		BufferedReader br = null;
		InputStreamReader isr = null;
		FileInputStream fis = null;
		File file = new File(config);
		String temp = "";

		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis, "UTF-8");
			br = new BufferedReader(isr);

			while ((temp = br.readLine()) != null) {
				if (temp.contains("AM_IP")) {
					this.amIP = temp.substring(temp.indexOf("=") + 1);
				} else if (temp.contains("AM_PORT")) {
					this.amPort = Integer.valueOf(temp.substring(temp.indexOf("=") + 1));
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				isr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void connectAgentManager() {
		try {
			socket = new Socket(amIP, amPort);
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

	public void setAgent(PktHandler in) {
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
				} else if (recv.contains("connected")) {
					System.out.println("[Host-Agent] Connected with Agent-Manager");
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