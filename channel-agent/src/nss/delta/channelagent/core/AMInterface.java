package nss.delta.channelagent.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import jpcap.NetworkInterface;
import nss.delta.channelagent.pkthandler.NIC;
import nss.delta.channelagent.pkthandler.PktHandler;
import nss.delta.channelagent.testcase.LinkFabricator;
import nss.delta.channelagent.testcase.SwitchIdentificationSpoofer;
import nss.delta.channelagent.testcase.SwitchTableFlooder;

public class AMInterface extends Thread {
	private int result = 1;

	private Socket socket;

	private InputStream in;
	private DataInputStream dis;
	private OutputStream out;
	private DataOutputStream dos;

	// for Agent-manager
	private String amIP;
	private int amPort;

	private SwitchTableFlooder tableFlooding;
	private SwitchIdentificationSpoofer idSpoofing;
	private LinkFabricator linkDeception;

	private PktHandler pktHandler;

	private String targets;
	private NetworkInterface device;
	private byte OFVersion;
	private String ofPort;

	public AMInterface(String ip, int port) {
		amIP = ip;
		amPort = port;
	}

	public void setConfiguration(String str) {
		String[] list = new String(str).split(",");
		String controller_ip = "";
		String switch_ip = "";

		for (String s : list) {
			if (s.startsWith("version")) {
				String OFVersion = s.substring(s.indexOf(":") + 1);
				if (OFVersion.equals("1.0"))
					this.OFVersion = 1;
				else if (OFVersion.equals("1.3"))
					this.OFVersion = 4;
			} else if (s.startsWith("nic")) {
				String nic = s.substring(s.indexOf(":") + 1);
				this.device = NIC.getInterfaceByName(nic);
			} else if (s.startsWith("controller_ip")) {
				controller_ip = s.substring(s.indexOf(":") + 1);
			} else if (s.startsWith("switch_ip")) {
				switch_ip = s.substring(s.indexOf(":") + 1);
			} else if (s.startsWith("port")) {
				this.ofPort = s.substring(s.indexOf(":") + 1);
			}
		}

		this.targets = controller_ip + "," + switch_ip;
		pktHandler = new PktHandler(device, targets, this.OFVersion, this.ofPort);
		pktHandler.startARPSpoofing(); // forTest
	}

	public void connectServer() {
		try {
			socket = new Socket(amIP, amPort);
			socket.setReuseAddress(true);

			in = socket.getInputStream();
			dis = new DataInputStream(in);

			out = socket.getOutputStream();
			dos = new DataOutputStream(out);

			dos.writeUTF("ChannelAgent");
			dos.flush();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String recv = "";

		try {
			while (true) {
				// reads characters encoded with modified UTF-8
				recv = dis.readUTF();

				// config
				if (recv.startsWith("config")) {
					this.setConfiguration(recv);
					continue;
					// MITM
				} else if (recv.equalsIgnoreCase("B-2-A")) {
					System.out.println("\n[ATTACK] MITM start");
					pktHandler.startARPSpoofing();
					pktHandler.setTypeOfAttacks(PktHandler.MITM);

					Thread.sleep(10000);

					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("B-1-A")) { // Evaesdrop
					pktHandler.setARPspoof(true);
					pktHandler.setTypeOfAttacks(PktHandler.EVAESDROP);
					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("B-1-A-V")) {
					pktHandler.setARPspoof(false);
					String result = "";

					pktHandler.printNetwrokNodes(result);

					if (result != null && !result.isEmpty() && result.length() > 0)
						result = "Success\n:: Result of Topology Building! ::\n" + result;
					else
						result = "fail";

					System.out.println("\n\n[ATTACK] " + result);

					dos.writeUTF(result);
				} else if (recv.equalsIgnoreCase("C-3-A")) { // control Message
																// Manipulation
					pktHandler.setARPspoof(true);
					pktHandler.setTypeOfAttacks(PktHandler.CONTROLMESSAGEMANIPULATION);

					Thread.sleep(15000);

					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("A-4-A-3")) { // Malformed
																// control
																// message
					pktHandler.setARPspoof(true);
					pktHandler.setTypeOfAttacks(PktHandler.MALFORMEDCONTROLMESSAGE);

					Thread.sleep(5000);

					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("A-4-A-1")) { // Switch Table
																// Flooding
					tableFlooding = new SwitchTableFlooder();
					tableFlooding.startSwitchTableFlooding();

					Thread.sleep(15000);

					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("A-4-A-2")) { // Switch
																// Identification
																// Spoofing
					idSpoofing = new SwitchIdentificationSpoofer();
					idSpoofing.start();

					// check the time interval
					Thread.sleep(15000);

					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("A-10-A-2")) { // Link
																// fabrication

					// check the time interval
					linkDeception = new LinkFabricator();
					linkDeception.startLinkFabrication();

					Thread.sleep(15000);

					dos.writeUTF("success");
				} else if (recv.startsWith("fuzzing")) {
					// handler.setFuzzing(recv.substring(7));
				} else if (recv.equalsIgnoreCase("exit")) {
					pktHandler.setTypeOfAttacks(PktHandler.EMPTY);
					pktHandler.setARPspoof(false);
					/*
					 * handler.stopSwitchTableFlooder();
					 * handler.stopSwitchIdentificationSpoofer();
					 * handler.stopLinkFabrication();
					 */
				}

				dos.flush();
			}
		} catch (Exception e) {
			// if any error occurs
			System.out.println("Exception in AMIface");
			e.printStackTrace();

			if (dis != null)
				try {
					dis.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			System.exit(0);
		}
	}
}