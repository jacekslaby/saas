package com.j9soft.saas.alarms.service;

import com.j9soft.saas.alarms.dao.RequestDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Container for state of one call to saveNewRequest or createRequestsWithArray, i.e. state of a batched publish.
 */
public class PublishTask {

    final Lock lock = new ReentrantLock();
    final Condition resultsAreReady = lock.newCondition();

    private int numberOfcompletedRequests;
    private List<Exception> results;

    public PublishTask() {
        this.numberOfcompletedRequests = 0;
        this.results = new ArrayList<Exception>();
    }

    public RequestDao.Callback createCallback() {
        int index;

        lock.lock();
        try {
            index = results.size();

            // Dummy data, which just helps us keep track of the index & ensures there's a slot for
            // storage when we get the callback
            results.add(null);
        } finally {
            lock.unlock();
        }

        return new RequestDao.Callback() {
            @Override
            public void onCompletion(Exception exception) {
                PublishTask.this.onCompletion(index, exception);
            }
        };
    }

    /**
     * Invoked by Dao layer when one request is completed.
     * @param exception null if published successfully
     */
    public void onCompletion(int messageNum, Exception exception) {

        lock.lock();
        try {
            results.set(messageNum, exception);
            numberOfcompletedRequests += 1;

            if (numberOfcompletedRequests == results.size()) {
                resultsAreReady.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Waits until all requests are completed.
     * @return array containing null for requests published successfully
     * @throws InterruptedException
     */
    public Exception[] getResults() throws InterruptedException {
        lock.lock();
        try {
            while (numberOfcompletedRequests < results.size()) {
                resultsAreReady.await();
            }
            return this.results.toArray(new Exception[0]);
        } finally {
            lock.unlock();
        }
    }
}
