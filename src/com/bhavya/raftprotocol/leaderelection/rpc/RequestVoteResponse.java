package com.bhavya.raftprotocol.leaderelection.rpc;

public class RequestVoteResponse implements RaftMessage {
    private final int term;
    private final boolean voteGranted;

    public RequestVoteResponse(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public int getTerm() {
        return term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    @Override
    public RaftMessageType getType() {
        return RaftMessageType.REQUEST_VOTE_RESPONSE;
    }
}
