package com.bhavya.raftprotocol.example;

import com.bhavya.raftprotocol.leaderelection.RaftNode;

public class RaftServer1 {
    public static void main(String[] args) {
        RaftNode raftNode = new RaftNode(1, 8081);
    }
}
