package org.deltaproject.webui;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.deltaproject.webui.TestCase.Status.COMPLETE;
import static org.deltaproject.webui.TestCase.Status.RUNNING;

/**
 * A test queue of DELTA GUI.
 * Created by Changhoon on 7/8/16.
 */
public class TestQueue {

    private static final ConcurrentHashMap<Integer, TestCase> TEST_CASE_QUEUE
            = new ConcurrentHashMap<>();

    private static final ConcurrentLinkedQueue<Integer> INDEX_QUEUE
            = new ConcurrentLinkedQueue<>();

    private static final TestQueue INSTANCE = new TestQueue();

    private static AtomicInteger qIndex = new AtomicInteger(0);

    private TestCase runningTestCase;

    protected TestQueue() {
    }

    public static TestQueue getInstance() {
        return INSTANCE;
    }

    public static Collection<TestCase> getTestcases() {
        return TEST_CASE_QUEUE.values();
    }

    public static Integer push(TestCase testcase) {
        int index = qIndex.incrementAndGet();
        testcase.setIndex(index);
        TEST_CASE_QUEUE.put(index, testcase);
        INDEX_QUEUE.add(index);
        return index;
    }

    public static void remove(Integer index) {
        INDEX_QUEUE.remove(index);
        TEST_CASE_QUEUE.remove(index);
    }

    public static TestCase getNext() {
        return TEST_CASE_QUEUE.get(INDEX_QUEUE.poll());
    }

    public static boolean isEmpty() {
        return INDEX_QUEUE.isEmpty();
    }

    public static TestCase get(Integer index) {
        return TEST_CASE_QUEUE.get(index);
    }

    public static void update(Integer index, TestCase testcase) {
        TEST_CASE_QUEUE.replace(index, testcase);
    }

    public void setRunningTestCase(TestCase runningTestCase) {
        this.runningTestCase = runningTestCase;
        runningTestCase.setStatus(RUNNING);
    }

    public void unsetRunningTestCase(TestCase runningTestCase) {
        this.runningTestCase.setStatus(COMPLETE);
        this.runningTestCase = null;
    }

    public TestCase getRunningTestCase() {
        return runningTestCase;
    }

    public void stopRunningTestCase() {

    }
}
