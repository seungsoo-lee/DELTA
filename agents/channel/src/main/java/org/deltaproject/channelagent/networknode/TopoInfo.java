package org.deltaproject.channelagent.networknode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TopoInfo {
    // child nodes
    private ArrayList<NetworkNode> switchList;

    public TopoInfo() {
        switchList = new ArrayList<NetworkNode>();
    }

    public NetworkNode findSW(String parentIP, int parentPort) {
        for (NetworkNode n : switchList) {
            if (n.getIpAddr().equals(parentIP) && n.getPort() == parentPort) {
                return n;
            }
        }

        return null;
    }

    public boolean insertSwitch(NetworkNode node) {
        NetworkNode n = findSW(node.getIpAddr(), node.getPort());

        if (n == null) {
            switchList.add(node);
        }

        if (n != null && n.getDPID() == null) {
            n.setDPID(node.getDPID());
        }

        return true;
    }

    public boolean insertNode(NetworkNode node, int inPort, String parentIP, int parentPort) {
        NetworkNode p = findSW(parentIP, parentPort);

        if (p == null) {
            p = new NetworkNode();
            p.setNodeType(NetworkNode.SWITCH);
            p.setDPID(null);
            p.setIPAddr(parentIP);
            p.setPort(parentPort);
        }

        if (!p.getChildMap().containsKey(inPort))
            p.putChildNode(inPort, node);

        return true;
    }

    public String getTopoInfo() {
        String result = "";

        for (NetworkNode n : switchList) {
            HashMap<Integer, NetworkNode> hosts = n.getChildMap();

            Set<Entry<Integer, NetworkNode>> set = hosts.entrySet();
            Iterator<Entry<Integer, NetworkNode>> it = set.iterator();

            result += "Switch " + n.getDPID() + "\n";

            while (it.hasNext()) {
                Map.Entry<Integer, NetworkNode> e = (Map.Entry<Integer, NetworkNode>) it.next();

                if (e.getValue().getNodeType() == NetworkNode.HOST) {
                    result += "\t Port " + e.getKey() + " - HOST " + e.getValue().getIpAddr() + "\n";
                } else {
                    result += "\t Port " + e.getKey() + " - LINK " + e.getValue().getDPID() + ":" + e.getValue().getPort() + "\n";
                }


            }
            result += "\n";
        }

        return result;
    }
}
