package com.bhavya.raftprotocol.leaderelection;

import com.bhavya.raftprotocol.leaderelection.rpc.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RaftNode {
    private int id;

    //network information of the server
    private String hostName;
    private int port;

    private RaftState state;

    //persistent state on all servers
    private int currentTerm;
    private int votedFor;
    private List<String> log = new ArrayList<>();

    //volatile state on all servers
    private int commitIndex;
    private int lastApplied;

    //volatile state on leaders
    private int[] nextIndex;
    private int[] matchIndex;

    private ServerSocket serverSocket;
    private Map<Integer, Socket> connections;

    private int electionTimeout;

    private List<RaftPeer> peers;

    private boolean hasReceivedHeartbeat;

    private int votesReceived = 0;

    ExecutorService executorService;

    public RaftNode(int id, String hostName, int port, List<RaftPeer> peerList) {
        this.id = id;
        this.hostName = hostName;
        this.port = port;
        this.state = RaftState.FOLLOWER;

        this.currentTerm = 0;
        this.votedFor = -1;
        this.commitIndex = 0;
        this.lastApplied = 0;
        this.nextIndex = new int[0];
        this.matchIndex = new int[0];

        this.hasReceivedHeartbeat = false;

        this.peers = peerList;

        this.electionTimeout = getElectionTimeout();
        System.out.println("Election timeout for server " + id + " is " + electionTimeout);
        System.out.println("Peer list for server " + id + " is " + peers.stream().map(RaftPeer::toString).reduce("", (a, b) -> a + b + ", "));
        this.connections = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public void startServer() {
        this.startListening();
        this.runAsFollower();
    }

    public void startListening() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                serverSocket = new ServerSocket(port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleClient(Socket clientSocket) {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                RaftMessage raftMessage = (RaftMessage) inputStream.readObject();
                if (raftMessage == null) {
                    break;
                }
                switch (raftMessage.getType()) {
                    case APPEND_ENTRIES -> {
                        AppendEntries appendEntries = (AppendEntries) raftMessage;
                        processAppendEntries(appendEntries);
                    }
                    case APPEND_ENTRIES_RESPONSE -> {
                        AppendEntriesResponse appendEntriesResponse = (AppendEntriesResponse) raftMessage;
                        System.out.println("Server " + id + " received append entries response from server " +
                                appendEntriesResponse.getFollowerId() + " with success " + appendEntriesResponse.isSuccess());
                    }
                    case REQUEST_VOTE -> {
                        RequestVote requestVote = (RequestVote) raftMessage;
                        System.out.println("Server " + id + " received request vote from server " + requestVote.getCandidateId());
                        processVote(requestVote);
                    }
                    case REQUEST_VOTE_RESPONSE -> {
                        RequestVoteResponse requestVoteResponse = (RequestVoteResponse) raftMessage;
                        System.out.println("Server " + id + " received request vote response from server " +
                                requestVoteResponse.getVoterId() + " with vote granted " + requestVoteResponse.isVoteGranted());
                        processVoteResponse(requestVoteResponse);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error in handling client " + clientSocket.getPort() + " on server " + id);
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void sendMessage(int destinationId, String destinationHost, int destinationPort, RaftMessage raftMessage) {
        try {
            if (!connections.containsKey(destinationId)) {
                System.out.println("Server " + id + " is connecting to server " + destinationId);
                Socket socket = new Socket(destinationHost, destinationPort);
                connections.put(destinationId, socket);
            }
            ObjectOutputStream outputStream = new ObjectOutputStream(connections.get(destinationId).getOutputStream());
            outputStream.writeObject(raftMessage);
            outputStream.flush();
            System.out.println("Server " + id + " sent message to server " + destinationId);
        } catch (IOException e) {
            System.out.println("Error in sending message from server " + id + " to server " + destinationId);
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private void runAsFollower() {
        System.out.println("Server " + id + " is running as follower");
        resetTimer();
    }

    private void runAsCandidate() {
        System.out.println("Server " + id + " is running as candidate");
        this.currentTerm++;
        this.votedFor = id;
        this.state = RaftState.CANDIDATE;
        resetTimer();
        this.sendRequestVote();
    }

    private void runAsLeader() {
        System.out.println("Server " + id + " is running as leader");
        sendAppendEntries();
    }

    private int getElectionTimeout() {
        return (int) (Math.random() * 150);
    }

    private void resetTimer() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Election Timeout expired for server " + id);
                if (state == RaftState.FOLLOWER && !isHeartBeatReceived()) {
                    runAsCandidate();
                }
            }
        };
        timer.schedule(timerTask, electionTimeout);
    }

    private void sendRequestVote() {
        for (RaftPeer peer : peers) {
            if (peer.getId() != id) {
                System.out.println("Sending RequestVote from server " + id + " to server " + peer.getId());
                sendMessage(peer.getId(), peer.getHostName(), peer.getPort(), new RequestVote(currentTerm, id, lastApplied, log.size()));
            }
        }
    }

    private boolean isHeartBeatReceived() {
        return hasReceivedHeartbeat;
    }

    private void processVote(RequestVote requestVote) {
        RequestVoteResponse voteResponse = new RequestVoteResponse(id, requestVote.getTerm(), false);
        Optional<RaftPeer> peerOptional = getPeer(requestVote.getCandidateId());
        if (peerOptional.isEmpty()) {
            System.out.println("Server " + id + " received request vote from unknown server " + requestVote.getCandidateId());
            return;
        }
        RaftPeer peer = peerOptional.get();
        if (votedFor == -1 && requestVote.getTerm() > currentTerm) {
            voteResponse.acceptVote();
            this.votedFor = requestVote.getCandidateId();
            this.currentTerm++;
            resetTimer();
            System.out.println("Server " + id + " voted for server " + requestVote.getCandidateId());
        }
        sendMessage(requestVote.getCandidateId(), peer.getHostName(), peer.getPort(), voteResponse);
    }

    private void processVoteResponse(RequestVoteResponse voteResponse) {
        if (voteResponse.getTerm() > currentTerm) {
            this.currentTerm = voteResponse.getTerm();
            this.state = RaftState.FOLLOWER;
            this.votedFor = -1;
            this.votesReceived = 0;
            resetTimer();
        }
        if (voteResponse.isVoteGranted()) {
            votesReceived++;
            if (votesReceived > peers.size() / 2) {
                runAsLeader();
            }
        }
    }

    private void sendAppendEntries() {
        for (RaftPeer peer : peers) {
            if (peer.getId() != id) {
                System.out.println("Sending AppendEntries from server " + id + " to server " + peer.getId());
                sendMessage(peer.getId(), peer.getHostName(), peer.getPort(), new AppendEntries(currentTerm, id, 0, 0, null, 0));
            }
        }
    }

    private void processAppendEntries(AppendEntries appendEntries) {
        int term = appendEntries.getTerm();
        Optional<RaftPeer> peerOptional = getPeer(appendEntries.getLeaderId());
        if (peerOptional.isEmpty()) {
            System.out.println("Server " + id + " received append entries from unknown server " + appendEntries.getLeaderId());
            return;
        }

        RaftPeer peer = peerOptional.get();
        if (term < currentTerm) {
            sendAppendEntriesRequestResponse(peer, false);
            return;
        }

        this.currentTerm = term;
        if (this.state == RaftState.CANDIDATE || this.state == RaftState.LEADER) {
            System.out.println("Server " + id + " running as follower");
            this.state = RaftState.FOLLOWER;
            this.votedFor = -1;
            this.votesReceived = 0;
        }
        sendAppendEntriesRequestResponse(peer, true);
        resetTimer();
    }

    private Optional<RaftPeer> getPeer(int peerId) {
        return peers.stream().filter(p -> p.getId() == peerId).findFirst();
    }

    private void sendAppendEntriesRequestResponse(RaftPeer peer, boolean success) {
        sendMessage(peer.getId(), peer.getHostName(), peer.getPort(), new AppendEntriesResponse(id, currentTerm, success));
    }
}
