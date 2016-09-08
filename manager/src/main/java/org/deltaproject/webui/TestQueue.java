package org.deltaproject.webui;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by changhoon on 7/8/16.
 */
public class TestQueue {

    private static final ConcurrentHashMap<Integer, TestCase> TEST_CASE_QUEUE
            = new ConcurrentHashMap<>();

    private static final ConcurrentLinkedQueue<Integer> INDEX_QUEUE
            = new ConcurrentLinkedQueue<>();

    private static AtomicInteger q_index = new AtomicInteger(0);

    private static final TestQueue instance = new TestQueue();

    protected TestQueue() {
    }

    public static TestQueue getInstance() {
        return instance;
    }

    public static Collection<TestCase> getTestcases() {
        return TEST_CASE_QUEUE.values();
    }

    public static Integer push(TestCase testcase) {
        int index = q_index.incrementAndGet();
        testcase.setIndex(index);
        TEST_CASE_QUEUE.put(index, testcase);
        INDEX_QUEUE.add(index);
        return index;
    }

    public static TestCase getNext() {
        return TEST_CASE_QUEUE.get(INDEX_QUEUE.poll());
    }

    public static boolean isEmpty() {
        return TEST_CASE_QUEUE.isEmpty();
    }

    public static void update(Integer index, TestCase testcase) {
        TEST_CASE_QUEUE.replace(index, testcase);
    }

}
