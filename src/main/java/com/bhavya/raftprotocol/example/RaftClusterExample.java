package com.bhavya.raftprotocol.example;

import com.bhavya.raftprotocol.leaderelection.RaftCluster;
import com.bhavya.raftprotocol.leaderelection.RaftPeer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RaftClusterExample {

    public static void main(String[] args) {
        System.out.println("Running Raft Cluster...");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        RaftCluster cluster = null;

        while (true) {
            System.out.println("Enter one of the following commands: 1. Configure Cluster 2. Update Cluster Info 3. Start Server");
            try {
                int command = Integer.parseInt(reader.readLine());
                if (command == 1) {
                    cluster = new RaftCluster(getPeers());
                    System.out.println("Cluster configured successfully!");
                } else if (command == 2) {
                    assert cluster != null;
                    cluster.updateClusterInfoToServer();
                    System.out.println("Cluster info updated successfully!");
                } else {
                    System.out.println("Quitting...");
                    break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    private static List<RaftPeer> getPeers() {
        List<RaftPeer> peers = new ArrayList<>();
        peers.add(new RaftPeer(1, "localhost", 8081));
//        peers.add(new RaftPeer(2, "localhost", 8082));
//        peers.add(new RaftPeer(3, "localhost", 8083));
        return peers;
    }
}
