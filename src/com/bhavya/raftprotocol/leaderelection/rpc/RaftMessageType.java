package com.bhavya.raftprotocol.leaderelection.rpc;

public enum RaftMessageType {
    APPEND_ENTRIES,
    APPEND_ENTRIES_RESPONSE,
    REQUEST_VOTE,
    REQUEST_VOTE_RESPONSE
}
