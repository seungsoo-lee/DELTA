package org.deltaproject.channelagent.pkthandler;

import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.slf4j.impl.SimpleLogger;

import java.io.IOException;
import java.util.List;

public class NIC {
    public static PcapNetworkInterface getInterfaceByName(String name) throws IOException {
        List<PcapNetworkInterface> allDevs;

        try {
            allDevs = Pcaps.findAllDevs();
        } catch (PcapNativeException e) {
            throw new IOException(e.getMessage());
        }

        if (allDevs == null || allDevs.isEmpty()) {
            throw new IOException("No NIF to capture.");
        }

        for (PcapNetworkInterface nic : allDevs) {
            if (nic.getName().equals(name)) {
                return nic;
            }
        }

        return null;
    }
}
