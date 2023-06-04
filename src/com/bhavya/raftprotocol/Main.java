package com.bhavya.raftprotocol;

import com.bhavya.raftprotocol.leaderelection.RaftCluster;

public class Main {

    public static void main(String[] args) {
        System.out.println("Running Raft Protocol");

        RaftCluster cluster = new RaftCluster();
        cluster.addServer(1, "localhost", 8081);
        cluster.addServer(2, "localhost", 8082);
        cluster.addServer(3, "localhost", 8083);
        cluster.addServer(4, "localhost", 8084);
        cluster.addServer(5, "localhost", 8085);
    }
}
