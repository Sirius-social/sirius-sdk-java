package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.agent.aries_rfc.AriesProblemReport;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

public class SimpleConsensusProblemReport extends AriesProblemReport {

    static {
        Message.registerMessageClass(SimpleConsensusProblemReport.class, SimpleConsensusMessage.PROTOCOL, "problem_report");
    }

    public static Builder<?> builder() {
        return new SimpleConsensusProblemReportBuilder();
    }

    public SimpleConsensusProblemReport(String message) {
        super(message);
    }

    public static abstract class Builder<B extends Builder<B>> extends AriesProblemReport.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }

        public SimpleConsensusProblemReport build() {
            return new SimpleConsensusProblemReport(generateJSON().toString());
        }
    }

    private static class SimpleConsensusProblemReportBuilder extends Builder<SimpleConsensusProblemReportBuilder> {
        @Override
        protected SimpleConsensusProblemReportBuilder self() {
            return this;
        }
    }

}
