package org.deltaproject.hostagent;

public class Main {
	public static void main(String args[]) {
		if (args.length == 0) {
			System.err.println("Enter the Config File");
			System.exit(1);
		}
		
		AMInterface am_if = new AMInterface(args[0]);
		am_if.connectAgentManager();
		try {
			Thread.sleep(500);
			am_if.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
