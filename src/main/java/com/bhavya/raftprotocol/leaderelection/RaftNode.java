package com.bhavya.raftprotocol.leaderelection;

import com.bhavya.raftprotocol.leaderelection.rpc.*;
import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RaftNode {
    private int id;

    //network information of the server
    private int port;

    private RaftState state;

    //persistent state on all servers
    private int currentTerm;
    private int votedFor;
    private Map<Integer, TcpConnection> connections;
    private int electionTimeout;
    private Map<Integer, RaftPeer> peers;
    private int votesReceived = 0;

    private boolean isHeartBeatReceived = false;

    public RaftNode(int id, int port) {
        this.id = id;
        this.port = port;

        this.currentTerm = 0;
        this.votedFor = -1;

        this.peers = new HashMap<>();

        this.electionTimeout = getElectionTimeout();
        System.out.println("Election timeout for server " + id + " is " + electionTimeout);
        this.connections = new HashMap<>();

        this.startTcpServer();
    }

    public void startTcpServer() {
        System.out.println("Starting TCP server for server " + id + " on port " + port);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

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
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = bufferedReader.readLine();
                System.out.println("Server " + id + " received message from server :" + message);
                if (message == null) {
                    System.out.println("Server " + id + " is closing connection with server " + clientSocket.getPort());
                    clientSocket.close();
                    break;
                }

                Gson gson = new Gson();
                RaftMessage raftMessage = gson.fromJson(message, RaftMessage.class);
                System.out.println("Raft message: " + raftMessage);
                if (raftMessage.getType().equals(RaftMessageType.CLUSTER_INFO_UPDATE.toString())) {
                    ClusterInfoUpdate clusterInfoUpdate = gson.fromJson(raftMessage.getMessage(), ClusterInfoUpdate.class);
                    processClusterInfoUpdate(clusterInfoUpdate);
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    dataOutputStream.writeBytes("ACK\n");
                    dataOutputStream.flush();
                } else if (raftMessage.getType().equals(RaftMessageType.START_SERVER.toString())) {
                    runAsFollower();
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    dataOutputStream.writeBytes("ACK\n");
                    dataOutputStream.flush();
                } else if (raftMessage.getType().equals(RaftMessageType.REQUEST_VOTE.toString())) {
                    RequestVote requestVote = gson.fromJson(raftMessage.getMessage(), RequestVote.class);
                    RequestVoteResponse response = processRequestVote(requestVote);
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    dataOutputStream.writeBytes(new Gson().toJson(response) + "\n");
                    dataOutputStream.flush();
                } else if (raftMessage.getType().equals(RaftMessageType.APPEND_ENTRIES.toString())) {
                    AppendEntries appendEntries = gson.fromJson(raftMessage.getMessage(), AppendEntries.class);
                    AppendEntriesResponse response = processAppendEntries(appendEntries);
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    dataOutputStream.writeBytes(new Gson().toJson(response) + "\n");
                    dataOutputStream.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("Error in handling client " + clientSocket.getPort() + " on server " + id);
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private void runAsFollower() {
        System.out.println("Server " + id + " is running as follower");
        this.state = RaftState.FOLLOWER;
        resetTimer();
    }

    private void runAsCandidate() {
        System.out.println("Server " + id + " is running as candidate");
        this.currentTerm++;
        this.votedFor = id;
        this.votesReceived = 1;
        this.state = RaftState.CANDIDATE;
        for (RaftPeer peer : peers.values()) {
            RequestVote requestVote = new RequestVote(currentTerm, id, 0, 0);
            sendRequestVote(peer.getId(), requestVote);
        }
        resetTimer();
    }

    private void runAsLeader() {
        if (this.state != RaftState.LEADER) {
            System.out.println("Server " + id + " is running as leader");
            this.state = RaftState.LEADER;
            sendHeartBeatToFollowers();
        }
    }

    private int getElectionTimeout() {
        return (int) (Math.random() * 3600);
    }

    private void resetTimer() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Election Timeout expired for server " + id);
                if ((state == RaftState.FOLLOWER && !isHeartBeatReceived) || (state == RaftState.CANDIDATE)) {
                    runAsCandidate();
                } else if (state == RaftState.LEADER) {
                    sendHeartBeatToFollowers();
                }
            }
        };
        this.electionTimeout = getElectionTimeout();
        System.out.println("Election timeout for server " + id + " is " + electionTimeout);
        timer.schedule(timerTask, electionTimeout);
    }

    private void processClusterInfoUpdate(ClusterInfoUpdate clusterInfoUpdate) {
        for (RaftPeer peer : clusterInfoUpdate.getServers()) {
            if (peer.getId() == id) {
                continue;
            }
            if (!peers.containsKey(peer.getId())) {
                peers.put(peer.getId(), peer);
            }
        }
        System.out.println("Server " + id + " has peers: " + peers);
    }

    private TcpConnection getClientConnection(Integer destinationId) {
        if (!connections.containsKey(destinationId)) {
            RaftPeer peer = peers.get(destinationId);
            TcpConnection connection = new TcpConnection(peer.getHostName(), peer.getPort());
            connections.put(destinationId, connection);
        }
        return connections.get(destinationId);
    }

    private void sendRequestVote(Integer destinationId, RequestVote requestVote) {
        TcpConnection tcpConnection = getClientConnection(destinationId);
        RaftMessage message = new RaftMessage(RaftMessageType.REQUEST_VOTE.toString(), new Gson().toJson(requestVote));
        String response = tcpConnection.sendMessage(new Gson().toJson(message));
        if (response != null) {
            RequestVoteResponse requestVoteResponse = new Gson().fromJson(response, RequestVoteResponse.class);
            if (requestVoteResponse.isVoteGranted()) {
                votesReceived++;
                if (votesReceived > (peers.size() + 1)/ 2) {
                    runAsLeader();
                }
            }
        }
    }

    private RequestVoteResponse processRequestVote(RequestVote requestVote) {
        RequestVoteResponse response = new RequestVoteResponse(currentTerm, false, id);
        if (requestVote.getTerm() < currentTerm) {
            return response;
        }
        if (votedFor == -1 || votedFor == requestVote.getCandidateId()) {
            votedFor = requestVote.getCandidateId();
            response.setVoteGranted(true);
            this.votedFor = requestVote.getCandidateId();
            this.currentTerm = requestVote.getTerm();
            resetTimer();
        }
        return response;
    }

    private void sendAppendEntriesRequest(Integer destinationId, AppendEntries appendEntries) {
        TcpConnection tcpConnection = getClientConnection(destinationId);
        RaftMessage message = new RaftMessage(RaftMessageType.APPEND_ENTRIES.toString(), new Gson().toJson(appendEntries));
        String response = tcpConnection.sendMessage(new Gson().toJson(message));
        if (response != null) {
            AppendEntriesResponse appendEntriesResponse = new Gson().fromJson(response, AppendEntriesResponse.class);
            if (appendEntriesResponse.isSuccess()) {
                System.out.println("Follower " + destinationId + " is in sync with leader " + id);
            }
        }
    }

    private void sendHeartBeatToFollowers() {
        for (RaftPeer peer : peers.values()) {
            AppendEntries appendEntries = new AppendEntries(currentTerm, id);
            sendAppendEntriesRequest(peer.getId(), appendEntries);
        }
        resetTimer();
    }

    private AppendEntriesResponse processAppendEntries(AppendEntries appendEntries) {
        if (appendEntries.getTerm() < currentTerm) {
            return new AppendEntriesResponse(currentTerm, false);
        }
        if (this.state == RaftState.CANDIDATE) {
            this.state = RaftState.FOLLOWER;
        }
        this.currentTerm = appendEntries.getTerm();
        this.isHeartBeatReceived = true;
        resetTimer();
        return new AppendEntriesResponse(currentTerm, true);
    }
}
