package com.sirius.sdk.agent.aries_rfc;

public class AriesProblemReport {
}

/*
class AriesProblemReport(AriesProtocolMessage):

        NAME = 'problem_report'

        def __init__(self, problem_code: str=None, explain: str=None, thread_id: str=None, *args, **kwargs):
        super().__init__(*args, **kwargs)
        if problem_code:
        self['problem-code'] = problem_code
        if explain:
        self['explain'] = explain
        if thread_id is not None:
        thread = self.get(THREAD_DECORATOR, {})
        thread['thid'] = thread_id
        self[THREAD_DECORATOR] = thread

@property
    def problem_code(self) -> str:
            return self.get('problem-code', '')

@property
    def explain(self) -> str:
            return self.get('explain', '')

*/
