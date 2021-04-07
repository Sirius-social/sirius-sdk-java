package com.sirius.sdk.agent.consensus.simple.state_machines;

import com.sirius.sdk.agent.consensus.simple.messages.InitRequestLedgerMessage;
import com.sirius.sdk.agent.consensus.simple.messages.SimpleConsensusProblemReport;
import com.sirius.sdk.agent.microledgers.AbstractMicroledger;
import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.base.AbstractStateMachine;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractCoProtocol;
import com.sirius.sdk.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicroLedgerSimpleConsensus extends AbstractStateMachine {
    Pairwise.Me me;
    SimpleConsensusProblemReport problemReport = null;
    Map<String, Pairwise> cachedP2P = new HashMap<>();

    public MicroLedgerSimpleConsensus(Context context, Pairwise.Me me, int timeToLiveSec) {
        this.context = context;
        this.me = me;
        this.timeToLiveSec = timeToLiveSec;
    }

    public MicroLedgerSimpleConsensus(Context context, Pairwise.Me me) {
        this.context = context;
        this.me = me;
        this.timeToLiveSec = 60;
    }

    public Pairwise.Me getMe() {
        return me;
    }

    public SimpleConsensusProblemReport getProblemReport() {
        return problemReport;
    }

    private AbstractCoProtocol acceptors(List<Pairwise> theirs, String threadId) {
        //AbstractCoProtocol co = new CoProtocolThreadedTheirs
        return null;
    }

    /**
     *
     * @param ledgerName name of new microledger
     * @param participants list of DIDs that present pairwise list of the Microledger relationships
     *                 (Assumed DIDs are public or every participant has relationship with each other via pairwise)
     * @param genesis genesis block of the new microledger if all participants accept transaction
     * @return
     */
    public Pair<Boolean, AbstractMicroledger> initMicroledger(String ledgerName, List<String> participants, List<Transaction> genesis) {
        try {
            bootstrap(participants);
        } catch (SiriusValidationError siriusValidationError) {
            siriusValidationError.printStackTrace();
        }
        return null;
    }

    private void bootstrap(List<String> patricipants) throws SiriusValidationError {
        for (String did : patricipants) {
            if (!did.equals(me.getDid())) {
                if (!cachedP2P.containsKey(did)) {
                    Pairwise p = context.getPairwiseList().loadForDid(did);
                    if (p == null) {
                        throw new SiriusValidationError("Unknown pairwise for DID: " + did);
                    }
                    cachedP2P.put(did, p);
                }
            }
        }
    }

    public Pair<Boolean, AbstractMicroledger> acceptMicroledger(Pairwise leader, InitRequestLedgerMessage propose) {
        return null;
    }

    @Override
    public List<String> protocols() {
        return null;
    }
}
