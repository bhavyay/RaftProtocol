package com.bhavya.raftprotocol.leaderelection.rpc;

public class AppendEntries implements RaftMessage {
    private int term;
    private int leaderId;
    private int prevLogIndex;
    private int prevLogTerm;
    private String[] entries;
    private int leaderCommit;

    @Override
    public RaftMessageType getType() {
        return RaftMessageType.APPEND_ENTRIES;
    }
}
