package org.deltaproject.webui;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by changhoon on 7/8/16.
 */
public class TestQueue {

    private static final ConcurrentLinkedQueue<TestCase> TEST_CASE_QUEUE
            = new ConcurrentLinkedQueue<>();

    private static final ConcurrentHashMap<Integer, TestCase> TEST_HISTORY
            = new ConcurrentHashMap<>();

    public static ConcurrentLinkedQueue<TestCase> getQueue() {
        return TEST_CASE_QUEUE;
    }

    public static ConcurrentHashMap<Integer, TestCase> getHistory() {
        return TEST_HISTORY;
    }
}
