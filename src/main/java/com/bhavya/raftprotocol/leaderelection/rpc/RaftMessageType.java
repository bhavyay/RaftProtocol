package com.bhavya.raftprotocol.leaderelection.rpc;

public enum RaftMessageType {
    CLUSTER_INFO_UPDATE,
    START_SERVER,
    REQUEST_VOTE,
    APPEND_ENTRIES,
}
