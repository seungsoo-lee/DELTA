package org.deltaproject.manager.core;

import org.deltaproject.manager.utils.AgentLogger;
import org.deltaproject.manager.utils.ProgressBar;
import org.deltaproject.webui.TestCase;
import org.deltaproject.webui.TestCaseDirectory;
import org.deltaproject.webui.TestCaseExecutor;
import org.deltaproject.webui.TestQueue;
import org.deltaproject.webui.WebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class AgentManager extends Thread {
    private static final Logger log = LoggerFactory.getLogger(AgentManager.class);
    private AttackConductor conductor;
    private ServerSocket listenAgent;

    private int portNum = 3366;
    private BufferedReader sc;
    private WebUI webUI = new WebUI();
    private TestCaseExecutor testCaseExecutor;

    private Socket temp;

    public AgentManager(String path) {
        conductor = new AttackConductor(path);

        testCaseExecutor = new TestCaseExecutor(conductor);
        testCaseExecutor.start();
        webUI.activate();
        Runtime.getRuntime().addShutdownHook(AgentLogger.getShutdownInstance());
    }

    public void showMenu() throws IOException {
        String input;

        sc = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("\n DELTA: A Penetration Testing Framework for Software-Defined Networks\n");
        printHelp();
        while (true) {
            System.out.print("\nCommand> ");

            input = sc.readLine();

            if (input.equalsIgnoreCase("q")) {
                closeServerSocket();
                webUI.deactivate();
                testCaseExecutor.interrupt();
                break;
            } else if (input.equalsIgnoreCase("h")) {
                printHelp();
            } else {
                try {
                    processUserInput(input);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void printHelp() {
        System.out.println(" [pP]\t- Show all known attacks");
        System.out.println(" [cC]\t- Show configuration info");
        System.out.println(" [kK]\t- Replaying known attack(s)");
        System.out.println(" [uU]\t- Finding an unknown attack");
        System.out.println(" [hH]\t- Show Menu");
        System.out.println(" [qQ]\t- Quit\n");
    }

    public boolean processUserInput(String in) throws IOException, InterruptedException {
        String input;

        if (in.equalsIgnoreCase("P")) {
            conductor.printAttackList();
        } else if (in.equalsIgnoreCase("K")) {
            System.out.print("\nSelect the attack code> ");
            input = sc.readLine();

            if (input.equalsIgnoreCase("A")) {
                // conductor.replayAllKnownAttacks();
            } else if (conductor.isPossibleAttack(input) && TestCaseDirectory.getDirectory().containsKey(input.trim())) {
                // conductor.replayKnownAttack(input);
                TestCase testCase = new TestCase(input.trim());
                testCase.setStatus(TestCase.Status.QUEUED);
                TestQueue.getInstance().push(testCase);
            } else {
                System.out.println("Attack Code [" + input + "] is not available");
                return false;
            }
        } else if (in.equalsIgnoreCase("C")) {
            System.out.println(conductor.showConfig());
        } else if (in.equalsIgnoreCase("U")) {
            System.out.println("\n [aA]\t- Asymmetric control message");
            System.out.println(" [sS]\t- Symmetric control message");
            System.out.println(" [iI]\t- Intra-controller control message");

            System.out.print("\nSelect handler control message> ");
            input = sc.readLine();
        }
        return true;
    }

    public void closeServerSocket() {
        try {
            listenAgent.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    @Override
    public void run() {
        log.info("Start Server socket");
        try {
            listenAgent = new ServerSocket(portNum);
            listenAgent.setReuseAddress(true);

            while (true) {
                temp = listenAgent.accept();
                conductor.setSocket(temp);
            }
        } catch (Exception e) {
            // e.printStackTrace();
            closeServerSocket();
        }
    }
}
