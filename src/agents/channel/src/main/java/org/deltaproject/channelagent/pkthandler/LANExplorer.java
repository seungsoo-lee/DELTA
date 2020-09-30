package org.deltaproject.channelagent.pkthandler;

import java.net.InetAddress;
import java.util.ArrayList;

/*
 * This class is used resolve the ip range of our network.
 * It just takes as an input an ip and the network mask and returns an ArrayList
 * which contains all the available ip of the network.
 * 
 * The basic idea is to take an ip and resolve the gateway ip and the broadcast ip ,using the mask,
 * then convert the gateway and the broadcast to integers and generate all the available ips for the
 * given network and deconvert the integers back to ips. Generally in a network your available ip range 
 * contains all the ips between your gateway and the broadcast (including the gateway and the broadcast)
 * so this is what happens in this class
 */
public class LANExplorer {
    public static ArrayList<String> getIPs(String cidrIp, String mask) {
        System.out.println("Scanning: "+cidrIp+" with mask: "+mask);
    	int[] bounds = LANExplorer.rangeFromCidr(cidrIp, mask);
        
    	ArrayList<String> addresslist = new ArrayList<String>();
        for (int i = bounds[0]; i <= bounds[1]; i++) {
            String address = InetRange.intToIp(i);
            addresslist.add(address);
        }
        return addresslist;
    }

    public static int[] rangeFromCidr(String cidrIp, String mask) {   
        
        int[] result = new int[2];
        int network = InetRange.ipToInt(cidrIp) & InetRange.ipToInt(mask);
        int broadcast = InetRange.ipToInt(cidrIp) | ~InetRange.ipToInt(mask);
        //network + 1 usually equals the gateway or the start of your ip range
        result[0] = network + 1;
        result[1] = broadcast - 1; // upper bound
        System.out.println("Scanning from: "+ InetRange.intToIp(result[0])
        		+" to "+ InetRange.intToIp(result[1]));

        return result;
    }

    static class InetRange {
        public static int ipToInt(String ipAddress) {
            try {
                byte[] bytes = InetAddress.getByName(ipAddress).getAddress();
                int octet1 = (bytes[0] & 0xFF) << 24;
                int octet2 = (bytes[1] & 0xFF) << 16;
                int octet3 = (bytes[2] & 0xFF) << 8;
                int octet4 = bytes[3] & 0xFF;
                int address = octet1 | octet2 | octet3 | octet4;

                return address;
            } catch (Exception e) {
                e.printStackTrace();

                return 0;
            }
        }

        public static String intToIp(int ipAddress) {
            int octet1 = (ipAddress & 0xFF000000) >>> 24;
            int octet2 = (ipAddress & 0xFF0000) >>> 16;
            int octet3 = (ipAddress & 0xFF00) >>> 8;
            int octet4 = ipAddress & 0xFF;

            return new StringBuffer().append(octet1).append('.').append(octet2)
                                     .append('.').append(octet3).append('.')
                                     .append(octet4).toString();
        }
    }
}