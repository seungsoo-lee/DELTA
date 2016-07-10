package org.deltaproject.manager;

import org.deltaproject.manager.core.AgentManager;

import java.io.IOException;

/**
 * Created by changhoon on 5/10/16.
 */
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
