package nss.delta.hostagent.core;

import java.util.Scanner;

import nss.sdn.hostagent.HostAgent;

public class Main {
	public static void main(String args[]) {
		if (args.length == 0) {
			System.err.println("Enter the Config File");
			System.exit(1);
		}	
		
		HostAgent ca = new HostAgent(args[0]);
		
		ca.connectAgentsManager();
	}
}
