package org.deltaproject.manager.target;

import java.io.BufferedReader;

public interface TargetController {
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
