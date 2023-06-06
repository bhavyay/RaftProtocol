package com.bhavya.raftprotocol.leaderelection.rpc;

public class AppendEntriesResponse implements RaftMessage {
    private final int term;
    private final boolean success;

    private final int followerId;

    public AppendEntriesResponse(int followerId, int term, boolean success) {
        this.followerId = followerId;
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

    public int getFollowerId() {
        return followerId;
    }
}
