package com.sirius.sdk.agent.n_wise;

import java.util.List;

public interface Ledger {

    public boolean addMessage(String msg, String tag);

    public List<String> getMessages(String tag);
}
