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

    public static void main(String args[]) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java -jar handler/delta-manager-1.0-SNAPSHOT-jar-with-dependencies.jar ../tools/config/manager.cfg");
            System.exit(1);
        }

        System.out.print(ANSI_GREEN_N);
        System.out.print(
                "    ____   ______ __   ______ ___                                             \n" +
                "   / __ \\ / ____// /  /_  __//   |   _                                        \n" +
                "  / / / // __/  / /    / /  / /| |  (_)                                       \n" +
                " / /_/ // /___ / /___ / /  / ___ | _                                          \n" +
                "/_____//_____//_____//_/  /_/  |_|(_)                                         \n");
        System.out.print(ANSI_RESET);
        System.out.print(ANSI_GREEN_B);
        System.out.print(
                "   _____  ____   _   __   _____  ______ ______ __  __ ____   ____ ________  __\n" +
                "  / ___/ / __ \\ / | / /  / ___/ / ____// ____// / / // __ \\ /  _//_  __/\\ \\/ /\n" +
                "  \\__ \\ / / / //  |/ /   \\__ \\ / __/  / /    / / / // /_/ / / /   / /    \\  / \n" +
                " ___/ // /_/ // /|  /   ___/ // /___ / /___ / /_/ // _, _/_/ /   / /     / /  \n" +
                "/____//_____//_/ |_/   /____//_____/ \\____/ \\____//_/ |_|/___/  /_/     /_/   \n" +
                "    ______ _    __ ___     __    __  __ ___   ______ ____ ____   _   __       \n" +
                "   / ____/| |  / //   |   / /   / / / //   | /_  __//  _// __ \\ / | / /       \n" +
                "  / __/   | | / // /| |  / /   / / / // /| |  / /   / / / / / //  |/ /        \n" +
                " / /___   | |/ // ___ | / /___/ /_/ // ___ | / /  _/ / / /_/ // /|  /         \n" +
                "/_____/   |___//_/  |_|/_____/\\____//_/  |_|/_/  /___/ \\____//_/ |_/          \n" +
                "    ______ ____   ___     __  ___ ______ _       __ ____   ____   __ __       \n" +
                "   / ____// __ \\ /   |   /  |/  // ____/| |     / // __ \\ / __ \\ / //_/       \n" +
                "  / /_   / /_/ // /| |  / /|_/ // __/   | | /| / // / / // /_/ // ,<          \n" +
                " / __/  / _, _// ___ | / /  / // /___   | |/ |/ // /_/ // _, _// /| |         \n" +
                "/_/    /_/ |_|/_/  |_|/_/  /_//_____/   |__/|__/ \\____//_/ |_|/_/ |_|         \n" +
                "                                                                              \n");

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