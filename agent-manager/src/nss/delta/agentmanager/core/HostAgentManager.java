package nss.delta.agentmanager.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HostAgentManager extends Thread {
	private Socket socket;

	private DataInputStream dis;
	private DataOutputStream dos;

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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {

		}
	}
}
