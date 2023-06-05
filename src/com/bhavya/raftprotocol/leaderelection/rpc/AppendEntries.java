package com.bhavya.raftprotocol.leaderelection.rpc;

public class AppendEntries implements RaftMessage {
    private int term;
    private int leaderId;
    private int prevLogIndex;
    private int prevLogTerm;
    private String[] entries;
    private int leaderCommit;

    public AppendEntries(int term, int leaderId, int prevLogIndex, int prevLogTerm, String[] entries, int leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
    }

    @Override
    public RaftMessageType getType() {
        return RaftMessageType.APPEND_ENTRIES;
    }

    public int getTerm() {
        return term;
    }

    public int getLeaderId() {
        return leaderId;
    }
}
