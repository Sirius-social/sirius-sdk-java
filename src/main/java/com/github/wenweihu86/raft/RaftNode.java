package com.github.wenweihu86.raft;

import com.baidu.brpc.client.RpcCallback;
import com.github.wenweihu86.raft.models.*;

import com.github.wenweihu86.raft.service.RaftClientService;
import com.github.wenweihu86.raft.service.RaftConsensusService;
import com.github.wenweihu86.raft.storage.SegmentedLog;
import com.github.wenweihu86.raft.util.ConfigurationUtils;
import com.google.protobuf.ByteString;
import com.github.wenweihu86.raft.storage.Snapshot;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.sirius.sdk.agent.pairwise.Pairwise;
import examples.raft.server.service.ExampleService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;


public class RaftNode {

    public enum NodeState {
        STATE_FOLLOWER,
        STATE_PRE_CANDIDATE,
        STATE_CANDIDATE,
        STATE_LEADER
    }

    private static final Logger LOG = LoggerFactory.getLogger(RaftNode.class);
    private static final JsonFormat jsonFormat = new JsonFormat();

    private RaftOptions raftOptions;
    private Configuration configuration;
    private ConcurrentMap<String, Peer> peerMap = new ConcurrentHashMap<>();
    public Server localServer;
    private StateMachine stateMachine;
    private SegmentedLog raftLog;
    private Snapshot snapshot;

    private NodeState state = NodeState.STATE_FOLLOWER;

    private Long currentTerm;

    private String votedFor;
    private String leaderId;

    private Long commitIndex;

    private volatile long lastAppliedIndex;

    private Lock lock = new ReentrantLock();
    private Condition commitIndexCondition = lock.newCondition();
    private Condition catchUpCondition = lock.newCondition();

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture electionScheduledFuture;
    private ScheduledFuture heartbeatScheduledFuture;

    public RaftConsensusService raftConsensusService;
    public RaftClientService raftClientService;
    public  ExampleService exampleService;

    public RaftNode(RaftOptions raftOptions,
                    List<Server> servers,
                    Server localServer,
                    StateMachine stateMachine) {
        this.raftOptions = raftOptions;
        Configuration confBuilder = new Configuration();
        for (Server server : servers) {
            confBuilder.addServers(server);
        }
        configuration = confBuilder;
        this.localServer = localServer;
        this.stateMachine = stateMachine;

        // load log and snapshot
        raftLog = new SegmentedLog(localServer.getServerId(), raftOptions.getMaxSegmentFileSize());
        snapshot = new Snapshot(localServer.getServerId());
        snapshot.reload();

        currentTerm = raftLog.getMetaData().getCurrentTerm();
        votedFor = raftLog.getMetaData().getVotedFor();
        commitIndex = Math.max(snapshot.getMetaData().getLastIncludedIndex(), raftLog.getMetaData().getCommitIndex());
        // discard old log entries
        if (snapshot.getMetaData().getLastIncludedIndex() > 0
                && raftLog.getFirstLogIndex() <= snapshot.getMetaData().getLastIncludedIndex()) {
            raftLog.truncatePrefix(snapshot.getMetaData().getLastIncludedIndex() + 1);
        }
        // apply state machine
       Configuration snapshotConfiguration = snapshot.getMetaData().getConfiguration();
        if(snapshotConfiguration!=null){
            if (snapshotConfiguration.getServersCount() > 0) {
                configuration = snapshotConfiguration;
            }
        }
        String snapshotDataDir = snapshot.getSnapshotDir() + File.separator + "data";
        stateMachine.readSnapshot(snapshotDataDir);
        for (long index = snapshot.getMetaData().getLastIncludedIndex() + 1;
             index <= commitIndex; index++) {
            LogEntry entry = raftLog.getEntry(index);
            if (entry.getType() == EntryType.ENTRY_TYPE_DATA) {
                stateMachine.apply(entry.getData());
            } else if (entry.getType() == EntryType.ENTRY_TYPE_CONFIGURATION) {
                applyConfiguration(entry);
            }
        }
        lastAppliedIndex = commitIndex;
    }

    public void init() {
        for (Server server : configuration.getServersList()) {
            if (!peerMap.containsKey(server.getServerId())
                    && server.getServerId() != localServer.getServerId()) {
                Peer peer = new Peer(server);
                peer.setNextIndex(raftLog.getLastLogIndex() + 1);
                peerMap.put(server.getServerId(), peer);
            }
        }

        // init thread pool
        executorService = new ThreadPoolExecutor(
                raftOptions.getRaftConsensusThreadNum(),
                raftOptions.getRaftConsensusThreadNum(),
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        scheduledExecutorService = Executors.newScheduledThreadPool(2);
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                takeSnapshot();
            }
        }, raftOptions.getSnapshotPeriodSeconds(), raftOptions.getSnapshotPeriodSeconds(), TimeUnit.SECONDS);
        // start election
        //TODO FIX
     //   resetElectionTimer();
    }

    // client set command
    public boolean replicate(byte[] data, EntryType entryType) {
        lock.lock();
        long newLastLogIndex = 0;
        try {
            if (state != NodeState.STATE_LEADER) {
                LOG.debug("I'm not the leader");
                return false;
            }
            LogEntry logEntry = new LogEntry()
                    .setTerm(currentTerm)
                    .setType(entryType)
                    .setData(data);
            List<LogEntry> entries = new ArrayList<>();
            entries.add(logEntry);
            newLastLogIndex = raftLog.append(entries);
            //raftLog.updateMetaData(currentTerm, null, raftLog.getFirstLogIndex());

            for (Server server : configuration.getServersList()) {
                final Peer peer = peerMap.get(server.getServerId());
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        appendEntries(peer);
                    }
                });
            }

            if (raftOptions.isAsyncWrite()) {

                return true;
            }

            // sync wait commitIndex >= newLastLogIndex
            long startTime = System.currentTimeMillis();
            while (lastAppliedIndex < newLastLogIndex) {
                if (System.currentTimeMillis() - startTime >= raftOptions.getMaxAwaitTimeout()) {
                    break;
                }
                commitIndexCondition.await(raftOptions.getMaxAwaitTimeout(), TimeUnit.MILLISECONDS);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
        LOG.debug("lastAppliedIndex={} newLastLogIndex={}", lastAppliedIndex, newLastLogIndex);
        if (lastAppliedIndex < newLastLogIndex) {
            return false;
        }
        return true;
    }

    public void appendEntries(Peer peer) {
        AppendEntriesRequest requestBuilder = new AppendEntriesRequest();
        long prevLogIndex;
        long numEntries;

        boolean isNeedInstallSnapshot = false;
        lock.lock();
        try {
            long firstLogIndex = raftLog.getFirstLogIndex();
            if (peer.getNextIndex() < firstLogIndex) {
                isNeedInstallSnapshot = true;
            }
        } finally {
            lock.unlock();
        }

        LOG.debug("is need snapshot={}, peer={}", isNeedInstallSnapshot, peer.getServer().getServerId());
        if (isNeedInstallSnapshot) {
            if (!installSnapshot(peer)) {
                return;
            }
        }

        long lastSnapshotIndex;
        long lastSnapshotTerm;
        snapshot.getLock().lock();
        try {
            lastSnapshotIndex = snapshot.getMetaData().getLastIncludedIndex();
            lastSnapshotTerm = snapshot.getMetaData().getLastIncludedTerm();
        } finally {
            snapshot.getLock().unlock();
        }

        lock.lock();
        try {
            long firstLogIndex = raftLog.getFirstLogIndex();
            Validate.isTrue(peer.getNextIndex() >= firstLogIndex);
            prevLogIndex = peer.getNextIndex() - 1;
            long prevLogTerm;
            if (prevLogIndex == 0) {
                prevLogTerm = 0;
            } else if (prevLogIndex == lastSnapshotIndex) {
                prevLogTerm = lastSnapshotTerm;
            } else {
                prevLogTerm = raftLog.getEntryTerm(prevLogIndex);
            }
            requestBuilder.setServerId(localServer.getServerId());
            requestBuilder.setServer(localServer);
            requestBuilder.setTerm(currentTerm);
            requestBuilder.setPrevLogTerm(prevLogTerm);
            requestBuilder.setPrevLogIndex(prevLogIndex);
            numEntries = packEntries(peer.getNextIndex(), requestBuilder);
            requestBuilder.setCommitIndex(Math.min(commitIndex, prevLogIndex + numEntries));
        } finally {
            lock.unlock();
        }

        AppendEntriesRequest request = requestBuilder;
        AppendEntriesResponse response = peer.getRaftConsensusServiceAsync().appendEntries(request);

        lock.lock();
        try {
            if (response == null) {
                LOG.warn("appendEntries with peer[{}:{}] failed",
                        peer.getServer().getEndpoint().getHost(),
                        peer.getServer().getEndpoint().getPort());
                if (!ConfigurationUtils.containsServer(configuration, peer.getServer().getServerId())) {
                    peerMap.remove(peer.getServer().getServerId());
                   // peer.getRpcClient().stop();
                   // peer.getRpcClient().close();
                    //TODO
                }
                return;
            }
            LOG.info("AppendEntries response[{}] from server {} " +
                            "in term {} (my term is {})",
                    response.getResCode(), peer.getServer().getServerId(),
                    response.getTerm(), currentTerm);

            if (response.getTerm() > currentTerm) {
                stepDown(response.getTerm());
            } else {
                if (response.getResCode() == ResCode.RES_CODE_SUCCESS) {
                    peer.setMatchIndex(prevLogIndex + numEntries);
                    peer.setNextIndex(peer.getMatchIndex() + 1);
                    if (ConfigurationUtils.containsServer(configuration, peer.getServer().getServerId())) {
                        advanceCommitIndex();
                    } else {
                        if (raftLog.getLastLogIndex() - peer.getMatchIndex() <= raftOptions.getCatchupMargin()) {
                            LOG.debug("peer catch up the leader");
                            peer.setCatchUp(true);
                            // signal the caller thread
                            catchUpCondition.signalAll();
                        }
                    }
                } else {
                    peer.setNextIndex(response.getLastLogIndex() + 1);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    // in lock
    public void stepDown(long newTerm) {
        if (currentTerm > newTerm) {
            LOG.error("can't be happened");
            return;
        }
        if (currentTerm < newTerm) {
            currentTerm = newTerm;
            leaderId = "";
            votedFor = "";
            raftLog.updateMetaData(currentTerm, votedFor, null, null);
        }
        state = NodeState.STATE_FOLLOWER;
        // stop heartbeat
        if (heartbeatScheduledFuture != null && !heartbeatScheduledFuture.isDone()) {
            heartbeatScheduledFuture.cancel(true);
        }
        resetElectionTimer();
    }

    public void takeSnapshot() {
        if (snapshot.getIsInstallSnapshot().get()) {
            LOG.info("already in install snapshot, ignore take snapshot");
            return;
        }

        snapshot.getIsTakeSnapshot().compareAndSet(false, true);
        try {
            long localLastAppliedIndex;
            long lastAppliedTerm = 0;
            Configuration localConfiguration = new Configuration();
            lock.lock();
            try {
                if (raftLog.getTotalSize() < raftOptions.getSnapshotMinLogSize()) {
                    return;
                }
                if (lastAppliedIndex <= snapshot.getMetaData().getLastIncludedIndex()) {
                    return;
                }
                localLastAppliedIndex = lastAppliedIndex;
                if (lastAppliedIndex >= raftLog.getFirstLogIndex()
                        && lastAppliedIndex <= raftLog.getLastLogIndex()) {
                    lastAppliedTerm = raftLog.getEntryTerm(lastAppliedIndex);
                }
                //TODO
            //    localConfiguration.mergeFrom(configuration);
            } finally {
                lock.unlock();
            }

            boolean success = false;
            snapshot.getLock().lock();
            try {
                LOG.info("start taking snapshot");
                // take snapshot
                String tmpSnapshotDir = snapshot.getSnapshotDir() + ".tmp";
                snapshot.updateMetaData(tmpSnapshotDir, localLastAppliedIndex,
                        lastAppliedTerm, localConfiguration);
                String tmpSnapshotDataDir = tmpSnapshotDir + File.separator + "data";
                stateMachine.writeSnapshot(tmpSnapshotDataDir);
                // rename tmp snapshot dir to snapshot dir
                try {
                    File snapshotDirFile = new File(snapshot.getSnapshotDir());
                    if (snapshotDirFile.exists()) {
                        FileUtils.deleteDirectory(snapshotDirFile);
                    }
                    FileUtils.moveDirectory(new File(tmpSnapshotDir),
                            new File(snapshot.getSnapshotDir()));
                    LOG.info("end taking snapshot, result=success");
                    success = true;
                } catch (IOException ex) {
                    LOG.warn("move direct failed when taking snapshot, msg={}", ex.getMessage());
                }
            } finally {
                snapshot.getLock().unlock();
            }

            if (success) {
                // 重新加载snapshot
                long lastSnapshotIndex = 0;
                snapshot.getLock().lock();
                try {
                    snapshot.reload();
                    lastSnapshotIndex = snapshot.getMetaData().getLastIncludedIndex();
                } finally {
                    snapshot.getLock().unlock();
                }

                // discard old log entries
                lock.lock();
                try {
                    if (lastSnapshotIndex > 0 && raftLog.getFirstLogIndex() <= lastSnapshotIndex) {
                        raftLog.truncatePrefix(lastSnapshotIndex + 1);
                    }
                } finally {
                    lock.unlock();
                }
            }
        } finally {
            snapshot.getIsTakeSnapshot().compareAndSet(true, false);
        }
    }

    // in lock
    public void applyConfiguration(LogEntry entry) {

            //TODo Configuration newConfiguration = Configuration.parseFrom(entry.getData());
            Configuration newConfiguration = new Configuration();
            configuration = newConfiguration;
            // update peerMap
            for (Server server : newConfiguration.getServersList()) {
                if (!peerMap.containsKey(server.getServerId())
                        && server.getServerId() != localServer.getServerId()) {
                    Peer peer = new Peer(server);
                    peer.setNextIndex(raftLog.getLastLogIndex() + 1);
                    peerMap.put(server.getServerId(), peer);
                }
            }
            LOG.info("new conf is {}, leaderId={}", newConfiguration.toString(), leaderId);

    }

    public long getLastLogTerm() {
        long lastLogIndex = raftLog.getLastLogIndex();
        if (lastLogIndex >= raftLog.getFirstLogIndex()) {
            return raftLog.getEntryTerm(lastLogIndex);
        } else {
            // log为空，lastLogIndex == lastSnapshotIndex
            return snapshot.getMetaData().getLastIncludedTerm();
        }
    }

    public void resetElectionTimer() {
        if (electionScheduledFuture != null && !electionScheduledFuture.isDone()) {
            electionScheduledFuture.cancel(true);
        }
        electionScheduledFuture = scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                startPreVote();
            }
        }, getElectionTimeoutMs(), TimeUnit.MILLISECONDS);
    }

    private int getElectionTimeoutMs() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randomElectionTimeout = raftOptions.getElectionTimeoutMilliseconds()
                + random.nextInt(0, raftOptions.getElectionTimeoutMilliseconds());
        LOG.debug("new election time is after {} ms", randomElectionTimeout);
        return randomElectionTimeout;
    }


    private void startPreVote() {
        lock.lock();
        try {
            if (!ConfigurationUtils.containsServer(configuration, localServer.getServerId())) {
                resetElectionTimer();
                return;
            }
            LOG.info("Running pre-vote in term {}", currentTerm);
            state = NodeState.STATE_PRE_CANDIDATE;
        } finally {
            lock.unlock();
        }

        for (Server server : configuration.getServersList()) {
            if (server.getServerId().equals(localServer.getServerId())) {
                continue;
            }
            final Peer peer = peerMap.get(server.getServerId());
           // Pairwise pairwise = localServer.getCloudAgent().getPairwiseList().loadForDid(peer.getServer().getServerId());
            Pairwise pairwise = null;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    preVote(peer,pairwise);
                }
            });
        }
        //TODO FIX
       // resetElectionTimer();
    }


    private void startVote() {
        lock.lock();
        try {
            if (!ConfigurationUtils.containsServer(configuration, localServer.getServerId())) {
                resetElectionTimer();
                return;
            }
            currentTerm++;
            LOG.info("Running for election in term {}", currentTerm);
            state = NodeState.STATE_CANDIDATE;
            leaderId = "";
            votedFor = localServer.getServerId();
        } finally {
            lock.unlock();
        }

        for (Server server : configuration.getServersList()) {
            if (server.getServerId() == localServer.getServerId()) {
                continue;
            }
            final Peer peer = peerMap.get(server.getServerId());
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    requestVote(peer);
                }
            });
        }
    }


    private void preVote(Peer peer,Pairwise pairwise) {
        LOG.info("begin pre vote request");
        VoteRequest requestBuilder = new VoteRequest();
        lock.lock();
        try {
            peer.setVoteGranted(null);
            requestBuilder.setServer(localServer)
                    .setTerm(currentTerm)
                    .setPairwise(pairwise)
                    .setServerId(localServer.getServerId())
                    .setLastLogIndex(raftLog.getLastLogIndex())
                    .setLastLogTerm(getLastLogTerm());
        } finally {
            lock.unlock();
        }

        VoteRequest request = requestBuilder;
        peer.getRaftConsensusServiceAsync().preVote(
                request, new PreVoteResponseCallback(peer, request));
    }


    private void requestVote(Peer peer) {
        LOG.info("begin vote request");
        VoteRequest requestBuilder = new VoteRequest();
        lock.lock();
        try {
            peer.setVoteGranted(null);
            requestBuilder.setServer(localServer)
                    .setTerm(currentTerm)
                    .setServerId(localServer.getServerId())
                    .setLastLogIndex(raftLog.getLastLogIndex())
                    .setLastLogTerm(getLastLogTerm());
        } finally {
            lock.unlock();
        }

        VoteRequest request = requestBuilder;
        peer.getRaftConsensusServiceAsync().requestVote(
                request, new VoteResponseCallback(peer, request));
    }

    private class PreVoteResponseCallback implements RpcCallback<VoteResponse> {
        private Peer peer;
        private VoteRequest request;

        public PreVoteResponseCallback(Peer peer, VoteRequest request) {
            this.peer = peer;
            this.request = request;
        }

        @Override
        public void success(VoteResponse response) {
            lock.lock();
            try {
                peer.setVoteGranted(response.getGranted());
                if (currentTerm != request.getTerm() || state != NodeState.STATE_PRE_CANDIDATE) {
                    LOG.info("ignore preVote RPC result");
                    return;
                }
                if (response.getTerm() > currentTerm) {
                    LOG.info("Received pre vote response from server {} " +
                                    "in term {} (this server's term was {})",
                            peer.getServer().getServerId(),
                            response.getTerm(),
                            currentTerm);
                    stepDown(response.getTerm());
                } else {
                    if (response.getGranted()) {
                        LOG.info("get pre vote granted from server {} for term {}",
                                peer.getServer().getServerId(), currentTerm);
                        int voteGrantedNum = 1;
                        for (Server server : configuration.getServersList()) {
                            if (server.getServerId() == localServer.getServerId()) {
                                continue;
                            }
                            Peer peer1 = peerMap.get(server.getServerId());
                            if (peer1.isVoteGranted() != null && peer1.isVoteGranted() == true) {
                                voteGrantedNum += 1;
                            }
                        }
                        LOG.info("preVoteGrantedNum={}", voteGrantedNum);
                        if (voteGrantedNum > configuration.getServersCount() / 2) {
                            LOG.info("get majority pre vote, serverId={} when pre vote, start vote",
                                    localServer.getServerId());
                            startVote();
                        }
                    } else {
                        LOG.info("pre vote denied by server {} with term {}, my term is {}",
                                peer.getServer().getServerId(), response.getTerm(), currentTerm);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void fail(Throwable e) {
            LOG.warn("pre vote with peer[{}:{}] failed",
                    peer.getServer().getEndpoint().getHost(),
                    peer.getServer().getEndpoint().getPort());
            peer.setVoteGranted(new Boolean(false));
        }
    }

    private class VoteResponseCallback implements RpcCallback<VoteResponse> {
        private Peer peer;
        private VoteRequest request;

        public VoteResponseCallback(Peer peer, VoteRequest request) {
            this.peer = peer;
            this.request = request;
        }

        @Override
        public void success(VoteResponse response) {
            lock.lock();
            try {
                peer.setVoteGranted(response.getGranted());
                if (currentTerm != request.getTerm() || state != NodeState.STATE_CANDIDATE) {
                    LOG.info("ignore requestVote RPC result");
                    return;
                }
                if (response.getTerm() > currentTerm) {
                    LOG.info("Received RequestVote response from server {} " +
                                    "in term {} (this server's term was {})",
                            peer.getServer().getServerId(),
                            response.getTerm(),
                            currentTerm);
                    stepDown(response.getTerm());
                } else {
                    if (response.getGranted()) {
                        LOG.info("Got vote from server {} for term {}",
                                peer.getServer().getServerId(), currentTerm);
                        int voteGrantedNum = 0;
                        if (votedFor == localServer.getServerId()) {
                            voteGrantedNum += 1;
                        }
                        for (Server server : configuration.getServersList()) {
                            if (server.getServerId() == localServer.getServerId()) {
                                continue;
                            }
                            Peer peer1 = peerMap.get(server.getServerId());
                            if (peer1.isVoteGranted() != null && peer1.isVoteGranted() == true) {
                                voteGrantedNum += 1;
                            }
                        }
                        LOG.info("voteGrantedNum={}", voteGrantedNum);
                        if (voteGrantedNum > configuration.getServersCount() / 2) {
                            LOG.info("Got majority vote, serverId={} become leader", localServer.getServerId());
                            becomeLeader();
                        }
                    } else {
                        LOG.info("Vote denied by server {} with term {}, my term is {}",
                                peer.getServer().getServerId(), response.getTerm(), currentTerm);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void fail(Throwable e) {
            LOG.warn("requestVote with peer[{}:{}] failed",
                    peer.getServer().getEndpoint().getHost(),
                    peer.getServer().getEndpoint().getPort());
            peer.setVoteGranted(new Boolean(false));
        }
    }

    // in lock
    private void becomeLeader() {
        state = NodeState.STATE_LEADER;
        leaderId = localServer.getServerId();
        // stop vote timer
        if (electionScheduledFuture != null && !electionScheduledFuture.isDone()) {
            electionScheduledFuture.cancel(true);
        }
        // start heartbeat timer
        startNewHeartbeat();
    }

    // heartbeat timer, append entries
    // in lock
    private void resetHeartbeatTimer() {
        if (heartbeatScheduledFuture != null && !heartbeatScheduledFuture.isDone()) {
            heartbeatScheduledFuture.cancel(true);
        }
        heartbeatScheduledFuture = scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                startNewHeartbeat();
            }
        }, raftOptions.getHeartbeatPeriodMilliseconds(), TimeUnit.MILLISECONDS);
    }


    private void startNewHeartbeat() {
        LOG.debug("start new heartbeat, peers={}", peerMap.keySet());
        for (final Peer peer : peerMap.values()) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    appendEntries(peer);
                }
            });
        }
        resetHeartbeatTimer();
    }

    // in lock, for leader
    private void advanceCommitIndex() {
        // 获取quorum matchIndex
        int peerNum = configuration.getServersList().size();
        long[] matchIndexes = new long[peerNum];
        int i = 0;
        for (Server server : configuration.getServersList()) {
            if (server.getServerId() != localServer.getServerId()) {
                Peer peer = peerMap.get(server.getServerId());
                matchIndexes[i++] = peer.getMatchIndex();
            }
        }
        matchIndexes[i] = raftLog.getLastLogIndex();
        Arrays.sort(matchIndexes);
        long newCommitIndex = matchIndexes[peerNum / 2];
        LOG.debug("newCommitIndex={}, oldCommitIndex={}", newCommitIndex, commitIndex);
        if (raftLog.getEntryTerm(newCommitIndex) != currentTerm) {
            LOG.debug("newCommitIndexTerm={}, currentTerm={}",
                    raftLog.getEntryTerm(newCommitIndex), currentTerm);
            return;
        }

        if (commitIndex >= newCommitIndex) {
            return;
        }
        long oldCommitIndex = commitIndex;
        commitIndex = newCommitIndex;
        raftLog.updateMetaData(currentTerm, null, raftLog.getFirstLogIndex(), commitIndex);

        for (long index = oldCommitIndex + 1; index <= newCommitIndex; index++) {
            LogEntry entry = raftLog.getEntry(index);
            if (entry.getType() == EntryType.ENTRY_TYPE_DATA) {
                stateMachine.apply(entry.getData());
            } else if (entry.getType() == EntryType.ENTRY_TYPE_CONFIGURATION) {
                applyConfiguration(entry);
            }
        }
        lastAppliedIndex = commitIndex;
        LOG.debug("commitIndex={} lastAppliedIndex={}", commitIndex, lastAppliedIndex);
        commitIndexCondition.signalAll();
    }

    // in lock
    private long packEntries(long nextIndex, AppendEntriesRequest requestBuilder) {
        long lastIndex = Math.min(raftLog.getLastLogIndex(),
                nextIndex + raftOptions.getMaxLogEntriesPerRequest() - 1);
        for (long index = nextIndex; index <= lastIndex; index++) {
            LogEntry entry = raftLog.getEntry(index);
            requestBuilder.addEntries(entry);
        }
        return lastIndex - nextIndex + 1;
    }

    private boolean installSnapshot(Peer peer) {
        if (snapshot.getIsTakeSnapshot().get()) {
            LOG.info("already in take snapshot, please send install snapshot request later");
            return false;
        }
        if (!snapshot.getIsInstallSnapshot().compareAndSet(false, true)) {
            LOG.info("already in install snapshot");
            return false;
        }

        LOG.info("begin send install snapshot request to server={}", peer.getServer().getServerId());
        boolean isSuccess = true;
        TreeMap<String, Snapshot.SnapshotDataFile> snapshotDataFileMap = snapshot.openSnapshotDataFiles();
        LOG.info("total snapshot files={}", snapshotDataFileMap.keySet());
        try {
            boolean isLastRequest = false;
            String lastFileName = null;
            long lastOffset = 0;
            long lastLength = 0;
            while (!isLastRequest) {
                InstallSnapshotRequest request
                        = buildInstallSnapshotRequest(snapshotDataFileMap, lastFileName, lastOffset, lastLength);
                if (request == null) {
                    LOG.warn("snapshot request == null");
                    isSuccess = false;
                    break;
                }
                if (request.getIsLast()) {
                    isLastRequest = true;
                }
                LOG.info("install snapshot request, fileName={}, offset={}, size={}, isFirst={}, isLast={}",
                        request.getFileName(), request.getOffset(), request.getData().length,
                        request.getIsFirst(), request.getIsLast());
                InstallSnapshotResponse response
                        = peer.getRaftConsensusServiceAsync().installSnapshot(request);
                if (response != null && response.getResCode() == ResCode.RES_CODE_SUCCESS) {
                    lastFileName = request.getFileName();
                    lastOffset = request.getOffset();
                    lastLength = request.getData().length;
                } else {
                    isSuccess = false;
                    break;
                }
            }

            if (isSuccess) {
                long lastIncludedIndexInSnapshot;
                snapshot.getLock().lock();
                try {
                    lastIncludedIndexInSnapshot = snapshot.getMetaData().getLastIncludedIndex();
                } finally {
                    snapshot.getLock().unlock();
                }

                lock.lock();
                try {
                    peer.setNextIndex(lastIncludedIndexInSnapshot + 1);
                } finally {
                    lock.unlock();
                }
            }
        } finally {
            snapshot.closeSnapshotDataFiles(snapshotDataFileMap);
            snapshot.getIsInstallSnapshot().compareAndSet(true, false);
        }
        LOG.info("end send install snapshot request to server={}, success={}",
                peer.getServer().getServerId(), isSuccess);
        return isSuccess;
    }

    private InstallSnapshotRequest buildInstallSnapshotRequest(
            TreeMap<String, Snapshot.SnapshotDataFile> snapshotDataFileMap,
            String lastFileName, long lastOffset, long lastLength) {
        InstallSnapshotRequest requestBuilder = new InstallSnapshotRequest();

        snapshot.getLock().lock();
        try {
            if (lastFileName == null) {
                lastFileName = snapshotDataFileMap.firstKey();
                lastOffset = 0;
                lastLength = 0;
            }
            Snapshot.SnapshotDataFile lastFile = snapshotDataFileMap.get(lastFileName);
            long lastFileLength = lastFile.randomAccessFile.length();
            String currentFileName = lastFileName;
            long currentOffset = lastOffset + lastLength;
            int currentDataSize = raftOptions.getMaxSnapshotBytesPerRequest();
            Snapshot.SnapshotDataFile currentDataFile = lastFile;
            if (lastOffset + lastLength < lastFileLength) {
                if (lastOffset + lastLength + raftOptions.getMaxSnapshotBytesPerRequest() > lastFileLength) {
                    currentDataSize = (int) (lastFileLength - (lastOffset + lastLength));
                }
            } else {
                Map.Entry<String, Snapshot.SnapshotDataFile> currentEntry
                        = snapshotDataFileMap.higherEntry(lastFileName);
                if (currentEntry == null) {
                    LOG.warn("reach the last file={}", lastFileName);
                    return null;
                }
                currentDataFile = currentEntry.getValue();
                currentFileName = currentEntry.getKey();
                currentOffset = 0;
                int currentFileLenght = (int) currentEntry.getValue().randomAccessFile.length();
                if (currentFileLenght < raftOptions.getMaxSnapshotBytesPerRequest()) {
                    currentDataSize = currentFileLenght;
                }
            }
            byte[] currentData = new byte[currentDataSize];
            currentDataFile.randomAccessFile.seek(currentOffset);
            currentDataFile.randomAccessFile.read(currentData);
            requestBuilder.setData(currentData);
            requestBuilder.setFileName(currentFileName);
            requestBuilder.setOffset(currentOffset);
            requestBuilder.setIsFirst(false);
            if (currentFileName.equals(snapshotDataFileMap.lastKey())
                    && currentOffset + currentDataSize >= currentDataFile.randomAccessFile.length()) {
                requestBuilder.setIsLast(true);
            } else {
                requestBuilder.setIsLast(false);
            }
            if (currentFileName.equals(snapshotDataFileMap.firstKey()) && currentOffset == 0) {
                requestBuilder.setIsFirst(true);
                requestBuilder.setSnapshotMetaData(snapshot.getMetaData());
            } else {
                requestBuilder.setIsFirst(false);
            }
        } catch (Exception ex) {
            LOG.warn("meet exception:", ex);
            return null;
        } finally {
            snapshot.getLock().unlock();
        }

        lock.lock();
        try {
            requestBuilder.setTerm(currentTerm);
            requestBuilder.setServerId(localServer.getServerId());
            requestBuilder.setServer(localServer);
        } finally {
            lock.unlock();
        }

        return requestBuilder;
    }

    public Lock getLock() {
        return lock;
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public String getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

    public long getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public long getLastAppliedIndex() {
        return lastAppliedIndex;
    }

    public void setLastAppliedIndex(long lastAppliedIndex) {
        this.lastAppliedIndex = lastAppliedIndex;
    }

    public SegmentedLog getRaftLog() {
        return raftLog;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Server getLocalServer() {
        return localServer;
    }

    public NodeState getState() {
        return state;
    }

    public ConcurrentMap<String, Peer> getPeerMap() {
        return peerMap;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Condition getCatchUpCondition() {
        return catchUpCondition;
    }
}
