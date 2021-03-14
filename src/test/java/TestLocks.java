import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;


public class TestLocks {

    ConfTest confTest;

    public static List<String> generateRandomResources(int size) {
        List<String> res = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            res.add("resource-" + UUID.randomUUID().toString());
        }
        return res;
    }

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testSameAccount() throws InterruptedException {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams params = testSuite.getAgentParams("agent1");

        Agent session1 = new Agent(
                params.getServerAddress(),
                params.getCredentials().getBytes(StandardCharsets.UTF_8),
                params.getConnection(),
                5);

        Agent session2 = new Agent(
                params.getServerAddress(),
                params.getCredentials().getBytes(StandardCharsets.UTF_8),
                params.getConnection(),
                5);

        session1.open();
        session2.open();

        try {
            // check locking OK
            List<String> resources = generateRandomResources(100);
            Pair<Boolean, List<String>> okBusy = session1.acquire(resources, 5);
            try {
                Assert.assertTrue(okBusy.first);
                okBusy = session2.acquire(resources, 1);
                Assert.assertFalse(okBusy.first);
                Assert.assertEquals(new HashSet<>(okBusy.second), new HashSet<>(resources));
            } finally {
                session1.release();
            }

            // check session ok may lock after explicitly release
            okBusy = session2.acquire(resources, 1);
            Assert.assertTrue(okBusy.first);
            // Check after timeout
            resources = generateRandomResources(100);
            double timeoutSec = 5.0;
            okBusy = session1.acquire(resources, timeoutSec);
            Assert.assertTrue(okBusy.first);
            okBusy = session2.acquire(resources, timeoutSec);
            Assert.assertFalse(okBusy.first);
            Thread.sleep((long) (timeoutSec + 1) * 1000);
            okBusy = session2.acquire(resources, timeoutSec);
            Assert.assertTrue(okBusy.first);
        } finally {
            session1.close();
            session2.close();
        }
    }

    @Test
    public void testLockMultipleTime() {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams params = testSuite.getAgentParams("agent1");

        Agent session1 = new Agent(
                params.getServerAddress(),
                params.getCredentials().getBytes(StandardCharsets.UTF_8),
                params.getConnection(),
                5);

        Agent session2 = new Agent(
                params.getServerAddress(),
                params.getCredentials().getBytes(StandardCharsets.UTF_8),
                params.getConnection(),
                5);

        session1.open();
        session2.open();

        try {
            // check locking OK
            double timeout = 5.0;
            List<String> resources1 = generateRandomResources(100);
            Pair<Boolean, List<String>> okBusy = session1.acquire(resources1, timeout);
            Assert.assertTrue(okBusy.first);

            List<String> resources2 = generateRandomResources(100);
            okBusy = session1.acquire(resources2, timeout);
            Assert.assertTrue(okBusy.first);

            // session1 must unlock previously locked resources on new acquire call
            okBusy = session2.acquire(resources1, timeout);
            Assert.assertTrue(okBusy.first);
        } finally {
            session1.close();
            session2.close();
        }
    }

    @Test
    public void testDifferentAccounts() {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams params1 = testSuite.getAgentParams("agent1");
        AgentParams params2 = testSuite.getAgentParams("agent2");

        Agent session1 = new Agent(
                params1.getServerAddress(),
                params1.getCredentials().getBytes(StandardCharsets.UTF_8),
                params1.getConnection(),
                5);

        Agent session2 = new Agent(
                params2.getServerAddress(),
                params2.getCredentials().getBytes(StandardCharsets.UTF_8),
                params2.getConnection(),
                5);

        session1.open();
        session2.open();

        try {
            List<String> resources = generateRandomResources(1);
            Pair<Boolean, List<String>> okBusy1 = session1.acquire(resources, 10);
            Pair<Boolean, List<String>> okBusy2 = session2.acquire(resources, 10);
            Assert.assertTrue(okBusy1.first);
            Assert.assertTrue(okBusy2.first);
        } finally {
            session1.close();
            session2.close();
        }
    }

}
