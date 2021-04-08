package com.sirius.sdk.agent.consensus.simple.state_machines;

import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.consensus.simple.messages.InitRequestLedgerMessage;
import com.sirius.sdk.agent.consensus.simple.messages.InitResponseLedgerMessage;
import com.sirius.sdk.agent.consensus.simple.messages.SimpleConsensusProblemReport;
import com.sirius.sdk.agent.microledgers.AbstractMicroledger;
import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.base.AbstractStateMachine;
import com.sirius.sdk.errors.StateMachineTerminatedWithError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolThreadedTheirs;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class MicroLedgerSimpleConsensus extends AbstractStateMachine {
    //problem codes
    static final String REQUEST_NOT_ACCEPTED = "request_not_accepted";
    static final String REQUEST_PROCESSING_ERROR = "request_processing_error";
    static final String RESPONSE_NOT_ACCEPTED = "response_not_accepted";
    static final String RESPONSE_PROCESSING_ERROR = "response_processing_error";


    Logger log = Logger.getLogger(Inviter.class.getName());
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

    private CoProtocolThreadedTheirs acceptors(List<Pairwise> theirs, String threadId) {
        CoProtocolThreadedTheirs co = new CoProtocolThreadedTheirs(this.context, threadId, theirs, null,60);
        return co;
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
            List<Pairwise> relationships = new ArrayList<>(this.cachedP2P.values());
            try (CoProtocolThreadedTheirs co = acceptors(relationships, "simple-consensus-init-" + UUID.randomUUID())) {
                log.info("0% - Create ledger " + ledgerName);
                Pair<AbstractMicroledger, List<Transaction>> t1 = context.getMicrolegders().create(ledgerName, genesis);
                AbstractMicroledger ledger = t1.first;
                log.info("Ledger creation terminated successfully");
                try {
                    initMicroledgerInternal(co, ledger, participants, genesis);
                } catch (Exception ex) {

                }

            }
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

    private Pair<Boolean, List<String>> acquire(List<String> names, Double lockTimeoutSec, Double enterTimeoutSec) {
        String NAMESPACE = "ledgers";
        names = new ArrayList<>(new HashSet<String>(names));
        for (int i = 0; i < names.size(); i++) {
            names.set(i, NAMESPACE + "/" + names.get(i));
        }
        Pair<Boolean, List<String>> t1 = this.context.acquire(names, lockTimeoutSec, enterTimeoutSec);
        List<String> lockedLedgers = new ArrayList<>();
        for (String s : t1.second) {
            lockedLedgers.add(s.split("/")[1]);
        }
        return new Pair<>(t1.first, lockedLedgers);
    }

    private void release() {
        context.release();
    }

    private void initMicroledgerInternal(CoProtocolThreadedTheirs co, AbstractMicroledger ledger, List<String> participants, List<Transaction> genesis) throws StateMachineTerminatedWithError {
        Pair<Boolean, List<String>> t1 = acquire(Arrays.asList(ledger.name()), (double) this.timeToLiveSec, null);
        boolean success = t1.first;
        List<String> busy = t1.second;
        if (!success) {
            throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, "Preparing: Ledgers are locked by other state-machine", false);
        }
        try {
            // ============= STAGE 1: PROPOSE =================
            InitRequestLedgerMessage propose = InitRequestLedgerMessage.builder().
                    setTimeoutSec(this.timeToLiveSec).
                    setLedgerName(ledger.name()).
                    setGenesis(genesis).
                    setRootHash(ledger.rootHash()).
                    setParticipants(participants).
                    build();
            propose.addSignature(context.getCrypto(), this.me);
            InitResponseLedgerMessage requestCommit = InitResponseLedgerMessage.builder().build();
            requestCommit.assignFrom(propose);

            log.info("20% - Send propose");

            // Switch to await transaction acceptors action

            List<CoProtocolThreadedTheirs.SendAndWaitResult> results = co.sendAndWait(propose);
            log.info("30% - Received responses from all acceptors");

            List<String> erroredAcceptorsDid = new ArrayList<>();
            for (CoProtocolThreadedTheirs.SendAndWaitResult r : results) {
                if (!r.success)
                    erroredAcceptorsDid.add(r.pairwise.getTheir().getDid());
            }
            if (!erroredAcceptorsDid.isEmpty()) {
                throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR, "Stage-1: Participants unreachable");
            }

            log.info("40% - Validate responses");
            for (CoProtocolThreadedTheirs.SendAndWaitResult r : results) {
                if (r.message instanceof InitResponseLedgerMessage) {
                    InitResponseLedgerMessage response = (InitResponseLedgerMessage) r.message;
                    response.validate();
                    response.checkSignatures(context.getCrypto(), r.pairwise.getTheir().getDid());
                    JSONObject signature = response.signature(r.pairwise.getTheir().getDid());
                    requestCommit.signatures().put(signature);
                } else if (r.message instanceof SimpleConsensusProblemReport) {
                    SimpleConsensusProblemReport response = (SimpleConsensusProblemReport) r.message;
                    throw new StateMachineTerminatedWithError(response.getProblemCode(), response.getExplain());
                }
            }

            // ============= STAGE 2: COMMIT ============
            log.info("60% - Send commit request");
            results = co.sendAndWait(propose);
            log.info("70% - Received commit responses");
            erroredAcceptorsDid = new ArrayList<>();
            for (CoProtocolThreadedTheirs.SendAndWaitResult r : results) {
                if (!r.success)
                    erroredAcceptorsDid.add(r.pairwise.getTheir().getDid());
            }
            if (!erroredAcceptorsDid.isEmpty()) {
                throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR, "Stage-2: Participants unreachable");
            }

            log.info("80% - Validate commit responses from acceptors");
            for (CoProtocolThreadedTheirs.SendAndWaitResult r : results) {
                if (r.message instanceof SimpleConsensusProblemReport) {
                    SimpleConsensusProblemReport response = (SimpleConsensusProblemReport) r.message;
                    throw new StateMachineTerminatedWithError(RESPONSE_PROCESSING_ERROR,
                            "Participant DID: " + r.pairwise.getTheir().getDid() + " declined operation with error: " + response.getExplain());
                }
            }


        } catch (SiriusValidationError siriusValidationError) {
            siriusValidationError.printStackTrace();
        } finally {
            release();
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
