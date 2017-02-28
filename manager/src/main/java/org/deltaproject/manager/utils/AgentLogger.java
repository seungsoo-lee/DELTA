package org.deltaproject.manager.utils;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by jinwookim on 2017. 2. 16..
 */

public class AgentLogger {
    public final static String LOG_PATH = System.getenv("DELTA_ROOT") + "/log/";
    public final static String APP_AGENT = "app.log";
    public final static String CHANNEL_AGENT = "channel.log";
    public final static String HOST_AGENT = "host.log";

    public static ArrayList<Thread> loggerList = new ArrayList();

    public static LoggerThread getLoggerThreadInstance(Process proc, String name) {
        return new LoggerThread(proc, name);
    }

    public static RunWhenShuttingDown getShutdownInstance() {
        return new RunWhenShuttingDown();
    }

    public static void addLoggerThread(Thread loggerThread) {
        loggerList.add(loggerThread);
    }

    public static void stopAllLogger() {
        loggerList.forEach(loggerThread -> loggerThread.interrupt());
    }

    public static class LoggerThread implements Runnable {
        private String name;
        private BufferedReader stderrBr;

        LoggerThread(Process proc, String name) {
            this.stderrBr = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            this.name = name;
        }

        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    FileWriter output = new FileWriter(LOG_PATH + name, true);
                    String line = stderrBr.readLine();

                    if (line != null) {
                        output.write(line + "\n");
                    }
                    output.flush();
                    output.close();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class RunWhenShuttingDown extends Thread {
        public void run() {
            System.out.println("Control-C caught. Shutting down...");
            File f = new File(LOG_PATH + APP_AGENT);
            f.delete();
            f = new File(LOG_PATH + CHANNEL_AGENT);
            f.delete();
            f = new File(LOG_PATH + HOST_AGENT);
            f.delete();
        }
    }

    public static String readLogFile(String name) {
        String line = null;
        try {
            FileReader input = new FileReader(LOG_PATH + name);
            BufferedReader br = new BufferedReader(input);
            while (line == null) {
                line = br.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return line;
    }
}
