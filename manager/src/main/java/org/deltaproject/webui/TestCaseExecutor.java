package org.deltaproject.webui;

import org.deltaproject.manager.core.AttackConductor;

import static org.deltaproject.webui.TestCase.Status.*;
/**
 * Created by changhoon on 9/8/16.
 */
public class TestCaseExecutor extends Thread {

    private AttackConductor conductor;
    private TestQueue queue = TestQueue.getInstance();

    public TestCaseExecutor(AttackConductor conductor) {
        this.conductor = conductor;
    }

    @Override
    public void run() {

        while(true) {
            if (!queue.isEmpty()) {
                TestCase test = queue.getNext();
                try {
                    test.setStatus(RUNNING);
                    conductor.executeTestCase(test);
                    test.setStatus(COMPLETE);
                } catch (InterruptedException e) {
                    test.setStatus(UNAVAILABLE);
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
