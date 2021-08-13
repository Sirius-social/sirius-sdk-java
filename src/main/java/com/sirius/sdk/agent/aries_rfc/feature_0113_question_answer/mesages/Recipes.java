package com.sirius.sdk.agent.aries_rfc.feature_0113_question_answer.mesages;

import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.CoProtocolThreadedP2P;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class Recipes {

    public static AnswerMessage askAndWaitAnswer(Context context, QuestionMessage question, Pairwise to) {
        int ttlSec = 60;
        if (question.getExpiresTime() != null) {
            ttlSec = (int) ChronoUnit.SECONDS.between(ZonedDateTime.now(), question.getExpiresTime());
            if (ttlSec < 0)
                ttlSec = 60;
        }
        try (CoProtocolThreadedP2P cp = new CoProtocolThreadedP2P(context, question.getId(), to, ttlSec)) {
            Pair<Boolean, Message> res = cp.sendAndWait(question);
            if (res.first) {
                if (res.second instanceof AnswerMessage) {
                    return (AnswerMessage) res.second;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void makeAnswer(Context context, String response, QuestionMessage question, Pairwise to) {
        AnswerMessage answer = AnswerMessage.builder().setResponse(response).build();
        answer.setThreadId(question.getId());
        answer.setOutTime();
        context.sendTo(answer, to);
    }
}
