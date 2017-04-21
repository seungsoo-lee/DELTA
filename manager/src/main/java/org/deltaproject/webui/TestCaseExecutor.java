package org.deltaproject.webui;

import org.deltaproject.manager.core.AttackConductor;
import org.deltaproject.manager.utils.AgentLogger;

import static org.deltaproject.webui.TestCase.Status.*;

/**
 * A executor for queued test case.
 * Created by Changhoon on 9/8/16.
 */
public class TestCaseExecutor extends Thread {

    private AttackConductor conductor;
    private TestQueue queue = TestQueue.getInstance();
    private boolean running;

    public TestCaseExecutor(AttackConductor conductor) {
        this.conductor = conductor;
        running = true;
    }

    @Override
    public void run() {
        while (running) {
            if (!queue.isEmpty()) {
                TestCase test = queue.getNext();
                try {
                    test.setStatus(RUNNING);
                    conductor.executeTestCase(test);
                    test.setStatus(COMPLETE);
                    AgentLogger.stopAllLogger();
                } catch (InterruptedException e) {
                    test.setStatus(UNAVAILABLE);
                }
            }

            try {
                Thread.sleep(999);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
}
