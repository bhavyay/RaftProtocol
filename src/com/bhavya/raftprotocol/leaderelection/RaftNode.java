package com.bhavya.raftprotocol.leaderelection;

import com.bhavya.raftprotocol.leaderelection.rpc.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RaftNode {
    private int id;

    //network information of the server
    private String hostName;
    private int port;

    private RaftState state;

    //persistent state on all servers
    private int currentTerm;
    private int votedFor;
    private String[] log;

    //volatile state on all servers
    private int commitIndex;
    private int lastApplied;

    //volatile state on leaders
    private int[] nextIndex;
    private int[] matchIndex;

    private ServerSocket serverSocket;
    private Map<Integer, Socket> connections;

    private int electionTimeout;

    private Thread listenThread;

    public RaftNode(int id, String hostName, int port) {
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

        this.electionTimeout = getElectionTimeout();
        System.out.println("Election timeout for server " + id + " is " + electionTimeout);
        this.connections = new HashMap<>();
    }

    public void startServer() {
        this.startListening();
        this.runAsFollower();
    }

    public void startListening() {
        listenThread = new Thread(() -> {
           try {
               serverSocket = new ServerSocket(port);

               while (!Thread.currentThread().isInterrupted()) {
                   Socket clientSocket = serverSocket.accept();
                   handleClient(clientSocket);
               }
           } catch (IOException e) {
                e.printStackTrace();
           }
        });
        listenThread.start();
    }

    private void handleClient(Socket clientSocket) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
//            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            while (!Thread.currentThread().isInterrupted()) {
                RaftMessage raftMessage = (RaftMessage) inputStream.readObject();
                if (raftMessage == null) {
                    break;
                }
                switch (raftMessage.getType()) {
                    case APPEND_ENTRIES:
                        AppendEntries appendEntries = (AppendEntries) raftMessage;
                        break;
                    case APPEND_ENTRIES_RESPONSE:
                        AppendEntriesResponse appendEntriesResponse = (AppendEntriesResponse) raftMessage;
                        break;
                    case REQUEST_VOTE:
                        RequestVote requestVote = (RequestVote) raftMessage;
                        break;
                    case REQUEST_VOTE_RESPONSE:
                        RequestVoteResponse requestVoteResponse = (RequestVoteResponse) raftMessage;
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error in handling client " + clientSocket.getPort() + " on server " + id);
        }
    }

    public void sendMessage(int destinationId, String destinationHost, int destinationPort, RaftMessage raftMessage) {
        try {
            if (!connections.containsKey(destinationId)) {
                Socket socket = new Socket(destinationHost, destinationPort);
                connections.put(destinationId, socket);
            }
            ObjectOutputStream outputStream = new ObjectOutputStream(connections.get(destinationId).getOutputStream());
            outputStream.writeObject(raftMessage);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Error in sending message from server " + id + " to server " + destinationId);
        }
    }

    private void runAsFollower() {
        System.out.println("Server " + id + " is running as follower");
        resetTimer();
    }

    private void runAsCandidate() {
        // TODO
        System.out.println("Server " + id + " is running as candidate");
    }

    private void runAsLeader() {
        // TODO
    }

    private int getElectionTimeout() {
        return (int) (Math.random() * 30);
    }

    private void resetTimer() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Election Timeout expired for server " + id);
                if (state == RaftState.FOLLOWER) {
                    runAsCandidate();
                }
            }
        };
        timer.schedule(timerTask, electionTimeout);
    }
}
