package com.bhavya.raftprotocol.example;

import com.bhavya.raftprotocol.leaderelection.RaftNode;

public class RaftServer3 {
    public static void main(String[] args) {
        RaftNode raftNode = new RaftNode(3, 8083);
    }
}
