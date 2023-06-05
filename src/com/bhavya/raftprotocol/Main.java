package com.bhavya.raftprotocol;

import com.bhavya.raftprotocol.leaderelection.RaftCluster;
import com.bhavya.raftprotocol.leaderelection.RaftPeer;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Running Raft Protocol");

        RaftCluster cluster = new RaftCluster(getPeers());
        cluster.addServer(1, "localhost", 8081);
        cluster.addServer(2, "localhost", 8082);
        cluster.addServer(3, "localhost", 8083);
    }

    private static List<RaftPeer> getPeers() {
        List<RaftPeer> peers = new ArrayList<>();
        peers.add(new RaftPeer(1, "localhost", 8081));
        peers.add(new RaftPeer(2, "localhost", 8082));
        peers.add(new RaftPeer(3, "localhost", 8083));
        return peers;
    }
}
