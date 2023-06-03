package com.bhavya.raftprotocol.leaderelection.rpc;

public interface RaftMessage {
    public RaftMessageType getType();
}
