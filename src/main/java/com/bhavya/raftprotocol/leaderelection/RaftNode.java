package com.bhavya.raftprotocol.leaderelection;

import com.bhavya.raftprotocol.leaderelection.rpc.ClusterInfoUpdate;
import com.bhavya.raftprotocol.leaderelection.rpc.RaftMessage;
import com.bhavya.raftprotocol.leaderelection.rpc.RaftMessageType;
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
    private Map<Integer, Socket> connections;
    private int electionTimeout;
    private List<RaftPeer> peers;
    private int votesReceived = 0;

    public RaftNode(int id, int port) {
        this.id = id;
        this.port = port;

        this.currentTerm = 0;
        this.votedFor = -1;

        this.peers = new ArrayList<>();

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
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataOutputStream.writeBytes("ACK\n");
                dataOutputStream.flush();

                Gson gson = new Gson();
                RaftMessage raftMessage = gson.fromJson(message, RaftMessage.class);
                System.out.println("Raft message: " + raftMessage);
                if (raftMessage.getType().equals(RaftMessageType.CLUSTER_INFO_UPDATE.toString())) {
                    ClusterInfoUpdate clusterInfoUpdate = gson.fromJson(raftMessage.getMessage(), ClusterInfoUpdate.class);
                    processClusterInfoUpdate(clusterInfoUpdate);
                } else if (raftMessage.getType().equals(RaftMessageType.START_SERVER.toString())) {
                    runAsFollower();
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
//        resetTimer();
    }

    private void runAsLeader() {
        if (this.state != RaftState.LEADER) {
            System.out.println("Server " + id + " is running as leader");
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
                if ((state == RaftState.FOLLOWER || state == RaftState.CANDIDATE)) {
                    runAsCandidate();
                }
            }
        };
        this.electionTimeout = getElectionTimeout();
        System.out.println("Election timeout for server " + id + " is " + electionTimeout);
        timer.schedule(timerTask, electionTimeout);
    }

    private void processClusterInfoUpdate(ClusterInfoUpdate clusterInfoUpdate) {
        this.peers = clusterInfoUpdate.getServers();
    }
}
