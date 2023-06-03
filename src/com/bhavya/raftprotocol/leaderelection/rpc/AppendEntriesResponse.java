package com.bhavya.raftprotocol.leaderelection.rpc;

public class AppendEntriesResponse implements RaftMessage {
    private final int term;
    private final boolean success;

    public AppendEntriesResponse(int term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public int getTerm() {
        return term;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public RaftMessageType getType() {
        return RaftMessageType.APPEND_ENTRIES_RESPONSE;
    }
}
