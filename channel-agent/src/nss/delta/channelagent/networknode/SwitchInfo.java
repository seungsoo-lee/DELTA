package nss.delta.channelagent.networknode;

import java.util.ArrayList;

public class SwitchInfo {
	private byte[] macAddr = new byte[6];
	private byte[] ipAddr = new byte[4];
	private byte[] name;

	private ArrayList<Character> portList;
	private ArrayList<HostInfo> hostList;

	public SwitchInfo() {
		hostList = new ArrayList<HostInfo>();
		portList = new ArrayList<Character>();
	}

	public void addPortID(char in) {
		portList.add(in);
	}

	public byte[] getMacAddr() {
		return this.macAddr;
	}

	public void setMacAddr(byte[] in) {
		this.macAddr = in;
	}

	public void setPortID(byte[] in) {

	}

	public void setSystemName(byte[] in) {
		name = new byte[in.length];
		name = in;
	}

	public void setipAddr(byte[] in) {
		this.ipAddr = in;
	}

	public void addHost(HostInfo in) {
		this.hostList.add(in);
	}

}
