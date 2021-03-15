package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages;

import com.sirius.sdk.agent.aries_rfc.AriesProblemReport;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;


public class IssueProblemReport extends AriesProblemReport {

    static {
        Message.registerMessageClass(IssueProblemReport.class, BaseIssueCredentialMessage.PROTOCOL, "problem_report");
    }

    public static Builder<?> builder() {
        return new IssueProblemReportBuilder();
    }

    public IssueProblemReport(String message) {
        super(message);
    }

    public static abstract class Builder<B extends Builder<B>> extends AriesProblemReport.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }

        public IssueProblemReport build() {
            return new IssueProblemReport(generateJSON().toString());
        }
    }

    private static class IssueProblemReportBuilder extends Builder<IssueProblemReportBuilder> {
        @Override
        protected IssueProblemReportBuilder self() {
            return this;
        }
    }
}
