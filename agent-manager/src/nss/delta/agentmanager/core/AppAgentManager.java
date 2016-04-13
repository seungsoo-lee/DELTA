package nss.delta.agentmanager.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import nss.delta.agentmanager.targetcon.ControllerManager;

public class AppAgentManager {

	/* AppAgent */
	private Socket appSocket;
	private DataOutputStream dos;
	private DataInputStream dis;

	/* Activator for OpenDaylight */
	private Socket actSocket;
	private DataOutputStream dos2;
	private DataInputStream dis2;

	private int controllerType;

	public AppAgentManager() {

	}

	public void setControllerType(int in) {
		this.controllerType = in;
	}

	public void closeSocket() {
		if (actSocket != null)
			try {
				actSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		if (appSocket != null)
			try {
				appSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void setActSocket(Socket in, DataOutputStream w, DataInputStream r) {
		this.actSocket = in;
		this.dos2 = w;
		this.dis2 = r;
	}

	public void setAppSocket(Socket in, DataOutputStream w, DataInputStream r) {
		this.appSocket = in;
		this.dos = w;
		this.dis = r;
	}

	public boolean attempt(String code) {
		return true;
	}

	public void setFuzzingInputs() {

	}

	public boolean write(String code) {
		switch (this.controllerType) {
		case ControllerManager.OPENDAYLIGHT:
			if (code.contains("A-2-M") || code.contains("A-6-M")) {
				try {
					dos2.writeUTF(code);
					dos2.flush();

					if (code.contains("A-2-M")) {
						dis2.readUTF();
						dos.writeUTF(code);
					}
					return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			} else {
				try {
					dos.writeUTF(code);
					dos.flush();
					return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					return false;
				}
			}

		case ControllerManager.ONOS:
			try {
				dos.writeUTF(code);
				dos.flush();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				return false;
			}

		case ControllerManager.FLOODLIGHT:
			try {
				dos.writeUTF(code);
				dos.flush();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				return false;
			}
		}

		return true;
	}

	public void startFuzzing() {

	}

	public void setTargetType(int target) {
		write(Integer.toString(target));
	}

	public String read() {
		String result = "";
		try {
			result = dis.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public String read2() {
		String result = "";
		try {
			result = dis2.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
