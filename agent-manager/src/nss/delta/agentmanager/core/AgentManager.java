package nss.delta.agentmanager.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import nss.delta.agentmanager.core.AttackConductor;
import nss.delta.agentmanager.unknown.Fuzzing;
import nss.delta.agentmanager.utils.ProgressBar;

public class AgentManager extends Thread {
	private AttackConductor conductor;
	private ServerSocket listenAgent;
	private int portNum = 3366;
	private BufferedReader sc;

	public AgentManager(String path) {
		this.conductor = new AttackConductor(path);
	}

	public void showMenu() throws IOException {
		String input = "";

		sc = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			ProgressBar.clearConsole();
			System.out.println("\n DELTA: A Penetration Testing Framework for Software-Defined Networks\n");
			System.out.println(" [pP]\t- Show all known attacks");
			System.out.println(" [cC]\t- Show configuration info");
			System.out.println(" [kK]\t- Replaying known attack(s)");
			System.out.println(" [uU]\t- Finding an unknown attack");
			System.out.println(" [qQ]\t- Quit\n");
			System.out.print("\nCommand> ");

			input = sc.readLine();

			if (input.equalsIgnoreCase("q")) {
				closeServerSocket();
				break;
			} else {
				processUserInput(input);
				System.out.print("\nPress ENTER key to continue..");
				input = sc.readLine();
			}
		}
	}

	public boolean processUserInput(String in) throws IOException {
		String input = "";

		if (in.equalsIgnoreCase("P")) {
			conductor.printAttackList();
		} else if (in.equalsIgnoreCase("K")) {
			System.out.print("\nSelect the attack code (replay all, enter the 'A')> ");
			input = sc.readLine();

			if (input.equalsIgnoreCase("A")) {
				conductor.replayAllKnownAttacks();
			} else if (conductor.isPossibleAttack(input)) {
				conductor.replayKnownAttack(input);
			} else {
				System.out.println("Attack Code [" + input + "] is not available");
				return false;
			}

		} else if (in.equalsIgnoreCase("C")) {
			// conductor.showConfig();
		} else if (in.equalsIgnoreCase("U")) {
			System.out.println("\n [aA]\t- Asymmetric control message");
			System.out.println(" [sS]\t- Symmetric control message");
			System.out.println(" [iI]\t- Intra-controller control message");

			System.out.print("\nSelect target control message> ");
			input = sc.readLine();
		}
		return true;
	}

	public void closeServerSocket() {
		try {
			listenAgent.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			listenAgent = new ServerSocket(portNum);
			while (true) {
				Socket temp = listenAgent.accept();
				conductor.setSocket(temp);
			}
		} catch (IOException e) {
			closeServerSocket();
		} finally {
			closeServerSocket();
		}
	}
}
