package com.sirius.sdk.base;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class RetryerTest {

    @Test
    public void retryResult() throws Exception {
        String expected = "test";
        Retryer retryer = Retryer.builder().build();
        String actual = retryer.retry(() -> "test");
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void retryCount() {
        int retryCount = 5;
        AtomicInteger actualCount = new AtomicInteger(0);
        Retryer retryer = Retryer.builder().maxAttempts(retryCount).build();
        try {
            retryer.retry(() -> {
                int currentTry = actualCount.getAndIncrement();
                throw new TestRetryException("test call " + currentTry);
            });
        } catch (Exception e) {
            Assert.assertTrue("Unexpected exception was thrown", e instanceof TestRetryException);
            Assert.assertEquals("wrong retry count", retryCount, actualCount.get());
        }

    }

    public static class TestRetryException extends RuntimeException {

        public TestRetryException(String message) {
            super(message);
        }
    }

}