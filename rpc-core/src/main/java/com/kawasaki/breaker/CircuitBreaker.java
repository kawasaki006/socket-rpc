package com.kawasaki.breaker;

import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {
    private State state = State.CLOSED;
    private final AtomicInteger failCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger total = new AtomicInteger(0);

    private final int failThreshold;
    private final double successRateInHalfOpen;
    private final long windowTime;

    private long lastFailTime = 0;

    public CircuitBreaker(int failThreshold, double successRateInHalfOpen, long windowTime) {
        this.failThreshold = failThreshold;
        this.successRateInHalfOpen = successRateInHalfOpen;
        this.windowTime = windowTime;
    }

    public synchronized boolean canReq() {
        if (state == State.CLOSED) {
            return true;
        } else if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailTime <= windowTime) {
                // still in broken time
                return false;
            }

            state = State.HALF_OPEN;
            resetCounts();
            return true;
        } else if (state == State.HALF_OPEN) {
            total.incrementAndGet();
            return true;
        } else {
            throw new IllegalStateException("Circuit breaker state error");
        }
    }

    public synchronized void success() {
        if (state != State.HALF_OPEN) {
            resetCounts();
            return;
        }

        // in half open state
        successCount.incrementAndGet();
        if (successCount.get() >= successRateInHalfOpen * total.get()) {
            state = State.CLOSED;
            resetCounts();
        }
    }

    public synchronized void fail() {
        failCount.incrementAndGet();
        lastFailTime = System.currentTimeMillis();

        if (state == State.HALF_OPEN) {
            // open immediately if a request failed in half open state
            state = State.OPEN;
            return;
        }

        if (failCount.get() >= failThreshold) {
            // if fail count exceeds fail threshold, break
            state = State.OPEN;
        }
    }

    private void resetCounts() {
        total.set(0);
        failCount.set(0);
        successCount.set(0);
    }

    enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }
}
