package org.deltaproject.manager;

import org.deltaproject.manager.core.AgentManager;
import org.deltaproject.manager.utils.AgentLogger;
import org.slf4j.impl.SimpleLogger;

import java.io.IOException;
import java.util.logging.LogManager;

/**
 * Created by changhoon on 5/10/16.
 */
public class Main {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN_B = "\u001B[21;97m";
    public static final String ANSI_GREEN_N = "\u001B[1;92m";
    public static final String LOGO = "     ________    _______  ___  ___________   __      \n" +
            "    |\"      \"\\  /\"     \"||\"  |(\"     _   \") /\"\"\\     \n" +
            "    (.  ___  :)(: ______)||  | )__/  \\\\__/ /    \\    \n" +
            "    |: \\   ) || \\/    |  |:  |    \\\\_ /   /' /\\  \\   \n" +
            "    (| (___\\ || // ___)_  \\  |___ |.  |  //  __'  \\  \n" +
            "    |:       :)(:      \"|( \\_|:  \\\\:  | /   /  \\\\  \\ \n" +
            "    (________/  \\_______) \\_______)\\__|(___/    \\___)\n" +
            "                                                     ";

    public static void main(String args[]) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: bin/run-delta tools/config/<configuration file>");
            System.exit(1);
        }

        System.out.print(ANSI_GREEN_N);
        System.out.print("\n" + LOGO + "\n");
        System.out.print(ANSI_RESET);
        System.out.print(ANSI_GREEN_B);
        System.out.print(ANSI_RESET);
        System.setProperty(SimpleLogger.LOG_FILE_KEY, System.getenv("DELTA_ROOT") + "/log/manager.log");
        System.setProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "TRUE");
        System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "FALSE");
        AgentLogger.checkLogDirectory();

        LogManager.getLogManager().reset();

        AgentManager am = new AgentManager(args[0]);

        am.start();
        am.showMenu();
    }
}