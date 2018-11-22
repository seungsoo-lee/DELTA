package org.deltaproject.manager.core;

import org.aesh.command.*;
import org.aesh.command.builder.CommandBuilder;
import org.aesh.command.completer.CompleterInvocation;
import org.aesh.command.completer.OptionCompleter;
import org.aesh.command.impl.registry.AeshCommandRegistryBuilder;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Arguments;
import org.aesh.command.option.Option;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.registry.CommandRegistry;
import org.aesh.command.settings.Settings;
import org.aesh.command.settings.SettingsBuilder;
import org.aesh.command.shell.Shell;
import org.aesh.io.Resource;
import org.aesh.readline.ReadlineConsole;
import org.aesh.readline.terminal.formatting.TerminalString;
import org.aesh.terminal.tty.Signal;
//import org.aesh.readline.terminal.Key;

import org.deltaproject.manager.Main;
import org.deltaproject.manager.utils.AgentLogger;
import org.deltaproject.manager.utils.ProgressBar;
import org.deltaproject.webui.TestCase;
import org.deltaproject.webui.TestCaseDirectory;
import org.deltaproject.webui.TestCaseExecutor;
import org.deltaproject.webui.TestQueue;
import org.deltaproject.webui.WebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;

import static org.aesh.terminal.tty.Signal.*;

//import java.util.function.Consumer;

public class AgentManager extends Thread {
    static final String ANSI_RESET = "\u001B[0m";
    static final String ANSI_WHITE_BRIGHT = "\033[0;97m";

    private static final Logger log = LoggerFactory.getLogger(AgentManager.class);
    private static AttackConductor conductor;
    private static ServerSocket listenAgent;

    private int portNum = 3366;
    private static BufferedReader sc;
    private static WebUI webUI = new WebUI();
    private static TestCaseExecutor testCaseExecutor;
    private static Configuration configuration;

    private Socket temp;
//    InterruptHandler interrupthandler;
    Consumer interrupthandler;
    public AgentManager(String path) {
        conductor = new AttackConductor(path);

        testCaseExecutor = new TestCaseExecutor(conductor);
        testCaseExecutor.start();
        webUI.activate();

        Runtime.getRuntime().addShutdownHook(AgentLogger.getShutdownInstance());

        configuration = new Configuration();
        configuration.initialize(path);
    }

    public void showMenu(){

        try {


/********** example of other git repository using aesh?  *************************/
//            interrupthandler = signal -> {
//                if (signal != null) {
//                    switch (signal) {
//                        case INT:
////                            if (editMode.isInChainedAction()) {
////                                parse(Key.CTRL_C);
////                            } else {
////                                if (attributes.getLocalFlag(Attributes.LocalFlag.ECHOCTL)) {
////                                    conn.stdoutHandler().accept(new int[]{'^', 'C'});
////                                }
////                                if (!flags.containsKey(ReadlineFlag.NO_PROMPT_REDRAW_ON_INTR)) {
////                                    conn.stdoutHandler().accept(Config.CR);
////                                    this.buffer().buffer().reset();
////                                    consoleBuffer.drawLine();
////                                }
////                            }
////                            if (prevSignalHandler != null) {
////                                prevSignalHandler.accept(signal);
////                            }
//                            closeServerSocket();
//                            webUI.deactivate();
//                            testCaseExecutor.interrupt();
////
//                            break;
////                        case CONT:
////                            conn.enterRawMode();
////                            //just call resize since it will redraw the buffer and set size
////                            resize(conn.size());
////                            break;
////                        case EOF:
////                            parse(Key.CTRL_D);
////                            //if inputHandler is null we send a signal to the previous handler)
////                            /*
////                            if (prevSignalHandler != null) {
////                                prevSignalHandler.accept(signal);
////                            }
////                            */
////                            break;
//                        default:
//                            break;
//                    }
//                }
//            });

            /**********  handler : any signal? but working only Ctrl-C. (signal == null) ***************/
            ReadlineConsole console = null;
            ReadlineConsole finalConsole = console;
            interrupthandler = signal -> {
//                if (signal == Signal.INT) {
                closeServerSocket();
                webUI.deactivate();
                testCaseExecutor.interrupt();
//                }
                finalConsole.stop();
            };

            /****************** aeshell setting ***************/
            CommandRegistry registry = new AeshCommandRegistryBuilder()
                    .command(AttackListCommand.class)
                    .command(ConfigurationCommand.class)
                    .command(AttackCommand.class)
//                    .command(UnknownCommand.class)
                    .command(TestCommand.class)
                    .command(ExitCommand.class)
                    .command(HelpCommand.class)
//                    .command(HistoryCommand.class)
                    .command(ClearCommand.class)
                    .create();
            Settings settings = SettingsBuilder
                    .builder()
                    .commandRegistry(registry)
                    .persistHistory(false)
                    .setInterruptHandler(interrupthandler)
                    .build();
            console = new ReadlineConsole(settings);

            System.out.println("\n DELTA: A Penetration Testing Framework for Software-Defined Networks\n");
            printHelp();
            console.setPrompt("\033[0;96m" +"Command> " + ANSI_RESET);
            console.start();
            /**************************************************/

        } catch (Exception e) {
            log.error(e.toString());
        }

/****
*   export command, interrupt
* ***/

//        while (true) {
//
//            input = sc.readLine();
//
//            if (input.equalsIgnoreCase("q")) {
//                closeServerSocket();
//                webUI.deactivate();
//                testCaseExecutor.interrupt();
//                break;
//            } else if (input.equalsIgnoreCase("h")) {
//                printHelp();
//            } else {
//                try {
//                    processUserInput(input);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }


    /****** handler?  *****/
//public static class InterruptHandler implements Consumer{
//
//    @Override
//    public void accept(Object signal) {
//        switch ((Signal) signal) {
//            case INT:
//                closeServerSocket();
//                webUI.deactivate();
//                testCaseExecutor.interrupt();
//
//        }
//    }
//}
    @CommandDefinition(name = "p", description = "show all known attacks", aliases = {"P"})
    public static class AttackListCommand implements Command {

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
            System.out.print(ANSI_WHITE_BRIGHT);
            System.out.print("\n" + "[Attack List]" + "\n");
            System.out.print(ANSI_RESET);

            conductor.printAttackList();
            System.out.println();

            return CommandResult.SUCCESS;
        }
    }

    @CommandDefinition(name = "c", description = "show configuration info", aliases = {"C"})
    public static class ConfigurationCommand implements Command {

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
            System.out.print(ANSI_WHITE_BRIGHT);
            System.out.print("\n" + "[Configuration]" + "\n\n");
            System.out.print(ANSI_RESET);

            System.out.println(conductor.showConfig());

            return CommandResult.SUCCESS;
        }
    }

    public static class CodeCompleter implements OptionCompleter {
        @Override
        public void complete(CompleterInvocation completerData) {
            List<String> completeList = new ArrayList<>();
            TreeMap<String, String> treeMap = conductor.getAttackInfo();
            Iterator<String> treeMapIter = treeMap.keySet().iterator();
            while (treeMapIter.hasNext()) {
                String key = (String) treeMapIter.next();
                if(key.startsWith(completerData.getGivenCompleteValue())){
                    completeList.add(String.format("%s", key));
                }

            }
            completerData.setAppendSpace(false);
            completerData.setCompleterValues(completeList);
        }
    }

    @CommandDefinition(name = "a", description = "replaying known attack(s)", aliases = {"A"})
    public static class AttackCommand implements Command {

        @Arguments(completer = CodeCompleter.class, description = "testcase number")
        private List<Resource> arguments;

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
            if(arguments == null){
                System.out.println("Usage:");
                System.out.println("         a   [ARGUMENT]\n");
                System.out.println("One argument [code] is required for [aA] command.");
                return CommandResult.SUCCESS;
            }
            else if(arguments.size() != 1){
                System.out.println("Usage:");
                System.out.println("         a   [ARGUMENT]\n");
                System.out.println("One argument [code] is required for [aA] command.");
                return CommandResult.SUCCESS;
            }

            String input = arguments.get(0).toString();

//            if (input.equalsIgnoreCase("A")) //conductor.replayAllKnownAttacks();
            if (conductor.isPossibleAttack(input) && TestCaseDirectory.getDirectory().containsKey(input.trim())) {
                TestCase testCase = TestCaseDirectory.getDirectory().get(input);
                testCase.setConfiguration(configuration);
                System.out.println("\nStart attack!");
                System.out.println("You can see the detail in WebUI or Log file");

                conductor.refreshConfig(testCase.getConfiguration());
                conductor.executeTestCase(testCase);
                System.out.print("\nTest Result: ");
                System.out.println(testCase.getResult());
                System.out.println("If the result is 'FAIL', it is vulnerable to the attack.");
            }
            else {
                System.out.println("Attack Code [" + input + "] is not available");
            }

            return CommandResult.SUCCESS;
        }
    }

//    @CommandDefinition(name = "unknown", description = "finding unknown attack", aliases = {"u", "U"})
//    public static class UnknownCommand implements Command {
//
//        @Override
//        public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
//            return CommandResult.SUCCESS;
//        }
//    }

    @CommandDefinition(name = "test", description = "test attack", aliases = {"t", "T"})
    public static class TestCommand implements Command {

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {

            conductor.testAttack("test");

            return CommandResult.SUCCESS;
        }
    }

    @CommandDefinition(name = "help", description = "show menu", aliases = {"h", "H"})
    public static class HelpCommand implements Command {

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
            System.out.print(ANSI_WHITE_BRIGHT);
            System.out.print("\n" + "[Menu]" + "\n\n");
            System.out.print(ANSI_RESET);

            printHelp();

            return CommandResult.SUCCESS;
        }
    }
//
//    @CommandDefinition(name = "history", description = "show history file")
//    public static class HistoryCommand implements Command {
//
//        @Override
//        public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
//            final String ANSI_RESET = "\u001B[0m";
//            final String ANSI_WHITE_BRIGHT = "\033[0;97m";
//            System.out.print(ANSI_WHITE_BRIGHT);
//            System.out.print("\n" + "[History]" + "\n\n");
//            System.out.print(ANSI_RESET);
//
//            File historyfile = new File("/home/ubuntu/.aesh_history");
//            BufferedReader br = null;
//            try {
//                br = new BufferedReader(new FileReader(historyfile));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            String line;
//            try {
//                while ((line = br.readLine()) != null) {
//                    System.out.println(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            return CommandResult.SUCCESS;
//        }
//    }

    @CommandDefinition(name = "q", description = "quit the program", aliases = {"exit", "quit", "Q"})
    public static class ExitCommand implements Command {

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
            closeServerSocket();
            webUI.deactivate();
            testCaseExecutor.interrupt();

            commandInvocation.stop();
            return CommandResult.SUCCESS;
        }
    }

    @CommandDefinition(name = "clear", description = "clear the command line")
    public static class ClearCommand implements Command {

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {

            commandInvocation.getShell().clear();

            return CommandResult.SUCCESS;
        }
    }


    public static void printHelp() {
        System.out.println(" [aA]\t- Replaying known attack(s)");
        System.out.println(" [pP]\t- Show all known attacks");
        System.out.println(" [cC]\t- Show configuration info");
        System.out.println(" [uU]\t- Finding an unknown attack");
        System.out.println(" [hH]\t- Show Menu");
        System.out.println(" [qQ]\t- Quit\n");
        System.out.println(" \"<Tab>\" for available commands or [aA] arguments.\n");
    }

//    public boolean processUserInput(String in) throws IOException, InterruptedException {
//        String input;
//
//        if (in.equalsIgnoreCase("P")) {
//     //       conductor.printAttackList();
//        } else if (in.equalsIgnoreCase("K")) {
//            System.out.print("\nSelect the attack code> ");
//            input = sc.readLine();
//
//            if (input.equalsIgnoreCase("A")) {
//                // conductor.replayAllKnownAttacks();
//            } else if (conductor.isPossibleAttack(input) && TestCaseDirectory.getDirectory().containsKey(input.trim())) {
//                TestCase testCase = TestCaseDirectory.getDirectory().get(input);
//                testCase.setConfiguration(this.configuration);
//                System.out.println("\nStart attack!");
//                System.out.println("You can see the detail in WebUI or Log file");
//
//                conductor.refreshConfig(testCase.getConfiguration());
//                conductor.executeTestCase(testCase);
//                System.out.print("\nTest Result: ");
//                System.out.println(testCase.getResult());
//                System.out.println("If the result is 'FAIL', it is vulnerable to the attack.");
//            } else {
//                System.out.println("Attack Code [" + input + "] is not available");
//                return false;
//            }
//        } else if (in.equalsIgnoreCase("C")) {
//       //     System.out.println(conductor.showConfig());
//        } else if (in.equalsIgnoreCase("U")) {
//
//        } else if (in.equalsIgnoreCase("test")) {
////            conductor.testAttack("test");
//        }
//
//        return true;
//    }

    public static void closeServerSocket() {
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
