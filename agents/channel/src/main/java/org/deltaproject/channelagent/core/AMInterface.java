package org.deltaproject.channelagent.core;

import jpcap.NetworkInterface;

import org.deltaproject.channelagent.dummy.DummyOFSwitch;
import org.deltaproject.channelagent.pkthandle.NIC;
import org.deltaproject.channelagent.pkthandle.PktListener;
import org.deltaproject.channelagent.testcase.LinkFabricator;
import org.deltaproject.channelagent.testcase.SwitchIdentificationSpoofer;
import org.deltaproject.channelagent.testcase.SwitchTableFlooder;
import org.deltaproject.channelagent.testcase.TestAdvancedSet;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

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

	private PktListener pktListener;

	private NetworkInterface device;
	private byte OFVersion;
	private String ofPort;
	private String handler;
	private String controllerIP;
	private String switchIP;

	private DummyOFSwitch dummysw;

	public AMInterface(String ip, String port) {
		amIP = ip;
		amPort = Integer.parseInt(port);

		dummysw = new DummyOFSwitch();
	}

	public AMInterface(String config) {
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

	public void setConfiguration(String str) {
		String[] list = new String(str).split(",");

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
				controllerIP = s.substring(s.indexOf(":") + 1);
			} else if (s.startsWith("switch_ip")) {
				switchIP = s.substring(s.indexOf(":") + 1);
			} else if (s.startsWith("port")) {
				this.ofPort = s.substring(s.indexOf(":") + 1);
			} else if (s.startsWith("handler")) {
				this.handler = s.substring(s.indexOf(":") + 1);
			}
		}

		pktListener = new PktListener(device, controllerIP, switchIP, OFVersion, this.ofPort, this.handler);
	}

	public void connectAgentManager() {
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
				} else if (recv.equalsIgnoreCase("3.1.170")) { // Evaesdrop
					pktListener.startListening();
					pktListener.setTypeOfAttacks(PktListener.EVAESDROP);
					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("3.1.170-V")) {
					String result = "";

					pktListener.printNetwrokNodes(result);

					if (result != null && !result.isEmpty() && result.length() > 0)
						result = "Success\n:: Result of Topology Building! ::\n" + result;
					else
						result = "fail";

					System.out.println("\n\n[ATTACK] " + result);

					dos.writeUTF(result);
				} else if (recv.equalsIgnoreCase("3.1.180")) {
					System.out.println("[Channel-Agent] 3.1.180 - MITM start");
					pktListener.setTypeOfAttacks(PktListener.MITM);
					pktListener.startListening();
					pktListener.startARPSpoofing();

					Thread.sleep(10000);

					dos.writeUTF("success");
					pktListener.stopARPSpoofing();
				} else if (recv.equalsIgnoreCase("C-3-A")) { // control Message
																// Manipulation
					pktListener.setTypeOfAttacks(PktListener.CONTROLMESSAGEMANIPULATION);

					Thread.sleep(15000);

					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("A-4-A-3")) { // Malformed
																// control
																// message
					pktListener.setTypeOfAttacks(PktListener.MALFORMEDCONTROLMESSAGE);

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
					/*
					 * System.out.println("GET SEED");
					 * pktListener.setTypeOfAttacks(PktListener.SEED);
					 * pktListener.startListening();
					 * 
					 * Thread.sleep(10000); System.out.println("DummySW START");
					 * pktListener.setTypeOfAttacks(PktListener.EMPTY);
					 */

					dummysw.connectTargetController(controllerIP, ofPort);
					dummysw.setOFFactory(this.OFVersion);
					// dummysw.setSeed(pktListener.getSeedPackets());
					dummysw.start();
				} else if (recv.equalsIgnoreCase("exit")) {
					pktListener.setTypeOfAttacks(PktListener.EMPTY);
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