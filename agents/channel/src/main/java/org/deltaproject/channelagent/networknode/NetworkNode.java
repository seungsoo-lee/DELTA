package org.deltaproject.channelagent.networknode;

import java.util.ArrayList;
import java.util.HashMap;

public class NetworkNode {
	public static final int SWITCH = 1;
	public static final int HOST = 2;
	public static final int LINK = 3;

	private int nodeType;

	private String macAddr = "";
	private String ipAddr = "";
	private int port;
	private String dpid = "";

	private HashMap<Integer, NetworkNode> child;

	public NetworkNode() {
		child = new HashMap<Integer, NetworkNode>();
	}

	public int getNodeType() {
		return this.nodeType;
	}

	public String getMacAddr() {
		return this.macAddr;
	}

	public String getIpAddr() {
		return this.ipAddr;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public String getDPID() {
		return this.dpid;
	}
	
	public void setDPID(String id) {
		this.dpid = id;
	}

	public void setNodeType(int type) {
		this.nodeType = type;
	}

	public void setMacAddr(String addr) {
		this.macAddr = addr;
	}

	public void setIPAddr(String addr) {
		this.ipAddr = addr;
	}
	
	public void setPort(int p) {
		this.port = p;
	}

	public void putChildNode(int port, NetworkNode node) {
		child.put(port, node);
	}
	
	public HashMap<Integer, NetworkNode> getChildMap() {
		return this.child;
	}
}
