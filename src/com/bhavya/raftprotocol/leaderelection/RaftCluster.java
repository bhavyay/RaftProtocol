package com.bhavya.raftprotocol.leaderelection;

import java.util.ArrayList;
import java.util.List;

public class RaftCluster {

    private List<RaftNode> nodes = new ArrayList<>();
    private List<RaftPeer> peers = new ArrayList<>();

    public void addServer(int id, String hostName, int port) {
        try {
            RaftNode node = new RaftNode(id, hostName, port, peers);
            addNode(node);
            System.out.println("Added server " + id + " at " + hostName + ":" + port);
            node.startServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addNode(RaftNode node) {
        this.nodes.add(node);
        this.peers.add(new RaftPeer(node.getId(), node.getHostName(), node.getPort()));
    }
}
