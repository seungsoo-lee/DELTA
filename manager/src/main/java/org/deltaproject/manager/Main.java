package org.deltaproject.manager;

import org.deltaproject.manager.core.AgentManager;
import org.deltaproject.manager.utils.AgentLogger;
import org.slf4j.impl.SimpleLogger;

import java.io.IOException;

/**
 * Created by changhoon on 5/10/16.
 */
public class Main {

    public static void main(String args[]) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java -jar handler/delta-manager-1.0-SNAPSHOT-jar-with-dependencies.jar ../tools/config/manager.cfg");
            System.exit(1);
        }

        AgentLogger.checkLogDirectory();
        System.setProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "TRUE");
        System.setProperty(SimpleLogger.LOG_FILE_KEY, System.getenv("DELTA_ROOT") + "/log/manager.log");
        System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "FALSE");

        AgentManager am = new AgentManager(args[0]);

        am.start();
        am.showMenu();
    }
}