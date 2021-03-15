package com.sirius.sdk.agent.aries_rfc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AriesProblemReport extends AriesProtocolMessage {

    public AriesProblemReport(String message) {
        super(message);
    }

    public static abstract class Builder<B extends Builder<B>> extends AriesProtocolMessage.Builder<B> {
        String problemCode = null;
        String explain = null;
        String threadId = null;

        public B setProblemCode(String problemCode) {
            this.problemCode = problemCode;
            return self();
        }

        public B setExplain(String explain) {
            this.explain = explain;
            return self();
        }

        public B setThreadId(String threadId) {
            this.threadId = threadId;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            String id = jsonObject.optString("id");

            if (problemCode != null) {
                jsonObject.put("problem-code", problemCode);
            }

            if (explain != null) {
                jsonObject.put("explain", explain);
            }

            if (threadId != null) {
                JSONObject thread = jsonObject.optJSONObject(THREAD_DECORATOR);
                if (thread == null)
                    thread  =new JSONObject();
                thread.put("thid", thread);
                jsonObject.put(THREAD_DECORATOR, thread);
            }

            return jsonObject;
        }
    }
}
