package org.deltaproject.hostagent;

public class Main {
	public static void main(String args[]) {
		if (args.length == 2) {
			System.err.println("Usage: java -jar target/delta-agent-host.jar <agent manager's ip> <agent-manager's port>");
			System.exit(1);
		}
		
		AMInterface am_if = new AMInterface(args[0], args[1]);
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
