package nss.delta.channelagent.networknode;

import java.util.ArrayList;
import java.util.List;

public class NetworkInfo {
	// child nodes
	private List<NetworkInfo> nodes;
	private String ipAddr;
	private String macAddr;
	// port number which is attached to parent node
	private int portNum;
	// 1 = switch, 2 = host
	private int nodeType;
	public final int isEmpty = -1;
	public final int isSwitch = 1;
	public final int isHost = 2;

	public NetworkInfo() {
		nodes = new ArrayList();
		nodeType = isEmpty;
	}

	public List<NetworkInfo> getNodes() {
		return nodes;
	}

	public void setNodes(List<NetworkInfo> nodes) {
		this.nodes = nodes;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public String getMacAddr() {
		return macAddr;
	}

	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}

	public int getPortNum() {
		return portNum;
	}

	public void setPortNum(int portNum) {
		this.portNum = portNum;
	}

	public int getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}

	public boolean insertNode(NetworkInfo child, String parentIP, String parentMac) {
		if (child == null || parentIP == null || parentIP.isEmpty() || parentMac == null || parentMac.isEmpty())
			return false;

		if (getIpAddr().equalsIgnoreCase(parentIP) && getMacAddr().equalsIgnoreCase(parentMac)) {
			boolean isOld = false;
			boolean isAvailablePort = true;
			for (int i = 0; i < nodes.size(); i++) {
				// if
				// (nodes.get(i).getIpAddr().equalsIgnoreCase(child.getIpAddr())
				// &&
				// nodes.get(i).getMacAddr().equalsIgnoreCase(child.getMacAddr())
				// ) {
				if (nodes.get(i).getMacAddr().equalsIgnoreCase(child.getMacAddr())) {
					// nodes.get(i).setPortNum(child.getPortNum());
					isOld = true;
					break;
				}
			}
			for (int i = 0; i < nodes.size(); i++) {
				if ((nodes.get(i).getPortNum() == child.getPortNum()) || (getPortNum() == child.getPortNum())) {
					isAvailablePort = false;
					break;
				}
			}
			if ((!isOld) && isAvailablePort) {
				nodes.add(child);
				return true;
			}
			return false;
		}
		
		boolean result = false;
		for (int i = 0; i < nodes.size(); i++) {
			result = nodes.get(i).insertNode(child, parentIP, parentMac);
			if (result)
				return true;
		}
		
		return false;
	}

	public String toPrintNodes(String result, int tab) {
		if (tab == 0) {
			result += "= Switch : " + getIpAddr() + " , " + getMacAddr() + "\n";
		} else if (getNodeType() == isHost) {
			for (int i = 0; i < tab; i++)
				result += "\t";
			result += "L [" + getPortNum() + "] Host : " + getIpAddr() + " , " + getMacAddr() + "\n";
		} else if (getNodeType() == isSwitch) {
			for (int i = 0; i < tab; i++)
				result += "\t";
			result += "L [" + getPortNum() + "] Switch : " + getIpAddr() + " , " + getMacAddr() + "\n";
		}
		if (nodes != null && !nodes.isEmpty()) {
			for (int i = 0; i < nodes.size(); i++) {
				result = nodes.get(i).toPrintNodes(result, tab + 1);
			}
		}

		return result;
	}
}
