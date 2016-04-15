package nss.delta.agentmanager.core;

import java.io.IOException;

public class Main {
	public static void main(String args[]) throws IOException {
		if (args.length == 0) {
			System.err.println("Enter the Config File");
			System.exit(1);
		}
		
		AgentManager am = new AgentManager(args[0]);
		am.start();
		am.showMenu();
		am.closeServerSocket();		
		
		
//		TestSwitchCase ts = new TestSwitchCase();		
//		ts.testSlaveControllerViolation();	
	}
}