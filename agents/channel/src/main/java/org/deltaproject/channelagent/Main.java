package org.deltaproject.channelagent;


import org.deltaproject.channelagent.core.AMInterface;

public class Main {
	public static void main(String args[]) {
		if (args.length != 2) {
			System.err.println("Usage: java -jar delta-agent-channel-X.X-SNAPSHOT.jar <Agent-Manager IP> <Agent-Manager Port>");
			System.exit(1);
		}
		
		AMInterface am_if = new AMInterface(args[0], args[1]);
		am_if.connectAgentManager();
		
		try {
			Thread.sleep(500);
			am_if.start();
			am_if.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}