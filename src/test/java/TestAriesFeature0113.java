import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.aries_rfc.feature_0113_question_answer.mesages.AnswerMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0113_question_answer.mesages.QuestionMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0113_question_answer.mesages.Recipes;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
import helpers.ConfTest;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TestAriesFeature0113 {
    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testSane() throws InterruptedException {
        CloudAgent requesterAgent = confTest.getAgent("agent1");
        CloudAgent responderAgent = confTest.getAgent("agent2");
        requesterAgent.open();
        responderAgent.open();
        Pairwise requester2responder = confTest.getPairwise(requesterAgent, responderAgent);
        Pairwise responder2requester = confTest.getPairwise(responderAgent, requesterAgent);

        AgentParams requesterParams = confTest.getSuiteSingleton().getAgentParams("agent1");
        Thread requesterThread = new Thread(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(requesterParams.getServerAddress()).
                    setCredentials(requesterParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
                    setP2p(requesterParams.getConnection()).
                    build()) {
                QuestionMessage question = QuestionMessage.builder().
                        setValidResponses(Arrays.asList("Yes", "No")).
                        setQuestionText("Test question").
                        setQuestionDetail("Question detail").
                        setTtl(40).
                        build();
                AnswerMessage answer = Recipes.askAndWaitAnswer(context, question, requester2responder);
                Assert.assertNotNull(answer);
                Assert.assertEquals(answer.getResponse(), "Yes");
            }
        });
        requesterThread.start();

        Thread.sleep(100);

        AgentParams responderParams = confTest.getSuiteSingleton().getAgentParams("agent2");
        Thread responderThread = new Thread(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(responderParams.getServerAddress()).
                    setCredentials(responderParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
                    setP2p(responderParams.getConnection()).
                    build()) {
                Listener listener = context.subscribe();
                while (true) {
                    Event e = listener.listen().timeout(60, TimeUnit.SECONDS).blockingNext().iterator().next();
                    if (e.message() instanceof QuestionMessage) {
                        QuestionMessage question = (QuestionMessage) e.message();
                        Recipes.makeAnswer(context, "Yes", question, e.getPairwise());
                        return;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        responderThread.start();

        requesterThread.join(60000);
        responderThread.join(60000);
    }
}
