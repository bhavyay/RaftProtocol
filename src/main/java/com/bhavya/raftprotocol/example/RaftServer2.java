package com.bhavya.raftprotocol.example;

import com.bhavya.raftprotocol.leaderelection.RaftNode;

public class RaftServer2 {
    public static void main(String[] args) {
        RaftNode raftNode = new RaftNode(2, 8082);
    }
}
