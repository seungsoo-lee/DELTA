package org.deltaproject.manager;

import org.deltaproject.manager.core.AgentManager;
import org.slf4j.impl.SimpleLogger;

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

        System.setProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "TRUE");

        AgentManager am = new AgentManager(args[0]);
        am.start();
        am.showMenu();
        am.closeServerSocket();
    }
}