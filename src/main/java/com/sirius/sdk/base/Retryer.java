package com.sirius.sdk.base;

import com.sirius.sdk.utils.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Retryer {

    private static final Logger log = Logger.getLogger(Retryer.class.getName());

    private final Policy policy;

    private final Context context;

    public Retryer(Policy policy, Context context) {
        this.policy = policy;
        this.context = context;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <R> R retry(Callable<R> callable) throws Exception {
        while (policy.shouldRetry(context)) {
            try {
                return callable.call();
            } catch (Exception e) {
                context.setInvocationTimestamp(Instant.now());
                log.log(Level.WARNING, "try " + context.getInvocationNumber() + " failed: " + e.getMessage());
                context.incrementInvocationNumber();
                context.setResult(Pair.pair(null, e));
                Duration delay = policy.nextRunDelay(context);
                if (delay != null && !delay.isZero()) {
                    try {
                        Thread.sleep(delay.toMillis());
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        }
        if (context.getResult().second != null) {
            throw context.getResult().second;
        } else {
            return (R) context.getResult().first;
        }

    }

    public static class Context {

        private final AtomicInteger invocationNumber = new AtomicInteger(0);

        private final AtomicReference<Instant> invocationTimestamp = new AtomicReference<>();

        private final AtomicReference<Pair<Object, Exception>> result = new AtomicReference<>();

        public Pair<Object, Exception> getResult() {
            return result.get();
        }

        public void setResult(Pair<Object, Exception> result) {
            this.result.set(result);
        }

        public int getInvocationNumber() {
            return invocationNumber.get();
        }

        public int incrementInvocationNumber() {
            return invocationNumber.incrementAndGet();
        }

        public Instant getInvocationTimestamp() {
            return invocationTimestamp.get();
        }

        public void setInvocationTimestamp(Instant invocationTimestamp) {
            this.invocationTimestamp.set(invocationTimestamp);
        }
    }

    public interface Policy {

        int DEFAULT_RETRY_COUNT = 3;
        Duration DEFAULT_DELAY = Duration.ZERO;

        default boolean shouldRetry(Context context) {
            return context.getInvocationNumber() < DEFAULT_RETRY_COUNT;
        }

        default Duration nextRunDelay(Context context) {
            return DEFAULT_DELAY;
        }

        class Builder {

            private int maxAttempts = Policy.DEFAULT_RETRY_COUNT;
            private Duration waitDuration = Policy.DEFAULT_DELAY;
            private Function<Integer, Duration> intervalFunction;

            public Builder maxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
                return this;
            }

            public Builder waitDuration(Duration waitDuration) {
                this.waitDuration = waitDuration;
                this.intervalFunction = null;
                return this;
            }

            public Builder intervalFunction(Function<Integer, Duration> intervalFunction) {
                this.intervalFunction = intervalFunction;
                this.waitDuration = null;
                return this;
            }

            private Policy build() {
                return new Policy() {
                    @Override
                    public boolean shouldRetry(Context context) {
                        return context.getInvocationNumber() < maxAttempts;
                    }

                    @Override
                    public Duration nextRunDelay(Context context) {
                        if (waitDuration != null) {
                            return waitDuration;
                        } else if (intervalFunction != null) {
                            return intervalFunction.apply(context.getInvocationNumber());
                        } else {
                            return Policy.super.nextRunDelay(context);
                        }
                    }
                };
            }

        }
    }

    public static class Builder {

        private final Policy.Builder retryPolicy = new Policy.Builder();

        public Builder maxAttempts(int maxAttempts) {
            retryPolicy.maxAttempts(maxAttempts);
            return this;
        }

        public Builder waitDuration(Duration waitDuration) {
            retryPolicy.waitDuration(waitDuration);
            return this;
        }

        public Builder intervalFunction(Function<Integer, Duration> intervalFunction) {
            retryPolicy.intervalFunction(intervalFunction);
            return this;
        }

        public Retryer build() {
            return new Retryer(retryPolicy.build(), new Context());
        }

    }


}
