package org.deltaproject.hostagent;

public class Main {
	public static void main(String args[]) {
		if (args.length != 2) {
			System.err.println("Usage: java -jar handler/delta-agent-host.jar <agent manager's ip> <agent-manager's port>");
			System.exit(1);
		}

		Interface am_if = new Interface(args[0], args[1]);
		am_if.connectAgentManager();
		am_if.start();
	}
}