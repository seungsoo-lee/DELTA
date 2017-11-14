package org.deltaproject.manager.handler;

import java.io.BufferedReader;

public interface ControllerHandler {
    boolean createController();

    Process getProc();

    void killController();

    boolean installAppAgent();

    String getType();

    String getVersion();

    String getPath();

    int getPID();

    BufferedReader getStdOut();
}
