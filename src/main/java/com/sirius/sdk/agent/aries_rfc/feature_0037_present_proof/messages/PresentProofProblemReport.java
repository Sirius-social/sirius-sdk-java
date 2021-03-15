package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages;

import com.sirius.sdk.agent.aries_rfc.AriesProblemReport;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

public class PresentProofProblemReport extends AriesProblemReport {
    static {
        Message.registerMessageClass(PresentProofProblemReport.class, BasePresentProofMessage.PROTOCOL, "problem_report");
    }

    public static Builder<?> builder() {
        return new PresentProofProblemReportBuilder();
    }

    public PresentProofProblemReport(String message) {
        super(message);
    }

    public static abstract class Builder<B extends Builder<B>> extends AriesProblemReport.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }

        public PresentProofProblemReport build() {
            return new PresentProofProblemReport(generateJSON().toString());
        }
    }

    private static class PresentProofProblemReportBuilder extends Builder<PresentProofProblemReportBuilder> {
        @Override
        protected PresentProofProblemReportBuilder self() {
            return this;
        }
    }
}
