package org.deltaproject.odlagent;//package com.example.appagent;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.net.Socket;
//import java.net.UnknownHostException;
//
//public class Communication extends Thread {
//	int result = 1;
//
//	private AppAgent app;
//	private Activator act;
//
//	private Socket socket;
//	private InputStream in;
//	private DataInputStream dis;
//	private OutputStream out;
//	private DataOutputStream dos;
//
//	private String serverIP;
//	private int serverPort;
//
//	private BufferedWriter writer;
//	private BufferedReader reader;
//	
//	public void setServerAddr(String ip, int port) {
//		this.serverIP = ip;
//		this.serverPort = port;
//	}
//
//	public void connectServer(String agent) {
//		try {
//			socket = new Socket(serverIP, serverPort);
////			in = socket.getInputStream();
////			dis = new DataInputStream(in);
////			out = socket.getOutputStream();
////			dos = new DataOutputStream(out);
////
////			dos.writer(agent);
////			writer.flush();
//			
//			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			writer.write(agent);
//			writer.flush();
//			
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public void setAgent(AppAgent in) {
//		this.app = in;
//	}
//
//	public void setActivator(Activator in) {
//		this.act = in;
//	}
//
//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//		String recv = "";
//
//		try {
//			while (true) {
//				// reads characters encoded with modified UTF-8
//
//				recv = reader.readLine();
//
//				if (recv.contains("A-2-M") && act != null) {
//					if (act.Install_SubAgent()) {
//						writer.write("success");
//					} else
//						writer.write("fail");
//				} else if (recv.contains("A-2-M-2")) {
//					app.Infinite_Loop();
//				} else if (recv.contains("A-3-M")) {
//					if (app.Internal_Storage_Abuse())
//						writer.write("success");
//					else
//						writer.write("fail");
//				} else if (recv.contains("A-5-M-1")) {
//					if (app.Flow_Rule_Modification())
//						writer.write("success");
//					else
//						writer.write("fail");
//				} else if (recv.contains("A-5-M-2")) {
//					if (app.Flow_Table_Cleareance())
//						writer.write("success");
//					else
//						writer.write("fail");
//				} else if (recv.contains("A-6-M-1")) {
//					if (act.Service_Unregstration_Attack("arp"))
//						writer.write("success");
//					else
//						writer.write("fail");
//				} else if (recv.contains("A-6-M-2")) {
//					if (act.Application_Eviction("arp"))
//						writer.write("success");
//					else
//						writer.write("fail");
//				} else if (recv.contains("A-7-M-1")) {
//					app.Resource_Exhaustion_Mem();
//					writer.write("success");
//				} else if (recv.contains("A-7-M-2")) {
//					app.Resource_Exhaustion_CPU();
//					writer.write("success");
//				} else if (recv.contains("A-8-M")) {
//					if (app.System_Variable_Manipulation())
//						writer.write("success");
//					else
//						writer.write("fail");
//				} else if (recv.contains("A-9-M")) {
//					app.System_Command_Execution();
//					writer.write("success");
//				} else if (recv.contains("C-2-M")) {
//					if (app.Switch_Firmware_Abuse())
//						writer.write("success");
//					else
//						writer.write("fail");
//				} else if (recv.contains("Test")) {
//					writer.write("receive test");
//				}
//				
//				writer.flush();
//			}
//		} catch (Exception e) {
//			// if any error occurs
//			e.printStackTrace();
//
//		}
//	}
//}