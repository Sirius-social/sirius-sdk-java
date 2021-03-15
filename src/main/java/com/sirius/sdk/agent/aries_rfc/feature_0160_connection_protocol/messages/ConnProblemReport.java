package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages;

import com.sirius.sdk.agent.aries_rfc.AriesProblemReport;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

public class ConnProblemReport extends AriesProblemReport {

    static {
        Message.registerMessageClass(ConnProblemReport.class, ConnProtocolMessage.PROTOCOL, "problem_report");
    }

    public static Builder<?> builder() {
        return new ConnProblemReportBuilder();
    }

    public ConnProblemReport(String message) {
        super(message);
    }

    public static abstract class Builder<B extends Builder<B>> extends AriesProblemReport.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }

        public ConnProblemReport build() {
            return new ConnProblemReport(generateJSON().toString());
        }
    }

    private static class ConnProblemReportBuilder extends Builder<ConnProblemReportBuilder> {
        @Override
        protected ConnProblemReportBuilder self() {
            return this;
        }
    }
}
