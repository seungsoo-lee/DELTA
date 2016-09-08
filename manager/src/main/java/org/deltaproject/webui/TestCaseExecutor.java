package org.deltaproject.webui;

import org.deltaproject.manager.core.AttackConductor;

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
                conductor.replayKnownAttack();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
