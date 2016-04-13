package nss.delta.agentmanager.core;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

import nss.delta.agentmanager.dummycon.DummyController;
import nss.delta.agentmanager.testcase.TestSwitchCase;
import nss.delta.agentmanager.utils.ProgressBar;

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