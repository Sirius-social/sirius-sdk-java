package com.sirius.sdk.agent.consensus.simple.state_machines;

import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.consensus.simple.messages.*;
import com.sirius.sdk.agent.microledgers.AbstractMicroledger;
import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.base.AbstractStateMachine;
import com.sirius.sdk.errors.StateMachineTerminatedWithError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolThreadedP2P;
import com.sirius.sdk.hub.coprotocols.CoProtocolThreadedTheirs;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    private CoProtocolThreadedP2P leader(Pairwise their, String threadId, int timeToLiveSec) {
        CoProtocolThreadedP2P co = new CoProtocolThreadedP2P(context, threadId, their, null, timeToLiveSec);
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
                    log.info("100% - All participants accepted ledger creation");
                    return new Pair<>(true, ledger);
                } catch (StateMachineTerminatedWithError ex) {
                    log.info("100% - Terminated with error. Problem code: " + ex.getProblemCode() + " Explain: " + ex.getExplain());
                    this.problemReport = SimpleConsensusProblemReport.builder().
                            setProblemCode(ex.getProblemCode()).
                            setExplain(ex.getExplain()).
                            build();
                    if (ex.isNotify()) {
                        co.send(this.problemReport);
                    }
                    return new Pair<>(false, null);
                } catch (Exception ex) {
                    log.info("100% - Terminated with error");
                    ex.printStackTrace();
                }
            }
        } catch (SiriusValidationError siriusValidationError) {
            log.info("100% - Terminated with error");
            siriusValidationError.printStackTrace();
        }
        return null;
    }

    public Pair<Boolean, AbstractMicroledger> acceptMicroledger(Pairwise leader, InitRequestLedgerMessage propose) {
        if (!propose.getParticipants().contains(this.me.getDid()))
            throw new SiriusContextError("Invalid state machine initialization");
        int timeToLive = this.timeToLiveSec;
        if (propose.getTimeoutSec() > 0)
            timeToLive = propose.getTimeoutSec();
        try {
            bootstrap(propose.getParticipants());
        } catch (SiriusValidationError siriusValidationError) {
            log.info("100% - Terminated with error");
            siriusValidationError.printStackTrace();
            return new Pair<>(false, null);
        }

        try (CoProtocolThreadedP2P co = leader(leader, propose.getThreadId(), timeToLive)) {
            String ledgerName = propose.getLedger().optString("name", null);
            try {
                if (ledgerName == null) {
                    throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR, "Ledger name is Empty!");
                }
                for (String theirDid : propose.getParticipants()) {
                    if (!theirDid.equals(this.me.getDid())) {
                        if (!this.cachedP2P.containsKey(theirDid)) {
                            throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR,
                                    "Pairwise for DID: " + theirDid +" does not exists!");
                        }
                    }
                }
                log.info("0% - Start ledger " + ledgerName + " creation process");
                AbstractMicroledger ledger = acceptMicroledgerInternal(co, leader, propose, timeToLive);
                log.info("100% - Ledger creation terminated successfully");
                return new Pair<>(true, ledger);
            } catch (StateMachineTerminatedWithError ex) {
                this.problemReport = SimpleConsensusProblemReport.builder().
                        setProblemCode(ex.getProblemCode()).
                        setExplain(ex.getExplain()).
                        build();
                log.info("100% - Terminated with error. Problem code: " + ex.getProblemCode() + " Explain: " + ex.getExplain());
                if (ex.isNotify()) {
                    co.send(this.problemReport);
                    return new Pair<>(false, null);
                }
            }
        }

        return new Pair<>(false, null);
    }

    /**
     *
     * @param ledger Microledger instance to operate with
     * @param participants list of DIDs that present pairwise list of the Microledger relationships
     *                 (Assumed DIDs are public or every participant has relationship with each other via pairwise)
     * @param transactions transactions to commit
     * @return
     */
    public Pair<Boolean, List<Transaction>> commit(AbstractMicroledger ledger, List<String> participants, List<Transaction> transactions) {
        try {
            bootstrap(participants);
        } catch (SiriusValidationError siriusValidationError) {
            siriusValidationError.printStackTrace();
            return new Pair<>(false, null);
        }

        List<Pairwise> relationships = new ArrayList<>(this.cachedP2P.values());
        try (CoProtocolThreadedTheirs co = acceptors(relationships, "simple-consensus-commit-" + UUID.randomUUID())) {
            try {
                log.info("0% - Start committing " + transactions.size() + " transactions");
                List<Transaction> txns = commitInternal(co, ledger, transactions, participants);
                log.info("100% - Commit operation was accepted by all participants");
                return new Pair<>(true, txns);
            } catch (StateMachineTerminatedWithError ex) {
                ledger.resetUncommitted();
                log.info("Reset uncommitted");
                this.problemReport = SimpleConsensusProblemReport.builder().
                        setProblemCode(ex.getProblemCode()).
                        setExplain(ex.getExplain()).
                        build();
                log.info("100% - Terminated with error. Problem code " + ex.getProblemCode() + " explain " + ex.getExplain());
                if (ex.isNotify()) {
                    co.send(this.problemReport);
                }
            }
        }

        return new Pair<>(false, null);
    }

    private List<Transaction> commitInternal(CoProtocolThreadedTheirs co, AbstractMicroledger ledger, List<Transaction> transactions,
                                             List<String> participants) throws StateMachineTerminatedWithError {
        Pair<Boolean, List<String>> t1 = acquire(Arrays.asList(ledger.name()), (double) this.timeToLiveSec);
        if (!t1.first) {
            throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, "Preparing: Ledgers are locked by other state-machine", false);
        }

        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String txnTime = df.format(new Date(System.currentTimeMillis()));
            Triple<Integer, Integer, List<Transaction>> appendRes = ledger.append(transactions, txnTime);
            List<Transaction> txns = appendRes.third;
            ProposeTransactionsMessage propose = ProposeTransactionsMessage.builder().
                    setTransactions(txns).
                    setState(MicroLedgerState.fromLedger(ledger)).
                    setParticipants(participants).
                    setTimeoutSec(this.timeToLiveSec)
                    .build();

            // ==== STAGE-1 Propose transactions to participants ====
            CommitTransactionsMessage commit = CommitTransactionsMessage.builder().
                    setParticipants(participants).
                    build();
            PreCommitTransactionsMessage selfPreCommit = PreCommitTransactionsMessage.builder().
                    setState(propose.getState()).
                    build();
            selfPreCommit.signState(context.getCrypto(), this.me);
            commit.addPreCommit(this.me.getDid(), selfPreCommit);

            log.info("20% - Send Propose to participants");
            List<CoProtocolThreadedTheirs.SendAndWaitResult> results = co.sendAndWait(propose);
            log.info("30% - Received Propose from participants");

            List<String> erroredAcceptorsDid = new ArrayList<>();
            for (CoProtocolThreadedTheirs.SendAndWaitResult r : results) {
                if (!r.success)
                    erroredAcceptorsDid.add(r.pairwise.getTheir().getDid());
            }
            if (!erroredAcceptorsDid.isEmpty()) {
                throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR, "Stage-1: Participants unreachable");
            }

            log.info("50% - Validate responses");
            for (CoProtocolThreadedTheirs.SendAndWaitResult r : results) {
                if (r.message instanceof PreCommitTransactionsMessage) {
                    try {
                        PreCommitTransactionsMessage preCommit = (PreCommitTransactionsMessage) r.message;
                        preCommit.validate();
                        Pair<Boolean, String> pcvsRes = preCommit.verifyState(context.getCrypto(), r.pairwise.getTheir().getVerkey());
                        if (!pcvsRes.first) {
                            throw new SiriusValidationError("Stage-1: Error verifying signed ledger state for participant }" + r.pairwise.getTheir().getDid());
                        }
                        if (!preCommit.getHash().equals(propose.getState().getHash())) {
                            throw new SiriusValidationError("Stage-1: Non-consistent ledger state for participant " + r.pairwise.getTheir().getDid());
                        }
                        commit.addPreCommit(r.pairwise.getTheir().getDid(), preCommit);
                    } catch (SiriusValidationError ex) {
                        throw new StateMachineTerminatedWithError(RESPONSE_NOT_ACCEPTED,
                                "Stage-1: Error for participant " + r.pairwise.getTheir().getDid() + ": " + ex.getMessage());
                    }
                } else if (r.message instanceof SimpleConsensusProblemReport) {
                    SimpleConsensusProblemReport problemReport = (SimpleConsensusProblemReport) r.message;
                    String explain = "Stage-1: Problem report from participant " + r.pairwise.getTheir().getDid() + " " + problemReport.getExplain();
                    throw new StateMachineTerminatedWithError(RESPONSE_NOT_ACCEPTED, explain);
                }
            }

            // ===== STAGE-2: Accumulate pre-commits and send commit propose to all participants
            PostCommitTransactionsMessage postCommitAll = PostCommitTransactionsMessage.builder().
                    build();
            postCommitAll.addCommitSign(context.getCrypto(), commit, this.me);

            log.info("60% - Send Commit to participants");
            results = co.sendAndWait(commit);
            log.info("70% - Received Commit response from participants");

            erroredAcceptorsDid = new ArrayList<>();
            for (CoProtocolThreadedTheirs.SendAndWaitResult r : results) {
                if (!r.success)
                    erroredAcceptorsDid.add(r.pairwise.getTheir().getDid());
            }
            if (!erroredAcceptorsDid.isEmpty()) {
                throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR, "Stage-2: Participants unreachable");
            }

            log.info("80% - Validate responses");
            for (CoProtocolThreadedTheirs.SendAndWaitResult r : results) {
                if (r.message instanceof PostCommitTransactionsMessage) {
                    try {
                        PostCommitTransactionsMessage postCommit = (PostCommitTransactionsMessage) r.message;
                        postCommit.validate();
                        JSONArray postCommits = postCommit.getCommits();
                        for (Object o : postCommits) {
                            postCommitAll.getCommits().put(o);
                        }
                    } catch (SiriusValidationError ex) {
                        throw new StateMachineTerminatedWithError(RESPONSE_NOT_ACCEPTED,
                                "Stage-2: Error for participant " + r.pairwise.getTheir().getDid() + ": " + ex.getMessage());
                    }
                } else if (r.message instanceof SimpleConsensusProblemReport) {
                    SimpleConsensusProblemReport problemReport = (SimpleConsensusProblemReport) r.message;
                    String explain = "Stage-2: Problem report from participant " + r.pairwise.getTheir().getDid() + " " + problemReport.getExplain();
                    throw new StateMachineTerminatedWithError(RESPONSE_NOT_ACCEPTED, explain);
                }
            }

            // STAGE-3: Notify all participants with post-commits and finalize process
            log.info("90% - Send Post-Commit");
            co.send(postCommitAll);
            int uncommitted_size = ledger.uncommittedSize() - ledger.size();
            ledger.commit(uncommitted_size);
            return txns;
        } finally {
            release();
        }
    }

    public boolean acceptCommit(Pairwise leader, ProposeTransactionsMessage propose) {
        int timeToLive = this.timeToLiveSec;
        if (propose.getTimeoutSec() > 0)
            timeToLive = propose.getTimeoutSec();
        try (CoProtocolThreadedP2P co = leader(leader, propose.getThreadId(), timeToLive)) {
            AbstractMicroledger ledger = null;
            try {
                log.info("0% - Start acception " + propose.transactions().size() + " transactions");
                ledger = loadLedger(propose);
                acceptCommitInternal(co, ledger, leader, propose);
                log.info("100% - Acception terminated successfully");
                return true;
            } catch (StateMachineTerminatedWithError ex) {
                if (ledger != null)
                    ledger.resetUncommitted();
                this.problemReport = SimpleConsensusProblemReport.builder().
                        setProblemCode(ex.getProblemCode()).
                        setExplain(ex.getExplain()).
                        build();
                log.info("100% - Terminated with error " + ex.getProblemCode() + " " + ex.getExplain());
                if (ex.isNotify()) {
                    co.send(this.problemReport);
                }
            } catch (Exception ex) {
                if (ledger != null)
                    ledger.resetUncommitted();
                ex.printStackTrace();
            }
        }

        return false;
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

    private AbstractMicroledger loadLedger(ProposeTransactionsMessage propose) throws StateMachineTerminatedWithError {
        try {
            bootstrap(propose.getParticipants());
            propose.validate();
            if (propose.getParticipants().size() < 2) {
                throw new SiriusValidationError("Stage-1: participant count less than 2");
            }
            if (!propose.getParticipants().contains(this.me.getDid())) {
                throw new SiriusValidationError("Stage-1: " + this.me.getDid() + " is not participant");
            }
            boolean isLedgerExists = context.getMicrolegders().isExists(propose.getState().getName());
            if (!isLedgerExists) {
                throw new SiriusValidationError("Stage-1: Ledger with name " + propose.getState().getName() + " does not exists");
            }
        } catch (SiriusValidationError ex) {
            throw new StateMachineTerminatedWithError(RESPONSE_NOT_ACCEPTED, ex.getMessage());
        }
        return context.getMicrolegders().getLedger(propose.getState().getName());
    }

    private void acceptCommitInternal(CoProtocolThreadedP2P co, AbstractMicroledger ledger, Pairwise leader, ProposeTransactionsMessage propose) throws StateMachineTerminatedWithError, SiriusInvalidMessage, SiriusInvalidPayloadStructure {
        Pair<Boolean, List<String>> t1 = acquire(Arrays.asList(ledger.name()), (double) this.timeToLiveSec);
        if (!t1.first) {
            throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, "Preparing: Ledgers are locked by other state-machine");
        }

        try {
            // ===== STAGE-1: Process Propose, apply transactions and response ledger state on self-side
            ledger.append(propose.transactions());
            MicroLedgerState ledgerState = MicroLedgerState.fromLedger(ledger);
            PreCommitTransactionsMessage preCommit = PreCommitTransactionsMessage.builder().
                    setState(MicroLedgerState.fromLedger(ledger)).
                    build();
            preCommit.signState(context.getCrypto(), this.me);
            log.info("10% - Send Pre-Commit");

            Pair<Boolean, Message> t2 = co.sendAndWait(preCommit);
            if (t2.first) {
                log.info("20% - Received Pre-Commit response");
                if (t2.second instanceof CommitTransactionsMessage) {
                    // ===== STAGE-2: Process Commit request, check neighbours signatures
                    try {
                        log.info("30% - Validate Commit");
                        CommitTransactionsMessage commit = (CommitTransactionsMessage) t2.second;
                        if (!new HashSet<String>(commit.getParticipants()).equals(new HashSet<String>(propose.getParticipants()))) {
                            throw new SiriusValidationError("Non-consistent participants");
                        }
                        commit.validate();
                        commit.verifyPreCommits(context.getCrypto(), ledgerState);

                        // ===== STAGE-3: Process post-commit, verify participants operations
                        PostCommitTransactionsMessage postCommit = PostCommitTransactionsMessage.builder().
                                build();
                        postCommit.addCommitSign(context.getCrypto(), commit, this.me);

                        log.info("50% - Send Post-Commit");
                        Pair<Boolean, Message> t3 = co.sendAndWait(postCommit);
                        if (t3.first) {
                            log.info("60% - Received Post-Commit response");
                            if (t3.second instanceof PostCommitTransactionsMessage) {
                                try {
                                    log.info("80% - Validate response");
                                    PostCommitTransactionsMessage postCommitAll = (PostCommitTransactionsMessage) t3.second;
                                    postCommitAll.validate();
                                    List<String> verkeys = new ArrayList<>();
                                    for (Pairwise p : this.cachedP2P.values()) {
                                        verkeys.add(p.getTheir().getVerkey());
                                    }
                                    postCommitAll.verifyCommits(context.getCrypto(), commit, verkeys);

                                    int uncommitted_size = ledgerState.getUncommittedSize() - ledgerState.getSize();
                                    log.info("90% - Flush transactions to Ledger storage");
                                    ledger.commit(uncommitted_size);
                                } catch (SiriusValidationError ex) {
                                    throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED,
                                            "Stage-3: error for leader " + leader.getTheir().getDid() + " " + ex.getMessage());
                                }
                            } else if (t3.second instanceof SimpleConsensusProblemReport) {
                                this.problemReport = (SimpleConsensusProblemReport) t3.second;
                                throw new StateMachineTerminatedWithError(this.problemReport.getProblemCode(),
                                        "Stage-3: Problem report from leader" + leader.getTheir().getDid() + " " + this.problemReport.getExplain());
                            }
                        } else {
                            throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR,
                                    "Stage-3: Post-Commit awaiting terminated by timeout for leader: " + leader.getTheir().getDid());
                        }
                    } catch (SiriusValidationError ex) {
                        throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED,
                                "Stage-2: error for actor " + leader.getTheir().getDid()  + " " + ex.getMessage());
                    }
                } else if (t2.second instanceof SimpleConsensusProblemReport) {
                    this.problemReport = (SimpleConsensusProblemReport) t2.second;
                    String explain = "Stage-1: Problem report from leader" +  leader.getTheir().getDid() + " " + this.problemReport.getExplain();
                    throw new StateMachineTerminatedWithError(this.problemReport.getProblemCode(), this.problemReport.getExplain());
                } else {
                    throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, "Unexpected message @type: " + t2.second.getType());
                }
            } else {
                throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR,
                        "Stage-1: Commit awaiting terminated by timeout for leader: " + leader.getTheir().getDid());
            }
        } finally {
            release();
        }
    }

    private Pair<Boolean, List<String>> acquire(List<String> names, Double lockTimeoutSec) {
        String NAMESPACE = "ledgers";
        names = new ArrayList<>(new HashSet<String>(names));
        for (int i = 0; i < names.size(); i++) {
            names.set(i, NAMESPACE + "/" + names.get(i));
        }
        Pair<Boolean, List<String>> t1 = this.context.acquire(names, lockTimeoutSec, null);
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
        Pair<Boolean, List<String>> t1 = acquire(Arrays.asList(ledger.name()), (double) this.timeToLiveSec);
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
            results = co.sendAndWait(requestCommit);
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

            // ============== STAGE 3: POST-COMMIT ============
            Ack ack = Ack.builder().
                    setStatus(Ack.Status.OK).
                    build();
            log.info("90% - All checks OK. Send Ack to acceptors");
            co.send(ack);
        } catch (SiriusValidationError siriusValidationError) {
            siriusValidationError.printStackTrace();
        } finally {
            release();
        }
    }

    private AbstractMicroledger acceptMicroledgerInternal(CoProtocolThreadedP2P co, Pairwise leader, InitRequestLedgerMessage propose,
                                                          int timeout) throws StateMachineTerminatedWithError {
        Pair<Boolean, List<String>> t1 = acquire(Arrays.asList(propose.getLedger().getString("name")), (double)this.timeToLiveSec);
        if (!t1.first) {
            throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, "Preparing: Ledgers are locked by other state-machine");
        }
        try {
            // =============== STAGE 1: PROPOSE ===============
            try {
                propose.validate();
                propose.checkSignatures(context.getCrypto(), leader.getTheir().getDid());
                if (propose.getParticipants().size() < 2) {
                    throw new SiriusValidationError("Stage-1: participants less than 2");
                }
            } catch (SiriusValidationError ex) {
                throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, ex.getMessage());
            }
            List<Transaction> genesis = new ArrayList<>();
            JSONArray genJsonArr = propose.getLedger().getJSONArray("genesis");
            for (Object o : genJsonArr) {
                genesis.add(new Transaction((JSONObject) o));
            }
            log.info("10% - Initialize ledger");
            Pair<AbstractMicroledger, List<Transaction>> t2 = context.getMicrolegders().create(propose.getLedger().getString("name"), genesis);
            AbstractMicroledger ledger = t2.first;
            List<Transaction> txns = t2.second;
            log.info("20% - Ledger initialized successfully");
            if (!propose.getLedger().optString("root_hash").equals(ledger.rootHash())) {
                context.getMicrolegders().reset(ledger.name());
                throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR, "Stage-1: Non-consistent Root Hash");
            }
            InitResponseLedgerMessage response = InitResponseLedgerMessage.builder().
                    setTimeoutSec(timeout).
                    build();
            response.assignFrom(propose);
            JSONObject commitLedgerHash = response.ledgerHash();
            response.addSignature(context.getCrypto(), this.me);

            // =============== STAGE 2: COMMIT ===============
            log.info("30% - Send propose response");
            Pair<Boolean, Message> t3 = co.sendAndWait(response);
            if (t3.first) {
                log.info("50% - Validate request commit");
                if (t3.second instanceof InitResponseLedgerMessage) {
                    InitResponseLedgerMessage requestCommit = (InitResponseLedgerMessage) t3.second;
                    try {
                        requestCommit.validate();
                        JSONObject hashes = requestCommit.checkSignatures(context.getCrypto());
                        for (String theirDid : hashes.keySet()) {
                            JSONObject decoded = hashes.getJSONObject(theirDid);
                            if (!decoded.similar(commitLedgerHash)) {
                                throw new SiriusValidationError("Stage-2: NonEqual Ledger hash with participant " + theirDid);
                            }
                        }
                    } catch (SiriusValidationError ex) {
                        throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, ex.getMessage());
                    }

                    Set<String> commitParticipantsSet = new HashSet<>(requestCommit.getParticipants());
                    Set<String> proposeParticipantsSet = new HashSet<>(propose.getParticipants());
                    Set<Object> signersSet = new HashSet<>();
                    JSONArray signs = requestCommit.signatures();
                    for (Object sign : signs) {
                        signersSet.add(((JSONObject)sign).getString("participant"));
                    }

                    String errorExplain = null;
                    if (!proposeParticipantsSet.equals(signersSet)) {
                        errorExplain = "Stage-2: Set of signers differs from proposed participants set";
                    } else if (!commitParticipantsSet.equals(signersSet)) {
                        errorExplain = "Stage-2: Set of signers differs from commit participants set";
                    }

                    if (errorExplain != null) {
                        throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, errorExplain);
                    }

                    // Accept commit
                    log.info("70% - Send Ack");
                    Ack ack = Ack.builder().
                            setStatus(Ack.Status.OK).
                            build();
                    Pair<Boolean, Message> t4 = co.sendAndWait(ack);
                    // =========== STAGE-3: POST-COMMIT ===============
                    if (t4.first) {
                        log.info("90% - Response to Ack received");
                        if (t4.second instanceof Ack) {
                            return ledger;
                        } else if (t4.second instanceof SimpleConsensusProblemReport) {
                            this.problemReport = (SimpleConsensusProblemReport) t4.second;
                            log.info("Code: " + this.problemReport.getProblemCode() + "; Explain: " + this.problemReport.getExplain());
                            throw new StateMachineTerminatedWithError(this.problemReport.getProblemCode(), this.problemReport.getExplain());
                        }
                    } else {
                        throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR,
                                "Stage-3: Commit accepting was terminated by timeout for actor: " + leader.getTheir().getDid());
                    }
                } else if (t3.second instanceof SimpleConsensusProblemReport) {
                    this.problemReport = (SimpleConsensusProblemReport) t3.second;
                    throw new StateMachineTerminatedWithError(this.problemReport.getProblemCode(), this.problemReport.getExplain());
                }
            } else {
                throw new StateMachineTerminatedWithError(REQUEST_PROCESSING_ERROR,
                        "Stage-2: Commit response awaiting was terminated by timeout for actor: " + leader.getTheir().getDid());
            }
        } catch (SiriusInvalidMessage siriusInvalidMessage) {
            siriusInvalidMessage.printStackTrace();
        } catch (SiriusInvalidPayloadStructure siriusInvalidPayloadStructure) {
            siriusInvalidPayloadStructure.printStackTrace();
        } finally {
            release();
        }

        return null;
    }

    @Override
    public List<String> protocols() {
        return null;
    }
}
