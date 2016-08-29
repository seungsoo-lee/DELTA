package org.deltaproject.channelagent.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.UnknownHostException;

import org.deltaproject.channelagent.dummy.DummyOFSwitch;
import org.deltaproject.channelagent.pkthandle.NIC;
import org.deltaproject.channelagent.pkthandle.PktListener;
import org.deltaproject.channelagent.testcase.LinkFabricator;
import org.deltaproject.channelagent.testcase.SwitchIdentificationSpoofer;
import org.deltaproject.channelagent.testcase.SwitchTableFlooder;
import org.deltaproject.channelagent.testcase.TestAdvancedSet;

import jpcap.NetworkInterface;

public class AMInterface extends Thread {
	private int result = 1;

	private Socket socket;

	private InputStream in;
	private DataInputStream dis;
	private OutputStream out;
	private DataOutputStream dos;

	private Process processCbench;

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
	private String cbench;
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

	public boolean executeCbench() {
		try {
			processCbench = Runtime.getRuntime().exec(
					cbench + "cbench -c " + this.controllerIP + "  -p " + ofPort + " -m 10000 -l 10 -s 16 -M 1000 -t");

			Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
			pidField.setAccessible(true);
			Object value = pidField.get(processCbench);

			int cbenchPID = (Integer) value;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public void setConfiguration(String str) {
		String[] list = new String(str).split(",");
		String nic = "";

		for (String s : list) {
			if (s.startsWith("version")) {
				String OFVersion = s.substring(s.indexOf(":") + 1);
				if (OFVersion.equals("1.0"))
					this.OFVersion = 1;
				else if (OFVersion.equals("1.3"))
					this.OFVersion = 4;
			} else if (s.startsWith("nic")) {
				nic = s.substring(s.indexOf(":") + 1);
				this.device = NIC.getInterfaceByName(nic);
			} else if (s.startsWith("controller_ip")) {
				controllerIP = s.substring(s.indexOf(":") + 1);
			} else if (s.startsWith("switch_ip")) {
				String temp = s.substring(s.indexOf(":") + 1);

				if (temp.contains(",")) {
					switchIP = temp.substring(0, s.indexOf(","));
				} else {
					switchIP = temp;
				}
			} else if (s.startsWith("port")) {
				this.ofPort = s.substring(s.indexOf(":") + 1);
			} else if (s.startsWith("handler")) {
				this.handler = s.substring(s.indexOf(":") + 1);
			} else if (s.startsWith("cbench")) {
				this.cbench = s.substring(s.indexOf(":") + 1);
			}
		}

		System.out.println("\n[Channel-Agent] Configuration setup");
		System.out.println("[Channel-Agent] OF version/port: " + OFVersion + "/" + ofPort);
		System.out.println("[Channel-Agent] MITM NIC   : " + nic);
		System.out.println("[Channel-Agent] Target Controller IP: " + controllerIP);
		System.out.println("[Channel-Agent] Target Switch IP : " + switchIP);
		System.out.println("[Channel-Agent] Cbench Root Path :" + cbench);

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

				if (recv.startsWith("config")) {
					this.setConfiguration(recv);
					continue;
				} else if (recv.equalsIgnoreCase("3.1.010")) {
					System.out.println("\n[Channel-Agent] Pacekt-In Flooding test starts");
					this.executeCbench();
					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("3.1.160")) {
					System.out.println("\n[Channel-Agent] LinkFabrication test starts");
					pktListener.setTypeOfAttacks(TestAdvancedSet.LINKFABRICATION);
					pktListener.startListening();	
					pktListener.startARPSpoofing();
					
					Thread.sleep(40000);
					
					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("3.1.170")) {
					System.out.println("\n[Channel-Agent] Evaesdrop test starts");
					pktListener.setTypeOfAttacks(TestAdvancedSet.EVAESDROP);
					pktListener.startListening();	
					pktListener.startARPSpoofing();
				} else if (recv.equalsIgnoreCase("3.1.170-2")) {
					String result = pktListener.getTopoInfo();

					if (result != null && !result.isEmpty() && result.length() > 0)
						result = "success|\n:: Result of Topology Building! ::\n" + result;
					else
						result = "fail";

					System.out.println("\n[Channel-Agent] Topology Information " + result);					
					dos.writeUTF(result);
				} else if (recv.equalsIgnoreCase("3.1.180")) {
					System.out.println("\n[Channel-Agent] MITM test start");
					pktListener.setTypeOfAttacks(TestAdvancedSet.MITM);
					pktListener.startListening();
					pktListener.startARPSpoofing();
					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("3.1.050")) { // Switch Table
																// Flooding
					tableFlooding = new SwitchTableFlooder();
					tableFlooding.startSwitchTableFlooding();

					Thread.sleep(15000);

					dos.writeUTF("success");
				} else if (recv.equalsIgnoreCase("3.1.060")) {		
					System.out.println("\n[Channel-Agent] Switch Identification Spoofing Test");
					pktListener.testSwitchIdentification();
					
					
//					idSpoofing = new SwitchIdentificationSpoofer();
//					idSpoofing.start();

					// check the time interval
					//Thread.sleep(15000);

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
					pktListener.setTypeOfAttacks(TestAdvancedSet.EMPTY);
					pktListener.stopARPSpoofing();
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