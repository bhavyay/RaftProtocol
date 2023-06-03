package com.bhavya.raftprotocol.leaderelection;

public enum RaftState {
    FOLLOWER,
    CANDIDATE,
    LEADER
}
