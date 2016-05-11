package org.deltaproject.channelagent.pkthandler;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NIC {
	public static NetworkInterface nic() {
		NetworkInterface[] devices;
		// Obtain the list of network interfaces
		devices = JpcapCaptor.getDeviceList();
		for (int i = 0; i < devices.length; i++) {
			System.out.println(i + ": " + devices[i].name + "("
					+ devices[i].datalink_description + ")");
			// Prints the IP address for each NIC
			for (NetworkInterfaceAddress a : devices[i].addresses) {
				System.out.println(" address:" + a.address);
			}
		}
		System.out.print("> Choose the NIC you want to use:");
		// Reads the user's input
		String str = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			str = in.readLine();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		// Checks if the user input is null
		while (str.isEmpty()) {
			System.out.println("> Null input isnot valid!");
			System.out.println("> Choose the NIC you want to use:");
			try {
				str = in.readLine();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		// Converts the string to integer(thanks captain obvious)
		return devices[Integer.parseInt(str)];
	}
	
	public static NetworkInterface getInterfaceByName(String name){
		NetworkInterface[] devices;
		// Obtain the list of network interfaces
		devices = JpcapCaptor.getDeviceList();
		
		for(int i = 0; i < devices.length; i++){
			if(devices[i].name.equals(name)){
				return devices[i];
			}
		}
		
		return null;
	}
}
